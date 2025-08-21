package kr.rtuserver.framework.bukkit.core.module;

import kr.rtuserver.framework.bukkit.api.configuration.ConfigurationPart;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@SuppressWarnings({"CanBeFinal", "FieldCanBeLocal", "FieldMayBeFinal", "NotNullFieldNotInitialized", "InnerClassMayBeStatic"})
public class ThemeModule extends ConfigurationPart implements kr.rtuserver.framework.bukkit.api.core.module.ThemeModule {

    public Gradient gradient;
    @Comment("""
            Prefix of plugin name
            플러그인 이름 앞에 배치되는 문자입니다""")
    private String prefix = "『";
    @Comment("""
            Suffix of plugin name
            플러그인 이름 뒤에 배치되는 문자입니다""")
    private String suffix = "』";
    @Comment("""
            Hover message of system message
            시스템 메세제의 호버 메세지입니다""")
    private String systemMessage = "<gradient:#2979FF:#7C4DFF>시스템 메세지</gradient>\n<gray>%servertime_yyyy-MM-dd a h:mm%</gray>";

    public String getGradientStart() {
        return gradient.getStart();
    }

    public String getGradientEnd() {
        return gradient.getEnd();
    }

    @Getter
    public class Gradient extends ConfigurationPart {

        @Comment("""
                Start color of gradient
                그라데이션의 시작 색상입니다
                """)
        private String start = "#2979FF";

        @Comment("""
                End color of gradient
                그라데이션의 종료 색상입니다
                """)
        private String end = "#7C4DFF";

    }

}
