package kr.rtustudio.framework.bukkit.api.integration.wrapper;

import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.TranslationConfiguration;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.command.CommandTranslation;
import kr.rtustudio.framework.bukkit.api.configuration.internal.translation.message.MessageTranslation;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.integration.Integration;
import kr.rtustudio.framework.bukkit.api.player.PlayerAudience;
import lombok.Getter;

import org.jetbrains.annotations.NotNull;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.*;

public class PacketWrapper<T extends RSPlugin> implements Integration {

    @Getter private final T plugin;

    private final MessageTranslation message;
    private final CommandTranslation command;
    private final Framework framework = LightDI.getBean(Framework.class);
    private final PlayerAudience chat;

    private final Priority priority;

    private Integration.Wrapper wrapper;

    public PacketWrapper(T plugin) {
        this(plugin, Priority.NORMAL);
    }

    public PacketWrapper(T plugin, Priority priority) {
        this.plugin = plugin;
        this.message = plugin.getConfiguration().getMessage();
        this.command = plugin.getConfiguration().getCommand();
        this.chat = PlayerAudience.of(plugin);
        this.priority = priority;
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

    protected PlayerAudience chat() {
        return chat;
    }

    @Override
    public boolean isAvailable() {
        return plugin.getFramework().isEnabledDependency("packetevents");
    }

    @Override
    public boolean register() {
        wrapper = new PacketListener(priority);
        return wrapper.register();
    }

    @Override
    public boolean unregister() {
        boolean result = wrapper.unregister();
        if (result) wrapper = null;
        return result;
    }

    protected void onConnect(@NotNull UserConnectEvent event) {}

    protected void onLogin(@NotNull UserLoginEvent event) {}

    protected void onDisconnect(@NotNull UserDisconnectEvent event) {}

    protected void onReceive(@NotNull PacketReceiveEvent event) {}

    protected void onSend(@NotNull PacketSendEvent event) {}

    protected void onExternal(@NotNull PacketEvent event) {}

    public enum Priority {
        /** This listener will be run first and has little say in the outcome of events. */
        LOWEST,

        /** Listener is of low importance. */
        LOW,

        /**
         * The normal listener priority. If possible, always pick this. It allows other projects to
         * easily overturn your decisions. Moreover, it is pretty bold to assume that your project
         * should always have the final say.
         */
        NORMAL,

        /** Listener is of high importance. */
        HIGH,

        /** Listener is of critical importance. Use this to decide the final state of packets. */
        HIGHEST,

        /**
         * Only use this priority if you want to perform logic based on the outcome of an event.
         * Please do not modify packets in this stage.
         */
        MONITOR
    }

    class PacketListener
            implements com.github.retrooper.packetevents.event.PacketListener, Integration.Wrapper {

        private final PacketEventsAPI<?> api = PacketEvents.getAPI();

        private final PacketListenerPriority priority;

        private PacketListenerCommon listener = null;

        public PacketListener(Priority priority) {
            this.priority =
                    switch (priority) {
                        case LOWEST -> PacketListenerPriority.LOWEST;
                        case LOW -> PacketListenerPriority.LOW;
                        case NORMAL -> PacketListenerPriority.NORMAL;
                        case HIGH -> PacketListenerPriority.HIGH;
                        case HIGHEST -> PacketListenerPriority.HIGHEST;
                        case MONITOR -> PacketListenerPriority.MONITOR;
                    };
        }

        public void onUserConnect(@NotNull UserConnectEvent event) {
            onConnect(event);
        }

        public void onUserLogin(@NotNull UserLoginEvent event) {
            onLogin(event);
        }

        public void onUserDisconnect(@NotNull UserDisconnectEvent event) {
            onDisconnect(event);
        }

        public void onPacketReceive(@NotNull PacketReceiveEvent event) {
            onReceive(event);
        }

        public void onPacketSend(@NotNull PacketSendEvent event) {
            onSend(event);
        }

        public void onPacketEventExternal(@NotNull PacketEvent event) {
            onExternal(event);
        }

        @Override
        public boolean register() {
            try {
                this.listener = api.getEventManager().registerListener(this, priority);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public boolean unregister() {
            if (listener == null) return false;
            api.getEventManager().unregisterListener(listener);
            listener = null;
            return true;
        }
    }
}
