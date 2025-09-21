package kr.rtustudio.framework.bukkit.api.configuration;

import java.util.function.Consumer;

/** Marker interface for unique sections of a configuration. */
public abstract class ConfigurationPart {

    public <T> T make(T object, Consumer<? super T> consumer) {
        consumer.accept(object);
        return object;
    }
}
