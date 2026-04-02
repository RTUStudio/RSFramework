package kr.rtustudio.configurate.model;

import java.util.*;

/**
 * Immutable configuration container that maps YAML files in a folder by filename. On plural
 * registration, loads all {@code .yml} files in the specified folder and maps them using the
 * filename (without extension) as the key. Uses {@link java.util.LinkedHashMap} internally to
 * preserve insertion order.
 *
 * <p>폴더 내 YAML 파일들을 파일명 기준 키로 보관하는 불변 설정 컨테이너. 복수 등록 시 지정된 폴더의 모든 {@code .yml} 파일을 로드하여 파일명(확장자
 * 제외)을 키로 매핑한다. 내부적으로 {@link java.util.LinkedHashMap}을 사용하므로 삽입 순서가 보존된다.
 *
 * <pre>{@code
 * ConfigList<RegionConfig> regions =
 *     plugin.registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));
 *
 * RegionConfig spawn = regions.get("spawn");   // Config/Regions/spawn.yml
 * for (RegionConfig r : regions.values()) { ... }
 * }</pre>
 *
 * @param <C> configuration type extending {@link ConfigurationPart}
 */
public final class ConfigList<C extends ConfigurationPart> {

    private final Map<String, C> entries;

    public ConfigList(Map<String, C> entries) {
        this.entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    /**
     * Returns the instance loaded from {@code <key>.yml}.
     *
     * <p>{@code <key>.yml}에서 로드된 인스턴스를 반환한다.
     *
     * @param key filename without extension
     * @return config instance, or {@code null} if not found
     */
    public C get(String key) {
        return entries.get(key);
    }

    /**
     * Returns all keys (filenames without extension).
     *
     * <p>모든 키(파일명, 확장자 제외)를 반환한다.
     */
    public Set<String> keys() {
        return entries.keySet();
    }

    /**
     * Returns all config instances in filename order.
     *
     * <p>파일명 순서로 모든 설정 인스턴스를 반환한다.
     */
    public Collection<C> values() {
        return entries.values();
    }

    /**
     * Returns the internal map (unmodifiable).
     *
     * <p>내부 맵(수정 불가)을 반환한다.
     */
    public Map<String, C> asMap() {
        return entries;
    }

    /**
     * Checks whether a config with the given key exists.
     *
     * <p>지정한 키의 설정이 존재하는지 확인한다.
     *
     * @param key filename without extension
     * @return {@code true} if the config exists
     */
    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    /**
     * Returns the number of registered configs.
     *
     * <p>등록된 설정 개수를 반환한다.
     */
    public int size() {
        return entries.size();
    }

    /**
     * Returns {@code true} if no configs are registered.
     *
     * <p>등록된 설정이 없는지 확인한다.
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
