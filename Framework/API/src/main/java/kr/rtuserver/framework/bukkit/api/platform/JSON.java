package kr.rtuserver.framework.bukkit.api.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NoArgsConstructor;

@NoArgsConstructor
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
