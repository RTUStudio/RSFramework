package kr.rtustudio.framework.bukkit.api.configuration.internal.translation;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public String get(String key) {
        return stringCache.getOrDefault(key, "");
    }

    @NotNull
    public List<String> getList(String key) {
        return stringListCache.getOrDefault(key, List.of());
    }
}
