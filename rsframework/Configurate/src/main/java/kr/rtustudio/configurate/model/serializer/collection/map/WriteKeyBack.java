package kr.rtustudio.configurate.model.serializer.collection.map;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link MapSerializer}에서 역직렬화 후 키를 다시 직렬화하여 노드에 반영하도록 지정하는 어노테이션입니다.
 *
 * <p>키가 역직렬화 시 정규화되는 경우, 변경된 키를 설정 파일에 다시 기록합니다.
 */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteKeyBack {}
