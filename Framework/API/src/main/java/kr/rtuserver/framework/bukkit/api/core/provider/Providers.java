package kr.rtuserver.framework.bukkit.api.core.provider;

import kr.rtuserver.framework.bukkit.api.core.provider.name.NameProvider;

public interface Providers {

    NameProvider getName();

    void setName(NameProvider provider);
}
