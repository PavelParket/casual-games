package com.security_starter.annotation;

import com.security_starter.enums.Permissions;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

    Permissions value();

    /**
     * Allow value creating (null → non-null) through updateObject
     */
    boolean createAllowed() default true;

    /**
     * Allow value editing (non-null → other non-null) through updateObject
     */
    boolean editAllowed() default true;

    /**
     * Allow value dropping (non-null → null) through updateObject
     */
    boolean deleteAllowed() default true;
}
