package kr.rtuserver.framework.bukkit.api.event;

import lombok.Getter;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class CustomRegistryLoadedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Type type;

    public CustomRegistryLoadedEvent(Type type) {
        this.type = type;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public enum Type {
        ItemsAdder,
        Oraxen,
        Nexo,
        MMOItems,
        Vanilla
    }
}
