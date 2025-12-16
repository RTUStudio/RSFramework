package kr.rtustudio.framework.bukkit.api.integration;

public interface Integration {

    boolean isAvailable();

    boolean register();

    boolean unregister();

    interface Wrapper {

        boolean register();

        boolean unregister();
    }
}
