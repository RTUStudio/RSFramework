package kr.rtuserver.framework.bukkit.core.integration.provider;

import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import kr.rtuserver.framework.bukkit.api.integration.IntegrationProvider;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DiscordSRV implements IntegrationProvider {

    private final github.scarsz.discordsrv.DiscordSRV provider;
    @Getter
    private final PlayerIdentifier playerIdentifier;

    public DiscordSRV() {
        this.provider = github.scarsz.discordsrv.DiscordSRV.getPlugin();
        this.playerIdentifier = new PlayerIdentifier();
    }

    public class PlayerIdentifier implements kr.rtuserver.framework.bukkit.api.integration.adapter.PlayerIdentifier {

        @Nullable
        @Override
        public String getName(UUID uuid) {
            AccountLinkManager linkManager = provider.getAccountLinkManager();
            Guild guild = provider.getMainGuild();
            String id = linkManager.isInCache(uuid) ? linkManager.getDiscordIdFromCache(uuid) : linkManager.getDiscordId(uuid);
            Member member = guild.getMemberById(id);
            if (member == null) return null;
            return member.getEffectiveName();
        }

        @Nullable
        @Override
        public UUID getUUID(String name) {
            AccountLinkManager linkManager = provider.getAccountLinkManager();
            Guild guild = provider.getMainGuild();
            List<Member> list = guild.getMembersByEffectiveName(name, false);
            if (list.isEmpty()) return null;
            Member member = list.getFirst();
            return linkManager.getUuid(member.getId());
        }

    }

}
