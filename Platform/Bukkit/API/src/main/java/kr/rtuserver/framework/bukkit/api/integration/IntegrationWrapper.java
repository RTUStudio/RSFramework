package kr.rtuserver.framework.bukkit.api.integration;

public interface IntegrationWrapper {

    boolean isAvailable();

    boolean register();

    boolean unregister();

}
