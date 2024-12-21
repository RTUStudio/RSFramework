package kr.rtuserver.framework.bukkit.core.modules;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.RSConfiguration;
import lombok.Getter;

@Getter
public class CommandModule extends RSConfiguration<RSPlugin> implements kr.rtuserver.framework.bukkit.api.core.modules.CommandModule {
    private int executeLimit = 30;

    public CommandModule(RSPlugin plugin) {
        super(plugin, "Modules", "Command.yml", 1);
        setup(this);
    }

    private void init() {
        executeLimit = getInt("execute.limit", executeLimit, """
                RS 플러그인의 명령어 재사용 대기 시간 (틱)""");
    }
}
