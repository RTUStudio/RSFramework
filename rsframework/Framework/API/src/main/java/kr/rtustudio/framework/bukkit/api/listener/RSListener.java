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
 * Automatically injects common dependencies such as plugin, framework, translations, and notifier.
 * Register with {@link RSPlugin#registerEvent(RSListener)}.
 *
 * <p>RSFramework 전용 이벤트 리스너 추상 클래스. 플러그인, 프레임워크, 번역, 알림 등의 공통 의존성을 자동으로 주입받는다. {@link
 * RSPlugin#registerEvent(RSListener)}로 등록하여 사용한다.
 *
 * @param <T> owning plugin type
 */
public abstract class RSListener<T extends RSPlugin> implements Listener {

    @Getter protected final T plugin;
    @Getter protected final Framework framework;
    @Getter protected final MessageTranslation message;
    @Getter protected final CommandTranslation command;
    @Getter protected final Notifier notifier;

    /**
     * Creates a listener and initializes common dependencies.
     *
     * <p>리스너를 생성하고 공통 의존성을 초기화한다.
     *
     * @param plugin owning plugin
     */
    public RSListener(T plugin) {
        this.plugin = plugin;
        this.framework = LightDI.getBean(Framework.class);
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.notifier = Notifier.of(plugin);
    }
}
