package com.blr19c.common.code;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * 反射操作工具
 *
 * @author blr
 */
public class ReflectionUtils {

    /**
     * 跳过反射异常
     */
    public static <T extends Throwable> Throwable skipReflectionAnomaly(T throwable) {
        Throwable t = throwable;
        while (t instanceof UndeclaredThrowableException)
            t = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
        while (t instanceof ReflectiveOperationException)
            t = t.getCause();
        if (t instanceof UndeclaredThrowableException)
            return skipReflectionAnomaly(t);
        return t;
    }
}
