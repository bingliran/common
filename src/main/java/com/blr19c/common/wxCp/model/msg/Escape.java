package com.blr19c.common.wxCp.model.msg;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

/**
 * 转义标记
 *
 * @author blr
 */
public interface Escape extends Serializable {
    Map<String, String> toMap();

    default AbstractMap.SimpleEntry<String, ?> toLowerCaseKey(Map.Entry<?, ?> entry) {
        return new AbstractMap.SimpleEntry<>(String.valueOf(entry.getKey()).toLowerCase(), entry.getValue());
    }
}
