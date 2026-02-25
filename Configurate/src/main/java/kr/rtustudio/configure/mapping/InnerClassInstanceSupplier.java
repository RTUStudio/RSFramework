package kr.rtustudio.configure.mapping;

import static io.leangen.geantyref.GenericTypeReflector.erase;

import kr.rtustudio.configure.ConfigurationPart;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.util.CheckedFunction;
import org.spongepowered.configurate.util.CheckedSupplier;

/**
 * {@link ConfigurationPart}를 상속하는 비정적 내부 클래스의 인스턴스 생성을 담당하는 공급자입니다.
 *
 * <p>각 {@link ConfigurationPart} 타입별로 하나의 인스턴스만 생성되며, 내부 클래스 계층 구조를 자동으로 처리합니다.
 */
@Slf4j
final class InnerClassInstanceSupplier
        implements CheckedFunction<
                AnnotatedType, @Nullable Supplier<Object>, SerializationException> {

    private final Map<Class<?>, Object> instanceMap = new HashMap<>();
    private final Map<Class<?>, Object> initialOverrides;

    /**
     * @param initialOverrides 초기 인스턴스 오버라이드 맵 (타입 → 인스턴스)
     */
    InnerClassInstanceSupplier(final Map<Class<?>, Object> initialOverrides) {
        this.initialOverrides = initialOverrides;
    }

    @Override
    public Supplier<Object> apply(final AnnotatedType target) throws SerializationException {
        final Class<?> type = erase(target.getType());
        if (this.initialOverrides.containsKey(type)) {
            this.instanceMap.put(type, this.initialOverrides.get(type));
            return () -> this.initialOverrides.get(type);
        }
        if (ConfigurationPart.class.isAssignableFrom(type) && !this.instanceMap.containsKey(type)) {
            try {
                final Constructor<?> constructor;
                final CheckedSupplier<Object, ReflectiveOperationException> instanceSupplier;
                if (type.getEnclosingClass() != null && !Modifier.isStatic(type.getModifiers())) {
                    final Object instance = this.instanceMap.get(type.getEnclosingClass());
                    if (instance == null) {
                        throw new SerializationException(
                                "Cannot create a new instance of an inner class "
                                        + type.getName()
                                        + " without an instance of its enclosing class "
                                        + type.getEnclosingClass().getName());
                    }
                    constructor = type.getDeclaredConstructor(type.getEnclosingClass());
                    instanceSupplier = () -> constructor.newInstance(instance);
                } else {
                    constructor = type.getDeclaredConstructor();
                    instanceSupplier = constructor::newInstance;
                }
                constructor.setAccessible(true);
                final Object instance = instanceSupplier.get();
                this.instanceMap.put(type, instance);
                return () -> instance;
            } catch (final ReflectiveOperationException e) {
                log.error("Failed to create instance of {}", target, e);
                throw new SerializationException(
                        ConfigurationPart.class, target + " must be a valid ConfigurationPart", e);
            }
        } else {
            throw new SerializationException(target + " must be a valid ConfigurationPart");
        }
    }

    Map<Class<?>, Object> instanceMap() {
        return this.instanceMap;
    }
}
