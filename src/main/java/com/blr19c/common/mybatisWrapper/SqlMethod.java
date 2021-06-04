package com.blr19c.common.mybatisWrapper;

import org.apache.ibatis.mapping.SqlCommandType;

public enum SqlMethod {
    INSERT(SqlCommandType.INSERT, "insert into %s(%s) values <foreach collection=\"items\" item=\"item\" separator=\",\"> (%s) </foreach> </insert>"),
    ORACLE_INSERT(SqlCommandType.INSERT, "<script> INSERT ALL <foreach collection=\"items\" item=\"item\" separator=\"\"> INTO %s(%s) VALUES(%s) </foreach> SELECT 1 FROM DUAL </script>");

    private final SqlCommandType type;
    private final String sql;

    SqlMethod(SqlCommandType type, String sql) {
        this.type = type;
        this.sql = sql;
    }

    public SqlCommandType getType() {
        return type;
    }

    public String getSql() {
        return sql;
    }
}
