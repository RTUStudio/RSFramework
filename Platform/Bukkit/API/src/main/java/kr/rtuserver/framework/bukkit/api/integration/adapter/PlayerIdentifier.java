package kr.rtuserver.framework.bukkit.api.integration.adapter;

import java.util.UUID;

public interface PlayerIdentifier {

    String getName(UUID uuid);

    UUID getUUID(String name);

}
