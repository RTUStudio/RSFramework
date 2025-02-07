package kr.rtuserver.framework.bukkit.nms.v1_20_r4;

import kr.rtuserver.framework.bukkit.api.nms.NMSBiome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tags.TagKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Biome implements NMSBiome {

    private final DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    private final ResourceKey<Registry<net.minecraft.world.level.biome.Biome>> resourceKey = Registries.BIOME;
    private final Registry<net.minecraft.world.level.biome.Biome> registry = dedicatedServer.registries().compositeAccess().registryOrThrow(resourceKey);

    @Override
    public String getName(Location location) {
        return getResourceLocation(getNMSBiome(location)).toString();
    }

    @Override
    public List<String> getList() {
        return registry.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> getTag(String tag) {
        Optional<HolderSet.Named<net.minecraft.world.level.biome.Biome>> holders = registry.getTag(TagKey.create(resourceKey, new ResourceLocation(tag)));
        return holders.map(biomeNamed -> biomeNamed.stream().map(biomeBaseHolder -> getResourceLocation(biomeBaseHolder.value()).toString()).collect(Collectors.toList())).orElseGet(List::of);
    }

    private ResourceLocation getResourceLocation(net.minecraft.world.level.biome.Biome biome) {
        return registry.getKey(biome);
    }

    private net.minecraft.world.level.biome.Biome getNMSBiome(Location location) {
        BlockPos pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        CraftWorld world = (CraftWorld) location.getWorld();
        if (world != null)
            return world.getHandle().getChunk(pos).getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2).value();
        return null;
    }
}
