package kr.rtustudio.framework.bukkit.api.platform;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Detects the current server's Minecraft version, provides version compatibility checks and NMS
 * mapping. Also supports Paper/Folia environment detection.
 *
 * <p>현재 서버의 마인크래프트 버전을 감지하고, 버전 호환성 검사 및 NMS 매핑을 제공하는 유틸리티 클래스. Paper/Folia 환경 감지도 지원한다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MinecraftVersion {

    private static final String VERSION_STR = fromAPI(Bukkit.getBukkitVersion());
    private static final Version VERSION = new Version(VERSION_STR);
    private static final boolean IS_PAPER =
            hasClass("com.destroystokyo.paper.PaperConfig")
                    || hasClass("io.papermc.paper.configuration.Configuration");
    private static final boolean IS_FOLIA =
            hasClass("io.papermc.paper.threadedregions.RegionizedServer");

    @NotNull
    public static String fromAPI(String version) {
        return version.split("-")[0];
    }

    /**
     * Checks whether the server version is within the specified range.
     *
     * <p>서버 버전이 지정한 범위 내에 있는지 확인한다.
     *
     * @param minVersion minimum version (e.g. {@code "1.20.1"})
     * @param maxVersion maximum version (e.g. {@code "1.21.11"})
     * @return whether within range
     */
    public static boolean isSupport(String minVersion, String maxVersion) {
        Version min = new Version(minVersion);
        Version max = new Version(maxVersion);
        if (min.getMajor() > VERSION.getMajor() || max.getMajor() < VERSION.getMajor())
            return false;
        if (min.getMinor() > VERSION.getMinor() || max.getMinor() < VERSION.getMinor())
            return false;
        return min.getPatch() <= VERSION.getPatch() && max.getPatch() >= VERSION.getPatch();
    }

    public static boolean isSupport(List<String> versions) {
        return versions.contains(VERSION_STR);
    }

    /**
     * Checks whether the server version is at least the specified minimum.
     *
     * <p>서버 버전이 지정한 최소 버전 이상인지 확인한다.
     *
     * @param minVersion minimum version (e.g. {@code "1.20.1"})
     * @return whether supported
     */
    public static boolean isSupport(String minVersion) {
        Version min = new Version(minVersion);
        if (min.getMajor() > VERSION.getMajor()) return false;
        if (min.getMinor() > VERSION.getMinor()) return false;
        return min.getPatch() <= VERSION.getPatch();
    }

    @Deprecated
    public static boolean isLegacy() {
        return hasClass("net.minecraft.server.MinecraftServer");
    }

    /**
     * Checks whether the current server is Paper-based.
     *
     * <p>현재 서버가 Paper 기반인지 확인한다.
     */
    public static boolean isPaper() {
        return IS_PAPER;
    }

    /**
     * Checks whether the current server is Folia-based.
     *
     * <p>현재 서버가 Folia 기반인지 확인한다.
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Returns the server version string (e.g. {@code "1.21.5"}).
     *
     * <p>서버 버전 문자열을 반환한다.
     */
    @NotNull
    public static String getAsText() {
        return VERSION_STR;
    }

    /**
     * Returns the parsed server version object.
     *
     * <p>서버 버전 객체를 반환한다.
     */
    @NotNull
    public static Version get() {
        return VERSION;
    }

    /**
     * Returns the NMS package version corresponding to a Minecraft version string.
     *
     * <p>버전 문자열에 대응하는 NMS 패키지 버전을 반환한다.
     *
     * @param versionStr Minecraft version (e.g. {@code "1.21.5"})
     * @return NMS package version (e.g. {@code "v1_21_R4"})
     * @throws IllegalArgumentException if the version is unsupported
     */
    @NotNull
    public static String getNMS(String versionStr) {
        Version version = new Version(versionStr);
        return switch (version.getMajor()) {
            case 1 ->
                    switch (version.getMinor()) {
                        case 20 ->
                                switch (version.getPatch()) {
                                    case 0, 1 -> "v1_20_R1";
                                    case 2 -> "v1_20_R2";
                                    case 3, 4 -> "v1_20_R3";
                                    default -> "v1_20_R4"; // 1.20.5, 1.20.6
                                };
                        case 21 ->
                                switch (version.getPatch()) {
                                    case 0, 1 -> "v1_21_R1";
                                    case 2, 3 -> "v1_21_R2";
                                    case 4 -> "v1_21_R3";
                                    case 5 -> "v1_21_R4";
                                    case 6, 7, 8 -> "v1_21_R5";
                                    case 9, 10 -> "v1_21_R6";
                                    default -> "v1_21_R7"; // 1.21.11
                                };
                        default ->
                                throw new IllegalArgumentException(
                                        "Invalid minor version: " + versionStr);
                    };
                // case 26 -> // 26.1.0
            default -> throw new IllegalArgumentException("Invalid major version: " + versionStr);
        };
    }

    /**
     * Parses a Minecraft version string into major.minor.patch components.
     *
     * <p>마인크래프트 버전을 major.minor.patch로 파싱하여 보관하는 클래스.
     */
    @Getter
    public static class Version {

        private final int major;
        private final int minor;
        private final int patch;

        public Version(String version) {
            String[] numbers = version.split("\\.");
            String majorStr = numbers.length == 0 ? "0" : numbers[0];
            String minorStr = numbers.length <= 1 ? "0" : numbers[1];
            String patchStr = numbers.length <= 2 ? "0" : numbers[2];
            this.major = Integer.parseInt(majorStr);
            this.minor = Integer.parseInt(minorStr);
            this.patch = Integer.parseInt(patchStr);
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}
