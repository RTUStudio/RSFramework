package kr.rtustudio.framework.bukkit.core.configuration;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationType;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public class CommonTranslation
        implements kr.rtustudio.framework.bukkit.api.core.configuration.CommonTranslation {
    private final CommandTranslation command;
    private final MessageTranslation message;

    public CommonTranslation(RSPlugin plugin) {
        this.command = plugin.getConfiguration().getCommand();
        this.message = plugin.getConfiguration().getMessage();
    }

    @Override
    public @NotNull String get(TranslationType type, String key) {
        return get(type, null, key);
    }

    @Override
    public @NonNull String get(TranslationType type, String locale, String key) {
        return switch (type) {
            case COMMAND -> command.get(locale, "common." + key);
            case MESSAGE -> message.get(locale, "common." + key);
        };
    }

    @Override
    public @NotNull List<String> getList(TranslationType type, String key) {
        return getList(type, null, key);
    }

    @Override
    public @NonNull List<String> getList(TranslationType type, String locale, String key) {
        return switch (type) {
            case COMMAND -> command.getList(locale, "common." + key);
            case MESSAGE -> message.getList(locale, "common." + key);
        };
    }
}
