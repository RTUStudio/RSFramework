package kr.rtustudio.storage;

import lombok.Getter;

@Getter
public enum StorageType {
    JSON("Json"),
    SQLITE("SQLite"),
    MYSQL("MySQL"),
    MONGODB("MongoDB"),
    MARIADB("MariaDB"),
    POSTGRESQL("PostgreSQL");

    private final String name;

    StorageType(String name) {
        this.name = name;
    }

    public static StorageType get(String type) {
        try {
            return StorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return StorageType.JSON;
        }
    }
}
