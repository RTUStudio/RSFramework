package kr.rtuserver.protoweaver.api.impl.bukkit.nms;

import kr.rtuserver.protoweaver.api.util.ProtoLogger;

public interface IProtoWeaver extends ProtoLogger.IProtoLogger {

    void setup();

    boolean isModernProxy();

    default boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
