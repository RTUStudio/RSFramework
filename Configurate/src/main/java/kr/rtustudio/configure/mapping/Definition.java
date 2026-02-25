package kr.rtustudio.configure.mapping;

import io.leangen.geantyref.TypeToken;

import java.lang.annotation.Annotation;

/**
 * 어노테이션, 타입, 팩토리를 묶어 Configurate 객체 매핑에 등록할 수 있는 정의 레코드입니다.
 *
 * @param <A> 어노테이션 타입
 * @param <T> 대상 타입
 * @param <F> 팩토리 타입
 * @param annotation 어노테이션 클래스
 * @param type 대상 타입 토큰
 * @param factory 팩토리 인스턴스
 */
public record Definition<A extends Annotation, T, F>(
        Class<A> annotation, TypeToken<T> type, F factory) {

    public Definition(final Class<A> annotation, final Class<T> type, final F factory) {
        this(annotation, TypeToken.get(type), factory);
    }
}
