package kr.rtuserver.protoweaver.api.protocol.internal;

import com.google.gson.JsonObject;

public record StorageSync(String plugin, String name, JsonObject json) implements GlobalPacket {
}
