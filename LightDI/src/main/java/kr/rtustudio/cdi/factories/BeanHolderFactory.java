package kr.rtustudio.cdi.factories;

import kr.rtustudio.cdi.beans.BeanHolder;

import java.util.function.Supplier;

/**
 * @author Mihai Alexandru
 * @date 22.08.2018
 */
public interface BeanHolderFactory {

    BeanHolder getBeanHolder(Class<?> clazz, Supplier<?> instanceSupplier);
}
