package com.blr19c.common.collection;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * 将map转为更便于操作的象形map
 * 注: 使用时请格外注意泛型结构
 */
@SuppressWarnings(value = {"unchecked", "unused"})
public class PictogramMap {
    private final Map<?, ?> data;

    private PictogramMap(Map<?, ?> data, boolean isSynchronized) {
        this.data = isSynchronized ?
                (data instanceof ConcurrentHashMap ? data :
                        Objects.isNull(data) ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(data))
                : Objects.isNull(data) ? new HashMap<>() : data;
    }

    /**
     * 将一个任意类型的map转为PictogramMap象形map,并设置是否选用同步map
     */
    public static PictogramMap toPictogramMap(Map<?, ?> data, boolean isSynchronized) {
        return new PictogramMap(data, isSynchronized);
    }

    /**
     * 将一个任意类型的map转为PictogramMap象形map
     */
    public static PictogramMap toPictogramMap(Map<?, ?> data) {
        return toPictogramMap(data, false);
    }

    /**
     * 将一个json转为PictogramMap
     */
    public static PictogramMap toPictogramMapAsJson(String json) {
        return toPictogramMapAsJson(json, null);
    }

    /**
     * 将一个json转为PictogramMap,并设置是否选用同步map
     */
    public static PictogramMap toPictogramMapAsJson(String json, ObjectMapper objectMapper) {
        try {
            return toPictogramMap(Mapper.getObjectMapper(objectMapper).readValue(json, new TypeReference<Map<Object, Object>>() {
            }));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 将一个对象转为map 并设置 自定义转换
     */
    public static PictogramMap toPictogramMapAsModel(Object data, Function<Map.Entry<?, ?>, Map.Entry<?, ?>> entryFunction) {
        try {
            Class<?> c = data.getClass();
            Field[] f = c.getDeclaredFields();
            Map<Object, Object> map = new HashMap<>();
            for (Field field : f) {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;
                field.setAccessible(true);
                Map.Entry<?, ?> entry = entryFunction.apply(new AbstractMap.SimpleEntry<>(field.getName(), field.get(data)));
                map.put(entry.getKey(), entry.getValue());
            }
            return toPictogramMap(map);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Serialization error", e);
        }
    }

    /**
     * 获取一个PictogramMap实例 并含有空的非同步map
     */
    public static PictogramMap getInstance() {
        return getInstance(false);
    }

    /**
     * 获取一个PictogramMap实例 并含有空的指定同步map
     */
    public static PictogramMap getInstance(boolean isSynchronized) {
        return toPictogramMap(null, isSynchronized);
    }

    @Override
    /*
     * 当map存在时比较map
     */
    public int hashCode() {
        if (isEmpty()) return super.hashCode();
        return getMap().hashCode();
    }

    @Override
    /*
     * 相同的PictogramMap和相同的Map均视为相同
     */
    public boolean equals(Object obj) {
        if (Objects.isNull(obj)) return false;
        if (obj instanceof PictogramMap) {
            PictogramMap pictogramMap = (PictogramMap) obj;
            return Objects.equals(pictogramMap.getMap(), getMap());
        }
        if (obj instanceof Map) return Objects.equals(getMap(), obj);
        return false;
    }

    /**
     * 返回一个未经检查的map
     */
    public <K, V> Map<K, V> getMap() {
        return (Map<K, V>) data;
    }

    /**
     * 校验map是否为空
     */
    public boolean isEmpty() {
        return MapUtils.isEmpty(getMap());
    }

    /**
     * 获取一个Integer类型的value
     */
    public Integer getInteger(Object key) {
        return getInteger(key, null);
    }

    /**
     * 获取一个Integer类型的value并存在默认值
     */
    public Integer getInteger(Object key, Integer defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getInteger(getMap(), key, defaultValue);
    }

    /**
     * 获取一个int类型的value
     */
    public int getIntValue(Object key) {
        return getIntValue(key, 0);
    }

    /**
     * 获取一个int类型的value并存在默认值
     */
    public int getIntValue(Object key, int defaultValue) {
        Integer value = getInteger(key, defaultValue);
        return Objects.isNull(value) ? defaultValue : value;
    }

    /**
     * 获取一个任意类型的value
     */
    public <T> T getObject(Object key) {
        return getObject(key, null);
    }

    /**
     * 获取一个任意类型的value并存在默认值
     */
    public <T> T getObject(Object key, T defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getObject(getMap(), key, defaultValue);
    }

    /**
     * 获取一个String类型的value
     */
    public String getString(Object key) {
        return getString(key, null);
    }

    /**
     * 获取一个String类型的value并存在默认值
     */
    public String getString(Object key, String defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getString(getMap(), key, defaultValue);
    }

    /**
     * 获取一个Double类型的value
     */
    public Double getDouble(Object key) {
        return getDouble(key, null);
    }

    /**
     * 获取一个Double类型的value并存在默认值
     */
    public Double getDouble(Object key, Double defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getDouble(getMap(), key, defaultValue);
    }

    /**
     * 获取一个Long类型的value
     */
    public Long getLong(Object key) {
        return getLong(key, null);
    }

    /**
     * 获取一个Long类型的value并存在默认值
     */
    public Long getLong(Object key, Long defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getLong(getMap(), key, defaultValue);
    }

    /**
     * 链式校验一个value满足条件继续执行, 如返回结果为false则使用IllegalArgumentException抛出errorMessage
     */
    public <T> PictogramMap checkValue(Object key, Predicate<T> predicate, String errorMessage) {
        if (predicate.test(getObject(key))) return this;
        throw new IllegalArgumentException(errorMessage);
    }

    /**
     * 链式校验多个value满足条件继续执行, 如返回结果为false则使用IllegalArgumentException抛出errorMessage
     */
    public <T> PictogramMap checkValues(Predicate<T> predicate, String errorMessage, Object... keys) {
        for (Object key : keys) {
            if (!predicate.test(getObject(key)))
                throw new IllegalArgumentException(errorMessage);
        }
        return this;
    }

    /**
     * 遍历元素
     */
    public <K, V> PictogramMap peek(Consumer<? super Map.Entry<K, V>> action) {
        this.<K, V>getMap().entrySet().forEach(action);
        return this;
    }

    /**
     * entrySet流
     */
    public <K, V> PictogramStream<Map.Entry<K, V>> stream() {
        return PictogramStream.of(this.<K, V>getMap().entrySet());
    }

    /**
     * key流
     */
    public <K> PictogramStream<K> keyStream() {
        return PictogramStream.of(this.<K, Object>getMap().keySet());
    }

    public <V> PictogramStream<V> valueStream() {
        return PictogramStream.of(this.<Object, V>getMap().values());
    }

    /**
     * 清空map
     */
    public PictogramMap clear() {
        getMap().clear();
        return this;
    }

    /**
     * 根据条件删除数据
     */
    public PictogramMap clear(Predicate<Map.Entry<?, ?>> filter) {
        getMap().entrySet().removeIf(filter);
        return this;
    }

    /**
     * 删除value值为空的key
     */
    public PictogramMap clearToBlank() {
        return clear(e -> e.getValue() instanceof CharSequence ?
                StringUtils.isBlank((CharSequence) e.getValue()) : Objects.isNull(e.getValue()));
    }

    /**
     * 删除value值为Empty或者null的key
     */
    public PictogramMap clearToEmpty() {
        Predicate<Map.Entry<?, ?>> filter = e -> {
            Object value = e.getValue();
            if (value == null)
                return true;
            if (value instanceof Map)
                return ((Map<?, ?>) value).isEmpty();
            if (value instanceof Collection)
                return ((Collection<?>) value).isEmpty();
            if (value.getClass().isArray())
                return Array.getLength(value) == 0;
            return false;
        };
        return clear(filter);
    }

    /**
     * 向map中添加一个model
     */
    public PictogramMap putModel(Object model, Function<Map.Entry<?, ?>, Map.Entry<?, ?>> entryFunction) {
        return putAll(toPictogramMapAsModel(model, entryFunction));
    }

    /**
     * 向map中添加一个model
     */
    public PictogramMap putModel(Object model) {
        return putAll(toPictogramMapAsModel(model, e -> e));
    }

    /**
     * 向map中添加一个pictogramMap
     */
    public PictogramMap putAll(PictogramMap pictogramMap) {
        return putAll(pictogramMap.getMap());
    }

    /**
     * 向map中添加一个map
     */
    public PictogramMap putAll(Map<?, ?> map) {
        getMap().putAll(map);
        return this;
    }

    /**
     * 如果不存在则put
     */
    public <V> V putIfAbsent(Object key, V value) {
        return this.<Object, V>getMap().putIfAbsent(key, value);
    }

    /**
     * 计算是否存在
     */
    public <K, V> V computeIfPresent(K key,
                                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.<K, V>getMap().computeIfPresent(key, remappingFunction);
    }

    /**
     * 计算是否不存在
     */
    public <K, V> V computeIfAbsent(K key,
                                    Function<? super K, ? extends V> mappingFunction) {
        return this.<K, V>getMap().computeIfAbsent(key, mappingFunction);
    }


    /**
     * 根据旧value计算新value不存在则删除
     */
    public <K, V> V compute(K key,
                            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.<K, V>getMap().compute(key, remappingFunction);
    }

    /**
     * 合并
     */
    public <K, V> V merge(K key, V value,
                          BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return this.<K, V>getMap().merge(key, value, remappingFunction);
    }

    /**
     * 如果不存在则put
     */
    public PictogramMap putIfAbsentToThis(Object key, Object value) {
        putIfAbsent(key, value);
        return this;
    }

    /**
     * 计算是否存在
     */
    public <K, V> PictogramMap computeIfPresentToThis(K key,
                                                      BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        computeIfPresent(key, remappingFunction);
        return this;
    }

    /**
     * 计算是否不存在
     */
    public <K, V> PictogramMap computeIfAbsentToThis(K key,
                                                     Function<? super K, ? extends V> mappingFunction) {
        computeIfAbsent(key, mappingFunction);
        return this;
    }

    /**
     * 根据旧value计算新value不存在则删除
     */
    public <K, V> PictogramMap computeToThis(K key,
                                             BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        compute(key, remappingFunction);
        return this;
    }

    /**
     * 合并
     */
    public <K, V> PictogramMap mergeToThis(K key, V value,
                                           BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        merge(key, value, remappingFunction);
        return this;
    }

    /**
     * 向map中添加一组元素
     */
    public PictogramMap putValue(Object key, Object value) {
        getMap().put(key, value);
        return this;
    }

    /**
     * 从pictogramMap向map中添加一组元素
     */
    public PictogramMap putValue(Object key, PictogramMap pictogramMap) {
        return putValue(key, (Object) pictogramMap.getObject(key));
    }

    /**
     * 从map向map中添加一组元素
     */
    public PictogramMap putValue(Object key, Map<?, ?> map) {
        return putValue(key, map.get(key));
    }

    /**
     * 原有值转为stringValue
     */
    public PictogramMap putValueToString(Object key) {
        return putValue(key, getString(key));
    }

    /**
     * 添加并转为stringValue
     */
    public PictogramMap putValueToString(Object key, Object value) {
        return putValue(key, String.valueOf(value));
    }

    /**
     * 从枚举中向map转换一组元素
     */
    public <T extends Enum<T>> PictogramMap putValueToEnum(Object key, Class<? extends Enum<T>> cls) {
        return putValueToEnum(key, cls, "value");
    }

    /**
     * 从枚举中向map转换一组元素并指定方法名称
     */
    public <T extends Enum<T>> PictogramMap putValueToEnum(Object key, Class<? extends Enum<T>> cls,
                                                           String methodName) {
        try {
            Method method = cls.getMethod(methodName, String.class);
            Object data = method.invoke(null, getString(key));
            if (Objects.isNull(data)) return this;
            return putValue(key, data.getClass().getMethod("getValue").invoke(data));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Failed to add element", e);
        }
    }

    /**
     * 向map中添加一组元素且key,value不是null
     */
    public PictogramMap putValueNonNull(Object key, Object value) {
        if (Objects.isNull(key) || Objects.isNull(value))
            throw new IllegalArgumentException("key:value Can not be null,actual:" + key + ":" + value);
        return putValue(key, value);
    }

    /**
     * 将key的value替换为thisKey的value
     */
    public PictogramMap putValueToThis(Object key, Object thisKey) {
        return putValue(key, (Object) getObject(thisKey))
                .removeValue(thisKey);
    }

    /**
     * 删除value,可以选择多个或一个
     */
    public PictogramMap removeValue(Object... keys) {
        final Map<?, ?> map = getMap();
        Arrays.asList(keys).forEach(map::remove);
        return this;
    }

    /**
     * 比较两个pictogramMap的value
     */
    public boolean equalsValue(Object key, PictogramMap pictogramMap) {
        return Objects.equals(pictogramMap.getObject(key), getObject(key));
    }

    /**
     * 比较两个map的value
     */
    public boolean equalsValue(Object key, Map<?, ?> map) {
        return Objects.equals(map.get(key), getObject(key));
    }

    /**
     * 比较getValue和value
     */
    public boolean equalsValue(Object key, Object value) {
        return Objects.equals(value, getObject(key));
    }

    /**
     * 代码块执行
     */
    public <K, V> PictogramMap toCodeBlock(Consumer<Map<K, V>> consumer) {
        consumer.accept(getMap());
        return this;
    }

    /**
     * 转为String key的map
     */
    public <V> Map<String, V> getStringKeyMap() {
        return getMap().entrySet().stream().collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> (V) e.getValue()));
    }

    /**
     * 获取pageNum
     */
    public int getPageNum() {
        return getIntValue("pageNum");
    }

    /**
     * 获取pageSize
     */
    public int getPageSize() {
        return getIntValue("pageSize");
    }

    /**
     * 默认使用PageHelper分页
     */
    public PictogramMap startPage() {
        return startPage(PageHelper::startPage);
    }

    /**
     * 分页
     */
    public PictogramMap startPage(BiConsumer<Integer, Integer> page) {
        final int pageNum = getPageNum(), pageSize = getPageSize();
        if (pageNum <= 0 || pageSize <= 0)
            throw new IllegalArgumentException("pageSize and pageNum cannot be equal to or less than 0");
        page.accept(pageNum, pageSize);
        return this;
    }

    /**
     * 将所有的clobValue转为string
     */
    public PictogramMap clobValueToString() {
        try {
            for (Map.Entry<Object, Object> e : entrySet()) {
                if (e.getValue() instanceof Clob) {
                    Clob value = (Clob) e.getValue();
                    e.setValue(IOUtils.toString(value.getCharacterStream()));
                    value.free();
                }
            }
        } catch (SQLException | IOException e) {
            throw new IllegalArgumentException(e);
        }
        return this;
    }

    /**
     * 删除分页
     */
    public PictogramMap removePage() {
        return removeValue("pageNum", "pageSize");
    }

    /**
     * 设置分页
     */
    public PictogramMap setPage(int pageNum, int pageSize) {
        return putValue("pageNum", pageNum).putValue("pageSize", pageSize);
    }

    /**
     * 是否为空字符的key
     */
    public boolean isBlank(Object key) {
        return StringUtils.isBlank(getString(key));
    }

    /**
     * 是否为不是空字符的key
     */
    public boolean isNotBlank(Object key) {
        return !isBlank(key);
    }

    /**
     * 转换为实体
     */
    public <T> T toModel(Class<T> cls, Recognizer<Map<Object, Object>, String, Object> recognizer) {
        try {
            final Map<Object, Object> map = getMap();
            T obj = cls.newInstance();
            for (Field f : obj.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isFinal(f.getModifiers()))
                    continue;
                f.setAccessible(true);
                f.set(obj, recognizer.test(map, f.getName()));
            }
            return obj;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 转为实体 json方式
     */
    public <T> T toModel(TypeReference<T> cls) {
        return toModel(cls, null, null);
    }

    /**
     * 转为实体 json方式
     */
    public <T> T toModel(TypeReference<T> cls, ObjectMapper objectMapper, Function<PictogramMap, Object> func) {
        try {
            return Mapper.getObjectMapper(objectMapper).readValue(toJsonString(objectMapper, func), cls);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 转为json
     */
    public String toJsonString() {
        return toJsonString(null, null);
    }

    /**
     * 转为json
     */
    public String toJsonString(ObjectMapper objectMapper, Function<PictogramMap, Object> func) {
        try {
            return Mapper.getObjectMapper(objectMapper).writeValueAsString(func == null ? getMap() : func.apply(this));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 写出
     */
    public PictogramMap writerTo(Object src) throws IOException {
        return writerTo(src, null);
    }

    /**
     * 写出
     */
    public PictogramMap writerTo(Object src, ObjectMapper objectMapper) throws IOException {
        objectMapper = Mapper.getObjectMapper(objectMapper);
        if (src instanceof JsonGenerator)
            objectMapper.writeValue((JsonGenerator) src, getMap());
        else if (src instanceof File)
            objectMapper.writeValue((File) src, getMap());
        else if (src instanceof Writer)
            objectMapper.writeValue((Writer) src, getMap());
        else if (src instanceof OutputStream)
            objectMapper.writeValue((OutputStream) src, getMap());
        else
            throw new IllegalArgumentException("Unknown src");
        return this;
    }

    /**
     * 转为entrySet
     */
    public <K, V> Set<Map.Entry<K, V>> entrySet() {
        Map<K, V> map = getMap();
        return map.entrySet();
    }

    public interface Recognizer<D, K, R> {
        R test(D data, K key);
    }

    static class Mapper {
        static ObjectMapper defaultObjectMapper = new ObjectMapper();

        /**
         * 尽量使用自定义ObjectMapper defaultObjectMapper在多线程下响应速度会大幅降低
         */
        static ObjectMapper getObjectMapper(ObjectMapper objectMapper) {
            return objectMapper == null ? defaultObjectMapper : objectMapper;
        }
    }
}
