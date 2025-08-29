package kr.rtuserver.framework.bukkit.api.core.configuration;

import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.TranslationType;

public interface CommonTranslation {

    String get(TranslationType type, String key);

    String get(TranslationType type, String locale, String key);
}
