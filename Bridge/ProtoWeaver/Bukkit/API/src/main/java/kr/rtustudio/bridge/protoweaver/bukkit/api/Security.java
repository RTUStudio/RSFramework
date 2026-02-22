package kr.rtustudio.bridge.protoweaver.bukkit.api;

import kr.rtustudio.bridge.protoweaver.api.util.ProtoLogger;

public interface Security extends ProtoLogger.IProtoLogger {

    void setup(boolean tls);

    boolean isModernProxy();

    default boolean isPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig")
                || hasClass("io.papermc.paper.configuration.Configuration");
    }

    default boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
