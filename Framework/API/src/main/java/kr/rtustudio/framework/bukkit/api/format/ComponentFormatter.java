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
 * MiniMessage, Legacy, PlaceholderAPI 등 다양한 텍스트 형식을 {@link Component}로 변환하는 유틸리티 클래스입니다.
 *
 * <p>모든 메서드가 {@code static}이며, 인스턴스를 생성할 수 없습니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentFormatter {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    /**
     * MiniMessage 형식 문자열을 {@link Component}로 변환한다.
     *
     * @param miniMessage MiniMessage 형식 문자열
     * @return 변환된 컴포넌트
     */
    public static Component mini(String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    /**
     * {@link Component}를 MiniMessage 형식 문자열로 직렬화한다.
     *
     * @param component 직렬화할 컴포넌트
     * @return MiniMessage 형식 문자열
     */
    public static String mini(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    /**
     * PlaceholderAPI 플레이스홀더를 치환한 뒤 MiniMessage로 파싱한다.
     *
     * @param message MiniMessage 형식 문자열
     * @return 파싱된 컴포넌트
     */
    public static Component parse(String message) {
        return parse(null, message);
    }

    /**
     * 발신자 컨텍스트로 PlaceholderAPI 플레이스홀더를 치환한 뒤 MiniMessage로 파싱한다.
     *
     * @param sender 플레이스홀더 치환 대상 (플레이어가 아니면 {@code null}로 처리)
     * @param miniMessage MiniMessage 형식 문자열
     * @return 파싱된 컴포넌트
     */
    public static Component parse(CommandSender sender, String miniMessage) {
        return mini(
                framework().isEnabledDependency("PlaceholderAPI")
                        ? PlaceholderAPI.setPlaceholders(
                                (sender instanceof Player player) ? player : null, miniMessage)
                        : miniMessage);
    }

    /**
     * {@link Component}를 레거시 색상 코드({@code §}) 문자열로 변환한다.
     *
     * @param component 변환할 컴포넌트
     * @return 레거시 형식 문자열
     */
    public static String legacy(Component component) {
        return legacy(LegacyComponentSerializer.SECTION_CHAR, component);
    }

    /**
     * {@link Component}를 지정한 레거시 문자로 직렬화한다.
     *
     * @param legacyCharacter 레거시 색상 코드 문자 (예: {@code §}, {@code &})
     * @param component 변환할 컴포넌트
     * @return 레거시 형식 문자열
     */
    public static String legacy(char legacyCharacter, Component component) {
        return LegacyComponentSerializer.legacy(legacyCharacter).serialize(component);
    }

    /**
     * 레거시 색상 코드({@code §}) 문자열을 {@link Component}로 변환한다.
     *
     * @param message 레거시 형식 문자열
     * @return 변환된 컴포넌트
     */
    public static Component legacy(String message) {
        return legacy(LegacyComponentSerializer.SECTION_CHAR, message);
    }

    /**
     * 지정한 레거시 문자로 된 문자열을 {@link Component}로 변환한다.
     *
     * @param legacyCharacter 레거시 색상 코드 문자
     * @param message 레거시 형식 문자열
     * @return 변환된 컴포넌트
     */
    public static Component legacy(char legacyCharacter, String message) {
        return LegacyComponentSerializer.legacy(legacyCharacter).deserialize(message);
    }

    /**
     * 시스템 메시지 호버 텍스트를 포함한 컴포넌트를 생성한다.
     *
     * @param sender 플레이스홀더 치환 대상
     * @param miniMessage MiniMessage 형식 문자열
     * @return 호버 이벤트가 포함된 컴포넌트
     */
    public static Component system(CommandSender sender, String miniMessage) {
        Component lore = parse(sender, framework().getModule(ThemeModule.class).getSystemMessage());
        return parse(miniMessage).hoverEvent(HoverEvent.showText(lore));
    }

    /**
     * 시스템 메시지 호버 텍스트를 포함한 컴포넌트를 생성한다.
     *
     * @param sender 플레이스홀더 치환 대상
     * @param component 원본 컴포넌트
     * @return 호버 이벤트가 포함된 컴포넌트
     */
    public static Component system(CommandSender sender, Component component) {
        Component lore = parse(sender, framework().getModule(ThemeModule.class).getSystemMessage());
        return component.hoverEvent(HoverEvent.showText(lore));
    }
}
