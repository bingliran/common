package com.blr19c.common.mybatisWrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.collection.PictogramMap;

import java.util.function.Function;

/**
 * count(?) 查询
 *
 * @author blr
 */
public interface SelectCountSqlWrapper extends SqlWrapper {

    /**
     * 根据指定位置查询计数
     *
     * @param modelClass       所依靠表的实体类
     * @param sqlWhereFunction 查询条件
     */
    default <T> int selectCount(Class<T> modelClass,
                                Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        initMappedStatement(modelClass, SelectCountMethod.instance, Integer.class);
        LambdaQueryWrapper<T> sqlWhere = sqlWhereFunction.apply(new LambdaQueryWrapper<>());
        return getSqlSessionTemplate().<Integer>selectOne(
                getStatementId(SelectCountMethod.instance, modelClass),
                PictogramMap.getInstance()
                        .putValue(sqlWhere.getParamAlias(), sqlWhere)
                        .getMap()
        );
    }

    class SelectCountMethod extends AbstractSelectMethod {
        static SelectCountMethod instance = new SelectCountMethod();

        @Override
        public String getSql(TableInfo tableInfo) {
            return String.format(
                    SqlMethod.SELECT_COUNT.getSql(),
                    sqlCount(),
                    tableInfo.getTableName(),
                    sqlWhereEntityWrapper(true, tableInfo)
            );
        }
    }
}
