package com.blr19c.common.mybatisWrapper.wrapper.delete;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.mybatisWrapper.wrapper.SqlWrapper;
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
    default <T, R, C extends AbstractWrapper<T, R, C>>
    int delete(Class<T> modelClass,
               Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlQueryFunction) {
        String id = initMappedStatement(modelClass, Integer.class).getId();
        AbstractWrapper<T, R, C> deleteFunction = sqlQueryFunction.apply(new LambdaQueryWrapper<>());
        return getSqlSessionTemplate().delete(id, PictogramMap.getInstance(deleteFunction.getParamAlias(), deleteFunction));
    }

    @Override
    default AbstractWrapperMethod getWrapperMethod() {
        return DeleteMethod.instance;
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
