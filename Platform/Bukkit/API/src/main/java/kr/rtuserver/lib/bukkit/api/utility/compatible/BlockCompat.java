package kr.rtuserver.lib.bukkit.api.utility.compatible;

import dev.lone.itemsadder.api.CustomBlock;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.mechanics.Mechanic;
import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.lib.bukkit.api.core.RSFramework;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

// Supported Plugins: ItemsAdder, Oraxen
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockCompat {

    static RSFramework framework;

    static RSFramework framework() {
        if (framework == null) framework = LightDI.getBean(RSFramework.class);
        return framework;
    }

    @Nullable
    public static BlockData from(String data) {
        String[] split = data.split(":");
        String platform = split[0].toLowerCase();
        switch (platform) {
            case "oraxen" -> {
                if (framework().isEnabledDependency("Oraxen")) {
                    return OraxenBlocks.getOraxenBlockData(split[1]);
                } else return null;
            }
            case "itemsadder" -> {
                if (framework().isEnabledDependency("ItemsAdder")) {
                    CustomBlock customBlock = CustomBlock.getInstance(split[1] + ":" + split[2]);
                    return customBlock != null ? customBlock.getBaseBlockData() : null;
                } else return null;
            }
            default -> {
                String id = split.length > 1 ? split[1] : split[0];
                Material material = Material.getMaterial(id.toUpperCase());
                return material != null ? material.createBlockData() : null;
            }
        }
    }

    @NotNull
    public static String to(Block block) {
        if (framework().isEnabledDependency("Oraxen")) {
            Mechanic oraxen = OraxenBlocks.getOraxenBlock(block.getBlockData());
            if (oraxen != null)
                return "oraxen:" + oraxen.getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomBlock itemsAdder = CustomBlock.byAlreadyPlaced(block);
            if (itemsAdder != null)
                return "itemsadder:" + itemsAdder.getNamespacedID();
        }
        return "minecraft:" + block.getBlockData().getMaterial().toString().toLowerCase();
    }

    public static boolean place(Location location, String namespacedID) {
        String[] split = namespacedID.split(":");
        String platform = split[0].toLowerCase();
        switch (platform) {
            case "oraxen" -> {
                if (framework().isEnabledDependency("Oraxen")) {
                    if (OraxenBlocks.isOraxenBlock(namespacedID)) OraxenBlocks.place(split[1], location);
                    else return false;
                } else return false;
                return true;
            }
            case "itemsadder" -> {
                if (framework().isEnabledDependency("ItemsAdder")) {
                    String block = split[1] + ":" + split[2];
                    if (CustomBlock.isInRegistry(block)) CustomBlock.place(block, location);
                    else return false;
                } else return false;
                return true;
            }
            default -> {
                String id = split.length > 1 ? split[1] : split[0];
                Material material = Material.getMaterial(id.toUpperCase());
                if (material != null) location.getWorld().setBlockData(location, material.createBlockData());
                else return false;
                return true;
            }
        }
    }
}
