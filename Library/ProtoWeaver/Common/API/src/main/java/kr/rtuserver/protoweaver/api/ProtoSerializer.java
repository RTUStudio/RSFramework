package kr.rtuserver.protoweaver.api;

import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class ProtoSerializer<T> {

    public abstract T read(ByteArrayInputStream buffer);

    public abstract void write(ByteArrayOutputStream buffer, T value);

}