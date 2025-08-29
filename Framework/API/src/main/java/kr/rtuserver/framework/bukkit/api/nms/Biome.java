package kr.rtuserver.framework.bukkit.api.nms;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;

public interface Biome {

    NamespacedKey getKey(Location location);

    List<NamespacedKey> getList();

    List<NamespacedKey> getTag(NamespacedKey tag);
}
