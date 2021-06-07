package com.blr19c.common.mybatisWrapper.wrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.collection.PictogramMap;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.function.Function;

/**
 * 删除数据
 *
 * @author blr
 */
public interface DeleteWrapper extends SqlWrapper {

    /**
     * 根据sqlQueryFunction删除
     *
     * @param modelClass       所依靠表的实体类
     * @param sqlQueryFunction 删除项
     */
    default <T> int delete(Class<T> modelClass,
                           Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlQueryFunction) {
        String id = initMappedStatement(modelClass, DeleteMethod.instance, Integer.class).getId();
        LambdaQueryWrapper<T> deleteFunction = sqlQueryFunction.apply(new LambdaQueryWrapper<>());
        return getSqlSessionTemplate().delete(id, PictogramMap.getInstance(deleteFunction.getParamAlias(), deleteFunction));
    }

    class DeleteMethod extends AbstractWrapperMethod {
        static DeleteMethod instance = new DeleteMethod();

        @Override
        protected String getSql(TableInfo tableInfo) {
            //逻辑删除 & 真删除
            return tableInfo.isWithLogicDelete() ? logicDelete(tableInfo) : delete(tableInfo);
        }

        @Override
        protected SqlCommandType getSqlCommandType(TableInfo tableInfo) {
            return tableInfo.isWithLogicDelete() ? SqlCommandType.UPDATE : SqlCommandType.DELETE;
        }

        protected String logicDelete(TableInfo tableInfo) {
            return String.format(
                    SqlMethod.LOGIC_DELETE.getSql(),
                    tableInfo.getTableName(),
                    sqlLogicSet(tableInfo),
                    sqlWhereEntityWrapper(true, tableInfo),
                    sqlComment()
            );
        }

        protected String delete(TableInfo tableInfo) {
            return String.format(
                    SqlMethod.DELETE.getSql(),
                    tableInfo.getTableName(),
                    sqlWhereEntityWrapper(true, tableInfo),
                    sqlComment()
            );
        }
    }
}
