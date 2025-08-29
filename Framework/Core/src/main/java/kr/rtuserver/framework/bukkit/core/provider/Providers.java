package kr.rtuserver.framework.bukkit.core.provider;

import kr.rtuserver.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtuserver.framework.bukkit.core.Framework;
import kr.rtuserver.framework.bukkit.core.provider.name.VanillaNameProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Providers implements kr.rtuserver.framework.bukkit.api.core.provider.Providers {

    private final Framework framework;
    private NameProvider name;

    public NameProvider getName() {
        if (name == null) return new VanillaNameProvider();
        return name;
    }
}
