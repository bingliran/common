package com.blr19c.common.mybatisWrapper.wrapper;

import com.blr19c.common.collection.PictogramMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 转义标记map在返回时转换为PictogramMap
 * 自定义时可以继承EscapeMarkLinkedHashMap重写toPictogramMap并重新定义returnType
 *
 * @author blr
 */
public class EscapeMarkLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    public EscapeMarkLinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public EscapeMarkLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public EscapeMarkLinkedHashMap() {
    }

    public EscapeMarkLinkedHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public EscapeMarkLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    public PictogramMap toPictogramMap() {
        return PictogramMap.toPictogramMap(this)
                .clobValueToString()
                .blobValueToBytes()
                .underlineKeyToCamel();
    }
}
