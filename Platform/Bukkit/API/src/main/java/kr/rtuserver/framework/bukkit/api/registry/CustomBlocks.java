package kr.rtuserver.framework.bukkit.api.registry;

import com.nexomc.nexo.api.NexoBlocks;
import dev.lone.itemsadder.api.CustomBlock;
import io.th0rgal.oraxen.api.OraxenBlocks;
import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomBlocks {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    @Nullable
    public static BlockData from(@NotNull String namespacedID) {
        if (namespacedID.isEmpty()) return null;
        String[] split = namespacedID.split(":");
        switch (split[0].toLowerCase()) {
            case "nexo" -> {
                if (framework().isEnabledDependency("Nexo")) {
                    return NexoBlocks.blockData(split[1]);
                } else return null;
            }
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
                Material material = Material.matchMaterial(namespacedID.toLowerCase());
                return material != null ? material.createBlockData() : null;
            }
        }
    }

    @NotNull
    public static String to(@NotNull Block block) {
        if (framework().isEnabledDependency("Nexo")) {
            com.nexomc.nexo.mechanics.Mechanic mechanic = NexoBlocks.customBlockMechanic(block.getBlockData());
            if (mechanic != null) return "nexo:" + mechanic.getItemID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            io.th0rgal.oraxen.mechanics.Mechanic mechanic = OraxenBlocks.getOraxenBlock(block.getBlockData());
            if (mechanic != null) return "oraxen:" + mechanic.getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomBlock itemsAdder = CustomBlock.byAlreadyPlaced(block);
            if (itemsAdder != null) return "itemsadder:" + itemsAdder.getNamespacedID();
        }
        return block.getBlockData().getMaterial().getKey().toString();
    }

    public static boolean place(@NotNull Location location, @NotNull String namespacedID) {
        if (namespacedID.isEmpty()) return false;
        String[] split = namespacedID.split(":");
        switch (split[0].toLowerCase()) {
            case "nexo" -> {
                if (framework().isEnabledDependency("Nexo")) {
                    if (NexoBlocks.isCustomBlock(split[1])) NexoBlocks.place(split[1], location);
                    else return false;
                } else return false;
                return true;
            }
            case "oraxen" -> {
                if (framework().isEnabledDependency("Oraxen")) {
                    if (OraxenBlocks.isOraxenBlock(split[1])) OraxenBlocks.place(split[1], location);
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
                Material material = Material.matchMaterial(namespacedID.toLowerCase());
                if (material != null) location.getWorld().setBlockData(location, material.createBlockData());
                else return false;
                return true;
            }
        }
    }
}
