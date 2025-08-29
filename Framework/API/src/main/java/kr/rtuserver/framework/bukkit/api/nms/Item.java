package kr.rtuserver.framework.bukkit.api.nms;

import java.util.LinkedHashSet;
import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface Item {

    @Nullable
    ItemStack getItem(NamespacedKey key);

    List<NamespacedKey> getTag(NamespacedKey tag);

    LinkedHashSet<ItemStack> fromCreativeModeTab(NamespacedKey tabKey);
}
