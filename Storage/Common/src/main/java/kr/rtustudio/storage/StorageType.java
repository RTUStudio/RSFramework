package kr.rtustudio.storage;

public enum StorageType {
    JSON,
    SQLITE,
    MYSQL,
    MONGODB,
    MARIADB,
    POSTGRESQL;

    public static StorageType get(String type) {
        try {
            return StorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return StorageType.JSON;
        }
    }
}
