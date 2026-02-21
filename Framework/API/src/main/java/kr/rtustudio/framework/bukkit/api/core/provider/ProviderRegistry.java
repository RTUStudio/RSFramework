package kr.rtustudio.framework.bukkit.api.core.provider;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Provider} 인스턴스를 인터페이스 타입으로 관리하는 레지스트리입니다.
 *
 * <pre>{@code
 * registry.register(NameProvider.class, new VanillaNameProvider());
 * NameProvider provider = registry.get(NameProvider.class);
 * }</pre>
 */
public class ProviderRegistry {

    private final Map<Class<? extends Provider>, Provider> providers =
            new Reference2ObjectOpenHashMap<>();

    /**
     * 프로바이더를 등록하거나 교체한다.
     *
     * @param type 프로바이더 인터페이스 클래스
     * @param provider 등록할 프로바이더 인스턴스
     * @param <T> 프로바이더 타입
     */
    public <T extends Provider> void register(@NotNull Class<T> type, @NotNull T provider) {
        providers.put(type, provider);
    }

    /**
     * 지정한 타입의 프로바이더를 조회한다.
     *
     * @param type 프로바이더 인터페이스 클래스
     * @param <T> 프로바이더 타입
     * @return 등록된 프로바이더 인스턴스, 없으면 {@code null}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Provider> T get(@NotNull Class<T> type) {
        return (T) providers.get(type);
    }

    /**
     * 지정한 타입의 프로바이더가 등록되어 있는지 확인한다.
     *
     * @param type 프로바이더 인터페이스 클래스
     * @return 등록 여부
     */
    public boolean has(@NotNull Class<? extends Provider> type) {
        return providers.containsKey(type);
    }
}
