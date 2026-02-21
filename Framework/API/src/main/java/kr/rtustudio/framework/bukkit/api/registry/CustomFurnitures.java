package kr.rtustudio.framework.bukkit.api.registry;

import dev.lone.itemsadder.api.CustomFurniture;
import io.th0rgal.oraxen.api.OraxenFurniture;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.nexomc.nexo.api.NexoFurniture;

/**
 * Nexo, Oraxen, ItemsAdder 등 커스텀 가구 플러그인을 통합 처리하는 유틸리티 클래스입니다.
 *
 * <p>Namespaced ID 형식({@code nexo:id}, {@code oraxen:id} 등)으로 가구를 조회·배치·제거합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomFurnitures {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    /**
     * 엔티티로부터 가구 Namespaced ID를 조회한다.
     *
     * @param entity 대상 엔티티
     * @return Namespaced ID, 가구가 아니면 {@code null}
     */
    @Nullable
    public static String to(@NotNull Entity entity) {
        if (framework().isEnabledDependency("Nexo")) {
            if (NexoFurniture.isFurniture(entity))
                return "nexo:" + NexoFurniture.furnitureMechanic(entity).getItemID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            if (OraxenFurniture.isFurniture(entity))
                return "oraxen:" + OraxenFurniture.getFurnitureMechanic(entity).getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(entity);
            if (furniture != null) return "itemsadder:" + furniture.getNamespacedID();
        }
        return null;
    }

    /**
     * 블록으로부터 가구 Namespaced ID를 조회한다.
     *
     * @param block 대상 블록
     * @return Namespaced ID, 가구가 아니면 {@code null}
     */
    @Nullable
    public static String to(@NotNull Block block) {
        if (framework().isEnabledDependency("Nexo")) {
            if (NexoFurniture.isFurniture(block.getLocation()))
                return "nexo:" + NexoFurniture.furnitureMechanic(block.getLocation()).getItemID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            if (OraxenFurniture.isFurniture(block))
                return "oraxen:" + OraxenFurniture.getFurnitureMechanic(block).getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(block);
            if (furniture != null) return "itemsadder:" + furniture.getNamespacedID();
        }
        return null;
    }

    /**
     * 위치로부터 가구 Namespaced ID를 조회한다.
     *
     * @param location 대상 위치
     * @return Namespaced ID, 가구가 아니면 {@code null}
     */
    @Nullable
    public static String to(@NotNull Location location) {
        if (framework().isEnabledDependency("Nexo")) {
            if (NexoFurniture.isFurniture(location))
                return "nexo:" + NexoFurniture.furnitureMechanic(location).getItemID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            if (OraxenFurniture.isFurniture(location.getBlock()))
                return "oraxen:"
                        + OraxenFurniture.getFurnitureMechanic(location.getBlock()).getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(location.getBlock());
            if (furniture != null) return "itemsadder:" + furniture.getNamespacedID();
        }
        return null;
    }

    /**
     * 지정한 위치에 가구를 배치한다.
     *
     * @param location 배치 위치
     * @param namespacedID 가구 Namespaced ID
     * @return 배치 성공 여부
     */
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
                    if (CustomFurniture.isInRegistry(block))
                        CustomFurniture.spawn(block, location.getBlock());
                    else return false;
                } else return false;
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * 지정한 위치의 가구를 제거한다.
     *
     * @param location 제거 위치
     * @return 제거 성공 여부
     */
    public static boolean remove(@NotNull Location location) {
        if (framework().isEnabledDependency("Nexo")) {
            if (NexoFurniture.isFurniture(location)) return NexoFurniture.remove(location);
        }
        if (framework().isEnabledDependency("Oraxen")) {
            if (OraxenFurniture.isFurniture(location.getBlock()))
                return OraxenFurniture.remove(location, null);
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(location.getBlock());
            if (furniture == null) return false;
            furniture.remove(false);
            return true;
        }
        return true;
    }
}
