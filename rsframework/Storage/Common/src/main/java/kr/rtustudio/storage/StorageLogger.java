package kr.rtustudio.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StorageLogger {

    private StorageLogger() {}

    public static Logger get(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static void logAdd(Logger log, String table, String query) {
        log.debug("[Storage] ADD: {} - {}", table, query);
    }

    public static void logSet(Logger log, String table, String query) {
        log.debug("[Storage] SET: {} - {}", table, query);
    }

    public static void logGet(Logger log, String table, String query) {
        log.debug("[Storage] GET: {} - {}", table, query);
    }

    public static void logError(Logger log, String operation, String table, Throwable e) {
        log.error("[Storage] {} failed on table: {}", operation, table, e);
    }
}
