package com.blr19c.common.mybatisWrapper.enums;

import org.apache.ibatis.mapping.SqlCommandType;

public enum SqlMethod {
    /**
     * 循环列表的item
     */
    SQL_LIST_ITEM(SqlCommandType.UNKNOWN, "item"),
    /**
     * 批量插入sql
     */
    INSERT(SqlCommandType.INSERT, "<script>\n insert into %s \n%s values \n<foreach collection=\"collection\" item=\"item\" separator=\",\">\n %s \n</foreach> \n</script>"),
    /**
     * oracle的批量插入sql
     */
    ORACLE_INSERT(SqlCommandType.INSERT, "<script>\n INSERT ALL \n<foreach collection=\"collection\" item=\"item\" separator=\"\">\n INTO %s \n%s VALUES \n%s \n</foreach>\n SELECT 1 FROM DUAL \n</script>"),
    /**
     * mysql的合并(新增如果存在就更新)
     */
    MYSQL_MERGE(SqlCommandType.UPDATE, "<script>\n insert into %s \n%s values \n<foreach collection=\"collection\" item=\"item\" separator=\",\">\n %s \n</foreach> \n on duplicate key update %s \n</script>"),
    /**
     * oracle的合并(新增如果存在就更新)
     */
    ORACLE_MERGE(SqlCommandType.UPDATE, "<script>\n MERGE INTO %s targetTable USING \n (SELECT %s AS id FROM DUAL) sourceTable ON(sourceTable.id = targetTable.%s) WHEN MATCHED THEN \n UPDATE %s \n WHEN NOT MATCHED THEN \n INSERT %s VALUES %s \n</script>");

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
