package kr.rtustudio.framework.bukkit.api.core.provider;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * 프레임워크 전역으로 공유되는 서비스 제공자 인터페이스입니다.
 *
 * <p>{@link kr.rtustudio.framework.bukkit.api.RSPlugin#setProvider}로 등록하고 {@link
 * kr.rtustudio.framework.bukkit.api.RSPlugin#getProvider}로 조회합니다.
 */
public interface Provider {

    /** 이 프로바이더를 등록한 플러그인을 반환한다. 없으면 {@code null}. */
    @Nullable
    Plugin getPlugin();
}
