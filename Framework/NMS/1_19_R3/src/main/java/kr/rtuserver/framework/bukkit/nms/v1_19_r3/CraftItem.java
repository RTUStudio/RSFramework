package kr.rtuserver.framework.bukkit.nms.v1_19_r3;

import kr.rtuserver.framework.bukkit.api.nms.Item;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tags.TagKey;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.inventory.ItemStack;

public class CraftItem implements Item {

    private final DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    private final ResourceKey<Registry<net.minecraft.world.item.Item>> resourceKey =
            Registries.ITEM;
    private final Registry<net.minecraft.world.item.Item> registry =
            dedicatedServer.registries().compositeAccess().registryOrThrow(resourceKey);

    @Override
    public ItemStack getItem(NamespacedKey key) {
        ResourceLocation id = toResourceLocation(key);
        net.minecraft.world.item.Item item = registry.get(id);
        if (item == null) return null;
        return new net.minecraft.world.item.ItemStack(item).asBukkitCopy();
    }

    @Override
    public LinkedHashSet<ItemStack> fromCreativeModeTab(NamespacedKey tabKey) {
        return new LinkedHashSet<>();
    }

    @Override
    public List<NamespacedKey> getTag(NamespacedKey tag) {
        Optional<HolderSet.Named<net.minecraft.world.item.Item>> holders =
                registry.getTag(TagKey.create(resourceKey, toResourceLocation(tag)));
        return holders.map(
                        named ->
                                named.stream()
                                        .map(holder -> toKey(getResourceLocation(holder.value())))
                                        .toList())
                .orElseGet(List::of);
    }

    private ResourceLocation getResourceLocation(net.minecraft.world.item.Item item) {
        return registry.getKey(item);
    }

    private ResourceLocation toResourceLocation(NamespacedKey key) {
        return new ResourceLocation(key.getNamespace(), key.getKey());
    }

    private NamespacedKey toKey(ResourceLocation id) {
        return NamespacedKey.fromString(id.toString());
    }
}
