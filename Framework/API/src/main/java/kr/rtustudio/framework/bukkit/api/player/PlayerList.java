package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.proxy.ProxyPlayer;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.translation.Translator;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 프록시 및 로컬 서버의 온라인 플레이어 목록을 조회하는 유틸리티 클래스입니다.
 *
 * <p>Proxium 연결 시 프록시 전체 플레이어를, 미연결 시 현재 서버의 로컬 플레이어만 반환합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerList {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    /**
     * UUID로 플레이어를 조회한다.
     *
     * <p>Proxium 연결 시 프록시 플레이어 맵에서 직접 조회하며, 미연결 시 로컬 서버의 온라인 플레이어에서 검색합니다.
     *
     * @param uniqueId 플레이어 UUID
     * @return 프록시 플레이어, 없으면 {@code null}
     */
    @Nullable
    public static ProxyPlayer getPlayer(UUID uniqueId) {
        Proxium proxium = framework().getBridge(Proxium.class);
        if (proxium.isConnected()) {
            ProxyPlayer player = proxium.getPlayers().get(uniqueId);
            if (player != null) return player;
        }
        Player local = Bukkit.getPlayer(uniqueId);
        return local != null ? toProxyPlayer(local) : null;
    }

    /**
     * 프록시 포함 전체 온라인 플레이어 목록을 반환한다.
     *
     * @return 플레이어 집합
     */
    @NotNull
    public static Set<ProxyPlayer> getPlayers() {
        return getPlayers(true);
    }

    /**
     * 온라인 플레이어 목록을 반환한다.
     *
     * @param includeProxy {@code true}이면 프록시 전체 플레이어, {@code false}이면 로컬 서버만
     * @return 플레이어 집합
     */
    @NotNull
    public static Set<ProxyPlayer> getPlayers(boolean includeProxy) {
        Proxium proxium = framework().getBridge(Proxium.class);
        if (proxium.isConnected() && includeProxy)
            return new HashSet<>(proxium.getPlayers().values());
        return getLocalPlayers();
    }

    /**
     * 현재 서버의 온라인 플레이어를 {@link ProxyPlayer}로 변환하여 반환한다.
     *
     * @return 로컬 플레이어 집합
     */
    @NotNull
    public static Set<ProxyPlayer> getLocalPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(PlayerList::toProxyPlayer)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Bukkit {@link Player}를 {@link ProxyPlayer}로 변환한다.
     *
     * @param player 변환할 플레이어
     * @return 프록시 플레이어
     */
    @NotNull
    public static ProxyPlayer toProxyPlayer(@NotNull Player player) {
        Locale locale;
        if (MinecraftVersion.isPaper()) locale = player.locale();
        else
            locale =
                    Objects.requireNonNullElse(
                            Translator.parseLocale(player.getLocale()), Locale.US);
        return new ProxyPlayer(player.getUniqueId(), player.getName(), locale, null);
    }
}
