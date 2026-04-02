package kr.rtustudio.configurate.model;

/**
 * Record representing the path of a configuration file or folder.
 *
 * <p>설정 파일 또는 폴더의 경로를 나타내는 레코드.
 *
 * <p>Interpretation differs based on singular ({@code registerConfiguration}) vs plural ({@code
 * registerConfigurations}) registration:
 *
 * <p>단수 등록({@code registerConfiguration})과 복수 등록({@code registerConfigurations})에 따라 해석이 달라진다.
 *
 * <ul>
 *   <li><b>Singular</b> — {@link #last()} is the filename, rest is folder path
 *   <li><b>Plural</b> — Full path is the folder, loads all {@code .yml} files in that folder
 * </ul>
 *
 * <h2>Factory Methods</h2>
 *
 * <p>팩토리 메서드:
 *
 * <p>{@link #of(String...)} auto-prepends {@code Config/} prefix:
 *
 * <pre>{@code
 * ConfigPath.of("Setting")          // Config/Setting.yml
 * ConfigPath.of("Storage", "MySQL") // Config/Storage/MySQL.yml
 * ConfigPath.of("Regions")          // (plural) Config/Regions/*.yml
 * }</pre>
 *
 * <p>{@link #relative(String...)} uses the path as-is without prefix:
 *
 * <pre>{@code
 * ConfigPath.relative("Bridge", "Redis")              // Bridge/Redis.yml
 * ConfigPath.relative("Translation", "Message", "ko") // Translation/Message/ko.yml
 * }</pre>
 *
 * @param path path components ({@code "Config"} is auto-added when using {@link #of})
 * @param version config file version ({@code null} = no version management)
 */
public record ConfigPath(String[] path, Integer version) {

    /**
     * Creates a path with {@code Config/} prefix auto-prepended.
     *
     * <p>{@code Config/} 접두사를 자동으로 붙여 경로를 생성한다.
     *
     * @param path path components (min 1, {@code Config} auto-prepended)
     * @return a new {@link ConfigPath}
     */
    public static ConfigPath of(String... path) {
        if (path.length == 0) throw new IllegalArgumentException("path must not be empty");
        String[] full = new String[path.length + 1];
        full[0] = "Config";
        System.arraycopy(path, 0, full, 1, path.length);
        return new ConfigPath(full, null);
    }

    /**
     * Creates a path without any prefix, used as-is.
     *
     * <p>접두사 없이 경로를 그대로 사용한다.
     *
     * @param path path components (min 1)
     * @return a new {@link ConfigPath}
     */
    public static ConfigPath relative(String... path) {
        if (path.length == 0) throw new IllegalArgumentException("path must not be empty");
        return new ConfigPath(path, null);
    }

    /**
     * Returns a new {@link ConfigPath} with the specified version.
     *
     * <p>버전을 지정한 새 {@link ConfigPath}를 반환한다.
     *
     * @param v config file version
     * @return a new instance with the specified version
     */
    public ConfigPath version(int v) {
        return new ConfigPath(path, v);
    }

    /**
     * Returns the first path component.
     *
     * <p>경로의 첫 번째 요소를 반환한다.
     *
     * @return first path component (e.g. {@code "Config"})
     */
    public String first() {
        return path[0];
    }

    /**
     * Returns the last path component. Interpreted as filename for singular registration, or folder
     * name for plural registration.
     *
     * <p>경로의 마지막 요소를 반환한다. 단수 등록 시 파일명, 복수 등록 시 폴더명으로 해석된다.
     *
     * @return last path component
     */
    public String last() {
        return path[path.length - 1];
    }

    /**
     * Returns the filename with {@code .yml} extension guaranteed. If {@code last()} already ends
     * with {@code .yml}, it's returned as-is.
     *
     * <p>{@code .yml} 확장자가 보장된 파일명을 반환한다.
     *
     * @return filename with extension
     */
    public String fileName() {
        String name = last();
        return name.endsWith(".yml") ? name : name + ".yml";
    }

    /**
     * For singular registration: joins all but the last element as the folder path.
     *
     * <p>단수 등록용: 마지막 요소를 제외한 나머지를 폴더 경로로 결합한다.
     *
     * @return folder path (e.g. {@code "Config/Storage"})
     */
    public String folder() {
        if (path.length == 1) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.length - 1; i++) {
            if (i > 0) sb.append('/');
            sb.append(path[i]);
        }
        return sb.toString();
    }

    /**
     * For plural registration: joins all elements as the folder path.
     *
     * <p>복수 등록용: 전체 요소를 폴더 경로로 결합한다.
     *
     * @return folder path (e.g. {@code "Config/Regions"})
     */
    public String folderPath() {
        return String.join("/", path);
    }
}
