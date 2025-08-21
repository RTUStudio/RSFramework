package kr.rtuserver.framework.bukkit.api.command;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.Translation;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtuserver.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.core.module.ThemeModule;
import kr.rtuserver.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtuserver.framework.bukkit.api.player.PlayerChat;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@ToString(exclude = "commands")
public abstract class RSCommand<T extends RSPlugin> extends Command {

    private final Map<String, RSCommand<? extends RSPlugin>> commands = new HashMap<>();

    @Getter
    private final T plugin;

    @Getter
    private final PermissionDefault permissionDefault;

    @Getter
    private final List<String> names;

    private final MessageTranslation message;
    private final CommandTranslation command;
    private final Framework framework = LightDI.getBean(Framework.class);
    private final PlayerChat chat;
    private CommandSender sender;
    private Audience audience;

    private RSCommand<? extends RSPlugin> parent = null;
    private int index = 0;

    public RSCommand(T plugin, @NotNull String name) {
        this(plugin, List.of(name), PermissionDefault.TRUE);
    }

    public RSCommand(T plugin, @NotNull List<String> names) {
        this(plugin, names, PermissionDefault.TRUE);
    }

    public RSCommand(T plugin, @NotNull String name, PermissionDefault permission) {
        this(plugin, List.of(name), permission);
    }

    public RSCommand(T plugin, List<String> names, PermissionDefault permission) {
        super(names.getFirst());
        this.names = Collections.unmodifiableList(names);
        this.plugin = plugin;
        this.permissionDefault = permission;
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.chat = PlayerChat.of(plugin);
        super.setPermission(plugin.getName().toLowerCase() + ".command." + getName());
        List<String> aliases = new ArrayList<>(names.subList(1, names.size()));
        for (Translation translation : command.getMap().values()) {
            String name = translation.get(getName() + ".name");
            if (name.isEmpty() || name.equals(getName())) continue;
            aliases.add(name);
        }
        super.setAliases(aliases);
    }

    private void setParent(RSCommand<? extends RSPlugin> parent) {
        this.parent = parent;
        this.index++;
    }

    protected String getQualifiedName() {
        return parent == null ? getName() : parent.getQualifiedName() + "." + getName();
    }

    protected TranslationConfiguration message() {
        return message;
    }

    protected TranslationConfiguration command() {
        return command;
    }

    protected Framework framework() {
        return framework;
    }

    protected NameProvider provider() {
        return framework.getProviders().getName();
    }

    protected PlayerChat chat() {
        return chat;
    }

    protected CommandSender sender() {
        return sender;
    }

    protected Audience audience() {
        return audience;
    }

    protected Player player() {
        if (sender instanceof Player player) return player;
        return null;
    }

    public boolean isOp() {
        return sender.isOp();
    }

    public boolean hasPermission(String permission) {
        return plugin.hasPermission(sender, permission);
    }

    private boolean hasCommandPermission(String node) {
        if (node == null) return false;
        return sender.hasPermission(node);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        chat.setReceiver(sender);
        if (parent == null && (sender instanceof Player player)) {
            Map<UUID, Integer> cooldownMap = framework.getCommandLimit().getExecuteLimit();
            int cooldown = framework.getModules().getCommand().getExecuteLimit();
            if (cooldown > 0) {
                if (cooldownMap.containsKey(player.getUniqueId())) {
                    chat.announce(message.getCommon(player, "error.cooldown"));
                    return true;
                } else cooldownMap.put(player.getUniqueId(), cooldown);
            }
        }
        this.sender = sender;
        this.audience = plugin.getAdventure().sender(sender);
        RSCommandData data = new RSCommandData(args);
        RSCommand<? extends RSPlugin> sub = findCommand(data.args(index));
        if (sub == null) {
            if (hasCommandPermission(getPermission())) {
                if (!execute(data)) wrongUsage();
            } else chat.announce(message.getCommon(player(), "noPermission"));
        } else {
            if (sub.getName().equalsIgnoreCase("reload")) reload(data);
            if (hasCommandPermission(sub.getPermission())) {
                if (!sub.execute(sender, commandLabel, args)) sub.wrongUsage();
            } else chat.announce(message.getCommon(player(), "noPermission"));
        }
        return true;
    }

    private void wrongUsage() {
        chat.announce(message.getCommon(player(), "wrongUsage"));
        ThemeModule module = framework.getModules().getTheme();
        String startGradient = module.getGradientStart();
        String endGradient = module.getGradientEnd();
        List<RSCommand<? extends RSPlugin>> list = new ArrayList<>(commands.values());
        if (list.isEmpty()) return;
        boolean empty = true;
        StringBuilder builder = new StringBuilder("<gradient:" + startGradient + ":" + endGradient + ">");
        for (int i = 0; i < list.size(); i++) {
            RSCommand<? extends RSPlugin> cmd = list.get(i);
            String permission = cmd.getPermission();
            if (permission == null) continue;
            if (!sender.hasPermission(permission)) continue;
            String usage = cmd.getLocalizedUsage(player());
            if (usage.isEmpty()) usage = "/" + cmd.getLocalizedCommand(player());
            if (i > 0) builder.append("\n");
            String description = cmd.getLocalizedDescription(player());
            builder.append(" ⏵ <white>").append(usage).append("</white>");
            if (!description.isEmpty())
                builder.append("\n    ┗ ").append("<gray>").append(description).append("</gray>");
            empty = false;
        }
        builder.append("</gradient>");
        if (!empty) chat.send(builder.toString());
    }

    public void registerCommand(RSCommand<? extends RSPlugin> command) {
        command.setParent(this);
        String permission = "command." + command.getQualifiedName();
        command.setPermission(plugin.getName().toLowerCase() + "." + permission);
        plugin.registerPermission(permission, command.getPermissionDefault());
        commands.put(command.getName(), command);
    }

    private RSCommand<? extends RSPlugin> findCommand(String name) {
        if (name.isEmpty()) return null;
        for (RSCommand<? extends RSPlugin> sub : commands.values()) {
            if (sub.getLocalizedName(player()).equals(name)) return sub;
        }
        return null;
    }

    private String getTranslationKey() {
        return parent == null ? getName() : parent.getName() + ".commands." + getName();
    }

    protected String getLocalizedName(Player player) {
        return command.get(player, getTranslationKey() + ".name");
    }

    protected String getLocalizedDescription(Player player) {
        return command.get(player, getDescription());
    }

    protected String getLocalizedUsage(Player player) {
        return command.get(player, getUsage());
    }

    private String getLocalizedCommand(Player player) {
        if (parent == null) {
            String name = getLocalizedName(player);
            return name.isEmpty() ? getName() : name;
        } else return parent.getLocalizedCommand(player) + " " + getLocalizedName(player);
    }

    @NotNull
    @Override
    public String getDescription() {
        return getTranslationKey() + ".description";
    }

    @NotNull
    @Override
    public String getUsage() {
        return getTranslationKey() + ".usage";
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        chat.setReceiver(sender);
        this.sender = sender;
        this.audience = plugin.getAdventure().sender(sender);
        RSCommandData data = new RSCommandData(args);
        List<String> list = new ArrayList<>();
        if (data.length(index + 1)) for (RSCommand<? extends RSPlugin> cmd : commands.values()) {
            if (hasCommandPermission(cmd.getPermission())) list.add(cmd.getLocalizedName(player()));
        }
        RSCommand<? extends RSPlugin> sub = findCommand(data.args(0));
        if (sub == null) {
            if (hasCommandPermission(getPermission())) list.addAll(tabComplete(data));
        } else if (hasCommandPermission(sub.getPermission())) {
            if (data.length() > sub.index) list.addAll(sub.tabComplete(sender, alias, args));
        }
        return list;
    }

    protected boolean execute(RSCommandData data) {
        return false;
    }

    protected List<String> tabComplete(RSCommandData data) {
        return List.of();
    }

    protected void reload(RSCommandData data) {
    }

}
