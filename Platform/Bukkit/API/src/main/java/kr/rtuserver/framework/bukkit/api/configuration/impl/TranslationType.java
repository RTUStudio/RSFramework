package kr.rtuserver.framework.bukkit.api.configuration.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TranslationType {
    COMMAND("Command"),
    MESSAGE("Message");

    private final String name;
}
