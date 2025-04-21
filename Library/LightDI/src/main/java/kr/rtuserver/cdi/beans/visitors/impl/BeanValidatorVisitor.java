package kr.rtuserver.cdi.beans.visitors.impl;

import kr.rtuserver.cdi.beans.BeanStore;
import kr.rtuserver.cdi.beans.classholders.ClassHolder;
import kr.rtuserver.cdi.beans.classholders.impl.*;
import kr.rtuserver.cdi.beans.visitors.ClassHolderVisitor;
import kr.rtuserver.cdi.exceptions.BeanInstantiationException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mihai Alexandru
 * @date 04.09.2018
 */
public class BeanValidatorVisitor extends ClassHolderVisitor {

    private Set<Class<?>> classesInChain;

    public BeanValidatorVisitor(BeanStore beanStore, Set<Class<?>> classesInChain) {
        super(beanStore);
        this.classesInChain = new HashSet<>(classesInChain);
    }

    @Override
    public void visit(ListClassHolder listClassHolder) {
        // do nothing
    }

    @Override
    public void visit(FieldInjectNamedBeanClassHolder fieldInjectNamedBeanClassHolder) {
        // do nothing
    }

    @Override
    public void visit(ConstructorInjectClassHolder constructorInjectClassHolder) {
        // do nothing
    }

    @Override
    public void visit(PrototypeFactoryClassHolder prototypeFactoryClassHolder) {
        //do nothing
    }

    @Override
    public void visit(NamedBeanClassHolder namedBeanClassHolder) {
        validateClass(namedBeanClassHolder);
    }

    @Override
    public void visit(DefaultClassHolder defaultClassHolder) {
        validateClass(defaultClassHolder);
    }

    public Set<Class<?>> getUpdatedClassesInChain() {
        return classesInChain;
    }

    private void validateClass(ClassHolder classHolder) {
        if (!classesInChain.add(classHolder.getBeanClass())) {
            throw new BeanInstantiationException("Circular integration found for class: " + classHolder.getBeanClass());
        }
    }

}
