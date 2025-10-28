package kr.rtustudio.framework.bukkit.api.platform;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MinecraftVersion {

    private static final String VERSION_STR = fromAPI(Bukkit.getBukkitVersion());
    private static final Version VERSION = new Version(VERSION_STR);

    @NotNull
    public static String fromAPI(String version) {
        return version.split("-")[0];
    }

    /***
     * if you use Bukkit#getBukkitVersion, use fromAPI
     *
     * @param minVersion
     *            ex) 1.14.0
     * @param maxVersion
     *            ex) 1.21.1
     * @return isSupportVersion
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

    /***
     * check server is higher than minVersion
     *
     * @return isSupportVersion
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

    public static boolean isPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig")
                || hasClass("io.papermc.paper.configuration.Configuration");
    }

    public static boolean isFolia() {
        return hasClass("io.papermc.paper.threadedregions.RegionizedServer");
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @NotNull
    public static String getAsText() {
        return VERSION_STR;
    }

    @NotNull
    public static Version get() {
        return VERSION;
    }

    @NotNull
    public static String getNMS(String versionStr) {
        Version version = new Version(versionStr);
        return switch (version.getMinor()) {
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
                        case 6, 7, 8 -> "v1_21_R5"; // 1.21.6, 1.21.7, 1.21.8
                        default -> "v1_21_R6"; // 1.21.9, 1.21.10
                    };
            default -> "v1_21_R6";
        };
    }

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
