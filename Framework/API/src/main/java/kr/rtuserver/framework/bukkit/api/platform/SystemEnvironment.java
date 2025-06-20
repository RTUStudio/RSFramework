package kr.rtuserver.framework.bukkit.api.platform;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemEnvironment {

    public static String getOS() {
        return System.getProperty("os.name");
    }

    public static String getJDKVersion() {
        return System.getProperty("java.version");
    }

}
