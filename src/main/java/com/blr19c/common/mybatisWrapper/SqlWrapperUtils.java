package com.blr19c.common.mybatisWrapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.mybatisWrapper.wrapper.*;
import com.blr19c.common.spring.SpringBeanUtils;
import com.github.pagehelper.PageInfo;
import org.mybatis.spring.SqlSessionTemplate;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

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
     * 计数查询
     */
    public static <T> int count(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.INSTANCE.selectCount(modelClass, l -> sqlWhere);
    }

    public static <T> int count(Class<T> modelClass,
                                Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        return Wrapper.INSTANCE.selectCount(modelClass, sqlWhereFunction);
    }

    /**
     * 单条查询
     */
    public static <T> T one(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.INSTANCE.selectOne(modelClass, l -> sqlWhere);
    }

    public static <T> T one(Class<T> modelClass,
                            Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        return Wrapper.INSTANCE.selectOne(modelClass, sqlWhereFunction);
    }

    /**
     * 单条查询PictogramMap
     */
    public static <T> PictogramMap oneMap(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.INSTANCE.selectOneMap(modelClass, l -> sqlWhere);
    }

    public static <T> PictogramMap oneMap(Class<T> modelClass,
                                          Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        return Wrapper.INSTANCE.selectOneMap(modelClass, sqlWhereFunction);
    }

    /**
     * 列表查询
     */
    public static <T> List<T> list(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.INSTANCE.selectList(modelClass, l -> sqlWhere);
    }

    public static <T> List<T> list(Class<T> modelClass,
                                   Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        return Wrapper.INSTANCE.selectList(modelClass, sqlWhereFunction);
    }

    /**
     * 列表分页查询
     */
    public static <T> PageInfo<T> list(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere,
                                       int pageNum, int pageSize) {
        return Wrapper.INSTANCE.selectListToPage(modelClass, l -> sqlWhere, pageNum, pageSize);
    }

    public static <T> PageInfo<T> list(Class<T> modelClass,
                                       Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction,
                                       int pageNum, int pageSize) {
        return Wrapper.INSTANCE.selectListToPage(modelClass, sqlWhereFunction, pageNum, pageSize);
    }

    /**
     * 列表PictogramMap查询
     */
    public static <T> List<PictogramMap> listMap(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere) {
        return Wrapper.INSTANCE.selectListMap(modelClass, l -> sqlWhere);
    }

    public static <T> List<PictogramMap> listMap(Class<T> modelClass,
                                                 Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction) {
        return Wrapper.INSTANCE.selectListMap(modelClass, sqlWhereFunction);
    }

    /**
     * 列表PictogramMap分页查询
     */
    public static <T> PageInfo<PictogramMap> listMap(Class<T> modelClass, LambdaQueryWrapper<T> sqlWhere,
                                                     int pageNum, int pageSize) {
        return Wrapper.INSTANCE.selectListMapToPage(modelClass, l -> sqlWhere, pageNum, pageSize);
    }

    public static <T> PageInfo<PictogramMap> listMap(Class<T> modelClass,
                                                     Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> sqlWhereFunction,
                                                     int pageNum, int pageSize) {
        return Wrapper.INSTANCE.selectListMapToPage(modelClass, sqlWhereFunction, pageNum, pageSize);
    }

    /**
     * 修改
     */
    public static <T> int update(Class<T> modelClass,
                                 LambdaUpdateWrapper<T> updateFunction) {
        return Wrapper.INSTANCE.update(modelClass, l -> updateFunction);
    }

    public static <T> int update(Class<T> modelClass,
                                 Function<LambdaUpdateWrapper<T>, LambdaUpdateWrapper<T>> updateFunction) {
        return Wrapper.INSTANCE.update(modelClass, updateFunction);
    }

    /**
     * 删除
     */
    public static <T> int delete(Class<T> modelClass,
                                 Function<LambdaQueryWrapper<T>, LambdaQueryWrapper<T>> deleteFunction) {
        return Wrapper.INSTANCE.delete(modelClass, deleteFunction);
    }

    public static <T> int delete(Class<T> modelClass, LambdaQueryWrapper<T> deleteFunction) {
        return Wrapper.INSTANCE.delete(modelClass, l -> deleteFunction);
    }

    /**
     * 新增
     */
    public static int insert(Collection<Object> modelList) {
        if (modelList == null || modelList.isEmpty())
            return 0;
        return insert(modelList.iterator().next().getClass(), modelList);
    }

    public static int insert(Class<?> modelClass, Collection<Object> modelList) {
        if (modelList == null || modelList.isEmpty())
            return 0;
        return Wrapper.INSTANCE.insert(modelClass, modelList);
    }

    public static int insert(Object... models) {
        if (models == null || models.length == 0)
            return 0;
        return insert(models[0].getClass(), Arrays.asList(models));
    }

    static class Wrapper implements
            SelectCountSqlWrapper, SelectListSqlWrapper, SelectOneSqlWrapper, UpdateWrapper, DeleteWrapper,
            SelectListMapSqlWrapper, SelectOneMapSqlWrapper {
        static final Wrapper INSTANCE = new Wrapper();
        private static final InsertWrapper INSERT_WRAPPER;

        static {
            DataSource dataSource = SpringBeanUtils.getBean(SqlSessionTemplate.class).getSqlSessionFactory()
                    .getConfiguration().getEnvironment().getDataSource();
            InsertWrapper insertWrapper;
            try {
                DatabaseMetaData databaseMetaData = dataSource.getConnection().getMetaData();
                String dataBaseType = databaseMetaData.getDatabaseProductName();
                switch (dataBaseType.toLowerCase()) {
                    case "oracle":
                        insertWrapper = new OracleInsertWrapper() {
                        };
                        break;
                    case "mysql":
                    default:
                        insertWrapper = new InsertWrapper() {
                        };
                }
            } catch (Exception e) {
                e.printStackTrace();
                insertWrapper = new InsertWrapper() {
                };
            }
            INSERT_WRAPPER = insertWrapper;
        }

        int insert(Class<?> modelClass, Collection<Object> modelList) {
            return INSERT_WRAPPER.insert(modelClass, modelList);
        }
    }
}
