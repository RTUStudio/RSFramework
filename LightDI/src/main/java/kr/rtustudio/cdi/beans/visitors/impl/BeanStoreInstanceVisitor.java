package kr.rtustudio.cdi.beans.visitors.impl;

import kr.rtustudio.cdi.annotations.Component;
import kr.rtustudio.cdi.beans.BeanStore;
import kr.rtustudio.cdi.beans.classholders.impl.*;
import kr.rtustudio.cdi.beans.framework.PrototypeFactory;
import kr.rtustudio.cdi.beans.visitors.ClassHolderVisitor;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.reflections.Reflections;

/**
 * @author Mihai Alexandru
 * @date 01.09.2018
 */
public class BeanStoreInstanceVisitor extends ClassHolderVisitor {

    private Object instance;

    private Reflections reflections;

    public BeanStoreInstanceVisitor(BeanStore beanStore, Reflections reflections) {
        super(beanStore);
        this.reflections = reflections;
    }

    @Override
    public void visit(ListClassHolder listClassHolder) {
        var subtypes =
                reflections.getSubTypesOf(listClassHolder.getGenericTypeClass()).stream()
                        .filter(c -> c.isAnnotationPresent(Component.class))
                        .collect(Collectors.toSet());
        boolean listDependenciesInitialized =
                subtypes.stream().map(beanStore::getBean).allMatch(Optional::isPresent);
        if (listDependenciesInitialized) {
            instance = new ArrayList<>(beanStore.getBeans(listClassHolder.getBeanClass()));
        }
    }

    @Override
    public void visit(FieldInjectNamedBeanClassHolder fieldInjectNamedBeanClassHolder) {
        instance =
                beanStore
                        .getBean(
                                fieldInjectNamedBeanClassHolder.getBeanName(),
                                fieldInjectNamedBeanClassHolder.getBeanClass())
                        .orElse(null);
    }

    @Override
    public void visit(ConstructorInjectClassHolder constructorInjectClassHolder) {
        instance = beanStore.getBean(constructorInjectClassHolder.getBeanClass()).orElse(null);
    }

    @Override
    public void visit(PrototypeFactoryClassHolder prototypeFactoryClassHolder) {
        Optional<?> factoryBean = beanStore.getBean(prototypeFactoryClassHolder.getBeanClass());
        if (factoryBean.isPresent()) {
            instance =
                    new PrototypeFactory<>(
                            () ->
                                    beanStore
                                            .getBean(prototypeFactoryClassHolder.getBeanClass())
                                            .orElse(null));
        }
    }

    @Override
    public void visit(NamedBeanClassHolder namedBeanClassVisitor) {
        String beanName = namedBeanClassVisitor.getBeanName();
        instance = beanStore.getBean(beanName, namedBeanClassVisitor.getBeanClass()).orElse(null);
    }

    @Override
    public void visit(DefaultClassHolder defaultClassHolder) {
        instance = beanStore.getBean(defaultClassHolder.getBeanClass()).orElse(null);
    }

    public Optional<Object> getInstance() {
        return Optional.ofNullable(instance);
    }
}
