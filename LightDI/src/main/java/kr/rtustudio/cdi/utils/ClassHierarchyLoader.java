package kr.rtustudio.cdi.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mihai Alexandru
 * @date 18.08.2018
 */
public final class ClassHierarchyLoader {

    private ClassHierarchyLoader() {}

    public static Set<Class<?>> getParents(Class<?> clazz) {
        Set<Class<?>> parents = new HashSet<>();
        getParents(clazz, parents);
        return parents;
    }

    private static void getParents(Class<?> clazz, Set<Class<?>> parents) {
        if (clazz == null) {
            return;
        }
        if (clazz.equals(Object.class)) {
            return;
        }
        parents.add(clazz);
        getParents(clazz.getSuperclass(), parents);
        for (Class<?> i : clazz.getInterfaces()) {
            getParents(i, parents);
        }
    }
}
