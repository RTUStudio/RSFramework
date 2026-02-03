package kr.rtustudio.framework.bukkit.api.core.configuration;

import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public interface CommonTranslation {

    @NotNull
    String get(TranslationType type, String key);

    @NotNull
    String get(TranslationType type, String locale, String key);

    @NotNull
    List<String> getList(TranslationType type, String key);

    @NotNull
    List<String> getList(TranslationType type, String locale, String key);
}
