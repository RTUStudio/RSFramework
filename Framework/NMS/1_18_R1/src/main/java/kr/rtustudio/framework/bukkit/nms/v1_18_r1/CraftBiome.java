package kr.rtustudio.framework.bukkit.nms.v1_18_r1;

import kr.rtustudio.framework.bukkit.api.nms.Biome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;

public class CraftBiome implements Biome {

    private final DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    private final ResourceKey<Registry<net.minecraft.world.level.biome.Biome>> resourceKey =
            Registry.BIOME_REGISTRY;
    private final Registry<net.minecraft.world.level.biome.Biome> registry =
            dedicatedServer.registryAccess().registryOrThrow(resourceKey);

    @Override
    public NamespacedKey getKey(Location location) {
        return toKey(getResourceLocation(getNMSBiome(location)));
    }

    @Override
    public List<NamespacedKey> getList() {
        return registry.keySet().stream().map(this::toKey).toList();
    }

    @Override
    public List<NamespacedKey> getTag(NamespacedKey tag) {
        return List.of();
    }

    private ResourceLocation getResourceLocation(net.minecraft.world.level.biome.Biome biome) {
        return registry.getKey(biome);
    }

    private net.minecraft.world.level.biome.Biome getNMSBiome(Location location) {
        BlockPos pos =
                new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        CraftWorld world = (CraftWorld) location.getWorld();
        if (world != null)
            return world.getHandle()
                    .getChunk(pos)
                    .getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);
        return null;
    }

    private NamespacedKey toKey(ResourceLocation id) {
        return NamespacedKey.fromString(id.toString());
    }
}
