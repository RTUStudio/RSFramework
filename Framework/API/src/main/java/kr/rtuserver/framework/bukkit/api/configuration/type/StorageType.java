package kr.rtuserver.framework.bukkit.api.configuration.type;

public enum StorageType {

    JSON,
    MYSQL,
    MONGODB,
    MARIADB;

    public static StorageType get(String type) {
        try {
            return StorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return StorageType.JSON;
        }
    }

}
