package kr.rtuserver.lib.bukkit.api.utility.player;

import kr.rtuserver.lib.bukkit.api.core.RSFramework;
import kr.rtuserver.lib.common.api.cdi.LightDI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerList {

    static RSFramework framework;

    static RSFramework framework() {
        if (framework == null) framework = LightDI.getBean(RSFramework.class);
        return framework;
    }

    public static List<String> getOnlinePlayers() {
        return List.of();
    }
}
