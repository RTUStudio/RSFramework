package kr.rtuserver.protoweaver.api.protocol.serializer;

import kr.rtuserver.protoweaver.api.ProtoSerializer;
import kr.rtuserver.protoweaver.api.protocol.internal.CustomPacket;
import org.apache.fury.memory.Platform;

import java.io.*;

public class CustomPacketSerializer extends ProtoSerializer<CustomPacket> {

    public CustomPacketSerializer(Class<CustomPacket> type) {
        super(type);
    }

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
            return new CustomPacket(
                    dis.readUTF(),
                    dis.readUTF()
            );
        } catch (IOException e) {
            Platform.throwException(e);
            throw new RuntimeException(e);
        }
    }
}