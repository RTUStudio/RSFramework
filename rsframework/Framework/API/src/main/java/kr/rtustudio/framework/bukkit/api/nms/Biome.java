package kr.rtustudio.framework.bukkit.api.nms;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;

/**
 * NMS interface for biome-related operations.
 *
 * <p>바이옴 관련 NMS 인터페이스.
 */
public interface Biome {

    NamespacedKey getKey(Location location);

    List<NamespacedKey> getList();

    List<NamespacedKey> getTag(NamespacedKey tag);
}
