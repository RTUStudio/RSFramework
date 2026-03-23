package kr.rtustudio.framework.bukkit.api.registry;

import dev.lone.itemsadder.api.CustomBlock;
import io.th0rgal.oraxen.api.OraxenBlocks;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.nexomc.nexo.api.NexoBlocks;

/**
 * Nexo, Oraxen, ItemsAdder 등 커스텀 블록 플러그인과 바닐라 블록을 통합 처리하는 유틸리티 클래스입니다.
 *
 * <p>Namespaced ID 형식({@code nexo:block_id}, {@code oraxen:block_id} 등)으로 블록을 조회·배치합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomBlocks {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    /**
     * Namespaced ID로 블록 데이터를 조회한다.
     *
     * @param namespacedID {@code nexo:id}, {@code oraxen:id}, {@code itemsadder:ns:id} 또는 바닐라 아이디
     * @return 블록 데이터, 없으면 {@code null}
     */
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

    /**
     * 배치된 블록을 Namespaced ID 문자열로 변환한다.
     *
     * @param block 대상 블록
     * @return Namespaced ID 문자열
     */
    @NotNull
    public static String to(@NotNull Block block) {
        if (framework().isEnabledDependency("Nexo")) {
            if (NexoBlocks.isCustomBlock(block))
                return "nexo:" + NexoBlocks.chorusBlockMechanic(block).getItemID();
        }
        if (framework().isEnabledDependency("Oraxen")) {
            if (OraxenBlocks.isOraxenBlock(block))
                return "oraxen:" + OraxenBlocks.getBlockMechanic(block).getItemID();
        }
        if (framework().isEnabledDependency("ItemsAdder")) {
            CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
            if (customBlock != null) return "itemsadder:" + customBlock.getNamespacedID();
        }
        return block.getBlockData().getMaterial().getKey().toString();
    }

    /**
     * 지정한 위치에 블록을 배치한다.
     *
     * @param location 배치 위치
     * @param namespacedID 블록 Namespaced ID
     * @return 배치 성공 여부
     */
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
                    if (OraxenBlocks.isOraxenBlock(split[1]))
                        OraxenBlocks.place(split[1], location);
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
                if (material != null)
                    location.getWorld().setBlockData(location, material.createBlockData());
                else return false;
                return true;
            }
        }
    }
}
