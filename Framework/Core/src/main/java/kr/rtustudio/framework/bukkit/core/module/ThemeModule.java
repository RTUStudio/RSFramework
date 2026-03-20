package kr.rtustudio.framework.bukkit.core.module;

import kr.rtustudio.configurate.model.ConfigurationPart;
import lombok.Getter;

import org.spongepowered.configurate.objectmapping.meta.Comment;

@Getter
@SuppressWarnings({
    "unused",
    "CanBeFinal",
    "FieldCanBeLocal",
    "FieldMayBeFinal",
    "InnerClassMayBeStatic"
})
public class ThemeModule extends ConfigurationPart
        implements kr.rtustudio.framework.bukkit.api.core.module.ThemeModule {
    public Gradient gradient;

    @Comment(
            """
            Prefix character wrapping the plugin name in messages
            메시지에서 플러그인 이름을 감싸는 접두 문자""")
    private String prefix = "『";

    @Comment(
            """
            Suffix character wrapping the plugin name in messages
            메시지에서 플러그인 이름을 감싸는 접미 문자""")
    private String suffix = "』";

    @Comment(
            """
            Hover tooltip displayed when hovering over system messages (MiniMessage format)
            시스템 메시지 위에 마우스를 올렸을 때 표시되는 툴팁 (MiniMessage 형식)""")
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
                Start color of the plugin name gradient (hex)
                플러그인 이름 그라데이션 시작 색상 (16진수)""")
        private String start = "#2979FF";

        @Comment(
                """
                End color of the plugin name gradient (hex)
                플러그인 이름 그라데이션 종료 색상 (16진수)""")
        private String end = "#7C4DFF";
    }
}
