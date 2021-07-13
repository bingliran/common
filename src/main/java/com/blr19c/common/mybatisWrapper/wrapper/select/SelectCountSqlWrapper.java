package com.blr19c.common.mybatisWrapper.wrapper.select;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.mybatisWrapper.wrapper.SqlWrapper;

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
    default <T, R, C extends AbstractWrapper<T, R, C>>
    int selectCount(Class<T> modelClass,
                    Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction) {
        initMappedStatement(modelClass, Integer.class);
        AbstractWrapper<T, R, C> sqlWhere = sqlWhereFunction.apply(new LambdaQueryWrapper<>());
        return getSqlSessionTemplate().<Integer>selectOne(
                getStatementId(SelectCountMethod.instance, modelClass),
                PictogramMap.getInstance(sqlWhere.getParamAlias(), sqlWhere)
        );
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        return SelectCountMethod.instance;
    }

    class SelectCountMethod extends AbstractWrapperMethod {
        static SelectCountMethod instance = new SelectCountMethod();

        @Override
        protected String getSql(TableInfo tableInfo) {
            return String.format(
                    SqlMethod.SELECT_COUNT.getSql(),
                    sqlFirst(),
                    sqlCount(),
                    tableInfo.getTableName(),
                    sqlWhereEntityWrapper(true, tableInfo),
                    sqlComment()
            );
        }
    }
}
