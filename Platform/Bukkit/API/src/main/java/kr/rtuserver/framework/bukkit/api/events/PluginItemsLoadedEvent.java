package kr.rtuserver.framework.bukkit.api.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PluginItemsLoadedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Plugin plugin;

    public PluginItemsLoadedEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public enum Plugin {
        ITEMSADDER,
        ORAXEN,
        NEXO,
        MMOITEMS,
        VANILLA
    }


}
