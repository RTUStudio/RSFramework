package kr.rtuserver.framework.bukkit.api.registry;

import com.nexomc.nexo.api.NexoFurniture;
import dev.lone.itemsadder.api.CustomFurniture;
import io.th0rgal.oraxen.api.OraxenFurniture;
import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomFurnitures {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    @Nullable
    public static String to(@NotNull Entity entity) {
        if (framework().isEnabledDependency("Nexo")) {
            com.nexomc.nexo.mechanics.Mechanic mechanic = NexoFurniture.furnitureMechanic(entity);
            if (mechanic != null) return "nexo:" + mechanic.getItemID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            io.th0rgal.oraxen.mechanics.Mechanic mechanic = OraxenFurniture.getFurnitureMechanic(entity);
            if (mechanic != null) return "oraxen:" + mechanic.getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(entity);
            if (furniture != null) return "itemsadder:" + furniture.getNamespacedID();
        }
        return null;
    }

    @Nullable
    public static String to(@NotNull Block block) {
        if (framework().isEnabledDependency("Nexo")) {
            com.nexomc.nexo.mechanics.Mechanic mechanic = NexoFurniture.furnitureMechanic(block);
            if (mechanic != null) return "nexo:" + mechanic.getItemID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            io.th0rgal.oraxen.mechanics.Mechanic mechanic = OraxenFurniture.getFurnitureMechanic(block);
            if (mechanic != null) return "oraxen:" + mechanic.getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(block);
            if (furniture != null) return "itemsadder:" + furniture.getNamespacedID();
        }
        return null;
    }

    @Nullable
    public static String to(@NotNull Location location) {
        if (framework().isEnabledDependency("Nexo")) {
            com.nexomc.nexo.mechanics.Mechanic mechanic = NexoFurniture.furnitureMechanic(location);
            if (mechanic != null) return "nexo:" + mechanic.getItemID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            io.th0rgal.oraxen.mechanics.Mechanic mechanic = OraxenFurniture.getFurnitureMechanic(location.getBlock());
            if (mechanic != null) return "oraxen:" + mechanic.getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(location.getBlock());
            if (furniture != null) return "itemsadder:" + furniture.getNamespacedID();
        }
        return null;
    }

    public static boolean place(@NotNull Location location, @NotNull String namespacedID) {
        if (namespacedID.isEmpty()) return false;
        String[] split = namespacedID.split(":");
        switch (split[0].toLowerCase()) {
            case "nexo" -> {
                if (framework().isEnabledDependency("Nexo")) {
                    if (NexoFurniture.isFurniture(split[1]))
                        NexoFurniture.place(split[1], location, Rotation.NONE, BlockFace.UP);
                    else return false;
                } else return false;
                return true;
            }
            case "oraxen" -> {
                if (framework().isEnabledDependency("Oraxen")) {
                    if (OraxenFurniture.isFurniture(split[1]))
                        OraxenFurniture.place(split[1], location, Rotation.NONE, BlockFace.UP);
                    else return false;
                } else return false;
                return true;
            }
            case "itemsadder" -> {
                if (framework().isEnabledDependency("ItemsAdder")) {
                    String block = split[1] + ":" + split[2];
                    if (CustomFurniture.isInRegistry(block)) CustomFurniture.spawn(block, location.getBlock());
                    else return false;
                } else return false;
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
