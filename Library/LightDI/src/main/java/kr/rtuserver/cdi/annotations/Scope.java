package kr.rtuserver.cdi.annotations;

import kr.rtuserver.cdi.LightDI;

/**
 * @author Mihai Alexandru
 * @date 16.08.2018
 */
public enum Scope {

    /**
     * This indicates a component has only one instance per application.
     */
    SINGLETON,


    /**
     * This indicates a component will have an instance per request.
     * The request of course is reffering to the time when the bean is retrieved from the {@link LightDI}
     * or when injected via {@link Inject} annotation
     */
    PROTOTYPE;

}
