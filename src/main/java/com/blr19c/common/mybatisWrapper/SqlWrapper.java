package com.blr19c.common.mybatisWrapper;

import com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.blr19c.common.spring.SpringBeanUtil;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.Objects;

/**
 * 依靠mybatis和mybatis-plus实现无需注入(包括但不限于BaseMapper)无需sql的基本方法
 *
 * @author blr
 */
public interface SqlWrapper {

    /**
     * 初始化当前modelClass的模板
     */
    default MappedStatement initMappedStatement(Class<?> modelClass,
                                                AbstractSelectMethod selectMethod,
                                                Class<?> returnType) {
        String id = getStatementId(selectMethod, modelClass);
        Configuration configuration = SpbLazy.sqlSessionTemplate.getConfiguration();
        if (configuration.hasStatement(id))
            return configuration.getMappedStatement(id);
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, getResource(modelClass));
        assistant.setCurrentNamespace(getClassName(modelClass));
        TableInfo tableInfo = TableInfoHelper.getTableInfo(modelClass);
        if (tableInfo == null)
            tableInfo = TableInfoHelper.initTableInfo(assistant, modelClass);
        return selectMethod.initMappedStatement(assistant, id, modelClass, tableInfo, returnType);
    }

    default String getClassName(Class<?> modelClass) {
        return modelClass.getName().replace('.', '/');
    }

    default String getResource(Class<?> modelClass) {
        return getClassName(modelClass) + ".java (best guess)";
    }

    default String getStatementId(AbstractSelectMethod selectMethod, Class<?> modelClass) {
        return getResource(modelClass) + selectMethod.getClass().getSimpleName();
    }

    default SqlSessionTemplate getSqlSessionTemplate() {
        return SpbLazy.sqlSessionTemplate;
    }

    abstract class AbstractSelectMethod extends AbstractMethod {

        abstract String getSql(TableInfo tableInfo);

        public SqlCommandType getSqlCommandType() {
            return SqlCommandType.SELECT;
        }

        public void init(MapperBuilderAssistant assistant) {
            if (this.languageDriver == null)
                this.languageDriver = new MybatisXMLLanguageDriver();
            if (assistant != null) {
                this.builderAssistant = assistant;
                this.configuration = assistant.getConfiguration();
            }
        }

        public MappedStatement initMappedStatement(MapperBuilderAssistant assistant,
                                                   String id,
                                                   Class<?> modelClass,
                                                   TableInfo tableInfo,
                                                   Class<?> returnType) {
            SqlCommandType sqlCommandType = Objects.requireNonNull(getSqlCommandType());
            init(assistant);
            SqlSource sqlSource = languageDriver.createSqlSource(assistant.getConfiguration(), getSql(tableInfo), modelClass);
            switch (sqlCommandType) {
                case INSERT:
                    return this.addInsertMappedStatement(modelClass, modelClass, id, sqlSource,
                            NoKeyGenerator.INSTANCE, tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
                case SELECT:
                    return this.addSelectMappedStatementForTable(modelClass, id, sqlSource, tableInfo);
                case DELETE:
                    return this.addDeleteMappedStatement(modelClass, id, sqlSource);
                case UPDATE:
                    return this.addUpdateMappedStatement(modelClass, modelClass, id, sqlSource);
            }
            throw new IllegalArgumentException("sqlCommandType:" + sqlCommandType + "Not recognized");
        }

        @Override
        public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
            throw new IllegalArgumentException("Does not provide this service");
        }
    }


    class SpbLazy {
        static SqlSessionTemplate sqlSessionTemplate = SpringBeanUtil.getBean(SqlSessionTemplate.class);
    }
}