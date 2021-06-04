package com.blr19c.common.mybatisWrapper;

import org.apache.ibatis.mapping.SqlCommandType;

/**
 * 插入 oracle专用
 *
 * @author blr
 */
public interface OracleInsertWrapper extends InsertWrapper {

    /**
     * 新增
     *
     * @param modelClass 实体类class
     * @param modelList  新增的数据
     */
    default <T> int insert(Class<T> modelClass, Object... modelList) {
        initTypeHandler();
        String id = initMappedStatement(modelClass, OracleInsertMethod.instance, Integer.class).getId();
        return getSqlSessionTemplate().insert(id, toSqlForeach(modelList));
    }

    class OracleInsertMethod extends InsertMethod {
        static OracleInsertMethod instance = new OracleInsertMethod();

        @Override
        public SqlCommandType getSqlCommandType() {
            return SqlMethod.ORACLE_INSERT.getType();
        }

        @Override
        protected SqlMethod sqlMethod() {
            return SqlMethod.ORACLE_INSERT;
        }
    }
}
