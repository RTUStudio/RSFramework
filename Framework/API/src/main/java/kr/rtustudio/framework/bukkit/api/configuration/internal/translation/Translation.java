package kr.rtustudio.framework.bukkit.api.configuration.internal.translation;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/**
 * 단일 로케일의 번역 YAML 파일을 로드하여 키-값 캐시를 제공하는 클래스입니다.
 *
 * <p>{@code Translations/<type>/<lang>.yml} 파일을 로드하며, 문자열 및 문자열 리스트 타입을 지원합니다.
 */
@ToString
@SuppressWarnings("unused")
public class Translation extends RSConfiguration.Wrapper<RSPlugin> {

    private final Map<String, String> stringCache = new HashMap<>();
    private final Map<String, List<String>> stringListCache = new HashMap<>();

    public Translation(RSPlugin plugin, String folder, String lang) {
        super(plugin, ConfigPath.relative("Translation", folder, lang));
        setup(this);
    }

    private void init() {
        for (String key : keys()) {
            if (isList(key)) {
                List<String> result = getStringList(key, List.of());
                if (result.isEmpty()) continue;
                stringListCache.put(key, result);
            } else {
                String result = getString(key, "");
                if (result.isEmpty()) continue;
                stringCache.put(key, result);
            }
        }
    }

    /**
     * 키에 해당하는 번역 문자열을 반환한다.
     *
     * @param key 번역 키
     * @return 번역된 문자열, 없으면 빈 문자열
     */
    @NotNull
    public String get(String key) {
        return stringCache.getOrDefault(key, "");
    }

    /**
     * 키에 해당하는 번역 문자열 리스트를 반환한다.
     *
     * @param key 번역 키
     * @return 번역된 문자열 리스트, 없으면 빈 리스트
     */
    @NotNull
    public List<String> getList(String key) {
        return stringListCache.getOrDefault(key, List.of());
    }
}
