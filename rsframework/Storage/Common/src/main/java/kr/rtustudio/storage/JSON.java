package kr.rtustudio.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** {@link JsonObject}를 빌더 패턴으로 구성할 수 있는 유틸리티 클래스입니다. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JSON {

    private final JsonObject json = new JsonObject();

    @NotNull
    public static JSON of() {
        return new JSON();
    }

    @NotNull
    public static JSON of(@NotNull String key, @NotNull JsonElement value) {
        return new JSON().append(key, value);
    }

    @NotNull
    public static JSON of(@NotNull String key, @NotNull Number value) {
        return new JSON().append(key, value);
    }

    @NotNull
    public static JSON of(@NotNull String key, @NotNull String value) {
        return new JSON().append(key, value);
    }

    @NotNull
    public static JSON of(@NotNull String key, @NotNull Boolean value) {
        return new JSON().append(key, value);
    }

    @NotNull
    public static JSON of(@NotNull String key, @NotNull Character value) {
        return new JSON().append(key, value);
    }

    @NotNull
    public JSON append(@NotNull String key, @NotNull JsonElement value) {
        json.add(key, value);
        return this;
    }

    @NotNull
    public JSON append(@NotNull String key, @NotNull Number value) {
        json.addProperty(key, value);
        return this;
    }

    @NotNull
    public JSON append(@NotNull String key, @NotNull String value) {
        json.addProperty(key, value);
        return this;
    }

    @NotNull
    public JSON append(@NotNull String key, @NotNull Boolean value) {
        json.addProperty(key, value);
        return this;
    }

    @NotNull
    public JSON append(@NotNull String key, @NotNull Character value) {
        json.addProperty(key, value);
        return this;
    }

    @NotNull
    public JsonObject get() {
        return json;
    }
}
