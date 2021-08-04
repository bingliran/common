package com.blr19c.common.collection;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * map计算通用方法
 */
public interface MapComputeInterface extends MapGetInterface {

    /**
     * map size
     */
    default int size() {
        return getMap().size();
    }

    /**
     * 校验map是否为空
     */
    default boolean isEmpty() {
        return MapUtils.isEmpty(getMap());
    }

    /**
     * 是否包含指定key
     */
    default boolean containsKey(Object key) {
        return getMap().containsKey(key);
    }

    /**
     * 是否包含指定value
     */
    default boolean containsValue(Object value) {
        return getMap().containsValue(value);
    }

    /**
     * 是否为空字符的key
     */
    default boolean isBlank(Object key) {
        return StringUtils.isBlank(getString(key));
    }

    /**
     * 是否为不是空字符的key
     */
    default boolean isNotBlank(Object key) {
        return !isBlank(key);
    }

    /**
     * 比较两个map的value
     */
    default boolean equalsValue(Object key, Map<?, ?> map) {
        return Objects.equals(map.get(key), getObject(key));
    }

    /**
     * 比较getValue和value
     */
    default boolean equalsValue(Object key, Object value) {
        return Objects.equals(value, getObject(key));
    }

    /**
     * 如果不存在则put
     */
    default <V> V putIfAbsent(Object key, V value) {
        return this.<Object, V>getMap().putIfAbsent(key, value);
    }

    /**
     * 计算是否存在
     */
    default <K, V> V computeIfPresent(K key,
                                      BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.<K, V>getMap().computeIfPresent(key, remappingFunction);
    }

    /**
     * 计算是否不存在
     */
    default <K, V> V computeIfAbsent(K key,
                                     Function<? super K, ? extends V> mappingFunction) {
        return this.<K, V>getMap().computeIfAbsent(key, mappingFunction);
    }


    /**
     * 根据旧value计算新value不存在则删除
     */
    default <K, V> V compute(K key,
                             BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.<K, V>getMap().compute(key, remappingFunction);
    }

    /**
     * 合并
     */
    default <K, V> V merge(K key, V value,
                           BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return this.<K, V>getMap().merge(key, value, remappingFunction);
    }
}
