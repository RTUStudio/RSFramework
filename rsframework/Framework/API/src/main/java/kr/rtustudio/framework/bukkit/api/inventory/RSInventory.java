package kr.rtustudio.framework.bukkit.api.inventory;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.platform.MinecraftVersion;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import lombok.Getter;
import net.kyori.adventure.text.Component;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * 플러그인별 커스텀 인벤토리 UI의 기반 추상 클래스입니다.
 *
 * <p>클릭, 드래그, 닫기 이벤트 핸들링과 플러그인 리소스(번역, 알림 등)에 접근할 수 있습니다.
 *
 * @param <T> 소유 플러그인 타입
 */
public abstract class RSInventory<T extends RSPlugin> implements InventoryHolder {

    @Getter protected final T plugin;
    @Getter protected final Framework framework;
    @Getter protected final MessageTranslation message;
    @Getter protected final CommandTranslation command;
    @Getter protected final Notifier notifier;

    public RSInventory(T plugin) {
        this.plugin = plugin;
        this.framework = LightDI.getBean(Framework.class);
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.notifier = Notifier.of(plugin);
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

    public void onClose(Event<InventoryCloseEvent> event) {}

    public record Event<T extends InventoryEvent>(
            T event, Inventory inventory, Player player, boolean isInventory) {}

    public record Drag(
            Map<Integer, ItemStack> items, ItemStack cursor, ItemStack oldCursor, DragType type) {}

    public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {}
}
