package kr.rtustudio.cdi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mihai Alexandru
 * @date 16.08.2018
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Inject {

    String value() default "";
}
