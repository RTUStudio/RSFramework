package kr.rtustudio.framework.bukkit.api.event;

import lombok.Getter;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/** 커스텀 레지스트리(ItemsAdder, Oraxen, Nexo 등)가 로드 완료되었을 때 발생하는 이벤트입니다. */
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

    /** 레지스트리 소스 타입. */
    public enum Type {
        ItemsAdder,
        Oraxen,
        Nexo,
        MMOItems,
        Vanilla
    }
}
