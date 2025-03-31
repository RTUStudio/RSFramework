package kr.rtuserver.framework.bukkit.api.core.module;

public interface Modules {

    CommandModule getCommandModule();

    ThemeModule getThemeModule();

    void reload();
}
