package kr.rtuserver.cdi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Mihai Alexandru
 * @date 16.08.2018
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Component {

    String name() default "";

    Scope scope() default Scope.SINGLETON;
}
