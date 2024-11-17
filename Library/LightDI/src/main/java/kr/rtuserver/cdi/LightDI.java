package kr.rtuserver.cdi;

import kr.rtuserver.cdi.annotations.Component;
import kr.rtuserver.cdi.beans.BeanStore;
import kr.rtuserver.cdi.exceptions.BeanInstantiationException;
import kr.rtuserver.cdi.exceptions.BeanNotFoundException;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

/**
 * @author Mihai Alexandru
 * @date 16.08.2018
 */
public class LightDI {

    private static final Logger logger = LoggerFactory.getLogger(LightDI.class);

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    private static final AtomicReference<BeanStore> beanStore = new AtomicReference<>();

    private static final ReentrantLock setAndResetLock = new ReentrantLock();

    public static void init(String... packages) {
        //ensures only one thread executed the inializeDI() method.
        if (initialized.compareAndSet(false, true)) {
            setAndResetLock.lock();
            try {
                initializeDI(packages);
            } finally {
                setAndResetLock.unlock();
            }

        } else {
            logger.error("DI framework already initialized. Cannot initialize multiple DI frameworks for the same application");
        }
    }

    public static void reset() {
        if (initialized.compareAndSet(true, false)) {
            setAndResetLock.lock();
            try {
                beanStore.set(null);
            } finally {
                setAndResetLock.unlock();
            }

        }
    }

    public static <T> T getBean(String beanName, Class<T> beanClass) {
        return safelyGetBean(() -> beanStore.get().getBean(beanName, beanClass).orElseThrow(() -> new BeanNotFoundException(beanName)));
    }

    public static <T> List<T> getBeans(Class<T> beanClass) {
        return safelyGetBean(() -> beanStore.get().getBeans(beanClass));
    }

    public static <T> T getBean(Class<T> beanClass) {
        return safelyGetBean(() -> beanStore.get().getBean(beanClass).orElseThrow(() -> new BeanNotFoundException(beanClass)));
    }

    // ----------------------------------------------------------------------------------------

    private static void initializeDI(String... packages) {

        Reflections reflections = new Reflections(packages);

        Set<Class<?>> components = reflections.getTypesAnnotatedWith(Component.class);

        beanStore.compareAndSet(null, new BeanStore(reflections));

        beanStore.get().init(components);

    }

    private static <T> T safelyGetBean(Supplier<T> beanSupplier) {
        if (!initialized.get()) {
            throw new BeanInstantiationException("No bean beans initialized. Call the init() method first.");
        }
        if (isNull(beanStore.get())) {
            try {
                setAndResetLock.tryLock(1, TimeUnit.MINUTES);
                return beanSupplier.get();
            } catch (InterruptedException e) {
                //initialization took too long. forget about the bean... you have a performance issue
                throw new BeanInstantiationException(e);
            } finally {
                setAndResetLock.unlock();
            }
        }
        return beanSupplier.get();
    }


}
