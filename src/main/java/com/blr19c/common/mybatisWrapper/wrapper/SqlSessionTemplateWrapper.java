package com.blr19c.common.mybatisWrapper.wrapper;

import com.blr19c.common.code.ReflectionUtils;
import com.blr19c.common.collection.PictogramMap;
import com.blr19c.common.collection.PictogramStream;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * 生成兼容PictogramMap等其他组件的SqlSessionTemplate
 *
 * @author blr
 */
public class SqlSessionTemplateWrapper extends SqlSessionTemplate {

    public SqlSessionTemplateWrapper(SqlSessionFactory sqlSessionFactory) {
        this(sqlSessionFactory, sqlSessionFactory.getConfiguration().getDefaultExecutorType());
    }

    public SqlSessionTemplateWrapper(SqlSessionFactory sqlSessionFactory, ExecutorType executorType) {
        this(sqlSessionFactory, executorType,
                new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true));

    }

    public SqlSessionTemplateWrapper(SqlSessionFactory sqlSessionFactory, ExecutorType executorType, PersistenceExceptionTranslator exceptionTranslator) {
        super(sqlSessionFactoryProxy(sqlSessionFactory), executorType, exceptionTranslator);
    }

    private static SqlSessionFactory sqlSessionFactoryProxy(SqlSessionFactory sqlSessionFactory) {
        return (SqlSessionFactory) newProxyInstance(SqlSessionFactory.class.getClassLoader(),
                new Class[]{SqlSessionFactory.class}, new SqlSessionFactoryInterceptor(sqlSessionFactory));
    }

    private static SqlSession sqlSessionProxy(SqlSession sqlSession) {
        return (SqlSession) newProxyInstance(SqlSessionFactory.class.getClassLoader(),
                new Class[]{SqlSession.class}, new SqlSessionInterceptor(sqlSession));
    }

    private static class SqlSessionFactoryInterceptor implements InvocationHandler {
        private final SqlSessionFactory sqlSessionFactory;

        SqlSessionFactoryInterceptor(SqlSessionFactory sqlSessionFactory) {
            this.sqlSessionFactory = sqlSessionFactory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                Object obj = method.invoke(sqlSessionFactory, args);
                if (obj instanceof SqlSession)
                    return sqlSessionProxy((SqlSession) obj);
                return obj;
            } catch (Exception e) {
                throw ReflectionUtils.skipReflectionException(e);
            }
        }
    }

    private static class SqlSessionInterceptor implements InvocationHandler {
        private final SqlSession sqlSession;

        SqlSessionInterceptor(SqlSession sqlSession) {
            this.sqlSession = sqlSession;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        Object arg = args[i];
                        if (arg instanceof PictogramMap) {
                            args[i] = ((PictogramMap) arg).getMap();
                            continue;
                        }
                        if (arg instanceof PictogramStream) {
                            args[i] = ((PictogramStream<?>) arg).toList();
                        }
                    }
                }
                Object res = method.invoke(sqlSession, args);
                if (res instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) res;
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) instanceof EscapeMarkLinkedHashMap)
                            list.set(i, ((EscapeMarkLinkedHashMap<?, ?>) list.get(i)).toPictogramMap());
                    }
                    return list;
                }
                if (res instanceof EscapeMarkLinkedHashMap) {
                    return ((EscapeMarkLinkedHashMap<?, ?>) res).toPictogramMap();
                }
                return res;
            } catch (Exception e) {
                throw ReflectionUtils.skipReflectionException(e);
            }
        }
    }
}
