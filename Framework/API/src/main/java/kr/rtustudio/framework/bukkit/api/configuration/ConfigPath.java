package kr.rtustudio.framework.bukkit.api.configuration;

/**
 * 설정 파일/폴더 경로를 나타내는 레코드.
 *
 * <p>가변인자 {@code path}로 경로를 구성하며, 단수/복수 등록 메서드에 따라 해석이 달라진다.
 *
 * <ul>
 *   <li>{@code registerConfiguration} (단수) — {@link #last()}가 <b>파일명</b>, {@link #first()}부터 그 앞까지가
 *       폴더
 *   <li>{@code registerConfigurations} (복수) — 전체 경로가 <b>폴더</b>
 * </ul>
 *
 * <h3>{@code of()} — {@code Config/} 접두사 자동 부여</h3>
 *
 * <pre>{@code
 * ConfigPath.of("Setting")                // Config/Setting.yml
 * ConfigPath.of("Storage", "MySQL")       // Config/Storage/MySQL.yml
 * ConfigPath.of("Regions")               // (복수) Config/Regions/*.yml
 * }</pre>
 *
 * <h3>{@code relative()} — 접두사 없이 그대로</h3>
 *
 * <pre>{@code
 * ConfigPath.relative("Bridge", "Redis")           // Bridge/Redis.yml
 * ConfigPath.relative("Translation", "Message", "ko") // Translation/Message/ko.yml
 * }</pre>
 *
 * @param path 경로 구성 요소 ({@code of()}로 생성 시 첫 요소가 {@code "Config"})
 * @param version 설정 파일 버전 (nullable)
 */
public record ConfigPath(String[] path, Integer version) {

    /**
     * {@code Config/} 접두사를 자동으로 붙여 경로를 생성한다.
     *
     * <p>{@code ConfigPath.of("Storage", "MySQL")} → {@code Config/Storage/MySQL.yml}
     *
     * @param path 경로 구성 요소 (최소 1개, {@code Config}는 자동 추가)
     * @return 새 {@link ConfigPath}
     */
    public static ConfigPath of(String... path) {
        if (path.length == 0) throw new IllegalArgumentException("path must not be empty");
        String[] full = new String[path.length + 1];
        full[0] = "Config";
        System.arraycopy(path, 0, full, 1, path.length);
        return new ConfigPath(full, null);
    }

    /**
     * 접두사 없이 경로를 그대로 사용한다.
     *
     * <p>{@code ConfigPath.relative("Bridge", "Redis")} → {@code Bridge/Redis.yml}
     *
     * @param path 경로 구성 요소 (최소 1개)
     * @return 새 {@link ConfigPath}
     */
    public static ConfigPath relative(String... path) {
        if (path.length == 0) throw new IllegalArgumentException("path must not be empty");
        return new ConfigPath(path, null);
    }

    /**
     * 버전을 지정한 새 {@link ConfigPath}를 반환한다.
     *
     * @param v 설정 파일 버전
     * @return 버전이 지정된 새 인스턴스
     */
    public ConfigPath version(int v) {
        return new ConfigPath(path, v);
    }

    /**
     * 경로의 첫 번째 요소를 반환한다.
     *
     * @return 첫 번째 경로 요소 (예: {@code "Config"})
     */
    public String first() {
        return path[0];
    }

    /**
     * 경로의 마지막 요소를 반환한다.
     *
     * <p>단수 등록 시 파일명, 복수 등록 시 폴더명으로 해석된다.
     *
     * @return 마지막 경로 요소 (예: {@code "MySQL"}, {@code "Regions"})
     */
    public String last() {
        return path[path.length - 1];
    }

    /**
     * 단수 등록용: 마지막 요소를 제외한 나머지를 폴더 경로로 결합한다.
     *
     * @return 폴더 경로 (예: {@code "Config/Storage"})
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
     * 복수 등록용: 전체 요소를 폴더 경로로 결합한다.
     *
     * @return 폴더 경로 (예: {@code "Config/Regions"})
     */
    public String folderPath() {
        return String.join("/", path);
    }
}
