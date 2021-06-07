package com.blr19c.common.mybatisWrapper.wrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.collection.PictogramMap;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.function.Function;

/**
 * 修改数据
 *
 * @author blr
 */
public interface UpdateWrapper extends SqlWrapper {

    /**
     * 根据sqlUpdateFunction更改
     *
     * @param modelClass        所依靠表的实体类
     * @param sqlUpdateFunction 修改项
     */
    default <T> int update(Class<T> modelClass,
                           Function<LambdaUpdateWrapper<T>, LambdaUpdateWrapper<T>> sqlUpdateFunction) {
        String id = initMappedStatement(modelClass, UpdateMethod.instance, Integer.class).getId();
        LambdaUpdateWrapper<T> updateFunction = sqlUpdateFunction.apply(new LambdaUpdateWrapper<>());
        return getSqlSessionTemplate().update(id,
                PictogramMap.getInstance(updateFunction.getParamAlias(), updateFunction));
    }

    class UpdateMethod extends AbstractWrapperMethod {
        static UpdateMethod instance = new UpdateMethod();

        @Override
        protected SqlCommandType getSqlCommandType(TableInfo tableInfo) {
            return SqlCommandType.UPDATE;
        }

        @Override
        protected String getSql(TableInfo tableInfo) {
            return String.format(
                    SqlMethod.UPDATE.getSql(),
                    tableInfo.getTableName(),
                    sqlSet(true, true, tableInfo, true, ENTITY, ENTITY_DOT),
                    sqlWhereEntityWrapper(true, tableInfo),
                    sqlComment()
            );
        }
    }
}
