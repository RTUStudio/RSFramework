package kr.rtustudio.broker.protoweaver.bukkit.api.nms;

import kr.rtustudio.broker.protoweaver.api.util.ProtoLogger;

public interface IProtoWeaver extends ProtoLogger.IProtoLogger {

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
