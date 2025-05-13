package kr.rtuserver.framework.bukkit.api.configuration.translation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TranslationType {
    COMMAND("Command"),
    MESSAGE("Message");

    private final String name;
}
