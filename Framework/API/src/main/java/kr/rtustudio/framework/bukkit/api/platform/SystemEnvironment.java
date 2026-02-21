package kr.rtustudio.framework.bukkit.api.platform;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 서버 시스템 환경 정보를 조회하는 유틸리티 클래스입니다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemEnvironment {

    /** 운영체제 이름을 반환한다. */
    public static String getOS() {
        return System.getProperty("os.name");
    }

    /** JDK 버전을 반환한다. */
    public static String getJDKVersion() {
        return System.getProperty("java.version");
    }
}
