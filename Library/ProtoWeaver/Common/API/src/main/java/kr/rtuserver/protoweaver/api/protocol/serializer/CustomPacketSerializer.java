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
        System.out.println("[CPS] " + value.classType() + ": " + value.json());
        try (DataOutputStream dos = new DataOutputStream(buffer)) {
            System.out.println("[CPS] 1");
            dos.writeUTF(value.classType());
            System.out.println("[CPS] 2");
            dos.writeUTF(value.json());
            System.out.println("[CPS] 3");
        } catch (IOException e) {
            Platform.throwException(e);
        }
        System.out.println("[CPS] 4");
    }

    @Override
    public CustomPacket read(ByteArrayInputStream buffer) {
        System.out.println("[CPR] " + buffer);
        try (DataInputStream dis = new DataInputStream(buffer)) {
            System.out.println("[CPR] 1");
            return new CustomPacket(
                    dis.readUTF(),
                    dis.readUTF()
            );
        } catch (IOException e) {
            Platform.throwException(e);
            System.out.println("[CPR] 2");
            throw new RuntimeException(e);
        }
    }
}