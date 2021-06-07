package com.blr19c.common.mybatisWrapper.wrapper;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.mybatisWrapper.SqlMethod;
import org.apache.ibatis.mapping.SqlCommandType;

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
    default <T> int insert(Class<T> modelClass, Collection<Object> modelList) {
        initTypeHandler();
        String id = initMappedStatement(modelClass, OracleInsertMethod.instance, Integer.class).getId();
        return getSqlSessionTemplate().insert(id, toSqlForeach(modelList));
    }

    class OracleInsertMethod extends InsertMethod {
        static OracleInsertMethod instance = new OracleInsertMethod();

        @Override
        protected SqlCommandType getSqlCommandType(TableInfo tableInfo) {
            return SqlMethod.ORACLE_INSERT.getType();
        }

        @Override
        protected SqlMethod sqlMethod() {
            return SqlMethod.ORACLE_INSERT;
        }
    }
}
