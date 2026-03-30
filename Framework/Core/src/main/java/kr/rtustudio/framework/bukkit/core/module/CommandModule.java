package kr.rtustudio.framework.bukkit.core.module;

import kr.rtustudio.configurate.model.ConfigurationPart;
import lombok.Getter;
import net.kyori.adventure.key.Key;

import java.util.List;

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
        implements kr.rtustudio.framework.bukkit.api.core.module.CommandModule {
    public Execute execute;
    public SoundFeedback soundFeedback;

    public int getExecuteLimit() {
        return execute.limit;
    }

    @Override
    public List<FixedSound> getSuccessSounds() {
        if (!soundFeedback.enabled) return List.of();
        return soundFeedback.success;
    }

    @Override
    public List<FixedSound> getFailureSounds() {
        if (!soundFeedback.enabled) return List.of();
        return soundFeedback.failure;
    }

    @Override
    public List<RandomSound> getTabCompleteSounds() {
        if (!soundFeedback.enabled) return List.of();
        return soundFeedback.tabComplete;
    }

    @Getter
    public class Execute extends ConfigurationPart {
        @Comment(
                """
                        Command cooldown (tick)
                        명령어 재사용 대기 시간 (틱)""")
        private int limit = 30;
    }

    @Getter
    public class SoundFeedback extends ConfigurationPart {
        @Comment(
                """
                        Enable sound feedback on command actions
                        명령어 동작 시 사운드 피드백 활성화""")
        private boolean enabled = true;

        @Comment(
                """
                        Sounds to play on command success
                        명령어 성공 시 재생할 사운드 목록""")
        private List<FixedSound> success =
                List.of(
                        new FixedSound(
                                Key.key("minecraft", "block.amethyst_cluster.break"), 0.77f, 1.65f),
                        new FixedSound(
                                Key.key("minecraft", "block.respawn_anchor.charge"),
                                0.125f,
                                2.99f));

        @Comment(
                """
                        Sounds to play on command failure
                        명령어 실패 시 재생할 사운드 목록""")
        private List<FixedSound> failure =
                List.of(
                        new FixedSound(
                                Key.key("minecraft", "block.respawn_anchor.deplete"), 0.77f, 0.25f),
                        new FixedSound(
                                Key.key("minecraft", "block.beacon.deactivate"), 0.2f, 0.45f));

        @Comment(
                """
                        Sounds to play on command tab completion (Supports random pitch/volume)
                        명령어 탭 완성 시 재생할 사운드 목록 (랜덤 피치 및 볼륨 지원)""")
        private List<RandomSound> tabComplete =
                List.of(
                        new RandomSound(
                                Key.key("minecraft", "block.amethyst_block.chime"),
                                0.25f,
                                0.25f,
                                0.125f,
                                1.95f));
    }
}
