package com.blr19c.common.mybatisWrapper.wrapper.update;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.blr19c.common.collection.PictogramStream;
import com.blr19c.common.mybatisWrapper.enums.SqlMethod;
import com.blr19c.common.mybatisWrapper.wrapper.insert.OracleInsertWrapper;

import java.util.Collection;

/**
 * oracle插入或者修改数据(MERGE INTO语法)
 *
 * @author blr
 */
public interface OracleMergeWrapper extends MergeWrapper, OracleInsertWrapper {

    /**
     * 合并
     *
     * @param modelClass 实体类class
     * @param modelList  合并的数据
     */
    @Override
    default <T> int merge(Class<T> modelClass, Collection<T> modelList) {
        final String id = initMappedStatement(modelClass, Integer.class).getId();
        return PictogramStream.of(modelList)
                .mapToInt(model -> getSqlSessionTemplate().update(id, model))
                .sum();
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        return OracleMergeMethod.instance;
    }


    class OracleMergeMethod extends OracleInsertWrapper.OracleInsertMethod {
        static OracleMergeMethod instance = new OracleMergeMethod();

        @Override
        protected SqlMethod sqlMethod() {
            return SqlMethod.ORACLE_MERGE;
        }

        @Override
        protected String getInsertPrefix() {
            return null;
        }

        @Override
        protected String getSql(TableInfo tableInfo) {
            return String.format(
                    sqlMethod().getSql(),
                    tableInfo.getTableName(),
                    SqlScriptUtils.safeParam(tableInfo.getKeyProperty()),
                    tableInfo.getKeyColumn(),
                    sqlSet(tableInfo.isWithLogicDelete(), false, tableInfo, false, null, null),
                    getFieldSql(tableInfo),
                    getValueSql(tableInfo)
            );
        }
    }
}
