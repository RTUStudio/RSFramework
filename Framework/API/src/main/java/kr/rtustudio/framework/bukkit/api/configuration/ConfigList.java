package kr.rtustudio.framework.bukkit.api.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An ordered, keyed collection of {@link ConfigurationPart} instances loaded from a folder.
 *
 * <p>Returned by {@link kr.rtustudio.framework.bukkit.api.RSPlugin#registerConfigurations}.
 *
 * <pre>{@code
 * ConfigList<RegionConfig> regions =
 *     plugin.registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));
 *
 * RegionConfig spawn = regions.get("spawn");   // Configs/Regions/spawn.yml
 * for (RegionConfig r : regions.values()) { ... }
 * }</pre>
 *
 * @param <C> the {@link ConfigurationPart} type
 */
public final class ConfigList<C extends ConfigurationPart> {

    private final Map<String, C> entries;

    public ConfigList(Map<String, C> entries) {
        this.entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    /** Returns the instance loaded from {@code <key>.yml}, or {@code null} if not found. */
    public C get(String key) {
        return entries.get(key);
    }

    /** Returns all keys (file names without {@code .yml}). */
    public Set<String> keys() {
        return entries.keySet();
    }

    /** Returns all loaded instances in file-name order. */
    public Collection<C> values() {
        return entries.values();
    }

    /** Returns the underlying map (unmodifiable). */
    public Map<String, C> asMap() {
        return entries;
    }

    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
