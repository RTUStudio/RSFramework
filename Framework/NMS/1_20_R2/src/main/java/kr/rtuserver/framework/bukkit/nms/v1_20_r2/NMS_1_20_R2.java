package kr.rtuserver.framework.bukkit.nms.v1_20_r2;

import kr.rtuserver.framework.bukkit.api.nms.NMS;
import kr.rtuserver.framework.bukkit.api.nms.NMSBiome;
import kr.rtuserver.framework.bukkit.api.nms.NMSCommand;
import lombok.Getter;

@Getter
public class NMS_1_20_R2 implements NMS {

    private final NMSBiome biome = new Biome();
    private final NMSCommand command = new Command();
}
