package kr.rtuserver.cdi.beans.framework;

import java.util.function.Supplier;

/**
 * @author Mihai Alexandru
 * @date 08.09.2018
 */
public class PrototypeFactory<T> {

    private final Supplier<T> instanceSupplier;

    public PrototypeFactory(Supplier<T> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    public T get() {
        return instanceSupplier.get();
    }

}
