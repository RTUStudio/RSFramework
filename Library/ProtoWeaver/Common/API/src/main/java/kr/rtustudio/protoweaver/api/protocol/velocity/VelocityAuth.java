package kr.rtustudio.protoweaver.api.protocol.velocity;

import kr.rtustudio.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.protoweaver.api.protocol.handler.auth.ClientAuthHandler;
import kr.rtustudio.protoweaver.api.protocol.handler.auth.ServerAuthHandler;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
public class VelocityAuth implements ServerAuthHandler, ClientAuthHandler {

    @Setter private static byte[] secret = null;

    @Override
    public boolean handleAuth(ProtoConnection connection, byte[] key) {
        return Arrays.equals(key, secret);
    }

    @Override
    public byte[] getSecret() {
        File file = new File("forwarding.secret");
        if (!file.exists()) return null;

        try {
            @Cleanup BufferedReader reader = new BufferedReader(new FileReader(file));
            return reader.readLine().getBytes(StandardCharsets.UTF_8);
        } catch (IOException ignore) {
            return null;
        }
    }
}
