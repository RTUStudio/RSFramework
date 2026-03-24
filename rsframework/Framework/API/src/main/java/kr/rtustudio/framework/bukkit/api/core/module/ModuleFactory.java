package kr.rtustudio.framework.bukkit.api.core.module;

/** {@link Module} 인스턴스를 생성·관리하는 팩토리 인터페이스입니다. */
public interface ModuleFactory {

    /**
     * 지정한 타입의 모듈을 조회한다.
     *
     * @param type 모듈 인터페이스 클래스
     * @param <T> 모듈 타입
     * @return 등록된 모듈 인스턴스
     */
    <T extends Module> T getModule(Class<T> type);

    /**
     * 모듈 인스턴스를 등록하거나 교체한다.
     *
     * @param module 등록할 모듈 인스턴스
     * @param <T> 모듈 타입
     */
    <T extends Module> void setModule(T module);

    /** 모든 모듈의 설정을 리로드한다. */
    void reload();
}
