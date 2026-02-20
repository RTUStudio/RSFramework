package kr.rtustudio.framework.bukkit.api.core.provider;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry for {@link Provider} instances, keyed by interface type.
 *
 * <pre>{@code
 * registry.register(NameProvider.class, new VanillaNameProvider());
 * NameProvider provider = registry.get(NameProvider.class);
 * }</pre>
 */
public class ProviderRegistry {

    private final Map<Class<? extends Provider>, Provider> providers =
            new Reference2ObjectOpenHashMap<>();

    public <T extends Provider> void register(@NotNull Class<T> type, @NotNull T provider) {
        providers.put(type, provider);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Provider> T get(@NotNull Class<T> type) {
        return (T) providers.get(type);
    }

    public boolean has(@NotNull Class<? extends Provider> type) {
        return providers.containsKey(type);
    }
}
