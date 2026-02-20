package kr.rtustudio.cdi.objects;

import kr.rtustudio.cdi.annotations.Component;

import java.util.Objects;

/**
 * @author Mihai Alexandru
 * @date 18.08.2018
 */
public class BeanNameKey {

    private final String name;

    public BeanNameKey(Class<?> componentClass) {
        name = getComponentName(componentClass);
    }

    public BeanNameKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // -----------------------------------------------------------------------

    private String getComponentName(Class<?> componentClass) {
        Component component = componentClass.getAnnotation(Component.class);
        String componentName = component.name();
        if (isEmpty(componentName)) {
            componentName = getNameBasedOnClass(componentClass);
        }
        return componentName;
    }

    private String getNameBasedOnClass(Class<?> componentClass) {
        return unCapitalizeWord(componentClass.getSimpleName());
    }

    private boolean isEmpty(String name) {
        if (Objects.isNull(name)) {
            return true;
        }
        return "".equals(name.trim());
    }

    private String unCapitalizeWord(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return String.valueOf(str.charAt(0)).toLowerCase() + str.substring(1);
    }

    // ------------------------ hash code & equals ------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanNameKey beanKey = (BeanNameKey) o;
        return Objects.equals(name, beanKey.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
