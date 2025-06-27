package kr.rtuserver.protoweaver.api.util;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Objects;

public class ReflectionUtil {

    private final Object instance;
    private final Class<?> clazz;

    public ReflectionUtil(Object instance) {
        this.instance = instance;
        clazz = instance.getClass();
    }

    public ReflectionUtil(Class<?> clazz) {
        instance = null;
        this.clazz = clazz;
    }

    /**
     * Create an instance of {@link ReflectionUtil}. Can be used for static or non-static actions
     */
    public static ReflectionUtil of(Object instance) {
        return new ReflectionUtil(instance);
    }

    /**
     * Create an instance of {@link ReflectionUtil} that can only be used for static actions
     */
    public static ReflectionUtil of(Class<?> clazz) {
        return new ReflectionUtil(clazz);
    }

    /**
     * Create an instance of {@link ReflectionUtil} from a field in another {@link ReflectionUtil} instance
     */
    public ReflectionUtil of(String name) {
        try {
            return ReflectionUtil.of(findField(name, clazz).get(instance));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    // Search super classes for field
    private Field findField(String name, Class<?> clazz) throws NoSuchFieldException {
        if (clazz == null) throw new NoSuchFieldException();

        Field field;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            field = findField(name, clazz.getSuperclass());
        }
        field.setAccessible(true);
        return field;
    }

    // Search super classes for methods
    private Method findMethod(String name, Class<?> clazz, Class<?>[] argTypes) throws NoSuchMethodException {
        if (clazz == null) throw new NoSuchMethodException();

        Method method;
        try {
            method = clazz.getDeclaredMethod(name, argTypes);
        } catch (NoSuchMethodException e) {
            method = findMethod(name, clazz.getSuperclass(), argTypes);
        }
        method.setAccessible(true);
        return method;
    }

    /**
     * Get the value of a field. Can be private or static
     */
    public <T> T get(String name, Class<T> type) {
        try {
            return type.cast(findField(name, clazz).get(instance));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the value of a field. Can be private, final, or static
     */
    public ReflectionUtil set(String name, Object value) {
        try {
            findField(name, clazz).set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(name + "/" + value + "/" + e);
        }
        return this;
    }

    /**
     * Invoke a function with a return type
     */
    public <T> T call(String name, Class<T> returnType, Object... args) {
        try {
            Class<?>[] classes = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);
            Object returnVal = findMethod(name, clazz, classes).invoke(instance, args);
            if (returnVal == null || returnType == null) return null;
            return returnType.cast(returnVal);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke a function with no return type
     */
    public ReflectionUtil call(String name, Object... args) {
        call(name, null, args);
        return this;
    }

    /**
     * Get a list of the generic type params of a class
     */
    public Class<?>[] generics() {
        if (clazz.isEnum()) return new Class[]{};   // Enums cant have generics

        Type generic = clazz.getGenericSuperclass();
        if (generic instanceof ParameterizedType) {
            return Arrays.stream(((ParameterizedType) generic).getActualTypeArguments()).map(t -> {
                        try {
                            return Class.forName(t.getTypeName());
                        } catch (ClassNotFoundException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray(Class[]::new);
        }
        return new Class[]{};
    }

}