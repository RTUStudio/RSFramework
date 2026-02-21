package kr.rtustudio.framework.bukkit.api.platform;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** {@link JsonObject}를 빌더 패턴으로 구성할 수 있는 유틸리티 클래스입니다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JSON {

    private final JsonObject json = new JsonObject();

    public static JSON of() {
        return new JSON();
    }

    public static JSON of(String key, JsonElement value) {
        return new JSON().append(key, value);
    }

    public static JSON of(String key, Number value) {
        return new JSON().append(key, value);
    }

    public static JSON of(String key, String value) {
        return new JSON().append(key, value);
    }

    public static JSON of(String key, Boolean value) {
        return new JSON().append(key, value);
    }

    public static JSON of(String key, Character value) {
        return new JSON().append(key, value);
    }

    public JSON append(String key, JsonElement value) {
        json.add(key, value);
        return this;
    }

    public JSON append(String key, Number value) {
        json.addProperty(key, value);
        return this;
    }

    public JSON append(String key, String value) {
        json.addProperty(key, value);
        return this;
    }

    public JSON append(String key, Boolean value) {
        json.addProperty(key, value);
        return this;
    }

    public JSON append(String key, Character value) {
        json.addProperty(key, value);
        return this;
    }

    public JsonObject get() {
        return json;
    }
}
