package kr.rtuserver.framework.bukkit.nms.v1_19_r3;

import kr.rtuserver.framework.bukkit.api.nms.NMS;
import kr.rtuserver.framework.bukkit.api.nms.NMSBiome;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;

public class NMS_1_19_R3 implements NMS {

    private final NMSBiome biome = new Biome();

    @Override
    public NMSBiome biome() {
        return biome;
    }

    @Override
    public CommandMap commandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }
}
