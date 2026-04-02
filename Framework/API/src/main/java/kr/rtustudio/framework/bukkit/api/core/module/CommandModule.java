package kr.rtustudio.framework.bukkit.api.core.module;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Module that provides command execution cooldown and sound feedback configuration.
 *
 * <p>명령어 실행 제한(쿨다운) 및 성공/실패 시 사운드 피드백 설정을 제공하는 모듈.
 */
public interface CommandModule extends Module {

    interface CommandSound {
        void play(Audience audience);
    }

    @ConfigSerializable
    record FixedSound(Key sound, float volume, float pitch) implements CommandSound {
        @Override
        public void play(Audience audience) {
            audience.playSound(Sound.sound(sound, Sound.Source.MASTER, volume, pitch));
        }
    }

    @ConfigSerializable
    record RandomSound(Key sound, float minVolume, float maxVolume, float minPitch, float maxPitch)
            implements CommandSound {
        private float random(float min, float max) {
            return min + (ThreadLocalRandom.current().nextFloat() * (max - min));
        }

        @Override
        public void play(Audience audience) {
            float v = minVolume == maxVolume ? minVolume : random(minVolume, maxVolume);
            float p = minPitch == maxPitch ? minPitch : random(minPitch, maxPitch);
            audience.playSound(Sound.sound(sound, Sound.Source.MASTER, v, p));
        }
    }

    /**
     * Returns the command execution cooldown in ticks. 0 or less disables cooldown.
     *
     * <p>명령어 실행 쿨다운 틱 수를 반환한다. 0 이하면 쿨다운 비활성화.
     */
    int getExecuteLimit();

    /**
     * Returns the list of sounds to play on command success, or empty list if disabled.
     *
     * <p>성공 시 재생할 사운드 목록을 반환한다. 피드백이 비활성화되면 빈 리스트를 반환한다.
     */
    List<FixedSound> getSuccessSounds();

    /**
     * Returns the list of sounds to play on command failure, or empty list if disabled.
     *
     * <p>실패 시 재생할 사운드 목록을 반환한다. 피드백이 비활성화되면 빈 리스트를 반환한다.
     */
    List<FixedSound> getFailureSounds();

    /**
     * Returns the list of sounds for tab completion. Pitch and volume may be randomized per call.
     *
     * <p>명령어 탭 완성 시 재생할 사운드 목록을 반환한다. 호출될 때마다 무작위 피치/볼륨이 적용된다.
     */
    List<RandomSound> getTabCompleteSounds();
}
