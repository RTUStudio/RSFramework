package kr.rtustudio.cdi.beans.visitors.impl;

import kr.rtustudio.cdi.annotations.Component;
import kr.rtustudio.cdi.beans.BeanStore;
import kr.rtustudio.cdi.beans.classholders.ClassHolder;
import kr.rtustudio.cdi.beans.classholders.impl.*;
import kr.rtustudio.cdi.beans.visitors.ClassHolderVisitor;
import kr.rtustudio.cdi.factories.impl.ClassHolderFactory;
import kr.rtustudio.cdi.utils.ClassHierarchyLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import org.reflections.Reflections;

/**
 * @author Mihai Alexandru
 * @date 03.09.2018
 */
public class BeanDependenciesVisitor extends ClassHolderVisitor {

    private List<ClassHolder> dependencies;

    private ClassHolderFactory classHolderFactory;

    private Reflections reflections;

    public BeanDependenciesVisitor(
            BeanStore beanStore, ClassHolderFactory classHolderFactory, Reflections reflections) {
        super(beanStore);
        this.classHolderFactory = classHolderFactory;
        this.reflections = reflections;
    }

    @Override
    public void visit(ListClassHolder listClassHolder) {
        var subtypes = reflections.getSubTypesOf(listClassHolder.getGenericTypeClass());
        var subtypesClassHolders = getSubTypesHolders(subtypes);
        dependencies = Collections.unmodifiableList(subtypesClassHolders);
    }

    @Override
    public void visit(PrototypeFactoryClassHolder prototypeFactoryClassHolder) {
        classHolderFactory
                .getClassHolder(prototypeFactoryClassHolder.getBeanClass())
                .ifPresent(
                        ch -> {
                            dependencies = Collections.singletonList(ch);
                        });
    }

    @Override
    public void visit(FieldInjectNamedBeanClassHolder fieldInjectNamedBeanClassHolder) {
        var subtypes = getSubtypesIncludingActualType(fieldInjectNamedBeanClassHolder);
        dependencies =
                getSubTypesHolders(subtypes).stream()
                        .filter(
                                st ->
                                        isSubtTypeNamedBean(
                                                st, fieldInjectNamedBeanClassHolder.getBeanName()))
                        .findFirst()
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
    }

    @Override
    public void visit(ConstructorInjectClassHolder constructorInjectClassHolder) {
        var subtypes = getSubtypesIncludingActualType(constructorInjectClassHolder);
        dependencies = getSubTypesHolders(subtypes);
    }

    @Override
    public void visit(NamedBeanClassHolder namedBeanClassHolder) {
        initClassDependencies(namedBeanClassHolder);
    }

    @Override
    public void visit(DefaultClassHolder defaultClassHolder) {
        initClassDependencies(defaultClassHolder);
    }

    public List<ClassHolder> getDependencies() {
        return dependencies;
    }

    private List<ClassHolder> getSubTypesHolders(Set<? extends Class<?>> subtypes) {
        return subtypes.stream()
                .map(classHolderFactory::getClassHolder)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void initClassDependencies(ClassHolder ch) {
        dependencies = new ArrayList<>();
        var parents = ClassHierarchyLoader.getParents(ch.getBeanClass());
        for (Class<?> c : parents) {
            Constructor[] constructors = c.getConstructors();
            for (Constructor constructor : constructors) {
                dependencies.addAll(classHolderFactory.getClassHolders(constructor));
            }
            for (Field f : c.getDeclaredFields()) {
                classHolderFactory.getClassHolder(f).ifPresent(dependencies::add);
            }
        }
    }

    private boolean isSubtTypeNamedBean(ClassHolder classHolder, String beanName) {
        String subTypeBeanName = classHolder.getBeanClass().getAnnotation(Component.class).name();
        return Objects.equals(beanName, subTypeBeanName);
    }

    private Set<Class<?>> getSubtypesIncludingActualType(ClassHolder ch) {
        Set<Class<?>> types = new HashSet<>();
        types.add(ch.getBeanClass());
        types.addAll(reflections.getSubTypesOf(ch.getBeanClass()));
        return types;
    }
}
