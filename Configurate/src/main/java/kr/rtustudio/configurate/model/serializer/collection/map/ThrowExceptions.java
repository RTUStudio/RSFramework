package kr.rtustudio.configurate.model.serializer.collection.map;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link MapSerializer}에서 개별 항목 실패 시 예외를 던지도록 강제하는 어노테이션입니다.
 *
 * <p>이 어노테이션이 없으면 실패한 항목은 로그만 남기고 건너뜁니다.
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThrowExceptions {}
