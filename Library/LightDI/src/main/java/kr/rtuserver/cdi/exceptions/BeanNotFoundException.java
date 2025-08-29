package kr.rtuserver.cdi.exceptions;

/**
 * @author Mihai Alexandru
 * @date 21.08.2018
 */
public class BeanNotFoundException extends RuntimeException {

    public BeanNotFoundException(String beanName) {
        super("No bean not found for name: " + beanName);
    }

    public BeanNotFoundException(Class<?> clazz) {
        super("No bean found for given class: " + clazz.getSimpleName());
    }
}
