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
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener extends RSListener<RSPlugin> {

    public InventoryListener(RSPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    private void onRSInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        Player player = (Player) e.getWhoClicked();
        InventoryHolder holder = e.getView().getTopInventory().getHolder();

        if (holder instanceof RSInventory.Default<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory.Default);
            RSInventory.Default.Event<InventoryClickEvent> event = new RSInventory.Default.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Default.Click click = new RSInventory.Default.Click(e.getSlot(), e.getSlotType(), e.getClick());
            try {
                e.setCancelled(!rsInv.onClick(event, click));
            } catch (Exception ex) {
                e.setCancelled(true);
                Component errorMessage = ComponentFormatter.mini(getMessage().get(player, "error.inventory"));
                getPlugin().console(errorMessage);
                getPlugin().getAdventure().player(player).sendMessage(errorMessage);
                ex.printStackTrace();
            }
            return;
        }

        if (holder instanceof RSInventory.Page<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = inv != null && !(inv.getHolder() instanceof RSInventory.Page);
            RSInventory.Page.Event<InventoryClickEvent> event = new RSInventory.Page.Event<>(e, inv, player, isPlayerInventory, rsInv.getPage());
            RSInventory.Page.Click click = new RSInventory.Page.Click(e.getSlot(), e.getSlotType(), e.getClick());
            try {
                e.setCancelled(!rsInv.onClick(event, click));
            } catch (Exception ex) {
                e.setCancelled(true);
                Component errorMessage = ComponentFormatter.mini(getMessage().get(player, "error.inventory"));
                getPlugin().console(errorMessage);
                getPlugin().getAdventure().player(player).sendMessage(errorMessage);
                ex.printStackTrace();
            }
            return;
        }
    }

    @EventHandler
    private void onRSInventoryDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getWhoClicked();
        InventoryHolder holder = e.getView().getTopInventory().getHolder();

        if (holder instanceof RSInventory.Default<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory.Default);
            RSInventory.Default.Event<InventoryDragEvent> holderEvent = new RSInventory.Default.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Default.Drag drag = new RSInventory.Default.Drag(e.getNewItems(), e.getCursor(), e.getOldCursor(), e.getType());
            try {
                e.setCancelled(!rsInv.onDrag(holderEvent, drag));
            } catch (Exception ex) {
                e.setCancelled(true);
                Component errorMessage = ComponentFormatter.mini(getMessage().get(player, "error.inventory"));
                getPlugin().console(errorMessage);
                getPlugin().getAdventure().player(player).sendMessage(errorMessage);
                ex.printStackTrace();
            }
            return;
        }

        if (holder instanceof RSInventory.Page<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory.Page);
            RSInventory.Page.Event<InventoryDragEvent> holderEvent = new RSInventory.Page.Event<>(e, inv, player, isPlayerInventory, rsInv.getPage());
            RSInventory.Page.Drag drag = new RSInventory.Page.Drag(e.getNewItems(), e.getCursor(), e.getOldCursor(), e.getType());
            try {
                e.setCancelled(!rsInv.onDrag(holderEvent, drag));
            } catch (Exception ex) {
                e.setCancelled(true);
                Component errorMessage = ComponentFormatter.mini(getMessage().get(player, "error.inventory"));
                getPlugin().console(errorMessage);
                getPlugin().getAdventure().player(player).sendMessage(errorMessage);
                ex.printStackTrace();
            }
            return;
        }
    }

    @EventHandler
    private void onRSInventoryClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getPlayer();
        InventoryHolder holder = e.getView().getTopInventory().getHolder();

        if (holder instanceof RSInventory.Default<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory.Default);
            RSInventory.Default.Event<InventoryCloseEvent> holderEvent = new RSInventory.Default.Event<>(e, inv, player, isPlayerInventory);
            RSInventory.Default.Close close = new RSInventory.Default.Close(e.getReason());
            try {
                rsInv.onClose(holderEvent, close);
            } catch (Exception ex) {
                Component errorMessage = ComponentFormatter.mini(getMessage().get(player, "error.inventory"));
                getPlugin().console(errorMessage);
                getPlugin().getAdventure().player(player).sendMessage(errorMessage);
                ex.printStackTrace();
            }
            return;
        }

        if (holder instanceof RSInventory.Page<? extends RSPlugin> rsInv) {
            boolean isPlayerInventory = !(inv.getHolder() instanceof RSInventory.Page);
            RSInventory.Page.Event<InventoryCloseEvent> holderEvent = new RSInventory.Page.Event<>(e, inv, player, isPlayerInventory, rsInv.getPage());
            RSInventory.Page.Close close = new RSInventory.Page.Close(e.getReason());
            try {
                rsInv.onClose(holderEvent, close);
            } catch (Exception ex) {
                Component errorMessage = ComponentFormatter.mini(getMessage().get(player, "error.inventory"));
                getPlugin().console(errorMessage);
                getPlugin().getAdventure().player(player).sendMessage(errorMessage);
                ex.printStackTrace();
            }
            return;
        }
    }

}
