package kr.rtustudio.cdi.beans.classholders.impl;

import kr.rtustudio.cdi.beans.classholders.ClassHolder;
import kr.rtustudio.cdi.beans.visitors.ClassHolderVisitor;

/**
 * @author Mihai Alexandru
 * @date 08.09.2018
 */
public class PrototypeFactoryClassHolder implements ClassHolder {

    private Class<?> beanClass;

    public PrototypeFactoryClassHolder(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public void accept(ClassHolderVisitor classHolderVisitor) {
        classHolderVisitor.visit(this);
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }
}
