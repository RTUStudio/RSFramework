package kr.rtuserver.cdi.beans;

import static java.util.Objects.isNull;

import kr.rtuserver.cdi.beans.classholders.ClassHolder;
import kr.rtuserver.cdi.beans.visitors.impl.*;
import kr.rtuserver.cdi.factories.BeanHolderFactory;
import kr.rtuserver.cdi.factories.impl.BeanHolderFactoryImpl;
import kr.rtuserver.cdi.factories.impl.ClassHolderFactory;
import kr.rtuserver.cdi.objects.BeanNameKey;
import kr.rtuserver.cdi.utils.ClassHierarchyLoader;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.reflections.Reflections;

/**
 * @author Mihai Alexandru
 * @date 17.08.2018
 */
public class BeanStore {

    private final Map<Class<?>, Map<BeanNameKey, BeanHolder>> beanMap;

    private final ClassHolderFactory classHolderFactory;

    private final BeanHolderFactory beanHolderFactory;

    private final Reflections reflections;

    public BeanStore(Reflections reflections) {
        beanMap = new HashMap<>();
        classHolderFactory = new ClassHolderFactory();
        beanHolderFactory = new BeanHolderFactoryImpl();
        this.reflections = reflections;
    }

    /**
     * @param clazz
     * @param <T>
     * @return the given bean implementation for the class.
     * @throws IllegalArgumentException if more than one bean is found for the given class. In this
     *     case the method {@link BeanStore#getBean(String, Class)} should be used.
     */
    public <T> Optional<T> getBean(Class<T> clazz) {
        var clazzBeans = beanMap.get(clazz);
        if (isNull(clazzBeans)) {
            return Optional.empty();
        }
        if (clazzBeans.size() != 1) {
            throw new IllegalArgumentException("More than one bean found for the given class");
        }
        return (Optional<T>) Optional.of(clazzBeans.values().iterator().next().get());
    }

    /**
     * @param beanName The name of the bean
     * @param clazz the class of the bean
     * @param <T> Type or supertype of the desired bean.
     * @return empty optional if no bean found in the store. Optional of bean T if the bean was
     *     found in the store.
     */
    public <T> Optional<T> getBean(String beanName, Class<T> clazz) {
        var clazzBeans = beanMap.get(clazz);
        if (isNull(clazzBeans)) {
            return Optional.empty();
        }
        BeanHolder beanHolder = clazzBeans.get(new BeanNameKey(beanName));
        if (isNull(beanHolder)) {
            return Optional.empty();
        }
        return Optional.of(clazz.cast(beanHolder.get()));
    }

    /**
     * @param classHolder
     * @return
     */
    public Optional<Object> getBean(ClassHolder classHolder) {
        BeanStoreInstanceVisitor instanceVisitor = new BeanStoreInstanceVisitor(this, reflections);
        classHolder.accept(instanceVisitor);
        return instanceVisitor.getInstance();
    }

    /**
     * @param clazz
     * @param <T>
     * @return list of bean implementation for the given class. Returns empty list if no bean
     *     implementation found for the given class
     */
    public <T> List<T> getBeans(Class<T> clazz) {
        var clazzBeansMap = beanMap.get(clazz);
        if (clazzBeansMap != null) {
            return (List<T>)
                    clazzBeansMap.values().stream()
                            .map(BeanHolder::get)
                            .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    // ----------------------------------------------------------------------------------------

    /**
     * Initializes the store with the given classes.
     *
     * @param classes
     */
    public void init(Collection<Class<?>> classes) {
        var classHolders =
                classes.stream()
                        .map(classHolderFactory::getClassHolder)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        for (ClassHolder ch : classHolders) {
            processComponent(ch, new HashSet<>());
        }
    }

    // ------------------------------------------------------------------------------
    private void processComponent(ClassHolder classHolder, Set<Class<?>> classesInChain) {
        validateClassHolder(classHolder, classesInChain);
        if (getBean(classHolder).isPresent()) {
            classesInChain.remove(classHolder.getBeanClass());
            return;
        }
        var dependencies = getDependencies(classHolder);
        for (ClassHolder ch : dependencies) {
            processComponent(ch, classesInChain);
        }

        classesInChain.remove(classHolder.getBeanClass());

        Supplier<Object> instanceSupplier = getInstanceSupplier(classHolder);
        Optional<BeanHolder> beanHolder = getBeanHolder(classHolder, instanceSupplier);
        beanHolder.ifPresent(bh -> addBean(classHolder, bh));
    }

    private void validateClassHolder(ClassHolder classHolder, Set<Class<?>> classesInChain) {
        BeanValidatorVisitor validatorVisitor = new BeanValidatorVisitor(this, classesInChain);
        classHolder.accept(validatorVisitor);
        classesInChain.addAll(validatorVisitor.getUpdatedClassesInChain());
    }

    private List<ClassHolder> getDependencies(ClassHolder ch) {
        BeanDependenciesVisitor dependenciesVisitor =
                new BeanDependenciesVisitor(this, classHolderFactory, reflections);
        ch.accept(dependenciesVisitor);
        return dependenciesVisitor.getDependencies();
    }

    private Supplier<Object> getInstanceSupplier(ClassHolder ch) {
        BeanInstanceVisitor beanInstanceVisitor = new BeanInstanceVisitor(this, classHolderFactory);
        ch.accept(beanInstanceVisitor);
        return beanInstanceVisitor.getInstanceSupplier();
    }

    private Optional<BeanHolder> getBeanHolder(ClassHolder ch, Supplier<Object> instanceSupplier) {
        BeanInstanceHolderVisitor beanInstanceHolderVisitor =
                new BeanInstanceHolderVisitor(this, beanHolderFactory, instanceSupplier);
        ch.accept(beanInstanceHolderVisitor);
        return beanInstanceHolderVisitor.getBeanHolder();
    }

    private void addBean(ClassHolder classHolder, BeanHolder beanHolder) {
        BeanNameKey key = new BeanNameKey(classHolder.getBeanClass());
        for (Class<?> c : ClassHierarchyLoader.getParents(classHolder.getBeanClass())) {
            var classBeans = beanMap.get(c);
            if (isNull(classBeans)) {
                classBeans = new HashMap<>();
                beanMap.put(c, classBeans);
            }
            classBeans.put(key, beanHolder);
        }
    }
}
