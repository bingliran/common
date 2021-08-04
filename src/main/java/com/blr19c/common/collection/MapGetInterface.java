package com.blr19c.common.collection;

import org.apache.commons.collections4.MapUtils;

import java.util.Map;
import java.util.Objects;

/**
 * map的通用获取方法
 */
public interface MapGetInterface {

    /**
     * 获取map
     */
    <K, V> Map<K, V> getMap();

    /**
     * 获取一个Byte类型的value
     */
    default Byte getByte(Object key) {
        return getByte(key, null);
    }

    /**
     * 获取一个Byte类型的value并存在默认值
     */
    default Byte getByte(Object key, Byte defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getByteValue(getMap(), key, defaultValue);
    }

    /**
     * 获取一个byte类型的value
     */
    default byte getByteValue(Object key) {
        return getByteValue(key, (byte) 0);
    }

    /**
     * 获取一个byte类型的value并存在默认值
     */
    default byte getByteValue(Object key, byte defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getByteValue(getMap(), key, defaultValue);
    }

    /**
     * 获取一个Boolean类型的value
     */
    default Boolean getBoolean(Object key) {
        return getBoolean(key, null);
    }

    /**
     * 获取一个Boolean类型的value并存在默认值
     */
    default Boolean getBoolean(Object key, Boolean defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getBoolean(getMap(), key, defaultValue);
    }

    /**
     * 获取一个boolean类型的value
     */
    default boolean getBooleanValue(Object key) {
        return getBooleanValue(key, false);
    }

    /**
     * 获取一个boolean类型的value并存在默认值
     */
    default boolean getBooleanValue(Object key, boolean defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getBooleanValue(getMap(), key, defaultValue);
    }

    /**
     * 获取一个Short类型的value
     */
    default Short getShort(Object key) {
        return getShort(key, null);
    }

    /**
     * 获取一个Short类型的value并存在默认值
     */
    default Short getShort(Object key, Short defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getShort(getMap(), key, defaultValue);
    }

    /**
     * 获取一个short类型的value
     */
    default short getShortValue(Object key) {
        return getShort(key, null);
    }

    /**
     * 获取一个short类型的value并存在默认值
     */
    default short getShortValue(Object key, short defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getShortValue(getMap(), key, defaultValue);
    }

    /**
     * 获取一个Integer类型的value
     */
    default Integer getInteger(Object key) {
        return getInteger(key, null);
    }

    /**
     * 获取一个Integer类型的value并存在默认值
     */
    default Integer getInteger(Object key, Integer defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getInteger(getMap(), key, defaultValue);
    }

    /**
     * 获取一个int类型的value
     */
    default int getIntValue(Object key) {
        return getIntValue(key, 0);
    }

    /**
     * 获取一个int类型的value并存在默认值
     */
    default int getIntValue(Object key, int defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getIntValue(getMap(), key, defaultValue);
    }

    /**
     * 获取一个Float类型的value
     */
    default Float getFloat(Object key) {
        return getFloat(key, null);
    }

    /**
     * 获取一个Float类型的value并存在默认值
     */
    default Float getFloat(Object key, Float defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getFloat(getMap(), key, defaultValue);
    }

    /**
     * 获取一个float类型的value
     */
    default float getFloatValue(Object key) {
        return getFloatValue(key, 0F);
    }

    /**
     * 获取一个float类型的value并存在默认值
     */
    default float getFloatValue(Object key, float defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getFloatValue(getMap(), key, defaultValue);
    }

    /**
     * 获取一个Long类型的value
     */
    default Long getLong(Object key) {
        return getLong(key, null);
    }

    /**
     * 获取一个Long类型的value并存在默认值
     */
    default Long getLong(Object key, Long defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getLong(getMap(), key, defaultValue);
    }

    /**
     * 获取一个long类型的value
     */
    default long getLongValue(Object key) {
        return getLongValue(key, 0);
    }

    /**
     * 获取一个long类型的value并存在默认值
     */
    default long getLongValue(Object key, long defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getLongValue(getMap(), key, defaultValue);
    }

    /**
     * 获取一个Double类型的value
     */
    default Double getDouble(Object key) {
        return getDouble(key, null);
    }

    /**
     * 获取一个Double类型的value并存在默认值
     */
    default Double getDouble(Object key, Double defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getDouble(getMap(), key, defaultValue);
    }

    /**
     * 获取一个double类型的value
     */
    default double getDoubleValue(Object key) {
        return getDoubleValue(key, 0D);
    }

    /**
     * 获取一个double类型的value并存在默认值
     */
    default double getDoubleValue(Object key, double defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getDoubleValue(getMap(), key, defaultValue);
    }

    /**
     * 获取一个String类型的value
     */
    default String getString(Object key) {
        return getString(key, null);
    }

    /**
     * 获取一个String类型的value并存在默认值
     */
    default String getString(Object key, String defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getString(getMap(), key, defaultValue);
    }

    /**
     * 获取一个任意类型的value
     */
    default <T> T getObject(Object key) {
        return getObject(key, null);
    }

    /**
     * 获取一个任意类型的value并存在默认值
     */
    default <T> T getObject(Object key, T defaultValue) {
        if (Objects.isNull(key)) return defaultValue;
        return MapUtils.getObject(getMap(), key, defaultValue);
    }
}
