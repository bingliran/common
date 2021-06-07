package com.blr19c.common.mybatisWrapper.wrapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.mybatisWrapper.SqlMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;


/**
 * 插入数据
 *
 * @author blr
 */
public interface InsertWrapper extends SqlWrapper {

    /**
     * 新增
     *
     * @param modelClass 实体类class
     * @param modelList  新增的数据
     */
    default <T> int insert(Class<T> modelClass, Collection<Object> modelList) {
        initTypeHandler();
        String id = initMappedStatement(modelClass, InsertMethod.instance, Integer.class).getId();
        return getSqlSessionTemplate().insert(id, toSqlForeach(modelList));
    }

    default void initTypeHandler() {
        if (InsertMethod.typeHandlerRegistry == null) {
            InsertMethod.typeHandlerRegistry = getSqlSessionTemplate().getConfiguration().getTypeHandlerRegistry();
            InsertMethod.init();
        }
    }

    default PictogramMap toSqlForeach(Collection<Object> collection) {
        return PictogramMap.getInstance("collection", collection);
    }

    class InsertMethod extends AbstractWrapperMethod {
        static TypeHandlerRegistry typeHandlerRegistry;
        static Map<Type, Map<JdbcType, TypeHandler<?>>> typeHandlerMap;
        static InsertMethod instance = new InsertMethod();

        @SuppressWarnings("unchecked")
        static void init() {
            try {
                Field field = typeHandlerRegistry.getClass().getDeclaredField("typeHandlerMap");
                field.setAccessible(true);
                typeHandlerMap = (Map<Type, Map<JdbcType, TypeHandler<?>>>) field.get(typeHandlerRegistry);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        protected SqlCommandType getSqlCommandType(TableInfo tableInfo) {
            return SqlMethod.INSERT.getType();
        }

        @Override
        protected KeyGenerator getKeyGenerator(TableInfo tableInfo) {
            KeyGenerator keyGenerator = super.getKeyGenerator(tableInfo);
            return StringUtils.isNotBlank(tableInfo.getKeyProperty()) ?
                    (tableInfo.getIdType() == IdType.AUTO ?
                            new Jdbc3KeyGenerator() :
                            (tableInfo.getKeySequence() != null ?
                                    TableInfoHelper.genKeyGenerator("insertBatch", tableInfo, builderAssistant) :
                                    keyGenerator)
                    ) : keyGenerator;
        }

        protected SqlMethod sqlMethod() {
            return SqlMethod.INSERT;
        }

        @Override
        protected String getSql(TableInfo tableInfo) {
            StringJoiner field = new StringJoiner(",");
            StringJoiner value = new StringJoiner(",");
            if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
                field.add(tableInfo.getKeyColumn());
                value.add("#{item." + tableInfo.getKeyProperty() + "}");
            }
            for (TableFieldInfo tableFieldInfo : tableInfo.getFieldList()) {
                field.add(tableFieldInfo.getColumn());
                value.add("#{item." + tableFieldInfo.getProperty() + ",jdbcType=" + jdbcType(tableFieldInfo) + "}");
            }
            return String.format(
                    sqlMethod().getSql(),
                    tableInfo.getTableName(),
                    field.toString(),
                    value.toString()
            );
        }

        protected JdbcType jdbcType(TableFieldInfo tableFieldInfo) {
            Map<JdbcType, TypeHandler<?>> jdbcTypeTypeHandlerMap = typeHandlerMap.get(tableFieldInfo.getPropertyType());
            if (jdbcTypeTypeHandlerMap == null)
                return JdbcType.VARCHAR;
            Set<JdbcType> jdbcTypes = jdbcTypeTypeHandlerMap.keySet();
            if (jdbcTypes.isEmpty())
                return JdbcType.VARCHAR;
            for (JdbcType type : jdbcTypes) {
                if (type != null && type != JdbcType.NULL && type != JdbcType.OTHER && type != JdbcType.UNDEFINED)
                    return type;
            }
            return JdbcType.VARCHAR;
        }
    }
}
