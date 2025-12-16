package kr.rtustudio.framework.bukkit.nms.v1_21_r7;

import kr.rtustudio.framework.bukkit.api.nms.Item;
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
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CraftItem implements Item {

    private final DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    private final ResourceKey<@NotNull Registry<net.minecraft.world.item.@NotNull Item>>
            resourceKey = Registries.ITEM;
    private final Registry<net.minecraft.world.item.@NotNull Item> registry =
            dedicatedServer.registries().compositeAccess().lookupOrThrow(resourceKey);

    @Override
    public ItemStack getItem(NamespacedKey key) {
        Identifier id = toIdentifier(key);
        net.minecraft.world.item.Item item = registry.getValue(id);
        if (item == null) return null;
        return new net.minecraft.world.item.ItemStack(item).asBukkitCopy();
    }

    @Override
    public List<NamespacedKey> getTag(NamespacedKey tag) {
        Optional<HolderSet.Named<net.minecraft.world.item.@NotNull Item>> holders =
                registry.get(TagKey.create(resourceKey, toIdentifier(tag)));
        return holders.map(
                        named ->
                                named.stream()
                                        .map(holder -> toKey(getIdentifier(holder.value())))
                                        .toList())
                .orElseGet(List::of);
    }

    private Identifier getIdentifier(net.minecraft.world.item.Item item) {
        return registry.getKey(item);
    }

    private Identifier toIdentifier(NamespacedKey key) {
        return Identifier.fromNamespaceAndPath(key.getNamespace(), key.getKey());
    }

    private NamespacedKey toKey(Identifier id) {
        return NamespacedKey.fromString(id.toString());
    }
}
