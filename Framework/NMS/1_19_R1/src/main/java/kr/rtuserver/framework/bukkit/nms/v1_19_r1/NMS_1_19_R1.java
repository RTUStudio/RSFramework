package kr.rtuserver.framework.bukkit.nms.v1_19_r1;

import kr.rtuserver.framework.bukkit.api.nms.Biome;
import kr.rtuserver.framework.bukkit.api.nms.Command;
import kr.rtuserver.framework.bukkit.api.nms.Item;
import kr.rtuserver.framework.bukkit.api.nms.NMS;
import lombok.Getter;

@Getter
public class NMS_1_19_R1 implements NMS {

    private final Biome biome = new CraftBiome();
    private final Command command = new CraftCommand();
    private final Item item = new CraftItem();
}
