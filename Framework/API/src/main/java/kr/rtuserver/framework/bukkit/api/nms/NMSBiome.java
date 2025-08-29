package kr.rtuserver.framework.bukkit.api.nms;

import java.util.List;

import org.bukkit.Location;

public interface NMSBiome {

    String getName(Location location);

    List<String> getList();

    List<String> getTag(String tag);
}
