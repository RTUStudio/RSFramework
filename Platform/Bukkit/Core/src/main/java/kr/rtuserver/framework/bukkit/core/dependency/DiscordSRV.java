package kr.rtuserver.framework.bukkit.core.dependency;

import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DiscordSRV {

    private final RSPlugin plugin;
    private final github.scarsz.discordsrv.DiscordSRV discordSRV;

    public DiscordSRV(RSPlugin plugin) {
        this.plugin = plugin;
        this.discordSRV = github.scarsz.discordsrv.DiscordSRV.getPlugin();
    }

    @Nullable
    public String getName(UUID uuid) {
        AccountLinkManager linkManager = discordSRV.getAccountLinkManager();
        Guild guild = discordSRV.getMainGuild();
        String id = linkManager.isInCache(uuid) ? linkManager.getDiscordIdFromCache(uuid) : linkManager.getDiscordId(uuid);
        Member member = guild.getMemberById(id);
        if (member == null) return null;
        return member.getEffectiveName();
    }

    @Nullable
    public UUID getUUID(String name) {
        AccountLinkManager linkManager = discordSRV.getAccountLinkManager();
        Guild guild = discordSRV.getMainGuild();
        List<Member> list = guild.getMembersByEffectiveName(name, false);
        if (list.isEmpty()) return null;
        Member member = list.getFirst();
        return linkManager.getUuid(member.getId());
    }

}
