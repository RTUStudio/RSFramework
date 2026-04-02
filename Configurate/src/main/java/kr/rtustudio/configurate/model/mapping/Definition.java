package kr.rtustudio.configurate.model.mapping;

import io.leangen.geantyref.TypeToken;

import java.lang.annotation.Annotation;

/**
 * Definition record that bundles an annotation, type, and factory for registration in Configurate
 * object mapping.
 *
 * <p>어노테이션, 타입, 팩토리를 묶어 Configurate 객체 매핑에 등록할 수 있는 정의 레코드.
 *
 * @param <A> annotation type
 * @param <T> target type
 * @param <F> factory type
 * @param annotation annotation class
 * @param type target type token
 * @param factory factory instance
 */
public record Definition<A extends Annotation, T, F>(
        Class<A> annotation, TypeToken<T> type, F factory) {

    public Definition(final Class<A> annotation, final Class<T> type, final F factory) {
        this(annotation, TypeToken.get(type), factory);
    }
}
