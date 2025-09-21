package kr.rtustudio.framework.bukkit.api.configuration.internal.translation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TranslationType {
    COMMAND("Command"),
    MESSAGE("Message");

    private final String name;
}
