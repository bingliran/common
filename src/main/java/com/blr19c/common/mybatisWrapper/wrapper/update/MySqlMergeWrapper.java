package com.blr19c.common.mybatisWrapper.wrapper.update;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.collection.PictogramStream;
import com.blr19c.common.mybatisWrapper.enums.SqlMethod;
import com.blr19c.common.mybatisWrapper.wrapper.insert.InsertWrapper;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * MySql插入或者修改数据(on duplicate key update语法)
 *
 * @author blr
 */
public interface MySqlMergeWrapper extends MergeWrapper, InsertWrapper {

    /**
     * 合并
     *
     * @param modelClass 实体类class
     * @param modelList  合并的数据
     */
    @Override
    default <T> int merge(Class<T> modelClass, Collection<T> modelList) {
        String id = initMappedStatement(modelClass, Integer.class).getId();
        return getSqlSessionTemplate().update(id, toSqlForeach(modelList));
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        return MySqlMergeMethod.instance;
    }

    class MySqlMergeMethod extends InsertWrapper.InsertMethod {
        static MySqlMergeMethod instance = new MySqlMergeMethod();

        @Override
        protected SqlMethod sqlMethod() {
            return SqlMethod.MYSQL_MERGE;
        }

        @Override
        protected String getSql(TableInfo tableInfo) {
            String field = getFieldSql(tableInfo);
            String value = getValueSql(tableInfo);
            String merge = getMergeSql(tableInfo);
            return String.format(
                    sqlMethod().getSql(),
                    tableInfo.getTableName(),
                    field,
                    value,
                    merge
            );
        }

        protected String getMergeSql(TableInfo tableInfo) {
            return PictogramStream.of(tableInfo.getFieldList())
                    .map(TableFieldInfo::getColumn)
                    .map(c -> c + " = values(" + c + ")")
                    .collect(Collectors.joining(","));
        }
    }
}
