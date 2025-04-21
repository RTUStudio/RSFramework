package kr.rtuserver.framework.bukkit.api.core.module;

public interface CommandModule {

    int getExecuteLimit();

    boolean isTabCompletePlayersEnabled();

    TabCompletePlayersType getTabCompletePlayersType();

    String getTabCompletePlayersPrefix();

    enum TabCompletePlayersType {
        DISCORD_SRV
    }

}
