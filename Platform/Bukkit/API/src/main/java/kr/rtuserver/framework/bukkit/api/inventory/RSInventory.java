package kr.rtuserver.framework.bukkit.api.inventory;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.impl.SettingConfiguration;
import kr.rtuserver.framework.bukkit.api.config.impl.TranslationConfiguration;
import kr.rtuserver.framework.bukkit.api.utility.format.ComponentFormatter;
import kr.rtuserver.framework.bukkit.api.utility.platform.MinecraftVersion;
import kr.rtuserver.framework.bukkit.api.utility.player.PlayerChat;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RSInventory {

    @Getter
    public abstract static class Default<T extends RSPlugin> implements InventoryHolder {

        private final T plugin;
        private final SettingConfiguration setting;
        private final TranslationConfiguration message;
        private final TranslationConfiguration command;
        private final PlayerChat chat;

        @Setter
        private Inventory inventory;

        public Default(T plugin) {
            this.plugin = plugin;
            this.setting = plugin.getConfigurations().getSetting();
            this.message = plugin.getConfigurations().getMessage();
            this.command = plugin.getConfigurations().getCommand();
            this.chat = PlayerChat.of(plugin);
        }

        @NotNull
        public Inventory getInventory() {
            if (inventory == null) throw new UnsupportedOperationException("Not initialized inventory");
            return inventory;
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

        public void onClose(Event<InventoryCloseEvent> event, Close close) {
        }

        public record Event<T extends InventoryEvent>(T event, Inventory inventory, Player player,
                                                      boolean isInventory) {
        }

        public record Drag(Map<Integer, ItemStack> items, ItemStack cursor, ItemStack oldCursor, DragType type) {
        }

        public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
        }

        public record Close(InventoryCloseEvent.Reason reason) {
        }

    }

    @Getter
    public abstract static class Page<T extends RSPlugin> implements InventoryHolder {

        private final T plugin;
        private final SettingConfiguration setting;
        private final TranslationConfiguration message;
        private final TranslationConfiguration command;
        private final PlayerChat chat;

        private final List<Inventory> pages = new ArrayList<>();
        @Setter
        private int page = 0;

        public Page(T plugin) {
            this.plugin = plugin;
            this.setting = plugin.getConfigurations().getSetting();
            this.message = plugin.getConfigurations().getMessage();
            this.command = plugin.getConfigurations().getCommand();
            this.chat = PlayerChat.of(plugin);
        }

        @NotNull
        public Inventory getInventory() {
            Inventory inventory = pages.get(page);
            if (inventory == null) throw new UnsupportedOperationException("Not initialized inventory");
            return inventory;
        }

        protected Inventory createInventory(InventoryType type, Component title) {
            if (MinecraftVersion.isPaper()) return Bukkit.createInventory(this, type, title);
            else return Bukkit.createInventory(this, type, ComponentFormatter.legacy(title));
        }

        protected Inventory createInventory(int size, Component component) {
            if (MinecraftVersion.isPaper()) return Bukkit.createInventory(this, size, component);
            else return Bukkit.createInventory(this, size, ComponentFormatter.legacy(component));
        }

        protected abstract boolean loadPage(int index);

        protected enum Navigation {
            FIRST,
            PREVIOUS,
            NEXT,
            LAST
        }

        protected boolean loadPage(Navigation navigation) {
            int lastPage = pages.size() - 1;
            switch (navigation) {
                case FIRST -> {
                    if (page != 0) {
                        if (loadPage(0)) {
                            page = 0;
                            return true;
                        }
                    }
                }
                case PREVIOUS -> {
                    if (page <= 0) return false;
                    if (loadPage(page - 1)) {
                        page--;
                        return true;
                    }
                }
                case NEXT -> {
                    if (page >= lastPage) return false;
                    if (loadPage(page + 1)) {
                        page++;
                        return true;
                    }
                }
                case LAST -> {
                    if (page != lastPage) {
                       if (loadPage(lastPage)) {
                           page = lastPage;
                           return true;
                       }
                    }
                }
            }
            return false;
        }

        protected Inventory addPage(int page, int size, Component component) {
            Inventory inventory = createInventory(size, component);
            pages.add(page, inventory);
            return inventory;
        }

        public boolean onClick(Event<InventoryClickEvent> event, Click click) {
            return true;
        }

        public boolean onDrag(Event<InventoryDragEvent> event, Drag drag) {
            return true;
        }

        public void onClose(Event<InventoryCloseEvent> event, Close close) {
        }

        public record Event<T extends InventoryEvent>(T event, Inventory inventory, Player player,
                                                      boolean isInventory, int page) {
        }

        public record Drag(Map<Integer, ItemStack> items, ItemStack cursor, ItemStack oldCursor, DragType type) {
        }

        public record Click(int slot, InventoryType.SlotType slotType, ClickType type) {
        }

        public record Close(InventoryCloseEvent.Reason reason) {
        }

    }

}
