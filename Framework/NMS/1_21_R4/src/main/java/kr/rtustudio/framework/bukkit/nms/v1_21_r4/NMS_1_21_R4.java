package kr.rtustudio.framework.bukkit.nms.v1_21_r4;

import kr.rtustudio.framework.bukkit.api.nms.Biome;
import kr.rtustudio.framework.bukkit.api.nms.Command;
import kr.rtustudio.framework.bukkit.api.nms.Item;
import kr.rtustudio.framework.bukkit.api.nms.NMS;
import lombok.Getter;

@Getter
public class NMS_1_21_R4 implements NMS {

    private final Biome biome = new CraftBiome();
    private final Command command = new CraftCommand();
    private final Item item = new CraftItem();
}
