package kr.rtuserver.framework.bukkit.core.internal.listeners;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.inventory.RSInventory;
import kr.rtuserver.framework.bukkit.api.listener.RSListener;
import kr.rtuserver.framework.bukkit.api.utility.format.ComponentFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener extends RSListener {

    public InventoryListener(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    private void onRSInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();
        if (e.getView().getTopInventory().getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryClickEvent> event = new RSInventory.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Click click = new RSInventory.Click(e.getSlot(), e.getSlotType(), e.getClick());
            try {
                e.setCancelled(!rsInventory.onClick(event, click));
            } catch (Exception exception) {
                e.setCancelled(true);
                Component errorMessage = ComponentFormatter.mini(getMessage().get("error.inventory"));
                getPlugin().console(errorMessage);
                getPlugin().getAdventure().player(player).sendMessage(errorMessage);
                exception.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onRSInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTopInventory().getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryDragEvent> holderEvent = new RSInventory.Event<>(event, inv, player, isPlayerInventory);
            RSInventory.Drag click = new RSInventory.Drag(event.getNewItems(), event.getCursor(), event.getOldCursor(), event.getType());
            try {
                event.setCancelled(!rsInventory.onDrag(holderEvent, click));
            } catch (Exception ex) {
                event.setCancelled(true);
                Component errorMessage = ComponentFormatter.mini(getMessage().get("error.inventory"));
                getPlugin().console(errorMessage);
                getPlugin().getAdventure().player(player).sendMessage(errorMessage);
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    private void onRSInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();
        if (inv.getHolder() instanceof RSInventory rsInventory) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryCloseEvent> holderEvent = new RSInventory.Event<>(event, inv, player, isPlayerInventory);
            RSInventory.Close close = new RSInventory.Close(event.getReason());
            rsInventory.onClose(holderEvent, close);
        }
    }

}
