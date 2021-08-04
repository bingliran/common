package com.blr19c.common.collection;


import com.blr19c.common.code.ReflectionUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.pagehelper.PageHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.sql.Clob;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * 将map转为更便于操作的象形map
 * 注: 使用时请格外注意泛型结构
 */
@SuppressWarnings(value = {"unchecked"})
@JsonSerialize(using = PictogramJsonSerialize.class)
public class PictogramMap implements MapGetInterface, MapComputeInterface, MapPageInterface {
    private final Map<?, ?> data;

    public PictogramMap(Map<?, ?> data, boolean isSynchronized) {
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
        return toPictogramMapAsModel(data, entryFunction, true, false);
    }

    /**
     * 将一个对象转为map并设置自定义转换
     *
     * @param entryFunction  entry转换
     * @param callSuperClass true包括父类
     * @param useGetter      使用getter方法获取字段
     */
    public static PictogramMap toPictogramMapAsModel(Object data,
                                                     Function<Map.Entry<?, ?>, Map.Entry<?, ?>> entryFunction,
                                                     boolean callSuperClass,
                                                     boolean useGetter) {
        final PictogramMap pictogramMap = getInstance();
        //使用get方法
        if (useGetter) {
            ReflectionUtils.doWithPropertyDescriptors(data.getClass(), null,
                    p -> {
                        Method readMethod = p.getReadMethod();
                        ReflectionUtils.makeAccessible(readMethod);
                        pictogramMap.putValue(p.getName(), ReflectionUtils.invokeMethod(readMethod, data));
                    }, p -> {
                        Method readMethod = p.getReadMethod();
                        return readMethod != null &&
                                !ReflectionUtils.isObjectMethod(readMethod) &&
                                (callSuperClass || readMethod.getDeclaringClass() != data.getClass());
                    }
            );
            return pictogramMap;
        }
        //使用字段反射
        ReflectionUtils.ReflectionCallback<Field> fieldCallback = field -> {
            ReflectionUtils.makeAccessible(field);
            Map.Entry<?, ?> entry = entryFunction.apply(new AbstractMap.SimpleEntry<>(field.getName(), field.get(data)));
            pictogramMap.putValueToMap(entry);
        };
        ReflectionUtils.ReflectionFilter<Field> fieldFilter = field -> !Modifier.isStatic(field.getModifiers());
        if (callSuperClass) {
            ReflectionUtils.doWithFields(data.getClass(), fieldCallback, fieldFilter);
        } else {
            ReflectionUtils.doWithLocalFields(data.getClass(), fieldCallback, fieldFilter);
        }
        return pictogramMap;
    }

    /**
     * 获取一个PictogramMap实例 并含有空的非同步map
     */
    public static PictogramMap getInstance() {
        return getInstance(false);
    }

    /**
     * 获取一个带有key和value的非同步PictogramMap实例
     */
    public static PictogramMap getInstance(Object key, Object value) {
        return getInstance().putValue(key, value);
    }

    /**
     * 获取一个PictogramMap实例 并含有空的指定同步map
     */
    public static PictogramMap getInstance(boolean isSynchronized) {
        return toPictogramMap(null, isSynchronized);
    }

    /**
     * 设置全局分页名称
     */
    public static void setGlobalPageName(String numName, String sizeName, boolean modifyLock) {
        Page.setPage(numName, sizeName, modifyLock);
    }

    /**
     * 当map存在时比较map
     */
    @Override
    public int hashCode() {
        return getMap().hashCode();
    }

    /**
     * 相同的PictogramMap和相同的Map均视为相同
     */
    @Override
    public boolean equals(Object obj) {
        if (Objects.isNull(obj)) return false;
        if (obj instanceof PictogramMap) {
            PictogramMap pictogramMap = (PictogramMap) obj;
            return Objects.equals(pictogramMap.getMap(), getMap());
        }
        if (obj instanceof Map) return Objects.equals(getMap(), obj);
        return false;
    }

    @Override
    public String toString() {
        return "{PictogramMap: " + this.getMap() + "}";
    }

    /**
     * 返回一个未经检查的map
     */
    @Override
    public <K, V> Map<K, V> getMap() {
        return (Map<K, V>) data;
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
     * 替换指定key的value
     */
    public PictogramMap replace(Object key, Object value) {
        getMap().replace(key, value);
        return this;
    }

    /**
     * 指定key的value为oldValue时替换key的value为newValue
     */
    public PictogramMap replace(Object key, Object oldValue, Object newValue) {
        getMap().replace(key, oldValue, newValue);
        return this;
    }

    /**
     * 根据 valueFunction替换value
     */
    public <V> PictogramMap replace(Function<V, Object> valueFunction) {
        for (Map.Entry<Object, Object> e : this.entrySet()) {
            e.setValue(valueFunction.apply((V) e.getValue()));
        }
        return this;
    }

    /**
     * 根据 keyFunction替换key
     */
    public <K> PictogramMap replaceKey(Function<K, Object> keyFunction) {
        Iterator<Map.Entry<Object, Object>> iterator = this.entrySet().iterator();
        PictogramMap newMap = getInstance();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> next = iterator.next();
            Object apply = keyFunction.apply((K) next.getKey());
            if (Objects.equals(apply, next.getKey()))
                continue;
            newMap.putValue(apply, next.getValue());
            iterator.remove();
        }
        return this.putPictogramMap(newMap);
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

    /**
     * value流
     */
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
        return putPictogramMap(toPictogramMapAsModel(model, entryFunction));
    }

    /**
     * 向map中添加一个model
     */
    public PictogramMap putModel(Object model) {
        return putModel(model, e -> e);
    }

    /**
     * 向map中添加一个model
     */
    public PictogramMap putModel(Object model,
                                 Function<Map.Entry<?, ?>, Map.Entry<?, ?>> entryFunction,
                                 boolean callSuperClass,
                                 boolean useGetter) {
        return putPictogramMap(toPictogramMapAsModel(model, entryFunction, callSuperClass, useGetter));
    }

    /**
     * 向map中添加一个pictogramMap
     */
    public PictogramMap putPictogramMap(PictogramMap pictogramMap) {
        return putMap(pictogramMap.getMap());
    }

    /**
     * 向map中添加一个map
     */
    public PictogramMap putMap(Map<?, ?> map) {
        getMap().putAll(map);
        return this;
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
     * 在pictogramMap向this pictogramMap 添加整组数据
     */
    public PictogramMap putValues(PictogramMap pictogramMap, Object... keys) {
        if (keys == null || keys.length == 0)
            return this;
        PictogramStream.of(keys).forEach(key -> this.putValueToMap(key, pictogramMap));
        return this;
    }

    /**
     * 在map向this pictogramMap 添加整组数据
     */
    public PictogramMap putValues(Map<?, ?> map, Object... keys) {
        return putValues(toPictogramMap(map), keys);
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
        return putValue(key, getObject(thisKey)).removeValue(thisKey);
    }

    /**
     * 从pictogramMap向map中添加一组元素
     */
    public PictogramMap putValueToMap(Object key, PictogramMap pictogramMap) {
        return putValue(key, pictogramMap.getObject(key));
    }

    /**
     * 从map向map中添加一组元素
     */
    public PictogramMap putValueToMap(Object key, Map<?, ?> map) {
        return putValue(key, map.get(key));
    }

    /**
     * 从Map.Entry向map中添加一组元素
     */
    public PictogramMap putValueToMap(Map.Entry<?, ?> entry) {
        return putValue(entry.getKey(), entry.getValue());
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
     * 将所有的clobValue转为string
     */
    public PictogramMap clobValueToString() {
        return replace(Unchecked.function(o -> o instanceof Clob ? IOUtils.toString(((Clob) o).getCharacterStream()) : o));
    }

    /**
     * 将所有的blobValue转为byte[]
     */
    public PictogramMap blobValueToBytes() {
        return replace(Unchecked.function(o -> o instanceof Blob ? ((Blob) o).getBytes(1, (int) ((Blob) o).length()) : o));
    }

    /**
     * 字符串下划线key转驼峰格式
     */
    public PictogramMap underlineKeyToCamel() {
        return replaceKey(k -> k instanceof String && !com.baomidou.mybatisplus.core.toolkit.StringUtils.isCamel((String) k) ?
                com.baomidou.mybatisplus.core.toolkit.StringUtils.underlineToCamel((String) k) : k);
    }

    /**
     * 字符串驼峰key转下划线格式
     */
    public PictogramMap camelKeyToUnderline() {
        return replaceKey(k -> k instanceof String && com.baomidou.mybatisplus.core.toolkit.StringUtils.isCamel((String) k) ?
                com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline((String) k).toUpperCase() : k);
    }

    /**
     * 删除分页
     */
    public PictogramMap removePage() {
        return removeValue(Page.getPageNumName(), Page.getPageSizeName());
    }

    /**
     * 设置分页
     */
    public PictogramMap setPage(int pageNum, int pageSize) {
        return putValue(Page.getPageNumName(), pageNum).putValue(Page.getPageSizeName(), pageSize);
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
     * 转换为实体
     */
    public <T> T toModel(Class<T> cls, Recognizer<Map<Object, Object>, String, Object> recognizer) {
        return toModel(cls, recognizer, true, true);
    }


    /**
     * 转换为实体
     */
    public <T> T toModel(Class<T> cls,
                         Recognizer<Map<Object, Object>, String, Object> recognizer,
                         boolean callSuperClass,
                         boolean useSetter) {
        final T obj;
        try {
            obj = cls.getConstructor().newInstance();
        } catch (Exception e) {
            ReflectionUtils.handleReflectionException(e);
            throw new IllegalStateException("Should never get here");
        }
        final Map<Object, Object> map = getMap();
        //使用set方法
        if (useSetter) {
            ReflectionUtils.doWithPropertyDescriptors(cls, null,
                    p -> {
                        Method writeMethod = p.getWriteMethod();
                        Class<?> propertyType = p.getPropertyType();
                        ReflectionUtils.makeAccessible(writeMethod);
                        Object value = recognizer.test(map, p.getName());
                        ReflectionUtils.invokeMethod(writeMethod, obj, propertyType.cast(value));
                    }, p -> {
                        Method writeMethod = p.getWriteMethod();
                        return writeMethod != null &&
                                recognizer.test(map, p.getName()) != null &&
                                !ReflectionUtils.isObjectMethod(writeMethod) &&
                                (callSuperClass || writeMethod.getDeclaringClass() != cls);
                    }
            );
            return obj;
        }
        //使用字段反射
        ReflectionUtils.ReflectionFilter<Field> filter = f ->
                !Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers());
        ReflectionUtils.ReflectionCallback<Field> callback = f -> {
            f.setAccessible(true);
            f.set(obj, recognizer.test(map, f.getName()));
        };
        if (callSuperClass) {
            ReflectionUtils.doWithFields(cls, callback, filter);
        } else {
            ReflectionUtils.doWithLocalFields(cls, callback, filter);
        }
        return obj;
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
    public <T> T
    toModel(TypeReference<T> cls, ObjectMapper objectMapper, Function<PictogramMap, Object> func) {
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

        /**
         * 获取objectMapper
         */
        static ObjectMapper getObjectMapper(ObjectMapper objectMapper) {
            return objectMapper == null ? getObjectMapper() : objectMapper;
        }

        /**
         * 默认的objectMapper会忽略未知字段和大小写
         */
        private static ObjectMapper getObjectMapper() {
            return new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        }

    }
}
