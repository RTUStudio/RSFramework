package kr.rtuserver.framework.bukkit.api.format;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComponentFormatter {

    static Framework framework;

    static Framework framework() {
        if (framework == null) framework = LightDI.getBean(Framework.class);
        return framework;
    }

    public static Component mini(String miniMessage) {
        return MiniMessage.miniMessage().deserialize(miniMessage);
    }

    public static String mini(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    public static Component parse(String message) {
        return parse(null, message);
    }

    public static Component parse(CommandSender sender, String miniMessage) {
        return mini(
                framework().isEnabledDependency("PlaceholderAPI")
                        ? PlaceholderAPI.setPlaceholders(
                                (sender instanceof Player player) ? player : null, miniMessage)
                        : miniMessage);
    }

    public static String legacy(Component component) {
        return legacy(LegacyComponentSerializer.SECTION_CHAR, component);
    }

    public static String legacy(char legacyCharacter, Component component) {
        return LegacyComponentSerializer.legacy(legacyCharacter).serialize(component);
    }

    public static Component legacy(String message) {
        return legacy(LegacyComponentSerializer.SECTION_CHAR, message);
    }

    public static Component legacy(char legacyCharacter, String message) {
        return LegacyComponentSerializer.legacy(legacyCharacter).deserialize(message);
    }

    public static Component system(CommandSender sender, String miniMessage) {
        Component lore = parse(sender, framework().getModules().getTheme().getSystemMessage());
        return parse(miniMessage).hoverEvent(HoverEvent.showText(lore));
    }

    public static Component system(CommandSender sender, Component component) {
        Component lore = parse(sender, framework().getModules().getTheme().getSystemMessage());
        return component.hoverEvent(HoverEvent.showText(lore));
    }
}
