package kr.rtustudio.framework.bukkit.api.listener;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import lombok.Getter;

import org.bukkit.event.Listener;

/**
 * RSFramework 전용 이벤트 리스너 추상 클래스입니다.
 *
 * <p>플러그인, 프레임워크, 번역, 알림 등의 공통 의존성을 자동으로 주입받습니다. {@link RSPlugin#registerEvent(RSListener)}로 등록하여
 * 사용합니다.
 *
 * @param <T> 소유 플러그인 타입
 */
public abstract class RSListener<T extends RSPlugin> implements Listener {

    @Getter protected final T plugin;
    @Getter protected final Framework framework;
    @Getter protected final MessageTranslation message;
    @Getter protected final CommandTranslation command;
    @Getter protected final Notifier notifier;

    /**
     * 리스너를 생성하고 공통 의존성을 초기화한다.
     *
     * @param plugin 소유 플러그인
     */
    public RSListener(T plugin) {
        this.plugin = plugin;
        this.framework = LightDI.getBean(Framework.class);
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.notifier = Notifier.of(plugin);
    }
}
