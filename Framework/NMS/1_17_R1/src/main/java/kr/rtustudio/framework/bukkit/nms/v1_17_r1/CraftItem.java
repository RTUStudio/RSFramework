package kr.rtustudio.framework.bukkit.nms.v1_17_r1;

import kr.rtustudio.framework.bukkit.api.nms.Item;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.inventory.ItemStack;

public class CraftItem implements Item {

    private final DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();
    private final ResourceKey<Registry<net.minecraft.world.item.Item>> resourceKey =
            Registry.ITEM_REGISTRY;
    private final Registry<net.minecraft.world.item.Item> registry =
            dedicatedServer.registryAccess().registryOrThrow(resourceKey);

    @Override
    public ItemStack getItem(NamespacedKey key) {
        ResourceLocation id = toResourceLocation(key);
        net.minecraft.world.item.Item item = registry.get(id);
        if (item == null) return null;
        return new net.minecraft.world.item.ItemStack(item).asBukkitCopy();
    }

    @Override
    public List<NamespacedKey> getTag(NamespacedKey tag) {
        return List.of();
    }

    private ResourceLocation toResourceLocation(NamespacedKey key) {
        return new ResourceLocation(key.getNamespace(), key.getKey());
    }
}
