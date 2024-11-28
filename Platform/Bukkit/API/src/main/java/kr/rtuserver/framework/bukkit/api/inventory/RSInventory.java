package kr.rtuserver.framework.bukkit.api.inventory;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.impl.SettingConfiguration;
import kr.rtuserver.framework.bukkit.api.config.impl.TranslationConfiguration;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@Getter
public abstract class RSInventory implements InventoryHolder {


    private final RSPlugin plugin;
    private final SettingConfiguration setting;
    private final TranslationConfiguration message;
    private final TranslationConfiguration command;

    public RSInventory(RSPlugin plugin) {
        this.plugin = plugin;
        this.setting = plugin.getConfigurations().getSetting();
        this.message = plugin.getConfigurations().getMessage();
        this.command = plugin.getConfigurations().getCommand();
    }

    public boolean onClick(Event<InventoryClickEvent> event, Click click) {
        return true;
    }

    public boolean onDrag(Event<InventoryDragEvent> event, Drag drag) {
        return true;
    }

    public void onClose(Event<InventoryCloseEvent> event, Close close) {
    }

    public record Event<T extends InventoryEvent>(T event, Inventory inventory, Player player, boolean isInventory) {
    }

    public record Drag(Map<Integer, ItemStack> items, ItemStack cursor, ItemStack oldCursor, DragType type) {
    }

    public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
    }

    public record Close(InventoryCloseEvent.Reason reason) {
    }
}
