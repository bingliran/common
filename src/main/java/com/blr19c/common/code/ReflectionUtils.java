package com.blr19c.common.code;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 反射操作工具
 *
 * @author blr
 */
public class ReflectionUtils {

    /**
     * 匹配所有非桥接非合成方法
     */
    public static final ReflectionFilter<Method> USER_DECLARED_METHODS =
            (method -> !method.isBridge() && !method.isSynthetic());

    /**
     * 匹配所有非静态非不可变的字段
     */
    public static final ReflectionFilter<Field> COPYABLE_FIELDS =
            (field -> !(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())));


    /**
     * CGLIB重命名的前缀
     *
     * @see #isCglibRenamedMethod
     */
    private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

    private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 跳过反射异常
     */
    public static <T extends Throwable> Throwable skipReflectionException(T throwable) {
        Throwable t = throwable;
        while (t instanceof UndeclaredThrowableException)
            t = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
        while (t instanceof ReflectiveOperationException)
            t = t.getCause();
        if (t instanceof UndeclaredThrowableException)
            return skipReflectionException(t);
        return t;
    }

    /**
     * 处理反射异常
     */
    public static void handleReflectionException(Exception ex) {
        if (ex instanceof NoSuchMethodException) {
            throw new IllegalStateException("Method not found: " + ex.getMessage());
        }
        if (ex instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access method or field: " + ex.getMessage());
        }
        if (ex instanceof InvocationTargetException) {
            handleInvocationTargetException((InvocationTargetException) ex);
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * 处理调用异常
     */
    public static void handleInvocationTargetException(InvocationTargetException ex) {
        rethrowRuntimeException(ex.getTargetException());
    }

    /**
     * 将异常抛出为运行时异常
     */
    public static void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * 根据类型和参数获取构造函数
     */
    public static <T> Constructor<T> accessibleConstructor(Class<T> clazz,
                                                           Class<?>... parameterTypes) throws NoSuchMethodException {
        Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
        makeAccessible(constructor);
        return constructor;
    }

    /**
     * 开放构造函数权限
     */
    public static void makeAccessible(Constructor<?> constructor) {
        if ((!Modifier.isPublic(constructor.getModifiers()) ||
                !Modifier.isPublic(constructor.getDeclaringClass().getModifiers())) && !constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
    }

    /**
     * 查询无参数方法
     */
    @Nullable
    public static Method findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, EMPTY_CLASS_ARRAY);
    }

    /**
     * 根据指定类型名称参数查询方法
     */
    @Nullable
    public static Method findMethod(Class<?> clazz, String name, @Nullable Class<?>... paramTypes) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(name, "Method name must not be null");
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() :
                    getDeclaredMethods(searchType, false));
            for (Method method : methods) {
                if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
        return (paramTypes.length == method.getParameterCount() &&
                Arrays.equals(paramTypes, method.getParameterTypes()));
    }

    /**
     * 执行无参数方法
     */
    @Nullable
    public static Object invokeMethod(Method method, @Nullable Object target) {
        return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
    }

    /**
     * 指定参数执行方法
     */
    @Nullable
    public static Object invokeMethod(Method method, @Nullable Object target, @Nullable Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            handleReflectionException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    /**
     * exceptionType是否已经在method的定义中声明了
     */
    public static boolean declaresException(Method method, Class<?> exceptionType) {
        Assert.notNull(method, "Method must not be null");
        Class<?>[] declaredExceptions = method.getExceptionTypes();
        for (Class<?> declaredException : declaredExceptions) {
            if (declaredException.isAssignableFrom(exceptionType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 遍历当前类所有方法并使用ReflectionCallback<Method>执行
     */
    public static void doWithLocalMethods(Class<?> clazz, ReflectionCallback<Method> callback) {
        Method[] methods = getDeclaredMethods(clazz, false);
        for (Method method : methods) {
            try {
                callback.doWith(method);
            } catch (Exception ex) {
                handleReflectionException(ex);
            }
        }
    }

    /**
     * 遍历所有方法并使用ReflectionCallback<Method>执行
     */
    public static void doWithMethods(Class<?> clazz, ReflectionCallback<Method> callback) {
        doWithMethods(clazz, callback, null);
    }

    /**
     * 遍历所有方法通过ReflectionFilter<Method>过滤的使用ReflectionCallback<Method>执行
     */
    public static void doWithMethods(Class<?> clazz, ReflectionCallback<Method> mc, @Nullable ReflectionFilter<Method> mf) {
        Method[] methods = getDeclaredMethods(clazz, false);
        for (Method method : methods) {
            if (mf != null && !mf.matches(method)) {
                continue;
            }
            try {
                mc.doWith(method);
            } catch (Exception ex) {
                handleReflectionException(ex);
            }
        }
        if (clazz.getSuperclass() != null && (mf != USER_DECLARED_METHODS || clazz.getSuperclass() != Object.class)) {
            doWithMethods(clazz.getSuperclass(), mc, mf);
        } else if (clazz.isInterface()) {
            for (Class<?> superIfc : clazz.getInterfaces()) {
                doWithMethods(superIfc, mc, mf);
            }
        }
    }

    /**
     * 获取当前类的所有方法(包括当前类所继承和实现的接口)
     */
    public static Method[] getAllDeclaredMethods(Class<?> leafClass) {
        final List<Method> methods = new ArrayList<>(20);
        doWithMethods(leafClass, methods::add);
        return methods.toArray(EMPTY_METHOD_ARRAY);
    }

    /**
     * 获取当前类和所有超类的唯一声明方法集
     * 当前类方法首先被包含在内，在遍历超类层次结构时，任何与已包含的方法匹配的签名的方法都会被过滤掉
     */
    public static Method[] getUniqueDeclaredMethods(Class<?> leafClass) {
        return getUniqueDeclaredMethods(leafClass, null);
    }

    /**
     * 获取当前类和所有超类的唯一声明方法集并设置过滤器
     * 当前类方法首先被包含在内，在遍历超类层次结构时，任何与已包含的方法匹配的签名的方法都会被过滤掉
     */
    public static Method[] getUniqueDeclaredMethods(Class<?> leafClass, @Nullable ReflectionFilter<Method> mf) {
        final List<Method> methods = new ArrayList<>(20);
        doWithMethods(leafClass, method -> {
            boolean knownSignature = false;
            Method methodBeingOverriddenWithCovariantReturnType = null;
            for (Method existingMethod : methods) {
                if (method.getName().equals(existingMethod.getName()) &&
                        method.getParameterCount() == existingMethod.getParameterCount() &&
                        Arrays.equals(method.getParameterTypes(), existingMethod.getParameterTypes())) {
                    if (existingMethod.getReturnType() != method.getReturnType() &&
                            existingMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
                        methodBeingOverriddenWithCovariantReturnType = existingMethod;
                    } else {
                        knownSignature = true;
                    }
                    break;
                }
            }
            if (methodBeingOverriddenWithCovariantReturnType != null) {
                methods.remove(methodBeingOverriddenWithCovariantReturnType);
            }
            if (!knownSignature && !isCglibRenamedMethod(method)) {
                methods.add(method);
            }
        }, mf);
        return methods.toArray(EMPTY_METHOD_ARRAY);
    }

    /**
     * 获取当前类和所有超类的方法集
     */
    public static Method[] getDeclaredMethods(Class<?> clazz) {
        return getDeclaredMethods(clazz, true);
    }

    private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
        Assert.notNull(clazz, "Class must not be null");
        Method[] result;
        try {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
            if (defaultMethods != null) {
                result = new Method[declaredMethods.length + defaultMethods.size()];
                System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                int index = declaredMethods.length;
                for (Method defaultMethod : defaultMethods) {
                    result[index] = defaultMethod;
                    index++;
                }
            } else {
                result = declaredMethods;
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                    "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
        }
        return (result.length == 0 || !defensive) ? result : result.clone();
    }

    /**
     * 获取接口中的非抽象方法
     */
    @Nullable
    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

    /**
     * 是否为Object中的equals方法
     *
     * @see Object#equals(Object);
     */
    public static boolean isEqualsMethod(@Nullable Method method) {
        return method != null && method.getParameterCount() == 1
                && "equals".equals(method.getName()) && method.getParameterTypes()[0] == Object.class;
    }

    /**
     * 是否为Object中的hashCode方法
     *
     * @see Object#hashCode()
     */
    public static boolean isHashCodeMethod(@Nullable Method method) {
        return method != null && method.getParameterCount() == 0 && method.getName().equals("hashCode");
    }

    /**
     * 是否为Object中的toString方法
     *
     * @see Object#toString()
     */
    public static boolean isToStringMethod(@Nullable Method method) {
        return (method != null && method.getParameterCount() == 0 && method.getName().equals("toString"));
    }

    /**
     * 方法最初是否由Object类声明
     */
    public static boolean isObjectMethod(@Nullable Method method) {
        return (method != null && (method.getDeclaringClass() == Object.class ||
                isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method)));
    }

    /**
     * 是否为cglib的重命名方法
     */
    public static boolean isCglibRenamedMethod(Method renamedMethod) {
        String name = renamedMethod.getName();
        if (name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)) {
            int i = name.length() - 1;
            while (i >= 0 && Character.isDigit(name.charAt(i))) {
                i--;
            }
            return (i > CGLIB_RENAMED_METHOD_PREFIX.length() && (i < name.length() - 1) && name.charAt(i) == '$');
        }
        return false;
    }

    /**
     * 开放方法访问权限
     */
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) ||
                !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }


    /**
     * 查找field(向上直到Object类)
     */
    @Nullable
    public static Field findField(Class<?> clazz, String name) {
        return findField(clazz, name, null);
    }

    /**
     * 查找field(向上直到Object类) 可以单独根据name或者type查找
     */
    @Nullable
    public static Field findField(Class<?> clazz, @Nullable String name, @Nullable Class<?> type) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.isTrue(name != null || type != null, "Either name or type of the field must be specified");
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) &&
                        (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * 设置field的值
     */
    public static void setField(Field field, @Nullable Object target, @Nullable Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
        }
    }

    /**
     * 获取field的值
     */
    @Nullable
    public static Object getField(Field field, @Nullable Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    /**
     * 遍历当前类所有字段并使用ReflectionCallback<Field>执行
     */
    public static void doWithLocalFields(Class<?> clazz, ReflectionCallback<Field> fc) {
        doWithLocalFields(clazz, fc, null);
    }

    /**
     * 遍历当前类所有字段通过ReflectionFilter<Field>过滤的使用ReflectionCallback<Field>执行
     */
    public static void doWithLocalFields(Class<?> clazz, ReflectionCallback<Field> fc, @Nullable ReflectionFilter<Field> ff) {
        for (Field field : getDeclaredFields(clazz)) {
            if (ff != null && !ff.matches(field)) {
                continue;
            }
            try {
                fc.doWith(field);
            } catch (Exception ex) {
                handleReflectionException(ex);
            }
        }
    }

    /**
     * 遍历所有字段并使用ReflectionCallback<Field>执行
     */
    public static void doWithFields(Class<?> clazz, ReflectionCallback<Field> fc) {
        doWithFields(clazz, fc, null);
    }

    /**
     * 遍历所有字段通过ReflectionFilter<Field>过滤的使用ReflectionCallback<Field>执行
     */
    public static void doWithFields(Class<?> clazz, ReflectionCallback<Field> fc, @Nullable ReflectionFilter<Field> ff) {
        Class<?> targetClass = clazz;
        do {
            Field[] fields = getDeclaredFields(targetClass);
            for (Field field : fields) {
                if (ff != null && !ff.matches(field)) {
                    continue;
                }
                try {
                    fc.doWith(field);
                } catch (Exception ex) {
                    handleReflectionException(ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);
    }

    /**
     * 获取当前类所有字段
     */
    private static Field[] getDeclaredFields(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        try {
            return clazz.getDeclaredFields();
        } catch (Throwable ex) {
            throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                    "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
        }
    }

    /**
     * 获取java Beans的PropertyDescriptor
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> beanClass, @Nullable Class<?> stopClass) {
        try {
            return Introspector.getBeanInfo(beanClass, stopClass).getPropertyDescriptors();
        } catch (Exception ex) {
            handleReflectionException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    /**
     * 遍历所有PropertyDescriptor通过ReflectionFilter过滤的使用ReflectionCallback执行
     */
    public static void doWithPropertyDescriptors(Class<?> beanClass, @Nullable Class<?> stopClass,
                                                 ReflectionCallback<PropertyDescriptor> pc,
                                                 @Nullable ReflectionFilter<PropertyDescriptor> pf) {
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(beanClass, stopClass);
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (pf != null && !pf.matches(propertyDescriptor)) {
                continue;
            }
            try {
                pc.doWith(propertyDescriptor);
            } catch (Exception ex) {
                handleReflectionException(ex);
            }
        }
    }

    /**
     * 将src的所有字段值复制到dest
     */
    public static void shallowCopyFieldState(final Object src, final Object dest) {
        Assert.notNull(src, "Source for field copy cannot be null");
        Assert.notNull(dest, "Destination for field copy cannot be null");
        if (!src.getClass().isAssignableFrom(dest.getClass())) {
            throw new IllegalArgumentException("Destination class [" + dest.getClass().getName() +
                    "] must be same or subclass as source class [" + src.getClass().getName() + "]");
        }
        doWithFields(src.getClass(), field -> {
            makeAccessible(field);
            Object srcValue = field.get(src);
            field.set(dest, srcValue);
        }, COPYABLE_FIELDS);
    }

    /**
     * 是否为常量
     */
    public static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
    }

    /**
     * 开放字段访问权限
     */
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * 对每个单位行动
     */
    @FunctionalInterface
    public interface ReflectionCallback<T> {

        /**
         * 执行操作
         */
        void doWith(T t) throws Exception;
    }


    /**
     * 过滤可用于回调
     */
    @FunctionalInterface
    public interface ReflectionFilter<F> {

        /**
         * 是否匹配
         */
        boolean matches(F f);

        /**
         * 联合过滤器
         */
        default ReflectionFilter<F> and(ReflectionFilter<F> next) {
            Assert.notNull(next, "Next ReflectionFilter must not be null");
            return method -> matches(method) && next.matches(method);
        }
    }
}
