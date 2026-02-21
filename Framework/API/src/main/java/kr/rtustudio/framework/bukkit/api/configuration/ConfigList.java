package kr.rtustudio.framework.bukkit.api.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 폴더 내 YAML 파일들을 파일명 기준으로 정렬하여 보관하는 설정 컨테이너입니다.
 *
 * <p>{@link kr.rtustudio.framework.bukkit.api.RSPlugin#registerConfigurations}로 반환됩니다.
 *
 * <pre>{@code
 * ConfigList<RegionConfig> regions =
 *     plugin.registerConfigurations(RegionConfig.class, ConfigPath.of("Regions"));
 *
 * RegionConfig spawn = regions.get("spawn");   // Config/Regions/spawn.yml
 * for (RegionConfig r : regions.values()) { ... }
 * }</pre>
 *
 * @param <C> {@link ConfigurationPart} 타입
 */
public final class ConfigList<C extends ConfigurationPart> {

    private final Map<String, C> entries;

    public ConfigList(Map<String, C> entries) {
        this.entries = Collections.unmodifiableMap(new LinkedHashMap<>(entries));
    }

    /**
     * {@code <key>.yml}에서 로드된 인스턴스를 반환한다.
     *
     * @param key 파일명 (확장자 제외)
     * @return 설정 인스턴스, 없으면 {@code null}
     */
    public C get(String key) {
        return entries.get(key);
    }

    /** 모든 키(파일명, 확장자 제외)를 반환한다. */
    public Set<String> keys() {
        return entries.keySet();
    }

    /** 파일명 순서로 모든 설정 인스턴스를 반환한다. */
    public Collection<C> values() {
        return entries.values();
    }

    /** 내부 맵(수정 불가)을 반환한다. */
    public Map<String, C> asMap() {
        return entries;
    }

    /**
     * 지정한 키의 설정이 존재하는지 확인한다.
     *
     * @param key 파일명 (확장자 제외)
     * @return 존재 여부
     */
    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    /** 등록된 설정 개수를 반환한다. */
    public int size() {
        return entries.size();
    }

    /** 등록된 설정이 없는지 확인한다. */
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
