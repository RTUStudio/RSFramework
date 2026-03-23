package kr.rtustudio.framework.bukkit.api.format;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.core.module.ThemeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Converts various text formats (MiniMessage, Legacy, PlaceholderAPI) into {@link Component}. All
 * methods are {@code static} and no instances can be created.
 *
 * <p>MiniMessage, Legacy, PlaceholderAPI 등 다양한 텍스트 형식을 {@link Component}로 변환하는 유틸리티 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentFormatter {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    /**
     * Converts a MiniMessage format string to a {@link Component}.
     *
     * <p>MiniMessage 형식 문자열을 {@link Component}로 변환한다.
     *
     * @param miniMessage MiniMessage format string
     * @return converted component
     */
    public static Component mini(String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    /**
     * Serializes a {@link Component} to a MiniMessage format string.
     *
     * <p>{@link Component}를 MiniMessage 형식 문자열로 직렬화한다.
     *
     * @param component component to serialize
     * @return MiniMessage format string
     */
    public static String mini(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    /**
     * Substitutes PlaceholderAPI placeholders and parses with MiniMessage.
     *
     * <p>PlaceholderAPI 플레이스홀더를 치환한 뒤 MiniMessage로 파싱한다.
     *
     * @param message MiniMessage format string
     * @return parsed component
     */
    public static Component parse(String message) {
        return parse(null, message);
    }

    /**
     * Substitutes PlaceholderAPI placeholders with sender context and parses with MiniMessage.
     *
     * <p>발신자 컨텍스트로 PlaceholderAPI 플레이스홀더를 치환한 뒤 MiniMessage로 파싱한다.
     *
     * @param sender placeholder target (treated as {@code null} if not a player)
     * @param miniMessage MiniMessage format string
     * @return parsed component
     */
    public static Component parse(CommandSender sender, String miniMessage) {
        return mini(
                framework().isEnabledDependency("PlaceholderAPI")
                        ? PlaceholderAPI.setPlaceholders(
                                (sender instanceof Player player) ? player : null, miniMessage)
                        : miniMessage);
    }

    /**
     * Converts a {@link Component} to a legacy color code ({@code §}) string.
     *
     * <p>{@link Component}를 레거시 색상 코드 문자열로 변환한다.
     *
     * @param component component to convert
     * @return legacy format string
     */
    public static String legacy(Component component) {
        return legacy(LegacyComponentSerializer.SECTION_CHAR, component);
    }

    /**
     * Serializes a {@link Component} with the specified legacy character.
     *
     * <p>{@link Component}를 지정한 레거시 문자로 직렬화한다.
     *
     * @param legacyCharacter legacy color code char (e.g. {@code §}, {@code &})
     * @param component component to convert
     * @return legacy format string
     */
    public static String legacy(char legacyCharacter, Component component) {
        return LegacyComponentSerializer.legacy(legacyCharacter).serialize(component);
    }

    /**
     * Converts a legacy color code ({@code §}) string to a {@link Component}.
     *
     * <p>레거시 색상 코드 문자열을 {@link Component}로 변환한다.
     *
     * @param message legacy format string
     * @return converted component
     */
    public static Component legacy(String message) {
        return legacy(LegacyComponentSerializer.SECTION_CHAR, message);
    }

    /**
     * Converts a string with the specified legacy character to a {@link Component}.
     *
     * <p>지정한 레거시 문자로 된 문자열을 {@link Component}로 변환한다.
     *
     * @param legacyCharacter legacy color code char
     * @param message legacy format string
     * @return converted component
     */
    public static Component legacy(char legacyCharacter, String message) {
        return LegacyComponentSerializer.legacy(legacyCharacter).deserialize(message);
    }

    /**
     * Creates a component with a system message hover text.
     *
     * <p>시스템 메시지 호버 텍스트를 포함한 컴포넌트를 생성한다.
     *
     * @param sender placeholder target
     * @param miniMessage MiniMessage format string
     * @return component with hover event
     */
    public static Component system(CommandSender sender, String miniMessage) {
        Component lore = parse(sender, framework().getModule(ThemeModule.class).getSystemMessage());
        return parse(miniMessage).hoverEvent(HoverEvent.showText(lore));
    }

    /**
     * Creates a component with a system message hover text.
     *
     * <p>시스템 메시지 호버 텍스트를 포함한 컴포넌트를 생성한다.
     *
     * @param sender placeholder target
     * @param component original component
     * @return component with hover event
     */
    public static Component system(CommandSender sender, Component component) {
        Component lore = parse(sender, framework().getModule(ThemeModule.class).getSystemMessage());
        return component.hoverEvent(HoverEvent.showText(lore));
    }
}
