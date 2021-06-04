package com.blr19c.common.mybatisWrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.collection.PictogramMap;
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
    default <T> List<T> selectList(Class<T> modelClass,
                                   Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        initMappedStatement(modelClass, SelectListMethod.instance, modelClass);
        LambdaQueryWrapper<T> sqlWhere = sqlWhereFunction.apply(new LambdaQueryWrapper<>());
        return getSqlSessionTemplate().selectList(
                getStatementId(SelectListMethod.instance, modelClass),
                PictogramMap.getInstance()
                        .putValue(sqlWhere.getParamAlias(), sqlWhere)
                        .getMap()
        );
    }

    /**
     * 根据sqlWhere查询列表并分页
     *
     * @param modelClass       所依靠表的实体类
     * @param sqlWhereFunction 查询条件
     */
    default <T> PageInfo<T> selectListToPage(Class<T> modelClass,
                                             Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction,
                                             int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(selectList(modelClass, sqlWhereFunction));
    }

    class SelectListMethod extends AbstractSelectMethod {
        static SelectListMethod instance = new SelectListMethod();

        @Override
        public String getSql(TableInfo tableInfo) {
            return String.format(SqlMethod.SELECT_LIST.getSql(),
                    sqlSelectColumns(tableInfo, true),
                    tableInfo.getTableName(),
                    sqlWhereEntityWrapper(true, tableInfo)
            );
        }
    }
}
