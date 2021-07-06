package com.blr19c.common.mybatisWrapper.wrapper.insert;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.blr19c.common.mybatisWrapper.enums.SqlMethod;

import java.util.Collection;

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
    @Override
    default <T> int insert(Class<?> modelClass, Collection<T> modelList) {
        String id = initMappedStatement(modelClass, Integer.class).getId();
        return getSqlSessionTemplate().insert(id, toSqlForeach(modelList));
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        return OracleInsertMethod.instance;
    }

    class OracleInsertMethod extends InsertMethod {
        static OracleInsertMethod instance = new OracleInsertMethod();

        @Override
        protected SqlMethod sqlMethod() {
            return SqlMethod.ORACLE_INSERT;
        }

        @Override
        protected String getFieldSql(TableInfo tableInfo) {
            return SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlColumnMaybeIf(getInsertPrefix()),
                    LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);
        }

        @Override
        protected String getValueSql(TableInfo tableInfo) {
            return SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlPropertyMaybeIf(getInsertPrefix()),
                    LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);
        }
    }
}
