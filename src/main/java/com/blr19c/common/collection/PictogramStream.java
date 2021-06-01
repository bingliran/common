package com.blr19c.common.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;


/**
 * 除了原本的流之外还提供了一些便捷方法
 * 2021.5.25 blr
 */
@SuppressWarnings("unused")
public class PictogramStream<T> extends OriginalStream<T> {

    private PictogramStream(Stream<T> stream) {
        super(stream);
    }

    @SafeVarargs
    public static <T> PictogramStream<T> of(T... objs) {
        return new PictogramStream<>(Stream.of(objs));
    }

    public static <T> PictogramStream<T> of(Stream<T> stream) {
        return new PictogramStream<>(stream);
    }

    public static <T> PictogramStream<T> of(Collection<T> collection) {
        return new PictogramStream<>(collection.stream());
    }

    /**
     * 清除为null的
     */
    public PictogramStream<T> cleanToNull() {
        return filter(Objects::nonNull);
    }

    /**
     * 与filter相反
     */
    public PictogramStream<T> clean(Predicate<? super T> predicate) {
        return filter(t -> !predicate.test(t));
    }

    /**
     * 根据条件去重
     */
    public PictogramStream<T> distinct(Comparator<T> comparator) {
        return collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(comparator)), PictogramStream::of));
    }

    /**
     * 排序
     */
    public <U extends Comparable<? super U>> PictogramStream<T> sorted(Function<? super T, ? extends U> keyExtractor) {
        return sorted(Comparator.comparing(keyExtractor));
    }

    /**
     * map转换之后转为list
     */
    public <R> List<R> mapToList(Function<? super T, ? extends R> mapper) {
        return this.<R>map(mapper).toList();
    }

    /**
     * 在末尾添加
     */
    @SafeVarargs
    public final PictogramStream<T> addLast(T... objs) {
        return of(Stream.concat(stream, Stream.of(objs)));
    }

    /**
     * 在开头添加
     */
    @SafeVarargs
    public final PictogramStream<T> addFirst(T... objs) {
        return of(Stream.concat(Stream.of(objs), stream));
    }

    /**
     * 根据条件查询一个,没查到返回null
     */
    @SafeVarargs
    public final T searchOneOrNull(Predicate<? super T>... predicate) {
        List<T> search = search(predicate);
        if (search.isEmpty())
            return null;
        if (search.size() != 1)
            throw new IllegalArgumentException("There is more than one result");
        return search.get(0);
    }

    /**
     * 根据条件查询一个,没查到IllegalArgumentException
     */
    @SafeVarargs
    public final T searchOne(Predicate<? super T>... predicate) {
        T t = searchOneOrNull(predicate);
        if (t == null)
            throw new IllegalArgumentException("No results found");
        return t;
    }

    /**
     * 根据条件查询返回一个列表
     */
    @SafeVarargs
    public final List<T> search(Predicate<? super T>... predicate) {
        PictogramStream<T> stream = null;
        for (Predicate<? super T> p : predicate) {
            stream = filter(p);
        }
        return stream(stream).toList();
    }

    @SuppressWarnings("unchecked")
    private PictogramStream<T> stream(Object stream) {
        return stream == null ? of(this.stream) :
                stream instanceof PictogramStream ? (PictogramStream<T>) stream :
                        stream instanceof Stream ? of((Stream<T>) stream) : of();
    }

    public List<T> toList() {
        return stream.collect(Collectors.toList());
    }

    public Set<T> toSet() {
        return stream.collect(Collectors.toSet());
    }

    public <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory) {
        return stream.collect(Collectors.toCollection(collectionFactory));
    }

    public <K, U> PictogramMap toMap(Function<? super T, ? extends K> keyMapper,
                                     Function<? super T, ? extends U> valueMapper) {
        return PictogramMap.toPictogramMap(stream.collect(Collectors.toMap(keyMapper, valueMapper)));
    }
}

@SuppressWarnings("unused")
class OriginalStream<T> implements BaseStream<T, OriginalStream<T>> {
    protected final Stream<T> stream;

    OriginalStream(Stream<T> stream) {
        this.stream = stream;
    }

    public PictogramStream<T> filter(Predicate<? super T> predicate) {
        return PictogramStream.of(stream.filter(predicate));
    }

    public <R> PictogramStream<R> map(Function<? super T, ? extends R> mapper) {
        return PictogramStream.of(stream.map(mapper));
    }

    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper);
    }

    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper);
    }

    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper);
    }

    public <R> PictogramStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return PictogramStream.of(stream.flatMap(mapper));
    }

    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }


    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    public PictogramStream<T> distinct() {
        return PictogramStream.of(stream.distinct());
    }

    public PictogramStream<T> sorted() {
        return PictogramStream.of(stream.sorted());
    }

    public PictogramStream<T> sorted(Comparator<? super T> comparator) {
        return PictogramStream.of(stream.sorted(comparator));
    }

    public PictogramStream<T> peek(Consumer<? super T> action) {
        return PictogramStream.of(stream.peek(action));
    }

    public PictogramStream<T> limit(long maxSize) {
        return PictogramStream.of(stream.limit(maxSize));
    }

    public PictogramStream<T> skip(long n) {
        return PictogramStream.of(stream.skip(n));
    }

    public void forEach(Consumer<? super T> action) {
        stream.forEach(action);
    }

    public void forEachOrdered(Consumer<? super T> action) {
        stream.forEachOrdered(action);
    }

    @NotNull
    public Object[] toArray() {
        return stream.toArray();
    }

    @NotNull
    @SuppressWarnings("all")
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    @NotNull
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream.reduce(accumulator);
    }

    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream.collect(collector);
    }

    @NotNull
    public Optional<T> min(Comparator<? super T> comparator) {
        return stream.min(comparator);
    }

    @NotNull
    public Optional<T> max(Comparator<? super T> comparator) {
        return stream.max(comparator);
    }

    public long count() {
        return stream.count();
    }

    public boolean anyMatch(Predicate<? super T> predicate) {
        return stream.anyMatch(predicate);
    }

    public boolean allMatch(Predicate<? super T> predicate) {
        return stream.allMatch(predicate);
    }

    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream.noneMatch(predicate);
    }

    @NotNull
    public Optional<T> findFirst() {
        return stream.findFirst();
    }

    @NotNull
    public Optional<T> findAny() {
        return stream.findAny();
    }

    @NotNull
    public Iterator<T> iterator() {
        return stream.iterator();
    }

    @NotNull
    public Spliterator<T> spliterator() {
        return stream.spliterator();
    }

    public boolean isParallel() {
        return stream.isParallel();
    }

    @NotNull
    public PictogramStream<T> sequential() {
        return PictogramStream.of(stream.sequential());
    }

    @NotNull
    public PictogramStream<T> parallel() {
        return PictogramStream.of(stream.parallel());
    }

    @NotNull
    public PictogramStream<T> unordered() {
        return PictogramStream.of(stream.unordered());
    }

    @NotNull
    public PictogramStream<T> onClose(Runnable closeHandler) {
        return PictogramStream.of(stream.onClose(closeHandler));
    }

    public void close() {
        stream.close();
    }

    @Override
    public String toString() {
        return stream.toString();
    }
}
