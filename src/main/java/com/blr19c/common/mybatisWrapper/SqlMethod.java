package com.blr19c.common.mybatisWrapper;

import org.apache.ibatis.mapping.SqlCommandType;

public enum SqlMethod {
    SQL_LIST_ITEM(SqlCommandType.UNKNOWN, "item"),
    INSERT(SqlCommandType.INSERT, "<script> insert into %s %s values <foreach collection=\"collection\" item=\"item\" separator=\",\"> %s </foreach> </insert> </script>"),
    ORACLE_INSERT(SqlCommandType.INSERT, "<script> INSERT ALL <foreach collection=\"collection\" item=\"item\" separator=\"\"> INTO %s %s VALUES %s  </foreach> SELECT 1 FROM DUAL </script>");

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
