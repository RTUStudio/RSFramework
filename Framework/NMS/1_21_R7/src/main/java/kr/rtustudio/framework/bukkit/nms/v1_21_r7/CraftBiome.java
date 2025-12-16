package kr.rtustudio.framework.bukkit.nms.v1_21_r7;

import kr.rtustudio.framework.bukkit.api.nms.Biome;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class CraftBiome implements Biome {

    private final DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    private final ResourceKey<@NotNull Registry<net.minecraft.world.level.biome.@NotNull Biome>>
            resourceKey = Registries.BIOME;
    private final Registry<net.minecraft.world.level.biome.@NotNull Biome> registry =
            dedicatedServer.registries().compositeAccess().lookupOrThrow(resourceKey);

    @Override
    public NamespacedKey getKey(Location location) {
        return toKey(getIdentifier(getNMSBiome(location)));
    }

    @Override
    public List<NamespacedKey> getList() {
        return registry.keySet().stream().map(this::toKey).toList();
    }

    @Override
    public List<NamespacedKey> getTag(NamespacedKey tag) {
        Optional<HolderSet.Named<net.minecraft.world.level.biome.@NotNull Biome>> holders =
                registry.get(TagKey.create(resourceKey, toIdentifier(tag)));
        return holders.map(
                        named ->
                                named.stream()
                                        .map(holder -> toKey(getIdentifier(holder.value())))
                                        .toList())
                .orElseGet(List::of);
    }

    private Identifier getIdentifier(net.minecraft.world.level.biome.Biome biome) {
        return registry.getKey(biome);
    }

    private net.minecraft.world.level.biome.Biome getNMSBiome(Location location) {
        BlockPos pos =
                new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        CraftWorld world = (CraftWorld) location.getWorld();
        if (world != null)
            return world.getHandle()
                    .getChunk(pos)
                    .getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2)
                    .value();
        return null;
    }

    private NamespacedKey toKey(Identifier id) {
        return NamespacedKey.fromString(id.toString());
    }

    private Identifier toIdentifier(NamespacedKey key) {
        return Identifier.fromNamespaceAndPath(key.getNamespace(), key.getKey());
    }
}
