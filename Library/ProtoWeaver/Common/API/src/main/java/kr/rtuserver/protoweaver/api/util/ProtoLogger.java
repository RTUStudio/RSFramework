package kr.rtuserver.protoweaver.api.util;

import lombok.Setter;

public class ProtoLogger {

    @Setter private static IProtoLogger logger;

    public static void info(String message) {
        if (logger != null) logger.info(message);
        else System.out.println("INFO: " + message);
    }

    public static void warn(String message) {
        if (logger != null) logger.warn(message);
        else System.out.println("WARN: " + message);
    }

    public static void err(String message) {
        if (logger != null) logger.err(message);
        else System.out.println("ERR: " + message);
    }

    public interface IProtoLogger {
        void info(String message);

        void warn(String message);

        void err(String message);
    }
}
