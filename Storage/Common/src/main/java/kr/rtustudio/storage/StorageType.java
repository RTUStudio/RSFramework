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

    /**
     * Converts a string to a {@link StorageType}, ignoring case.
     *
     * <p>문자열을 StorageType으로 변환한다. 대소문자 무시.
     *
     * @param type string to convert
     * @return corresponding StorageType, or JSON if not found
     */
    public static StorageType fromString(String type) {
        try {
            return StorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return StorageType.JSON;
        }
    }

    /**
     * @deprecated Use {@link #fromString(String)} instead.
     *     <p>{@link #fromString(String)} 사용
     */
    @Deprecated(since = "4.3.0", forRemoval = true)
    public static StorageType get(String type) {
        return fromString(type);
    }
}
