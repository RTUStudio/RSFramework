package kr.rtuserver.framework.bukkit.api.registry;

import de.tr7zw.changeme.nbtapi.*;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import de.tr7zw.changeme.nbtapi.iface.ReadableNBT;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.nms.Item;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.Indyuce.mmoitems.MMOItems;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xerial.snappy.Snappy;

import com.google.gson.*;
import com.nexomc.nexo.api.NexoItems;
import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItems;
import com.willfp.ecoitems.items.ItemUtilsKt;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomItems {

    private static final Gson GSON = new Gson();

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    @Nullable
    public static ItemStack from(@NotNull String namespacedID) {
        if (namespacedID.isEmpty()) return null;
        String[] split = namespacedID.split(":");
        switch (split[0].toLowerCase()) {
            case "nexo" -> {
                if (split.length != 2) return null;
                if (framework().isEnabledDependency("Nexo")) {
                    com.nexomc.nexo.items.ItemBuilder itemBuilder = NexoItems.itemFromId(split[1]);
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
            case "oraxen" -> {
                if (split.length != 2) return null;
                if (framework().isEnabledDependency("Oraxen")) {
                    io.th0rgal.oraxen.items.ItemBuilder itemBuilder =
                            OraxenItems.getItemById(split[1]);
                    return itemBuilder != null ? itemBuilder.build() : null;
                } else return null;
            }
            case "ecoitems" -> {
                if (framework().isEnabledDependency("EcoItems")) {
                    if (split.length != 2) return null;
                    EcoItem item = EcoItems.INSTANCE.getByID(split[1]);
                    return item != null ? item.getItemStack() : null;
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
            case "cmt" -> {
                if (split.length != 4) return null;
                NamespacedKey key = NamespacedKey.fromString(split[1] + ":" + split[2]);
                Item item = framework().getNMS().getItem();
                LinkedHashSet<ItemStack> set = item.fromCreativeModeTab(key);
                System.out.println(
                        set.stream().map(ItemStack::getType).collect(Collectors.toList()));
                int index = Integer.getInteger(split[3]);
                if (index < 0 || index >= set.size()) return null;
                Iterator<ItemStack> iterator = set.iterator();
                ItemStack element = null;
                for (int i = 0; i <= index; i++) {
                    element = iterator.next();
                }
                return element;
            }
            default -> {
                NamespacedKey key = NamespacedKey.fromString(namespacedID);
                if (key == null) return null;
                Material material = Registry.MATERIAL.get(key);
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
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomStack itemsAdder = CustomStack.byItemStack(itemStack);
            if (itemsAdder != null) return "itemsadder:" + itemsAdder.getNamespacedID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            String oraxen = OraxenItems.getIdByItem(itemStack);
            if (oraxen != null) return "oraxen:" + oraxen;
        }
        if (framework().isEnabledDependency("EcoItems")) {
            EcoItem item = ItemUtilsKt.getEcoItem(itemStack);
            if (item != null) return "ecoitems:" + item.getID();
        }
        if (framework().isEnabledDependency("MMOItems")) {
            String type = MMOItems.getTypeName(itemStack);
            String id = MMOItems.getID(itemStack);
            if (id != null && type != null) return "mmoitems:" + type + ":" + id;
        }
        String result = itemStack.getType().getKey().toString();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return result;
        if (itemMeta.hasCustomModelData()) {
            return "custom:"
                    + itemStack.getType().toString().toLowerCase()
                    + itemMeta.getCustomModelData();
        } else return result;
    }

    public static boolean isSimilar(ItemStack stack1, ItemStack stack2) {
        String id1 = to(stack1);
        String id2 = to(stack2);
        if (id1.startsWith("minecraft:") || id2.startsWith("minecraft:"))
            return stack1.isSimilar(stack2);
        if (id1.startsWith("custom:") || id2.startsWith("custom:")) return stack1.isSimilar(stack2);
        return id1.equalsIgnoreCase(id2);
    }

    @Nullable
    public static String serialize(@NotNull ItemStack target) {
        return serialize(target, false);
    }

    @Nullable
    public static String serialize(@NotNull ItemStack target, boolean compress) {
        ReadWriteNBT result = toNBT(target);
        if (result == null) return null;
        if (compress) {
            try {
                byte[] bytes = Snappy.compress(result.toString());
                return Base64.getEncoder().encodeToString(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else return result.toString();
    }

    @Nullable
    public static ItemStack deserialize(@NotNull String nbt) {
        return deserialize(nbt, false);
    }

    @Nullable
    public static ItemStack deserialize(@NotNull String nbt, boolean compressed) {
        if (nbt.isEmpty()) return null;
        if (compressed) {
            try {
                byte[] bytes = Base64.getDecoder().decode(nbt);
                nbt = Snappy.uncompressString(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return fromNBT(NBT.parseNBT(nbt));
    }

    @NotNull
    public static String serializeArray(@NotNull ItemStack[] items) {
        return serializeArray(items, false);
    }

    @NotNull
    public static String serializeArray(@NotNull ItemStack[] items, boolean compress) {
        NBTContainer result = toNBTArray(items);
        if (compress) {
            try {
                byte[] bytes = Snappy.compress(result.toString());
                return Base64.getEncoder().encodeToString(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        } else return result.toString();
    }

    @NotNull
    public static ItemStack[] deserializeArray(@NotNull String nbt) {
        return deserializeArray(nbt, false);
    }

    @NotNull
    public static ItemStack[] deserializeArray(@NotNull String nbt, boolean compressed) {
        if (compressed) {
            try {
                byte[] bytes = Base64.getDecoder().decode(nbt);
                nbt = Snappy.uncompressString(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return fromNBTArray(NBT.parseNBT(nbt));
    }

    @Nullable
    public static ReadWriteNBT toNBT(@NotNull ItemStack target) {
        if (target.getType().isAir()) return null;
        int count = target.getAmount();

        String id = CustomItems.to(target);
        ItemStack original = CustomItems.from(id);

        ReadWriteNBT originNBT = NBT.itemStackToNBT(original);
        ReadWriteNBT targetNBT = NBT.itemStackToNBT(target);

        ReadWriteNBT diff = extractDifferenceNBT(originNBT, targetNBT);

        diff.setString("id", id);
        if (count > 1) diff.setInteger("count", count);

        return diff;
    }

    @Nullable
    public static ItemStack fromNBT(@NotNull ReadableNBT nbt) {
        ReadWriteNBT override = NBT.parseNBT(nbt.toString());
        String id = override.getString("id");
        override.removeKey("id");

        Integer count = override.getInteger("count");
        if (count == 0) count = 1;

        ItemStack itemStack = CustomItems.from(id);
        if (itemStack == null || itemStack.getType().isAir()) return null;
        itemStack.setAmount(count);

        ReadWriteNBT source = NBT.itemStackToNBT(itemStack);
        ReadWriteNBT result = mergeNBT(source, override);

        return NBT.itemStackFromNBT(result);
    }

    @NotNull
    public static NBTContainer toNBTArray(@NotNull ItemStack[] items) {
        NBTContainer container = new NBTContainer();
        container.setInteger("size", items.length);
        NBTCompoundList list = container.getCompoundList("items");
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType().isAir()) continue;
            NBTListCompound entry = list.addCompound();
            entry.setInteger("Slot", i);
            entry.mergeCompound(toNBT(item));
        }
        return container;
    }

    @NotNull
    public static ItemStack[] fromNBTArray(@NotNull ReadWriteNBT nbt) {
        if (!nbt.hasTag("size")) return new ItemStack[] {};
        ItemStack[] rebuild = new ItemStack[nbt.getInteger("size")];
        for (int i = 0; i < rebuild.length; i++) rebuild[i] = new ItemStack(Material.AIR);
        if (!nbt.hasTag("items")) return rebuild;
        ReadWriteNBTCompoundList list = nbt.getCompoundList("items");
        for (ReadWriteNBT lcomp : list) {
            if (lcomp instanceof NBTCompound) {
                int slot = lcomp.getInteger("Slot");
                rebuild[slot] = fromNBT(lcomp);
            }
        }
        return rebuild;
    }

    private static ReadWriteNBT extractDifferenceNBT(ReadWriteNBT original, ReadWriteNBT target) {
        ReadWriteNBT result = NBT.createNBTObject();
        for (String key : target.getKeys()) {
            if (!original.hasTag(key)) setNBT(result, target, key);
            else if (original.getType(key) == NBTType.NBTTagCompound
                    && target.getType(key) == NBTType.NBTTagCompound) {
                ReadWriteNBT nestedDiff =
                        extractDifferenceNBT(original.getCompound(key), target.getCompound(key));
                if (!nestedDiff.getKeys().isEmpty())
                    result.getOrCreateCompound(key).mergeCompound(nestedDiff);
            } else if (!equalsNBT(original, target, key)) setNBT(result, target, key);
        }
        return result;
    }

    private static ReadWriteNBT mergeNBT(ReadWriteNBT source, ReadWriteNBT override) {
        ReadWriteNBT result = NBT.parseNBT(source.toString());
        for (String key : override.getKeys()) {
            if (source.hasTag(key)) {
                NBTType type = source.getType(key);
                boolean sameType = source.getType(key) == override.getType(key);
                if (sameType && type == NBTType.NBTTagCompound) {
                    ReadWriteNBT nestedDiff =
                            mergeNBT(source.getCompound(key), override.getCompound(key));
                    if (!nestedDiff.getKeys().isEmpty())
                        result.getOrCreateCompound(key).mergeCompound(nestedDiff);
                } else if (sameType && type == NBTType.NBTTagString) {
                    try {
                        JsonElement sourceJson = JsonParser.parseString(source.getString(key));
                        JsonElement overrideJson = JsonParser.parseString(override.getString(key));
                        String json =
                                GSON.toJson(
                                        mergeJson(
                                                sourceJson.getAsJsonObject(),
                                                overrideJson.getAsJsonObject()));
                        result.setString(key, json);
                    } catch (JsonSyntaxException e) {
                        result.setString(key, override.getString(key));
                    }
                } else if (!equalsNBT(source, override, key)) setNBT(result, override, key);
            } else setNBT(result, override, key);
        }
        return result;
    }

    private static void setNBT(ReadWriteNBT result, ReadWriteNBT target, String key) {
        switch (target.getType(key)) {
            case NBTTagByte -> result.setByte(key, target.getByte(key));
            case NBTTagByteArray -> result.setByteArray(key, target.getByteArray(key));
            case NBTTagCompound ->
                    result.getOrCreateCompound(key).mergeCompound(target.getCompound(key));
            case NBTTagDouble -> result.setDouble(key, target.getDouble(key));
            case NBTTagFloat -> result.setFloat(key, target.getFloat(key));
            case NBTTagInt -> result.setInteger(key, target.getInteger(key));
            case NBTTagIntArray -> result.setIntArray(key, target.getIntArray(key));
            case NBTTagList ->
                    NBTReflectionUtil.set(
                            (NBTCompound) result,
                            key,
                            NBTReflectionUtil.getEntry((NBTCompound) target, key));
            case NBTTagLong -> result.setLong(key, target.getLong(key));
            case NBTTagShort -> result.setShort(key, target.getShort(key));
            case NBTTagString -> result.setString(key, target.getString(key));
            case NBTTagLongArray -> result.setLongArray(key, target.getLongArray(key));
        }
    }

    private static boolean equalsNBT(ReadWriteNBT compA, ReadWriteNBT compB, String key) {
        if (compA.getType(key) != compB.getType(key)) return false;
        return switch (compA.getType(key)) {
            case NBTTagByte -> compA.getByte(key).equals(compB.getByte(key));
            case NBTTagByteArray -> Arrays.equals(compA.getByteArray(key), compB.getByteArray(key));
            case NBTTagDouble -> compA.getDouble(key).equals(compB.getDouble(key));
            case NBTTagEnd -> true;
            case NBTTagFloat -> compA.getFloat(key).equals(compB.getFloat(key));
            case NBTTagInt -> compA.getInteger(key).equals(compB.getInteger(key));
            case NBTTagIntArray -> Arrays.equals(compA.getIntArray(key), compB.getIntArray(key));
            case NBTTagList ->
                    NBTReflectionUtil.getEntry((NBTCompound) compA, key)
                            .toString()
                            .equals(
                                    NBTReflectionUtil.getEntry((NBTCompound) compB, key)
                                            .toString());
            case NBTTagLong -> compA.getLong(key).equals(compB.getLong(key));
            case NBTTagShort -> compA.getShort(key).equals(compB.getShort(key));
            case NBTTagString -> compA.getString(key).equals(compB.getString(key));
            case NBTTagLongArray -> Arrays.equals(compA.getLongArray(key), compB.getLongArray(key));
            default -> false;
        };
    }

    private static JsonObject mergeJson(JsonObject source, JsonObject override) {
        JsonObject result = source.deepCopy();
        for (String key : override.keySet()) {
            JsonElement overrideVal = override.get(key);
            if (result.has(key)) {
                JsonElement sourceVal = result.get(key);
                if (sourceVal.isJsonObject() && overrideVal.isJsonObject()) {
                    result.add(
                            key,
                            mergeJson(sourceVal.getAsJsonObject(), overrideVal.getAsJsonObject()));
                } else result.add(key, overrideVal);
            } else result.add(key, overrideVal);
        }
        return result;
    }
}
