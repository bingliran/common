package com.blr19c.common.mybatisWrapper;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.mybatisWrapper.enums.DataBaseType;
import com.blr19c.common.mybatisWrapper.wrapper.delete.DeleteWrapper;
import com.blr19c.common.mybatisWrapper.wrapper.insert.InsertWrapper;
import com.blr19c.common.mybatisWrapper.wrapper.insert.OracleInsertWrapper;
import com.blr19c.common.mybatisWrapper.wrapper.select.*;
import com.blr19c.common.mybatisWrapper.wrapper.update.MergeWrapper;
import com.blr19c.common.mybatisWrapper.wrapper.update.MySqlMergeWrapper;
import com.blr19c.common.mybatisWrapper.wrapper.update.OracleMergeWrapper;
import com.blr19c.common.mybatisWrapper.wrapper.update.UpdateWrapper;
import com.blr19c.common.spring.SpringBeanUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.mybatis.spring.SqlSessionTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.springframework.jdbc.object.BatchSqlUpdate.DEFAULT_BATCH_SIZE;

/**
 * SqlWrapper的便捷工具类
 * <p>
 * 直接使用 sqlWhere是先加载
 * sqlWhereFunction是在init时后加载
 *
 * @author blr
 */
public class SqlWrapperUtils {

    /**
     * 数据库类型枚举
     */
    public static DataBaseType getDataBaseType() {
        return LazyDataBaseType.dataBaseType;
    }

    /**
     * 数据库类型名称
     */
    public static String getDataBaseName() {
        return LazyDataBaseType.databaseProductName;
    }

    /**
     * 计数查询
     */
    public static <T> int count(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.selectCountSqlWrapper.selectCount(modelClass, l -> sqlWhere);
    }

    public static <T, R, C extends AbstractWrapper<T, R, C>>
    int count(Class<T> modelClass,
              Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction) {
        return Wrapper.selectCountSqlWrapper.selectCount(modelClass, sqlWhereFunction);
    }

    /**
     * 单条查询
     */
    public static <T> T one(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.selectOneSqlWrapper.selectOne(modelClass, l -> sqlWhere);
    }

    public static <T, R, C extends AbstractWrapper<T, R, C>>
    T one(Class<T> modelClass,
          Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction) {
        return Wrapper.selectOneSqlWrapper.selectOne(modelClass, sqlWhereFunction);
    }

    /**
     * 单条查询PictogramMap
     */
    public static <T> PictogramMap oneMap(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.selectOneMapSqlWrapper.selectOneMap(modelClass, l -> sqlWhere);
    }

    public static <T, R, C extends AbstractWrapper<T, R, C>>
    PictogramMap oneMap(Class<T> modelClass,
                        Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction) {
        return Wrapper.selectOneMapSqlWrapper.selectOneMap(modelClass, sqlWhereFunction);
    }

    /**
     * 列表查询
     */
    public static <T> List<T> list(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.selectListSqlWrapper.selectList(modelClass, l -> sqlWhere);
    }

    public static <T, R, C extends AbstractWrapper<T, R, C>>
    List<T> list(Class<T> modelClass,
                 Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction) {
        return Wrapper.selectListSqlWrapper.selectList(modelClass, sqlWhereFunction);
    }

    /**
     * 列表分页查询
     */
    public static <T> PageInfo<T> list(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere,
                                       int pageNum, int pageSize) {
        return Wrapper.selectListSqlWrapper.selectListToPage(modelClass, l -> sqlWhere, pageNum, pageSize);
    }

    public static <T, R, C extends AbstractWrapper<T, R, C>>
    PageInfo<T> list(Class<T> modelClass,
                     Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction,
                     int pageNum, int pageSize) {
        return Wrapper.selectListSqlWrapper.selectListToPage(modelClass, sqlWhereFunction, pageNum, pageSize);
    }

    /**
     * 列表PictogramMap查询
     */
    public static <T> List<PictogramMap> listMap(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.selectListMapSqlWrapper.selectListMap(modelClass, l -> sqlWhere);
    }

    public static <T, R, C extends AbstractWrapper<T, R, C>>
    List<PictogramMap> listMap(Class<T> modelClass,
                               Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction) {
        return Wrapper.selectListMapSqlWrapper.selectListMap(modelClass, sqlWhereFunction);
    }

    /**
     * 列表PictogramMap分页查询
     */
    public static <T>
    PageInfo<PictogramMap> listMap(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere,
                                   int pageNum, int pageSize) {
        return Wrapper.selectListMapSqlWrapper.selectListMapToPage(modelClass, l -> sqlWhere, pageNum, pageSize);
    }

    public static <T, R, C extends AbstractWrapper<T, R, C>>
    PageInfo<PictogramMap> listMap(Class<T> modelClass,
                                   Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> sqlWhereFunction,
                                   int pageNum, int pageSize) {
        return Wrapper.selectListMapSqlWrapper.selectListMapToPage(modelClass, sqlWhereFunction, pageNum, pageSize);
    }

    /**
     * 修改
     */
    public static <T> int update(Class<T> modelClass,
                                 LambdaUpdateWrapper<T> updateFunction) {
        return Wrapper.updateWrapper.update(modelClass, l -> updateFunction);
    }

    public static <T> int update(Class<T> modelClass,
                                 T entity) {
        return Wrapper.updateWrapper.update(modelClass, entity, l -> l);
    }

    public static <T> int update(Class<T> modelClass,
                                 Function<LambdaUpdateWrapper<T>, LambdaUpdateWrapper<T>> updateFunction) {
        return Wrapper.updateWrapper.update(modelClass, updateFunction);
    }

    public static <T> int update(Class<T> modelClass, T entity,
                                 Function<LambdaUpdateWrapper<T>, LambdaUpdateWrapper<T>> updateFunction) {
        return Wrapper.updateWrapper.update(modelClass, entity, updateFunction);
    }

    /**
     * 删除
     */
    public static <T, R, C extends AbstractWrapper<T, R, C>>
    int delete(Class<T> modelClass,
               Function<LambdaQueryWrapper<T>, AbstractWrapper<T, R, C>> deleteFunction) {
        return Wrapper.deleteWrapper.delete(modelClass, deleteFunction);
    }

    public static <T> int delete(Class<T> modelClass, LambdaQueryWrapper<T> deleteFunction) {
        return Wrapper.deleteWrapper.delete(modelClass, l -> deleteFunction);
    }

    /**
     * 新增
     */
    public static <T> int insert(Collection<T> modelList) {
        if (modelList == null || modelList.isEmpty())
            return 0;
        return insert(modelList.iterator().next().getClass(), modelList);
    }

    public static int insert(Object... models) {
        if (models == null || models.length == 0)
            return 0;
        return insert(models[0].getClass(), Arrays.asList(models));
    }

    public static <T> int insert(Class<?> modelClass, Collection<T> modelList) {
        return splitBatch(modelClass, modelList, (m, l) -> Wrapper.insertWrapper.insert(m, l));
    }

    /**
     * 修改如果不存在则新增
     */
    public static int insertOrUpdate(Object... models) {
        if (models == null)
            return 0;
        return insertOrUpdate(Arrays.asList(models));
    }

    @SuppressWarnings("unchecked")
    public static <T> int insertOrUpdate(Collection<T> modelList) {
        Class<?> modelClass = modelList.iterator().next().getClass();
        return splitBatch(modelClass, modelList, (m, l) -> Wrapper.mergeWrapper.merge((Class<T>) m, (Collection<T>) l));
    }

    private static <T> int splitBatch(Class<?> modelClass, Collection<T> modelList,
                                      BiFunction<Class<?>, Collection<?>, Integer> function) {
        if (modelList == null || modelList.isEmpty())
            return 0;
        if (modelList.size() <= DEFAULT_BATCH_SIZE) {
            return function.apply(modelClass, modelList);
        }
        int count = 0;
        for (List<?> list : modelList instanceof List ?
                Lists.partition((List<?>) modelList, DEFAULT_BATCH_SIZE) :
                Iterables.paddedPartition(modelList, DEFAULT_BATCH_SIZE)) {
            count += function.apply(modelClass, list);
        }
        return count;
    }

    static class Wrapper {
        static InsertWrapper insertWrapper = DataBaseType.ORACLE == getDataBaseType() ?
                new OracleInsertWrapper() {
                } : new InsertWrapper() {
        };
        static SelectCountSqlWrapper selectCountSqlWrapper = new SelectCountSqlWrapper() {
        };
        static SelectListSqlWrapper selectListSqlWrapper = new SelectListSqlWrapper() {
        };
        static SelectOneSqlWrapper selectOneSqlWrapper = new SelectOneSqlWrapper() {
        };
        static UpdateWrapper updateWrapper = new UpdateWrapper() {
        };
        static DeleteWrapper deleteWrapper = new DeleteWrapper() {
        };
        static SelectListMapSqlWrapper selectListMapSqlWrapper = new SelectListMapSqlWrapper() {
        };
        static SelectOneMapSqlWrapper selectOneMapSqlWrapper = new SelectOneMapSqlWrapper() {
        };
        static MergeWrapper mergeWrapper = DataBaseType.ORACLE == getDataBaseType() ?
                new OracleMergeWrapper() {
                } : DataBaseType.MYSQL == getDataBaseType() ?
                new MySqlMergeWrapper() {
                } : new MergeWrapper() {
        };

    }

    /**
     * 获取DataBaseType
     */
    static class LazyDataBaseType {
        static String databaseProductName;
        static DataBaseType dataBaseType;

        static {
            try (Connection connection = SpringBeanUtils.getBean(SqlSessionTemplate.class).getSqlSessionFactory()
                    .getConfiguration().getEnvironment().getDataSource().getConnection()) {
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                databaseProductName = databaseMetaData.getDatabaseProductName();
                dataBaseType = DataBaseType.value(databaseProductName);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
