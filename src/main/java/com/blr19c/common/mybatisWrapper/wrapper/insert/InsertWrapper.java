package com.blr19c.common.mybatisWrapper.wrapper.insert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.collection.PictogramStream;
import com.blr19c.common.mybatisWrapper.enums.SqlMethod;
import com.blr19c.common.mybatisWrapper.wrapper.SqlWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * 插入数据
 *
 * @author blr
 */
public interface InsertWrapper extends SqlWrapper {

    /**
     * 新增
     *
     * @param modelClass 实体类class
     * @param modelList  新增的数据
     */
    default <T> int insert(Class<?> modelClass, Collection<T> modelList) {
        String id = initMappedStatement(modelClass, Integer.class).getId();
        return getSqlSessionTemplate().insert(id, toSqlForeach(modelList));
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        return InsertMethod.instance;
    }

    default PictogramMap toSqlForeach(Collection<?> collection) {
        return PictogramMap.getInstance("collection", collection);
    }

    class InsertMethod extends AbstractWrapperMethod {
        static InsertMethod instance = new InsertMethod();
        static final String INSERT_PREFIX = SqlMethod.SQL_LIST_ITEM.getSql() + ".";

        @Override
        protected SqlCommandType getSqlCommandType(TableInfo tableInfo) {
            return sqlMethod().getType();
        }

        @Override
        protected KeyGenerator getKeyGenerator(TableInfo tableInfo) {
            KeyGenerator keyGenerator = super.getKeyGenerator(tableInfo);
            return StringUtils.isNotBlank(tableInfo.getKeyProperty()) ?
                    (tableInfo.getIdType() == IdType.AUTO ?
                            new Jdbc3KeyGenerator() :
                            (tableInfo.getKeySequence() != null ?
                                    TableInfoHelper.genKeyGenerator("insertBatch", tableInfo, builderAssistant) :
                                    keyGenerator)
                    ) : keyGenerator;
        }

        protected SqlMethod sqlMethod() {
            return SqlMethod.INSERT;
        }

        protected String getInsertPrefix() {
            return INSERT_PREFIX;
        }

        protected String getFieldSql(TableInfo tableInfo) {
            return SqlScriptUtils.convertTrim(
                    PictogramStream.of(tableInfo.getFieldList())
                            .map(TableFieldInfo::getColumn)
                            .addFirst(tableInfo.getKeyColumn())
                            .collect(Collectors.joining(",")),
                    LEFT_BRACKET, RIGHT_BRACKET, null, COMMA
            );
        }

        protected String getValueSql(TableInfo tableInfo) {
            return SqlScriptUtils.convertTrim(
                    PictogramStream.of(tableInfo.getFieldList())
                            .map(t -> t.getInsertSqlProperty(getInsertPrefix()))
                            .addFirst(tableInfo.getKeyInsertSqlProperty(getInsertPrefix(), false))
                            .collect(Collectors.joining()),
                    LEFT_BRACKET, RIGHT_BRACKET, null, COMMA
            );
        }

        @Override
        protected String getSql(TableInfo tableInfo) {
            return String.format(
                    sqlMethod().getSql(),
                    tableInfo.getTableName(),
                    getFieldSql(tableInfo),
                    getValueSql(tableInfo)
            );
        }
    }
}
