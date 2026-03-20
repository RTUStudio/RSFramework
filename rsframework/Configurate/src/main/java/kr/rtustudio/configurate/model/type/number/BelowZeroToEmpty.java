package kr.rtustudio.configurate.model.type.number;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link IntOr} 또는 {@link DoubleOr} 필드에 적용하여, 음수 값을 빈 값(empty)으로 치환하는 어노테이션.
 *
 * <p>역직렬화 시 값이 0 미만이면 해당 타입의 empty 인스턴스({@code USE_DEFAULT} 또는 {@code DISABLED})로 변환된다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BelowZeroToEmpty {}
