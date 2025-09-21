package kr.rtustudio.framework.bukkit.core.configuration;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommonTranslation
        implements kr.rtustudio.framework.bukkit.api.core.configuration.CommonTranslation {

    private final RSPlugin plugin;

    @Override
    public String get(TranslationType type, String key) {
        return get(type, null, key);
    }

    @Override
    public String get(TranslationType type, String locale, String key) {
        return switch (type) {
            case COMMAND -> getCommand(locale, key);
            case MESSAGE -> getMessage(locale, key);
        };
    }

    private String getCommand(String locale, String key) {
        return plugin.getConfiguration().getCommand().get(locale, "common." + key);
    }

    private String getMessage(String locale, String key) {
        return plugin.getConfiguration().getMessage().get(locale, "common." + key);
    }
}
