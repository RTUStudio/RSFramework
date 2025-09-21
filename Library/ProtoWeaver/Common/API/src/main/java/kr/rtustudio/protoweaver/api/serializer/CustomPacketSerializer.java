package kr.rtustudio.protoweaver.api.serializer;

import kr.rtustudio.protoweaver.api.protocol.internal.CustomPacket;

import java.io.*;

import org.apache.fury.memory.Platform;

public class CustomPacketSerializer extends ProtoSerializer<CustomPacket> {

    @Override
    public void write(ByteArrayOutputStream buffer, CustomPacket value) {
        try (DataOutputStream dos = new DataOutputStream(buffer)) {
            dos.writeUTF(value.classType());
            dos.writeUTF(value.json());
        } catch (IOException e) {
            Platform.throwException(e);
        }
    }

    @Override
    public CustomPacket read(ByteArrayInputStream buffer) {
        try (DataInputStream dis = new DataInputStream(buffer)) {
            return new CustomPacket(dis.readUTF(), dis.readUTF());
        } catch (IOException e) {
            Platform.throwException(e);
            throw new RuntimeException(e);
        }
    }
}
