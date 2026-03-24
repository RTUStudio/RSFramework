package kr.rtustudio.framework.bukkit.api.core.provider;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * Framework-wide shared service provider interface. Registered via {@link
 * kr.rtustudio.framework.bukkit.api.RSPlugin#setProvider} and retrieved via {@link
 * kr.rtustudio.framework.bukkit.api.RSPlugin#getProvider}.
 *
 * <p>프레임워크 전역으로 공유되는 서비스 제공자 인터페이스.
 */
public interface Provider {

    /**
     * Returns the plugin that registered this provider, or {@code null}.
     *
     * <p>이 프로바이더를 등록한 플러그인을 반환한다.
     */
    @Nullable
    Plugin getPlugin();
}
