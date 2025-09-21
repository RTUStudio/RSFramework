package kr.rtustudio.framework.bukkit.nms.v1_19_r3;

import kr.rtustudio.framework.bukkit.api.nms.Biome;
import kr.rtustudio.framework.bukkit.api.nms.Command;
import kr.rtustudio.framework.bukkit.api.nms.Item;
import kr.rtustudio.framework.bukkit.api.nms.NMS;
import lombok.Getter;

@Getter
public class NMS_1_19_R3 implements NMS {

    private final Biome biome = new CraftBiome();
    private final Command command = new CraftCommand();
    private final Item item = new CraftItem();
}
