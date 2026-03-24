package kr.rtustudio.framework.bukkit.api.nms;

import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * NMS interface for custom item lookup and tag retrieval.
 *
 * <p>커스텀 아이템 조회 및 태그 검색 NMS 인터페이스.
 */
public interface Item {

    @Nullable
    ItemStack getItem(NamespacedKey key);

    List<NamespacedKey> getTag(NamespacedKey tag);
}
