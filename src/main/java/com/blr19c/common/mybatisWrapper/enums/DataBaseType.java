package com.blr19c.common.mybatisWrapper.enums;

import com.blr19c.common.collection.PictogramStream;

/**
 * 数据库类型枚举
 */
public enum DataBaseType {
    MYSQL, ORACLE, OTHER;


    public static DataBaseType value(String name) {
        if (name == null)
            return DataBaseType.OTHER;
        DataBaseType type = PictogramStream.of(DataBaseType.values())
                .searchOneOrNull(dataBaseType -> name.toUpperCase().equals(dataBaseType.toString()));
        return type == null ? DataBaseType.OTHER : type;
    }
}
