package kr.rtuserver.framework.bukkit.api.integration;

public interface Integration {

    boolean isAvailable();

    boolean register();

    boolean unregister();
}
