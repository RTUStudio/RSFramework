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
     * 문자열을 StorageType으로 변환한다. 대소문자 무시.
     *
     * @param type 변환할 문자열
     * @return 대응되는 StorageType, 없으면 JSON
     */
    public static StorageType fromString(String type) {
        try {
            return StorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return StorageType.JSON;
        }
    }

    /**
     * @deprecated {@link #fromString(String)} 사용
     */
    @Deprecated(since = "4.3.0", forRemoval = true)
    public static StorageType get(String type) {
        return fromString(type);
    }
}
