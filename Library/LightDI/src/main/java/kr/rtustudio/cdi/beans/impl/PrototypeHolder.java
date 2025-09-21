package kr.rtustudio.cdi.beans.impl;

import kr.rtustudio.cdi.beans.BeanHolder;

import java.util.function.Supplier;

/**
 * @author Mihai Alexandru
 * @date 18.08.2018
 */
public class PrototypeHolder implements BeanHolder {

    private Supplier<?> instanceSupplier;

    public PrototypeHolder(Supplier<?> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    @Override
    public Object get() {
        return instanceSupplier.get();
    }
}
