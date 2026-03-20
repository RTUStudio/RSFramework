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
public class CommandModule extends ConfigurationPart
        implements kr.rtustudio.framework.bukkit.api.core.module.CommandModule {
    public Execute execute;

    public int getExecuteLimit() {
        return execute.limit;
    }

    @Getter
    public class Execute extends ConfigurationPart {
        @Comment(
                """
                Minimum interval between command executions per player (ticks, 20 ticks = 1s)
                플레이어별 명령어 실행 최소 간격 (틱, 20틱 = 1초)""")
        private int limit = 30;
    }
}
