package kr.rtuserver.framework.bukkit.core.module;

import kr.rtuserver.framework.bukkit.api.configuration.ConfigurationPart;
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
public class CommandModule extends ConfigurationPart
        implements kr.rtuserver.framework.bukkit.api.core.module.CommandModule {

    public Execute execute;

    public int getExecuteLimit() {
        return execute.limit;
    }

    @Getter
    public class Execute extends ConfigurationPart {

        @Comment(
                """
                        Command cooldown (tick)
                        명령어 재사용 대기 시간 (틱)
                        """)
        private int limit = 30;
    }
}
