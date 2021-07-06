package com.blr19c.common.mybatisWrapper.wrapper.select;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.mybatisWrapper.wrapper.EscapeMarkLinkedHashMap;
import com.blr19c.common.mybatisWrapper.wrapper.SqlWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.function.Function;

/**
 * 列表查询并已PictogramMap返回结果
 *
 * @author blr
 */
public interface SelectListMapSqlWrapper extends SqlWrapper {

    /**
     * 根据sqlWhere查询列表
     *
     * @param modelClass       所依靠表的实体类
     * @param sqlWhereFunction 查询条件
     */
    default <T> List<PictogramMap> selectListMap(Class<T> modelClass,
                                                 Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        initMappedStatement(modelClass, EscapeMarkLinkedHashMap.class);
        LambdaQueryWrapper<T> sqlWhere = sqlWhereFunction.apply(new LambdaQueryWrapper<>());
        return getSqlSessionTemplate().selectList(
                getStatementId(SelectListMapMethod.instance, modelClass),
                PictogramMap.getInstance(sqlWhere.getParamAlias(), sqlWhere)
        );
    }

    /**
     * 根据sqlWhere查询列表并分页
     *
     * @param modelClass       所依靠表的实体类
     * @param sqlWhereFunction 查询条件
     */
    default <T> PageInfo<PictogramMap> selectListMapToPage(Class<T> modelClass,
                                                           Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction,
                                                           int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(selectListMap(modelClass, sqlWhereFunction));
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        return SelectListMapMethod.instance;
    }

    class SelectListMapMethod extends SelectListSqlWrapper.SelectListMethod {
        static SelectListMapMethod instance = new SelectListMapMethod();
    }
}
