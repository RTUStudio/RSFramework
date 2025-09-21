package kr.rtustudio.framework.bukkit.api.core.provider;

import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;

public interface Providers {

    NameProvider getName();

    void setName(NameProvider provider);
}
