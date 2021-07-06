package com.blr19c.common.mybatisWrapper.wrapper.select;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.exceptions.TooManyResultsException;

import java.util.List;
import java.util.function.Function;

/**
 * 单条查询
 *
 * @author blr
 */
public interface SelectOneSqlWrapper extends SelectListSqlWrapper {

    /**
     * 根据sqlWhere查询一个
     *
     * @param modelClass       所依靠表的实体类
     * @param sqlWhereFunction 查询条件
     */
    default <T> T selectOne(Class<T> modelClass, Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        List<T> ts = selectList(modelClass, sqlWhereFunction);
        if (ts == null || ts.isEmpty())
            return null;
        if (ts.size() != 1)
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + ts.size());
        return ts.get(0);
    }
}
