package com.blr19c.common.collection;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 限制map的大小 {@link DeleteStrategyEnum}
 * 如果{@link #putCache(Object, Object)}过于频繁
 * 可能并不会按照原有的删除策略执行
 * 在还未进行排序元素就已经删除
 *
 * @author blr
 */
public class CacheHashMap<K, V> extends ConcurrentHashMap<CacheHashMap.Node<K>, V> {
    private final int maximumCapacity, concurrencyLevel;
    private final boolean isLeastUsed;
    private final AtomicReference<LinkedBlockingDeque<Node<K>>> delReady;
    private Comparator<Node<K>> comparator;

    public CacheHashMap() {
        this(DeleteStrategyEnum.OLDEST, 64);
    }

    public CacheHashMap(DeleteStrategyEnum deleteStrategyEnum, int maximumCapacity) {
        this(maximumCapacity + maximumCapacity / 2, .75F, 16,
                deleteStrategyEnum, maximumCapacity);
    }

    /**
     * @param initialCapacity    初始容量 {@link ConcurrentHashMap#ConcurrentHashMap(int, float, int)}
     * @param loadFactor         负载因子 {@link ConcurrentHashMap#ConcurrentHashMap(int, float, int)}
     * @param concurrencyLevel   并发级别 {@link ConcurrentHashMap#ConcurrentHashMap(int, float, int)}
     * @param deleteStrategyEnum 删除策略
     * @param maximumCapacity    最大容量超过之后执行删除策略
     */
    public CacheHashMap(int initialCapacity, float loadFactor, int concurrencyLevel,
                        DeleteStrategyEnum deleteStrategyEnum, int maximumCapacity) {
        super(initialCapacity, loadFactor, concurrencyLevel);
        this.maximumCapacity = maximumCapacity;
        this.concurrencyLevel = concurrencyLevel;
        this.delReady = new AtomicReference<>(new LinkedBlockingDeque<>());
        this.isLeastUsed = deleteStrategyEnum == DeleteStrategyEnum.LEAST_USED;
        switch (deleteStrategyEnum) {
            case LEAST_USED:
                //如果使用次数相同先添加的在前面
                comparator = Comparator.comparing(kNode -> kNode.usageCount.intValue());
                comparator = comparator.thenComparing(kNode -> kNode.addTime);
                break;
            case OLDEST:
                comparator = Comparator.comparing(kNode -> kNode.addTime);
                break;
        }
    }

    /**
     * 获取缓存的value
     */
    public V getCache(K key) {
        Node<K> node = new Node<>(Instant.now(), new LongAdder(), Objects.requireNonNull(key));
        V value = super.get(node);
        if (value != null)
            addUsageCount(node);
        return value;
    }

    /**
     * 添加并执行缓存策略
     */
    public V putCache(K key, V value) {
        Node<K> node = new Node<>(Instant.now(), new LongAdder(), Objects.requireNonNull(key));
        V oldValue = super.put(node, value);
        //有old说明没新增
        if (oldValue != null)
            return oldValue;
        delReady.updateAndGet(nodes -> {
            nodes.offerLast(node);
            return nodes;
        });
        if (maximumCapacity >= this.size())
            return null;
        //在delReady尝试删除
        if (!delReady.get().isEmpty()) {
            Object p;
            int i = 2;
            do if ((p = delReady.get().pollFirst()) != null && !p.equals(key))
                p = super.remove(p);
            while (p == null && --i != 0);
            if (p != null) return null;
        }
        try {
            LinkedList<Node<K>> nodeLinkedList = sortedNode();
            do if (nodeLinkedList.peekFirst() == null)
                break;
            while (super.remove(nodeLinkedList.removeFirst()) == null);
        } catch (NoSuchElementException ignored) {
        }
        return null;
    }

    /**
     * 自定义比较器
     * 相当于自定义 {@link DeleteStrategyEnum}
     */
    public void setComparator(Comparator<Node<K>> comparator) {
        this.comparator = comparator;
    }

    /**
     * 返回条件排序
     */
    private LinkedList<Node<K>> sortedNode() {
        //10分钟的保护期
        Instant now = Instant.now().minusMillis(10);
        LinkedList<Node<K>> nodeList = sort(super.keySet(), LinkedList::new);
        //如果存在无保护
        if (nodeList.stream().anyMatch(kNode -> now.isBefore(kNode.addTime)))
            return nodeList;
        Collections.reverse(nodeList);
        return nodeList;
    }

    /**
     * 添加计数
     */
    private void addUsageCount(Node<K> node) {
        if (!isLeastUsed)
            return;
        Node<K> key = super.search(concurrencyLevel, (k, v) -> Objects.equals(k, node) ? k : null);
        if (key != null) {
            key.usageCount.increment();
            if (super.size() > concurrencyLevel)
                reDelReady();
        }
    }

    /**
     * 重新排序delReady
     */
    private void reDelReady() {
        delReady.getAndUpdate(nodes -> sort(nodes, LinkedBlockingDeque::new));
    }

    /**
     * 对 nodes 排序 {@link Node#currentSnapshot()}
     * <p>
     * TimSort#sort(Object[], int, int, Comparator, Object[], int, int)
     * 获取当前快照进行排序
     * 当排序过程中添加了 {@link Node#usageCount}
     * 会导致 Comparison method violates its general contract!
     * 即: 不满足 {@link Comparator#compare(Object, Object)}
     * compare(o1, o2) == -compare(o2, o1)
     * 因为在这个过程中 {@link Node#usageCount} 随时可能被更改
     */
    private <C extends Collection<Node<K>>> C sort(Collection<Node<K>> nodes, Supplier<C> collectionFactory) {
        return nodes.stream()
                .map(Node::currentSnapshot)
                .sorted(comparator)
                .map(Node::restore)
                .collect(Collectors.toCollection(collectionFactory));
    }

    static class Node<K> {

        /**
         * 元素被添加的时间
         */
        Instant addTime;

        /**
         * 使用次数
         */
        LongAdder usageCount;

        /**
         * 被添加的key
         */
        K key;

        /**
         * 原来的对象 在快照中使用
         */
        Node<K> original;

        Node(Instant addTime, LongAdder usageCount, K key) {
            this.addTime = addTime;
            this.usageCount = usageCount;
            this.key = key;
        }

        public Node(Instant addTime, long usageCount, K key, Node<K> original) {
            this.addTime = addTime;
            this.usageCount = new LongAdder();
            this.key = key;
            this.original = original;
            this.usageCount.add(usageCount);
        }

        /**
         * 获取当前快照
         */
        Node<K> currentSnapshot() {
            return new Node<>(addTime, usageCount.longValue(), key, this);
        }

        /**
         * 从快照恢复
         */
        Node<K> restore() {
            return original == null ? this : original;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof Node)
                return Objects.equals(((Node<?>) o).key, this.key);
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }

        @Override
        public String toString() {
            return "Node{" +
                    "addTime=" + addTime +
                    ", usageCount=" + usageCount +
                    ", key=" + key +
                    '}';
        }
    }

    /**
     * 删除策略
     */
    public enum DeleteStrategyEnum {
        /**
         * 最老的
         */
        OLDEST,
        /**
         * 最少使用的
         */
        LEAST_USED
    }
}
