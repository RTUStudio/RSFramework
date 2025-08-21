package kr.rtuserver.framework.bukkit.api.player;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import kr.rtuserver.protoweaver.bukkit.api.BukkitProtoWeaver;
import lombok.Getter;
import net.kyori.adventure.translation.Translator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class PlayerList {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    @Nullable
    public static ProxyPlayer getPlayer(UUID uniqueId) {
        return getPlayers().stream().filter(player -> player.uniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    @NotNull
    public static Set<ProxyPlayer> getPlayers() {
        return getPlayers(true);
    }

    @NotNull
    public static Set<ProxyPlayer> getPlayers(boolean includeProxy) {
        BukkitProtoWeaver protoWeaver = framework().getProtoWeaver();
        if (protoWeaver.isConnected() && includeProxy) return new HashSet<>(protoWeaver.getPlayers().values());
        Set<ProxyPlayer> players = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Locale locale;
            if (MinecraftVersion.isPaper()) locale = player.locale();
            else locale = Objects.requireNonNullElse(Translator.parseLocale(player.getLocale()), Locale.US);
            players.add(new ProxyPlayer(player.getUniqueId(), player.getName(), locale, null));
        }
        return players;
    }
}