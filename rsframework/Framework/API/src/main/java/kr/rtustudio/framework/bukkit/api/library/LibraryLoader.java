package kr.rtustudio.framework.bukkit.api.library;

import kr.rtustudio.framework.bukkit.api.RSPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;

/**
 * Maven 형식의 라이브러리를 런타임에 동적으로 로드하는 유틸리티 클래스입니다.
 *
 * <p>의존성 형식: {@code groupId:artifactId:version[:classifier]}
 *
 * <p>Libby 라이브러리 매니저를 사용하며, Maven Central, JitPack, PaperMC 리포지토리를 기본 지원합니다.
 */
public class LibraryLoader {

    private final Logger logger;
    private final BukkitLibraryManager manager;

    public LibraryLoader(@NotNull RSPlugin plugin) {
        this.logger = plugin.getLogger();
        this.manager = new BukkitLibraryManager(plugin, "Library");
        this.manager.addMavenCentral();
        this.manager.addJitPack();
        this.manager.addRepository("https://repo.papermc.io/repository/maven-public/");
    }

    /**
     * 커스텀 Maven 리포지토리를 추가한다.
     *
     * @param url 리포지토리 URL
     */
    public void addRepository(@NotNull String url) {
        this.manager.addRepository(Objects.requireNonNull(url, "Repository URL cannot be null"));
    }

    /**
     * 지정한 의존성을 로드한다.
     *
     * @param dependency {@code groupId:artifactId:version[:classifier]} 형식 문자열
     */
    public void load(@NotNull String dependency) {
        builder(dependency).load();
    }

    /**
     * 패키지 재배치(relocation)를 적용하여 의존성을 로드한다.
     *
     * @param dependency {@code groupId:artifactId:version[:classifier]} 형식 문자열
     * @param pattern 원본 패키지 패턴
     * @param relocatedPattern 재배치 대상 패키지 패턴
     */
    public void load(
            @NotNull String dependency, @NotNull String pattern, @NotNull String relocatedPattern) {
        builder(dependency).relocate(pattern, relocatedPattern).load();
    }

    /**
     * 의존성 로딩을 세밀하게 제어할 수 있는 빌더를 생성한다.
     *
     * @param dependency {@code groupId:artifactId:version[:classifier]} 형식 문자열
     * @return 라이브러리 빌더
     */
    public LibraryBuilder builder(@NotNull String dependency) {
        return new LibraryBuilder(parseDependency(dependency));
    }

    private DependencyInfo parseDependency(@NotNull String dependency) {
        String[] parts = Objects.requireNonNull(dependency, "Dependency cannot be null").split(":");
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException(
                    "Invalid format. Expected 'groupId:artifactId:version[:classifier]', got: "
                            + dependency);
        }

        String groupId = parts[0].trim();
        String artifactId = parts[1].trim();
        String version = parts[2].trim();
        String classifier = parts.length == 4 ? parts[3].trim() : null;

        if (groupId.isEmpty() || artifactId.isEmpty() || version.isEmpty()) {
            throw new IllegalArgumentException(
                    "GroupId, artifactId, and version cannot be empty: " + dependency);
        }

        return new DependencyInfo(groupId, artifactId, version, classifier);
    }

    private void loadLibrary(@NotNull LibraryBuilder builder) {
        try {
            Library library = builder.build();
            this.manager.loadLibrary(library);
        } catch (Exception e) {
            DependencyInfo info = builder.info;
            this.logger.log(Level.SEVERE, "Failed to load library: " + info.fullName(), e);
        }
    }

    private record DependencyInfo(
            String groupId, String artifactId, String version, @Nullable String classifier) {
        String fullName() {
            return classifier != null
                    ? "%s:%s:%s:%s".formatted(groupId, artifactId, version, classifier)
                    : "%s:%s:%s".formatted(groupId, artifactId, version);
        }
    }

    /** 패키지 재배치 정보를 보관하는 레코드. */
    public record Relocation(String pattern, String relocatedPattern) {
        public Relocation {
            Objects.requireNonNull(pattern, "Pattern cannot be null");
            Objects.requireNonNull(relocatedPattern, "Relocated pattern cannot be null");
        }
    }

    /** 라이브러리 로딩을 위한 빌더. 재배치, 체크섬 등을 설정할 수 있다. */
    public class LibraryBuilder {
        private final DependencyInfo info;
        private final List<Relocation> relocations = new ArrayList<>();
        private String checksum;

        private LibraryBuilder(DependencyInfo info) {
            this.info = info;
        }

        /**
         * 패키지 재배치를 추가한다.
         *
         * @param pattern 원본 패키지 패턴
         * @param relocatedPattern 재배치 대상 패키지 패턴
         * @return 이 빌더
         */
        public LibraryBuilder relocate(@NotNull String pattern, @NotNull String relocatedPattern) {
            this.relocations.add(new Relocation(pattern, relocatedPattern));
            return this;
        }

        /**
         * SHA-256 체크섬을 설정한다.
         *
         * @param checksum SHA-256 해시 문자열
         * @return 이 빌더
         */
        public LibraryBuilder checksum(@NotNull String checksum) {
            this.checksum = Objects.requireNonNull(checksum, "Checksum cannot be null");
            return this;
        }

        /** 설정된 옵션으로 라이브러리를 로드한다. */
        public void load() {
            loadLibrary(this);
        }

        private Library build() {
            Library.Builder builder =
                    Library.builder()
                            .groupId(this.info.groupId)
                            .artifactId(this.info.artifactId)
                            .version(this.info.version);

            if (this.info.classifier != null) builder.classifier(this.info.classifier);
            if (this.checksum != null) builder.checksum(this.checksum);
            this.relocations.forEach(r -> builder.relocate(r.pattern(), r.relocatedPattern()));

            return builder.build();
        }
    }
}
