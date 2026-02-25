package kr.rtustudio.framework.bukkit.core.module;

import kr.rtustudio.configure.ConfigurationPart;
import lombok.Getter;

import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@SuppressWarnings({
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "NotNullFieldNotInitialized",
    "InnerClassMayBeStatic"
})
public class ThemeModule extends ConfigurationPart
        implements kr.rtustudio.framework.bukkit.api.core.module.ThemeModule {
    public Gradient gradient;

    @Comment(
            """
                    Prefix of plugin name
                    플러그인 이름의 접두사""")
    private String prefix = "『";

    @Comment(
            """
                    Suffix of plugin name
                    플러그인 이름의 접미사""")
    private String suffix = "』";

    @Comment(
            """
                    Hover message displayed on system messages
                    시스템 메시지에 표시되는 호버 메시지""")
    private String systemMessage =
            "<gradient:#2979FF:#7C4DFF>시스템 메세지</gradient>\n<gray>%servertime_yyyy-MM-dd a h:mm%</gray>";

    public String getGradientStart() {
        return gradient.getStart();
    }

    public String getGradientEnd() {
        return gradient.getEnd();
    }

    @Getter
    public class Gradient extends ConfigurationPart {
        @Comment(
                """
                        Start color of gradient
                        그라데이션 시작 색상""")
        private String start = "#2979FF";

        @Comment(
                """
                        End color of gradient
                        그라데이션 종료 색상""")
        private String end = "#7C4DFF";
    }
}
