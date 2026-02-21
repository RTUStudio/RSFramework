package kr.rtustudio.framework.bukkit.core.internal.listeners;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import kr.rtustudio.framework.bukkit.api.inventory.RSInventory;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Slf4j
@SuppressWarnings("unused")
public class InventoryListener extends RSListener<RSPlugin> {
    public InventoryListener(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();
        InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if (holder instanceof RSInventory<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryClickEvent> event =
                    new RSInventory.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Click click =
                    new RSInventory.Click(e.getSlot(), e.getSlotType(), e.getClick());
            try {
                rsInv.getNotifier().setReceiver(player);
                e.setCancelled(!rsInv.onClick(event, click));
            } catch (Exception ex) {
                e.setCancelled(true);
                Component errorMessage =
                        ComponentFormatter.mini(message.get(player, "error.inventory"));
                plugin.console(errorMessage);
                plugin.getAdventure().player(player).sendMessage(errorMessage);
                log.error("Error in inventory click handler", ex);
            }
        }
    }

    @EventHandler
    private void onDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getWhoClicked();
        InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if (holder instanceof RSInventory<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryDragEvent> holderEvent =
                    new RSInventory.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Drag drag =
                    new RSInventory.Drag(
                            e.getNewItems(), e.getCursor(), e.getOldCursor(), e.getType());
            try {
                rsInv.getNotifier().setReceiver(player);
                e.setCancelled(!rsInv.onDrag(holderEvent, drag));
            } catch (Exception ex) {
                e.setCancelled(true);
                Component errorMessage =
                        ComponentFormatter.mini(message.get(player, "error.inventory"));
                plugin.console(errorMessage);
                plugin.getAdventure().player(player).sendMessage(errorMessage);
                log.error("Error in inventory click handler", ex);
            }
        }
    }

    @EventHandler
    private void onClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getPlayer();
        InventoryHolder holder = e.getView().getTopInventory().getHolder();
        if (holder instanceof RSInventory<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory);
            RSInventory.Event<InventoryCloseEvent> holderEvent =
                    new RSInventory.Event<>(e, inv, player, isPlayerInventory);
            try {
                rsInv.getNotifier().setReceiver(player);
                rsInv.onClose(holderEvent);
            } catch (Exception ex) {
                Component errorMessage =
                        ComponentFormatter.mini(message.get(player, "error.inventory"));
                plugin.console(errorMessage);
                plugin.getAdventure().player(player).sendMessage(errorMessage);
                log.error("Error in inventory click handler", ex);
            }
        }
    }
}
