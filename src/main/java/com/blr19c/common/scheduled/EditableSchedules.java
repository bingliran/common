package com.blr19c.common.scheduled;

import java.lang.annotation.*;

/**
 * EditableScheduled合并器 支持多个EditableScheduled
 *
 * @author blr
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EditableSchedules {

    EditableScheduled[] value();

}

