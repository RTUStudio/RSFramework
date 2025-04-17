package kr.rtuserver.framework.bukkit.core.module;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
public class CommandModule extends RSConfiguration<RSPlugin> implements kr.rtuserver.framework.bukkit.api.core.module.CommandModule {

    private int executeLimit = 30;
    private boolean tabCompletePlayersEnabled = false;
    private TabCompletePlayersType tabCompletePlayersType = TabCompletePlayersType.DISCORD_SRV;
    private String tabCompletePlayersPrefix = "@";

    public CommandModule(RSPlugin plugin) {
        super(plugin, "Modules", "Command.yml", 1);
        setup(this);
    }

    private void init() {
        executeLimit = getInt("execute.limit", executeLimit, """
                Command cooldown (tick)
                명령어 재사용 대기 시간 (틱)""");
        tabCompletePlayersEnabled = getBoolean("tabComplete.players.enable", tabCompletePlayersEnabled, """
                Enable custom player name suggestion
                커스텀 플레이어 이름 제안""");
        tabCompletePlayersType = TabCompletePlayersType.valueOf(getString("tabComplete.players.type", "DISCORD_SRV", """
                Entry type. Available options: DISCORD_SRV
                항목 타입. 사용 가능한 포멧: DISCORD_SRV"""));
        tabCompletePlayersPrefix = getString("tabComplete.players.prefix", tabCompletePlayersPrefix, """
                Prefix of player name. Prfix must not be empty
                플레이어 이름 앞에 배치되는 문자입니다. 공백은 허용되지 않습니다""");
    }
}
