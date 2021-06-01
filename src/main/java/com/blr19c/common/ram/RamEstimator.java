package com.blr19c.common.ram;

import org.apache.lucene.util.RamUsageEstimator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 内存估算器
 *
 * @author blr
 */
public class RamEstimator {

    /**
     * @param objects 指针
     */
    public static long sizeOf(Object... objects) {
        return Arrays.stream(objects).mapToLong(RamEstimator::sizeOf).sum();
    }

    public static long sizeOf(Object obj) {
        return sizeOfObject(obj, new HashMap<>());
    }

    private static long sizeOfObject(Object o, Map<Class<?>, Long> memo) {
        if (o == null)
            return 0;
        if (o instanceof String)
            return RamUsageEstimator.sizeOf((String) o);
        if (o instanceof Map)
            return ((Map<?, ?>) o).entrySet().stream()
                    .mapToLong(e -> shallowSizeOfInstance(e.getKey(), memo) + shallowSizeOfInstance(e.getValue(), memo))
                    .sum();
        if (o instanceof Collection)
            return ((Collection<?>) o).stream().mapToLong(co -> shallowSizeOfInstance(co, memo)).sum();
        return shallowSizeOfInstance(o, memo);
    }

    private static long shallowSizeOfInstance(Object obj, Map<Class<?>, Long> memo) {
        if (obj == null)
            return 0;
        Class<?> clazz = obj.getClass();
        if (clazz.isArray())
            return shallowSizeOfArray(obj, memo);
        if (clazz.isPrimitive())
            return RamUsageEstimator.primitiveSizes.get(clazz);
        long size = RamUsageEstimator.NUM_BYTES_OBJECT_HEADER;
        // Walk type hierarchy
        for (; clazz != null; clazz = clazz.getSuperclass()) {
            final Field[] fields = AccessController.doPrivileged((PrivilegedAction<Field[]>) clazz::getDeclaredFields);
            for (Field f : fields) {
                if (!Modifier.isStatic(f.getModifiers())) {
                    final Class<?> type = f.getType();
                    if (type.isPrimitive()) {
                        size += RamUsageEstimator.primitiveSizes.get(type);
                        continue;
                    }
                    Long typeSize = memo.get(type);
                    if (typeSize != null) {
                        size += typeSize;
                        continue;
                    }
                    f.setAccessible(true);
                    try {
                        long fiz = sizeOfObject(f.get(obj), memo);
                        memo.put(type, fiz);
                        size += fiz;
                    } catch (IllegalAccessException ignored) {
                    }
                }
            }
        }
        return RamUsageEstimator.alignObjectSize(size);
    }

    private static long shallowSizeOfArray(Object array, Map<Class<?>, Long> memo) {
        long size = RamUsageEstimator.NUM_BYTES_ARRAY_HEADER;
        final int len = Array.getLength(array);
        if (len > 0) {
            Class<?> arrayElementClazz = array.getClass().getComponentType();
            if (arrayElementClazz.isPrimitive()) {
                size += (long) len * RamUsageEstimator.primitiveSizes.get(arrayElementClazz);
            } else {
                size += sizeOfObject(Array.get(array, 0), memo) * len;
            }
        }
        return RamUsageEstimator.alignObjectSize(size);
    }

}