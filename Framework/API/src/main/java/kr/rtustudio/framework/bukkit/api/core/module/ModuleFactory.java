package kr.rtustudio.framework.bukkit.api.core.module;

public interface ModuleFactory {

    <T extends Module> T getModule(Class<T> type);

    <T extends Module> void setModule(T module);

    void reload();
}
