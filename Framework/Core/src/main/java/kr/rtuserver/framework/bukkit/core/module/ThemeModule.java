package kr.rtuserver.framework.bukkit.core.module;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;

@Getter
public class ThemeModule extends RSConfiguration<RSPlugin> implements kr.rtuserver.framework.bukkit.api.core.module.ThemeModule {

    private String gradientStart = "#2979FF";
    private String gradientEnd = "#7C4DFF";
    private String prefix = "『";
    private String suffix = "』";
    private String systemMessage = "<gradient:" + gradientStart + ":" + gradientEnd + ">시스템 메세지</gradient>\n<gray>%servertime_yyyy-MM-dd a h:mm%</gray>";

    public ThemeModule(RSPlugin plugin) {
        super(plugin, "Modules", "Theme.yml", 1);
        setup(this);
    }

    private void init() {
        gradientStart = getString("gradient.start", gradientStart, """
                Start color of gradient
                그라데이션의 시작 색상입니다""");
        gradientEnd = getString("gradient.end", gradientEnd, """
                End color of gradient
                그라데이션의 종료 색상입니다""");
        prefix = getString("plugin.prefix", prefix, """
                Prefix of plugin name
                플러그인 이름 앞에 배치되는 문자입니다""");
        suffix = getString("plugin.suffix", suffix, """
                Suffix of plugin name
                플러그인 이름 뒤에 배치되는 문자입니다""");
        systemMessage = getString("systemMessage", systemMessage, """
                Hover message of system messsage
                시스템 메세제의 호버 메세지입니다""");
    }

}
