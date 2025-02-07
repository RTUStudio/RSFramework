package kr.rtuserver.framework.bukkit.api.nms;

import org.bukkit.Location;

import java.util.List;

public interface NMSBiome {

    String getName(Location location);

    List<String> getList();

    List<String> getTag(String tag);

}
