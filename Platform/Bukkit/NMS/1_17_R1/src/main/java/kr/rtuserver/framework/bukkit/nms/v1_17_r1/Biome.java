package kr.rtuserver.framework.bukkit.nms.v1_17_r1;

import kr.rtuserver.framework.bukkit.api.nms.NMSBiome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

import java.util.List;
import java.util.stream.Collectors;

public class Biome implements NMSBiome {

    private final DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    private final ResourceKey<Registry<net.minecraft.world.level.biome.Biome>> resourceKey = Registry.BIOME_REGISTRY;
    private final Registry<net.minecraft.world.level.biome.Biome> registry = dedicatedServer.registryAccess().registryOrThrow(resourceKey);

    @Override
    public String getBiomeKey(Location location) {
        return getResourceLocation(getNMSBiome(location)).toString();
    }

    @Override
    public List<String> getBiomesAsString() {
        return registry.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> getBiomeTag(String tag) {
        return List.of();
    }

    private ResourceLocation getResourceLocation(net.minecraft.world.level.biome.Biome biome) {
        return registry.getKey(biome);
    }

    private net.minecraft.world.level.biome.Biome getNMSBiome(Location location) {
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        CraftWorld world = (CraftWorld) location.getWorld();
        if (world != null) return world.getHandle().getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);
        return null;
    }
}
