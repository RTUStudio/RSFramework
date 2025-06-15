package kr.rtuserver.framework.bukkit.api.inventory;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.configuration.translation.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.configuration.translation.command.CommandTranslation;
import kr.rtuserver.framework.bukkit.api.configuration.translation.message.MessageTranslation;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtuserver.framework.bukkit.api.player.PlayerChat;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;


public abstract class RSInventory<T extends RSPlugin> implements InventoryHolder {

    @Getter
    private final T plugin;

    private final MessageTranslation message;
    private final CommandTranslation command;
    private final Framework framework = LightDI.getBean(Framework.class);
    private final PlayerChat chat;

    public RSInventory(T plugin) {
        this.plugin = plugin;
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
        this.chat = PlayerChat.of(plugin);
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

    public PlayerChat chat() {
        return chat;
    }

    protected Inventory createInventory(InventoryType type, Component title) {
        if (MinecraftVersion.isPaper()) return Bukkit.createInventory(this, type, title);
        else return Bukkit.createInventory(this, type, ComponentFormatter.legacy(title));
    }

    protected Inventory createInventory(int size, Component component) {
        if (MinecraftVersion.isPaper()) return Bukkit.createInventory(this, size, component);
        else return Bukkit.createInventory(this, size, ComponentFormatter.legacy(component));
    }

    public boolean onClick(Event<InventoryClickEvent> event, Click click) {
        return true;
    }

    public boolean onDrag(Event<InventoryDragEvent> event, Drag drag) {
        return true;
    }

    public void onClose(Event<InventoryCloseEvent> event) {
    }

    public record Event<T extends InventoryEvent>(T event, Inventory inventory, Player player, boolean isInventory) {
    }

    public record Drag(Map<Integer, ItemStack> items, ItemStack cursor, ItemStack oldCursor, DragType type) {
    }

    public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
    }
}