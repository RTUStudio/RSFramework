package kr.rtustudio.protoweaver.bukkit.api.nms;

import kr.rtustudio.protoweaver.api.util.ProtoLogger;

public interface IProtoWeaver extends ProtoLogger.IProtoLogger {

    void setup();

    boolean isModernProxy();

    default boolean isPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig")
                || hasClass(
                        "kr.rtustudio.framework.bukkit.api.configuration.paper.configuration.Configuration");
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
