package kr.rtustudio.framework.bukkit.api.core.module;

public interface Modules {

    CommandModule getCommand();

    ThemeModule getTheme();

    void reload();
}
