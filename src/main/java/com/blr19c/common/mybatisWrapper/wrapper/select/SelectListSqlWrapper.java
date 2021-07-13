package com.blr19c.common.mybatisWrapper.wrapper.select;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.mybatisWrapper.wrapper.SqlWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.function.Function;

/**
 * 列表查询
 *
 * @author blr
 */
public interface SelectListSqlWrapper extends SqlWrapper {

    /**
     * 根据sqlWhere查询列表
     *
     * @param modelClass       所依靠表的实体类
     * @param sqlWhereFunction 查询条件
     */
    default <T, R, C extends AbstractWrapper<T, R, C>>
    List<T> selectList(Class<T> modelClass,
                       Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction) {
        initMappedStatement(modelClass, modelClass);
        AbstractWrapper<T, R, C> sqlWhere = sqlWhereFunction.apply(new LambdaQueryWrapper<>());
        return getSqlSessionTemplate().selectList(
                getStatementId(SelectListMethod.instance, modelClass),
                PictogramMap.getInstance(sqlWhere.getParamAlias(), sqlWhere)
        );
    }

    /**
     * 根据sqlWhere查询列表并分页
     *
     * @param modelClass       所依靠表的实体类
     * @param sqlWhereFunction 查询条件
     */
    default <T, R, C extends AbstractWrapper<T, R, C>>
    PageInfo<T> selectListToPage(Class<T> modelClass,
                                 Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction,
                                 int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(selectList(modelClass, sqlWhereFunction));
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        return SelectListMethod.instance;
    }

    class SelectListMethod extends AbstractWrapperMethod {
        static SelectListMethod instance = new SelectListMethod();

        @Override
        protected String getSql(TableInfo tableInfo) {
            return String.format(
                    SqlMethod.SELECT_LIST.getSql(),
                    sqlFirst(),
                    sqlSelectColumns(tableInfo, true),
                    tableInfo.getTableName(),
                    sqlWhereEntityWrapper(true, tableInfo),
                    sqlOrderBy(tableInfo),
                    sqlComment()
            );
        }
    }
}
