package com.blr19c.common.spring;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * springboot bean操作
 *
 * @author blr
 * @see ListableBeanFactory
 */
public class SpringBeanUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtil.applicationContext = applicationContext;
    }


    public static boolean containsBeanDefinition(@NotNull String beanName) {
        return applicationContext.containsBeanDefinition(beanName);
    }


    public static int getBeanDefinitionCount() {
        return applicationContext.getBeanDefinitionCount();
    }


    public static String @NotNull [] getBeanDefinitionNames() {
        return applicationContext.getBeanDefinitionNames();
    }


    public static <T> @NotNull ObjectProvider<T> getBeanProvider(@NotNull Class<T> requiredType, boolean allowEagerInit) {
        return applicationContext.getBeanProvider(requiredType, allowEagerInit);
    }


    public static <T> @NotNull ObjectProvider<T> getBeanProvider(@NotNull ResolvableType requiredType, boolean allowEagerInit) {
        return applicationContext.getBeanProvider(requiredType, allowEagerInit);
    }


    public static String @NotNull [] getBeanNamesForType(@NotNull ResolvableType type) {
        return applicationContext.getBeanNamesForType(type);
    }


    public static String @NotNull [] getBeanNamesForType(@NotNull ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
        return applicationContext.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }


    public static String @NotNull [] getBeanNamesForType(Class<?> type) {
        return applicationContext.getBeanNamesForType(type);
    }


    public static String @NotNull [] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return applicationContext.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }


    public static <T> @NotNull Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        return applicationContext.getBeansOfType(type);
    }


    public static <T> @NotNull Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {
        return applicationContext.getBeansOfType(type, includeNonSingletons, allowEagerInit);
    }


    public static String @NotNull [] getBeanNamesForAnnotation(@NotNull Class<? extends Annotation> annotationType) {
        return applicationContext.getBeanNamesForAnnotation(annotationType);
    }


    public static @NotNull Map<String, Object> getBeansWithAnnotation(@NotNull Class<? extends Annotation> annotationType) throws BeansException {
        return applicationContext.getBeansWithAnnotation(annotationType);
    }


    public static <A extends Annotation> A findAnnotationOnBean(@NotNull String beanName, @NotNull Class<A> annotationType) throws NoSuchBeanDefinitionException {
        return applicationContext.findAnnotationOnBean(beanName, annotationType);
    }


    public static @NotNull Object getBean(@NotNull String name) throws BeansException {
        return applicationContext.getBean(name);
    }


    public static <T> @NotNull T getBean(@NotNull String name, @NotNull Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }


    public static @NotNull Object getBean(@NotNull String name, Object @NotNull ... args) throws BeansException {
        return applicationContext.getBean(name, args);
    }


    public static <T> @NotNull T getBean(@NotNull Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }


    public static <T> @NotNull T getBean(@NotNull Class<T> requiredType, Object @NotNull ... args) throws BeansException {
        return applicationContext.getBean(requiredType, args);
    }


    public static <T> @NotNull ObjectProvider<T> getBeanProvider(@NotNull Class<T> requiredType) {
        return applicationContext.getBeanProvider(requiredType);
    }


    public static <T> @NotNull ObjectProvider<T> getBeanProvider(@NotNull ResolvableType requiredType) {
        return applicationContext.getBeanProvider(requiredType);
    }

    public static boolean containsBean(@NotNull String name) {
        return applicationContext.containsBean(name);
    }


    public static boolean isSingleton(@NotNull String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }


    public static boolean isPrototype(@NotNull String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isPrototype(name);
    }


    public static boolean isTypeMatch(@NotNull String name, @NotNull ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
        return applicationContext.isTypeMatch(name, typeToMatch);
    }


    public static boolean isTypeMatch(@NotNull String name, @NotNull Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
        return applicationContext.isTypeMatch(name, typeToMatch);
    }


    public static Class<?> getType(@NotNull String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }


    public static Class<?> getType(@NotNull String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name, allowFactoryBeanInit);
    }


    public static String @NotNull [] getAliases(@NotNull String name) {
        return applicationContext.getAliases(name);
    }
}