package kr.rtuserver.framework.bukkit.api.platform;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
     * @param minVersion ex) 1.14.0
     * @param maxVersion ex) 1.21.1
     * @return isSupportVersion
     */
    public static boolean isSupport(String minVersion, String maxVersion) {
        Version min = new Version(minVersion);
        Version max = new Version(maxVersion);
        return min.getVersion() <= VERSION.getVersion() && max.getVersion() >= VERSION.getVersion();
    }

    public static boolean isSupport(List<String> versions) {
        return versions.contains(VERSION_STR);
    }

    /***
     * check server is higher than minVersion
     * @return isSupportVersion
     */
    public static boolean isSupport(String minVersion) {
        Version min = new Version(minVersion);
        return min.getVersion() <= VERSION.getVersion();
    }

    @Deprecated
    public static boolean isLegacy() {
        return hasClass("net.minecraft.server.MinecraftServer");
    }

    public static boolean isPaper() {
        return hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
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
        return switch (version.getVersion()) {
            case 1171 -> "v1_17_R1";
            case 1180, 1181 -> "v1_18_R1";
            case 1182 -> "v1_18_R2";
            case 1190, 1191, 1192 -> "v1_19_R1";
            case 1193 -> "v1_19_R2";
            case 1194 -> "v1_19_R3";
            case 1200, 1201 -> "v1_20_R1";
            case 1202 -> "v1_20_R2";
            case 1203, 1204 -> "v1_20_R3";
            case 1205, 1206 -> "v1_20_R4";
            case 1210, 1211 -> "v1_21_R1";
            case 1212, 1213 -> "v1_21_R2";
            case 1214 -> "v1_21_R3";
            case 1215 -> "v1_21_R4";
            case 1216, 1217 -> "v1_21_R5";
            default -> "v1_21_R5";
        };
    }

    @Getter
    public static class Version {

        private final int major;
        private final int minor;
        private final int patch;
        private final int version;

        public Version(String version) {
            String[] numbers = version.split("\\.");
            String majorStr = numbers.length == 0 ? "0" : numbers[0];
            String minorStr = numbers.length <= 1 ? "00" : numbers[1];
            String patchStr = numbers.length <= 2 ? "0" : numbers[2];
            this.major = Integer.parseInt(majorStr);
            this.minor = Integer.parseInt(minorStr);
            this.patch = Integer.parseInt(patchStr);
            this.version = Integer.parseInt(majorStr + minorStr + patchStr);
        }

    }


}
