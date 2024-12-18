package kr.rtuserver.framework.bukkit.api.utility.compatible;

import com.nexomc.nexo.api.NexoItems;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xerial.snappy.Snappy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemCompat {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    @Nullable
    public static ItemStack from(@NotNull String namespacedID) {
        if (namespacedID.isEmpty()) return null;
        String[] split = namespacedID.split(":");
        String platform = split[0].toLowerCase();
        switch (platform) {
            case "nexo" -> {
                if (split.length != 2) return null;
                if (framework().isEnabledDependency("Nexo")) {
                    com.nexomc.nexo.items.ItemBuilder itemBuilder = NexoItems.itemFromId(split[1]);
                    return itemBuilder != null ? itemBuilder.build() : null;
                } else return null;
            }
            case "oraxen" -> {
                if (split.length != 2) return null;
                if (framework().isEnabledDependency("Oraxen")) {
                    io.th0rgal.oraxen.items.ItemBuilder itemBuilder = OraxenItems.getItemById(split[1]);
                    return itemBuilder != null ? itemBuilder.build() : null;
                } else return null;
            }
            case "itemsadder" -> {
                if (framework().isEnabledDependency("ItemsAdder")) {
                    if (split.length != 3) return null;
                    CustomStack customStack = CustomStack.getInstance(split[1] + ":" + split[2]);
                    return customStack != null ? customStack.getItemStack() : null;
                } else return null;
            }
            case "mmoitems" -> {
                if (framework().isEnabledDependency("MMOItems")) {
                    if (split.length != 3) return null;
                    return MMOItems.plugin.getItem(split[1], split[2]);
                } else return null;
            }
            case "custom" -> {
                if (split.length != 3) return null;
                Material material = Material.getMaterial(split[1].toUpperCase());
                if (material == null) return null;
                ItemStack itemStack = new ItemStack(material);
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta == null) return null;
                itemMeta.setCustomModelData(Integer.valueOf(split[2]));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
            default -> {
                Material material = Material.matchMaterial(namespacedID.toLowerCase());
                return material != null ? new ItemStack(material) : null;
            }
        }
    }

    @NotNull
    public static String to(@NotNull ItemStack itemStack) {
        if (framework().isEnabledDependency("Nexo")) {
            String nexo = NexoItems.idFromItem(itemStack);
            if (nexo != null) return "nexo:" + nexo;
        }
        if (framework().isEnabledDependency("Oraxen")) {
            String oraxen = OraxenItems.getIdByItem(itemStack);
            if (oraxen != null) return "oraxen:" + oraxen;
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomStack itemsAdder = CustomStack.byItemStack(itemStack);
            if (itemsAdder != null) return "itemsadder:" + itemsAdder.getNamespacedID();
        }
        if (framework().isEnabledDependency("MMOItems")) {
            String id = MMOItems.getID(itemStack);
            String type = MMOItems.getTypeName(itemStack);
            if (id != null && type != null) return "mmoitems:" + id + ":" + type;
        }
        String result = itemStack.getType().getKey().asString();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return result;
        if (!itemMeta.hasCustomModelData()) return result;
        return "custom:" + itemStack.getType().toString().toLowerCase() + itemMeta.getCustomModelData();
    }

    public static boolean isSimilar(ItemStack stack1, ItemStack stack2) {
        if (framework().isEnabledDependency("Nexo")) {
            String var1 = NexoItems.idFromItem(stack1);
            String var2 = NexoItems.idFromItem(stack2);
            if (var1 != null && var2 != null) return var1.equalsIgnoreCase(var2);
            else if (var1 != null) return NexoItems.itemFromId(var1).build().isSimilar(stack2);
            else if (var2 != null) return NexoItems.itemFromId(var2).build().isSimilar(stack1);
        }
        if (framework().isEnabledDependency("Oraxen")) {
            String var1 = OraxenItems.getIdByItem(stack1);
            String var2 = OraxenItems.getIdByItem(stack2);
            if (var1 != null && var2 != null) return var1.equalsIgnoreCase(var2);
            else if (var1 != null) return OraxenItems.getItemById(var1).build().isSimilar(stack2);
            else if (var2 != null) return OraxenItems.getItemById(var2).build().isSimilar(stack1);
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomStack var1 = CustomStack.byItemStack(stack1);
            CustomStack var2 = CustomStack.byItemStack(stack2);
            if (var1 != null && var2 != null) return var1.getNamespacedID().equalsIgnoreCase(var2.getNamespacedID());
            else if (var1 != null) return var1.getItemStack().isSimilar(stack2);
            else if (var2 != null) return var2.getItemStack().isSimilar(stack1);
        }
        if (framework().isEnabledDependency("MMOItems")) {
            Type type1 = MMOItems.getType(stack1);
            String id1 = MMOItems.getID(stack1);
            Type type2 = MMOItems.getType(stack2);
            String id2 = MMOItems.getID(stack2);
            MMOItem var1 = MMOItems.plugin.getMMOItem(type1, id1);
            MMOItem var2 = MMOItems.plugin.getMMOItem(type2, id2);
            if (var1 != null && var2 != null)
                return (var1.getType().getName() + ":" + var1.getId()).equalsIgnoreCase(var2.getType().getName() + ":" + var2.getId());
            else if (var1 != null) {
                ItemStack itemStack = MMOItems.plugin.getItem(type1, id1);
                if (itemStack != null) return itemStack.isSimilar(stack2);
            } else if (var2 != null) {
                ItemStack itemStack = MMOItems.plugin.getItem(type2, id2);
                if (itemStack != null) return itemStack.isSimilar(stack1);
            }
        }
        return stack1.isSimilar(stack2);
    }

    @Nullable
    public static String encode(ItemStack itemStack) {
        try {
            final ByteArrayOutputStream str = new ByteArrayOutputStream();
            final BukkitObjectOutputStream data = new BukkitObjectOutputStream(str);
            data.writeObject(itemStack);
            data.close();
            String result = Base64.getEncoder().encodeToString(Snappy.compress(str.toByteArray()));
            return result == null || result.isEmpty() ? null : result;
        } catch (final Exception e) {
            return null;
        }
    }

    @Nullable
    public static ItemStack decode(String str) {
        if (str == null || str.isEmpty()) return null;
        try {
            final ByteArrayInputStream stream = new ByteArrayInputStream(Snappy.uncompress(Base64.getDecoder().decode(str)));
            final BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
            ItemStack itemStack = (ItemStack) data.readObject();
            data.close();
            return itemStack;
        } catch (final IOException | ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public static String encodeArray(ItemStack[] array) {
        try {
            final ByteArrayOutputStream str = new ByteArrayOutputStream();
            final BukkitObjectOutputStream data = new BukkitObjectOutputStream(str);
            data.writeInt(array.length);
            for (ItemStack itemStack : array) data.writeObject(itemStack);
            data.close();
            String result = Base64.getEncoder().encodeToString(Snappy.compress(str.toByteArray()));
            return result == null || result.isEmpty() ? null : result;
        } catch (final Exception e) {
            return null;
        }
    }

    @Nullable
    public static ItemStack[] decodeArray(String str) {
        if (str == null || str.isEmpty()) return null;
        try {
            final ByteArrayInputStream stream = new ByteArrayInputStream(Snappy.uncompress(Base64.getDecoder().decode(str)));
            final BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
            int invSize = data.readInt();
            ItemStack[] array = new ItemStack[invSize];
            for (int i = 0; i < invSize; i++) array[i] = (ItemStack) data.readObject();
            data.close();
            return array;
        } catch (final IOException | ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public static String serialize(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) return null;
        ReadableNBT nbt = NBT.itemStackToNBT(itemStack);
        return nbt.toString();
    }

    @Nullable
    public static ItemStack deserialize(String nbt) {
        return NBT.itemStackFromNBT(NBT.parseNBT(nbt));
    }

    @Nullable
    public static String serializeArray(ItemStack[] itemStack) {
        ReadableNBT nbt = NBT.itemStackArrayToNBT(itemStack);
        return nbt.toString();
    }

    @Nullable
    public static ItemStack[] deserializeArray(String json) {
        return NBT.itemStackArrayFromNBT(NBT.parseNBT(json));
    }
}
