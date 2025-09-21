package kr.rtustudio.framework.bukkit.api.player;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtustudio.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.protoweaver.bukkit.api.BukkitProtoWeaver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.translation.Translator;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerList {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    @Nullable
    public static ProxyPlayer getPlayer(UUID uniqueId) {
        return getPlayers().stream()
                .filter(player -> player.uniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public static Set<ProxyPlayer> getPlayers() {
        return getPlayers(true);
    }

    @NotNull
    public static Set<ProxyPlayer> getPlayers(boolean includeProxy) {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        if (protoWeaver.isConnected() && includeProxy)
            return new HashSet<>(protoWeaver.getPlayers().values());
        Set<ProxyPlayer> players = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Locale locale;
            if (MinecraftVersion.isPaper()) locale = player.locale();
            else
                locale =
                        Objects.requireNonNullElse(
                                Translator.parseLocale(player.getLocale()), Locale.US);
            players.add(new ProxyPlayer(player.getUniqueId(), player.getName(), locale, null));
        }
        return players;
    }
}
