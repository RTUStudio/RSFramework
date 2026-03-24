package kr.rtustudio.framework.bukkit.api.platform;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for querying server system environment information.
 *
 * <p>서버 시스템 환경 정보를 조회하는 유틸리티 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemEnvironment {

    /** Returns the OS name. / 운영체제 이름을 반환한다. */
    public static String getOS() {
        return System.getProperty("os.name");
    }

    /** Returns the JDK version. / JDK 버전을 반환한다. */
    public static String getJDKVersion() {
        return System.getProperty("java.version");
    }
}
