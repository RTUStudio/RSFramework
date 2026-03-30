package kr.rtustudio.framework.bukkit.api.command;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.Translation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.core.module.CommandModule;
import kr.rtustudio.framework.bukkit.api.core.module.ThemeModule;
import kr.rtustudio.framework.bukkit.api.core.provider.name.NameProvider;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.kyori.adventure.audience.Audience;

import java.util.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

/**
 * Provides sub-command tree, permission checks, cooldown, auto tab-completion, and i18n support.
 * Subclasses override {@link #execute(CommandArgs)} and {@link #tabComplete(CommandArgs)} to
 * implement behavior.
 *
 * <p>RSFramework 전용 명령어 추상 클래스. 서브 커맨드 구조, 권한 체크, 쿨다운, 자동 탭 완성 및 다국어 지원 등을 포함한다. 하위 클래스에서 {@link
 * #execute(CommandArgs)}와 {@link #tabComplete(CommandArgs)}를 오버라이드하여 실제 동작을 구현한다.
 */
@ToString(exclude = "commands")
public abstract class RSCommand<T extends RSPlugin> extends Command {

    @Getter protected final T plugin;
    @Getter protected final Framework framework;
    @Getter protected final MessageTranslation message;
    @Getter protected final CommandTranslation command;
    @Getter protected final Notifier notifier;
    private final Map<String, RSCommand<? extends RSPlugin>> commands = new HashMap<>();
    @Getter private final PermissionDefault permissionDefault;

    @Getter private final List<String> names;

    @Getter private CommandSender sender;
    @Getter private Audience audience;

    private RSCommand<? extends RSPlugin> parent = null;
    private int index = 0;

    /**
     * Creates a command with a single name. Default permission is {@link PermissionDefault#TRUE}.
     *
     * <p>단일 이름으로 명령어를 생성한다. 기본 권한은 {@link PermissionDefault#TRUE}.
     *
     * @param plugin owning plugin
     * @param key command name
     */
    public RSCommand(T plugin, @NotNull String key) {
        this(plugin, List.of(key), PermissionDefault.TRUE);
    }

    /**
     * Creates a command with a name list (including aliases). Default permission is {@link
     * PermissionDefault#TRUE}.
     *
     * <p>이름 목록(별칭 포함)으로 명령어를 생성한다. 기본 권한은 {@link PermissionDefault#TRUE}.
     *
     * @param plugin owning plugin
     * @param keys command name list (first is primary, rest are aliases)
     */
    public RSCommand(T plugin, @NotNull List<String> keys) {
        this(plugin, keys, PermissionDefault.TRUE);
    }

    /**
     * Creates a command with a single name and permission level.
     *
     * <p>단일 이름과 권한 수준으로 명령어를 생성한다.
     *
     * @param plugin owning plugin
     * @param key command name
     * @param permission default permission level
     */
    public RSCommand(T plugin, @NotNull String key, PermissionDefault permission) {
        this(plugin, List.of(key), permission);
    }

    /**
     * Creates a command with a name list and permission level. Localized names from translation
     * files are automatically added as aliases.
     *
     * <p>이름 목록과 권한 수준으로 명령어를 생성한다. 번역 파일에 정의된 로컬라이즈 이름이 자동으로 별칭에 추가된다.
     *
     * @param plugin owning plugin
     * @param keys command name list (first is primary)
     * @param permission default permission level
     */
    public RSCommand(T plugin, List<String> keys, PermissionDefault permission) {
        super(keys.getFirst());
        this.names = Collections.unmodifiableList(keys);
        this.plugin = plugin;
        this.permissionDefault = permission;
        this.framework = LightDI.getBean(Framework.class);
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.notifier = Notifier.of(plugin);
        super.setPermission(plugin.getName().toLowerCase() + ".command." + getName());
        List<String> aliases = new ArrayList<>(names.subList(1, names.size()));
        for (Translation translation : command.getTranslations().values()) {
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

    /**
     * Returns the fully qualified path including parent commands (e.g. {@code "admin.reload"}).
     *
     * <p>부모 명령어를 포함한 전체 경로명을 반환한다.
     */
    protected String getQualifiedName() {
        return this.parent == null ? getName() : this.parent.getQualifiedName() + "." + getName();
    }

    /**
     * Returns the {@link NameProvider}.
     *
     * <p>{@link NameProvider}를 조회하여 반환한다.
     */
    protected NameProvider provider() {
        return this.framework.getProvider(NameProvider.class);
    }

    /**
     * Returns the sender as a {@link Player}, or {@code null} if not a player.
     *
     * <p>명령어 발신자가 플레이어이면 반환하고, 아니면 {@code null}을 반환한다.
     */
    protected Player player() {
        if (this.sender instanceof Player player) return player;
        return null;
    }

    /**
     * Checks whether the sender is OP.
     *
     * <p>명령어 발신자가 OP인지 확인한다.
     */
    public boolean isOp() {
        return this.sender.isOp();
    }

    /**
     * Checks whether the sender has the specified permission.
     *
     * <p>명령어 발신자가 지정한 권한을 가지고 있는지 확인한다.
     *
     * @param permission permission suffix
     * @return whether the sender has the permission
     */
    public boolean hasPermission(String permission) {
        return this.plugin.hasPermission(this.sender, permission);
    }

    private boolean hasPermissionNode(String node) {
        if (node == null) return false;
        return this.sender.hasPermission(node) || this.sender instanceof ConsoleCommandSender;
    }

    @Override
    public boolean execute(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        this.notifier.setReceiver(sender);
        if (this.parent == null && sender instanceof Player player) {
            if (checkCooldown(player)) return true;
        }
        this.sender = sender;
        this.audience = this.plugin.getAdventure().sender(sender);
        CommandArgs data = new CommandArgs(args);
        RSCommand<? extends RSPlugin> sub = findCommand(data.get(this.index));
        if (sub == null) {
            if (!hasPermissionNode(getPermission())) {
                handleResult(Result.NO_PERMISSION);
                return true;
            }
            handleResult(execute(data));
        } else {
            if (!hasPermissionNode(sub.getPermission())) {
                handleResult(Result.NO_PERMISSION);
                return true;
            }
            if (sub.getName().equalsIgnoreCase("reload")) this.reload(data);
            if (!sub.execute(sender, commandLabel, args)) sub.showUsage();
        }
        return true;
    }

    private void announceCommon(String key) {
        this.notifier.announce(this.message.getCommon(player(), key));
    }

    private boolean checkCooldown(Player player) {
        Map<UUID, Integer> cooldownMap = this.framework.getCommandLimit().getExecuteLimit();
        int cooldown = this.framework.getModule(CommandModule.class).getExecuteLimit();
        if (cooldown <= 0) return false;
        if (cooldownMap.containsKey(player.getUniqueId())) {
            announceCommon(MessageTranslation.ERROR_COOLDOWN);
            return true;
        }
        cooldownMap.put(player.getUniqueId(), cooldown);
        return false;
    }

    private void handleResult(Result result) {
        String key =
                switch (result) {
                    case ONLY_PLAYER -> MessageTranslation.ONLY_PLAYER;
                    case ONLY_CONSOLE -> MessageTranslation.ONLY_CONSOLE;
                    case NOT_FOUND_ONLINE_PLAYER -> MessageTranslation.NOT_FOUND_ONLINE_PLAYER;
                    case NOT_FOUND_OFFLINE_PLAYER -> MessageTranslation.NOT_FOUND_OFFLINE_PLAYER;
                    case NOT_FOUND_ITEM -> MessageTranslation.NOT_FOUND_ITEM;
                    case NO_PERMISSION -> MessageTranslation.NO_PERMISSION;
                    default -> null;
                };
        if (result == Result.WRONG_USAGE) showUsage();
        else if (key != null) announceCommon(key);
    }

    private void showUsage() {
        announceCommon(MessageTranslation.WRONG_USAGE);
        ThemeModule module = this.framework.getModule(ThemeModule.class);
        String startGradient = module.getGradientStart();
        String endGradient = module.getGradientEnd();

        List<RSCommand<? extends RSPlugin>> list = new ArrayList<>(this.commands.values());
        if (list.isEmpty()) return;

        StringBuilder builder =
                new StringBuilder("<gradient:" + startGradient + ":" + endGradient + ">");
        boolean empty = true;

        for (int i = 0; i < list.size(); i++) {
            RSCommand<? extends RSPlugin> cmd = list.get(i);
            String permission = cmd.getPermission();
            if (permission != null && hasPermissionNode(permission)) {
                String usage = cmd.getLocalizedUsage(player());
                if (usage.isEmpty()) usage = "/" + cmd.getLocalizedCommand(player());

                if (!empty) builder.append("\n");

                String description = cmd.getLocalizedDescription(player());
                builder.append(" ⏵ <white>").append(usage).append("</white>");
                if (!description.isEmpty()) {
                    builder.append("\n    ┗ ")
                            .append("<gray>")
                            .append(description)
                            .append("</gray>");
                }
                empty = false;
            }
        }
        builder.append("</gradient>");

        if (!empty) this.notifier.send(builder.toString());
    }

    /**
     * Registers a sub-command. A permission node is automatically generated and registered.
     *
     * <p>서브 커맨드를 등록한다. 권한 노드가 자동으로 생성되어 서버에 등록된다.
     *
     * @param command sub-command to register
     */
    public void registerCommand(RSCommand<? extends RSPlugin> command) {
        command.setParent(this);
        String permission = "command." + command.getQualifiedName();
        command.setPermission(this.plugin.getName().toLowerCase() + "." + permission);
        this.plugin.registerPermission(permission, command.getPermissionDefault());
        this.commands.put(command.getName(), command);
    }

    private RSCommand<? extends RSPlugin> findCommand(String name) {
        if (name.isEmpty()) return null;
        for (RSCommand<? extends RSPlugin> sub : this.commands.values()) {
            if (sub.getLocalizedName(player()).equals(name)) return sub;
        }
        return null;
    }

    private String getTranslationKey() {
        return this.parent == null
                ? getName()
                : this.parent.getTranslationKey() + ".commands." + getName();
    }

    protected String getLocalizedName(Player player) {
        return this.command.get(player, getTranslationKey() + ".name");
    }

    protected String getLocalizedDescription(Player player) {
        return this.command.get(player, getDescription());
    }

    protected String getLocalizedUsage(Player player) {
        return this.command.get(player, getUsage());
    }

    private String getLocalizedCommand(Player player) {
        if (this.parent == null) {
            String name = getLocalizedName(player);
            return name.isEmpty() ? getName() : name;
        } else return this.parent.getLocalizedCommand(player) + " " + getLocalizedName(player);
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
    public @NotNull List<String> tabComplete(
            @NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        this.notifier.setReceiver(sender);
        this.sender = sender;
        this.audience = this.plugin.getAdventure().sender(sender);
        CommandArgs data = new CommandArgs(args);
        List<String> list = new ArrayList<>();
        if (data.length(this.index + 1))
            for (RSCommand<? extends RSPlugin> cmd : this.commands.values()) {
                if (hasPermissionNode(cmd.getPermission()))
                    list.add(cmd.getLocalizedName(player()));
            }
        RSCommand<? extends RSPlugin> sub = findCommand(data.get(index));
        if (sub == null) {
            if (hasPermissionNode(getPermission())) list.addAll(tabComplete(data));
        } else if (hasPermissionNode(sub.getPermission())) {
            if (data.length() > sub.index) list.addAll(sub.tabComplete(sender, alias, args));
        }
        return list;
    }

    /**
     * Execute this command with parsed data and return a standardized {@link Result}. Subclasses
     * should override this method to implement the actual behavior.
     *
     * <p>명령 인자를 파싱한 후 표준화된 {@link Result} 값을 반환한다. 실제 동작은 하위 클래스에서 이 메소드를 오버라이드하여 구현한다.
     *
     * <ul>
     *   <li>Result.SUCCESS: operation succeeded / 작업 성공
     *   <li>Result.FAILURE: operation failed / 작업 실패
     *   <li>Result.ONLY_PLAYER / ONLY_CONSOLE / NOT_FOUND_* / WRONG_USAGE: framework auto-announces
     * </ul>
     */
    protected Result execute(CommandArgs data) {
        return Result.WRONG_USAGE;
    }

    /**
     * Returns tab-completion candidates. Default returns an empty list.
     *
     * <p>탭 완성 후보를 반환한다. 기본 구현은 빈 리스트를 반환한다.
     */
    protected List<String> tabComplete(CommandArgs data) {
        return List.of();
    }

    /**
     * Reloads configuration/state. Default is a no-op.
     *
     * <p>구성/상태를 재적용한다. 기본 구현은 아무 동작도 수행하지 않는다.
     */
    protected void reload(CommandArgs data) {}

    /**
     * Enum representing command execution results. The framework automatically sends common
     * notification messages based on each value.
     *
     * <p>명령어 실행 결과를 나타내는 열거형. 각 값에 따라 프레임워크가 자동으로 공통 안내 메시지를 발송한다.
     */
    @Getter
    @RequiredArgsConstructor
    public enum Result {
        SUCCESS(true),

        FAILURE(false),

        ONLY_PLAYER(false),

        ONLY_CONSOLE(false),

        NO_PERMISSION(false),

        NOT_FOUND_ONLINE_PLAYER(false),

        NOT_FOUND_OFFLINE_PLAYER(false),

        NOT_FOUND_ITEM(false),

        WRONG_USAGE(false);

        private final boolean success;
    }
}
