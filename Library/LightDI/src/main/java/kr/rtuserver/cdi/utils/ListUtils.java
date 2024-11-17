package kr.rtuserver.cdi.utils;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

/**
 * @author Mihai Alexandru
 * @date 27.08.2018
 */
public class ListUtils {

    public static final Optional<Class<?>> getListGenericType(Class<?> listClass) {
        if (!List.class.isAssignableFrom(listClass)) {
            return Optional.empty();
        }
        ParameterizedType parameterizedType = (ParameterizedType) listClass.getGenericSuperclass();
        return Optional.of(parameterizedType.getActualTypeArguments()[0].getClass());
    }

}
