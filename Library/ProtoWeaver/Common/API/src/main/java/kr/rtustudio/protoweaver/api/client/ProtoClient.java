package kr.rtustudio.protoweaver.api.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import kr.rtustudio.protoweaver.api.ProtoWeaver;
import kr.rtustudio.protoweaver.api.client.netty.ProtoTrustManager;
import kr.rtustudio.protoweaver.api.netty.ProtoConnection;
import kr.rtustudio.protoweaver.api.netty.Sender;
import kr.rtustudio.protoweaver.api.protocol.Protocol;
import kr.rtustudio.protoweaver.api.protocol.Side;
import kr.rtustudio.protoweaver.api.protocol.handler.ClientConnectionHandler;
import kr.rtustudio.protoweaver.api.protocol.handler.InternalConnectionHandler;
import lombok.Getter;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

import com.google.common.base.internal.Finalizer;

public class ProtoClient {

    static {
        // This exists so shadow jar does not yeet it
        Class<?> c = Finalizer.class;
    }

    @Getter private final InetSocketAddress address;
    private final SslContext sslContext;
    private final ArrayList<ConnectionEstablishedHandler> connectionEstablishedHandlers =
            new ArrayList<>();
    private final ArrayList<ConnectionLostHandler> connectionLostHandlers = new ArrayList<>();
    private EventLoopGroup workerGroup = null;
    @Getter private ProtoConnection connection = null;

    public ProtoClient(@NonNull InetSocketAddress address, @NonNull String hostsFile) {
        try {
            this.address = address;
            ProtoTrustManager trustManager =
                    new ProtoTrustManager(address.getHostName(), address.getPort(), hostsFile);
            this.sslContext =
                    SslContextBuilder.forClient().trustManager(trustManager.getTm()).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProtoClient(@NonNull InetSocketAddress address) {
        this(address.getHostName(), address.getPort());
    }

    public ProtoClient(@NonNull String host, int port, @NonNull String hostsFile) {
        this(new InetSocketAddress(host, port), hostsFile);
    }

    public ProtoClient(@NonNull String host, int port) {
        this(host, port, ".");
    }

    public ProtoClient connect(@NonNull Protocol protocol) {
        ProtoWeaver.load(protocol);

        Bootstrap b = new Bootstrap();
        workerGroup = new NioEventLoopGroup();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.handler(
                new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(@NonNull SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(
                                        "ssl",
                                        sslContext.newHandler(
                                                ch.alloc(),
                                                address.getHostName(),
                                                address.getPort()));
                        connection =
                                new ProtoConnection(
                                        InternalConnectionHandler.getProtocol(), Side.CLIENT, ch);
                    }
                });

        ChannelFuture f = b.connect(address);
        new Thread(
                        () -> {
                            try {
                                f.awaitUninterruptibly();
                                if (f.isSuccess()) {
                                    ((ClientConnectionHandler) connection.getHandler())
                                            .start(connection, protocol);
                                    // Wait for protocol to switch to passed in one
                                    while (connection == null
                                            || connection.isOpen()
                                                    && !connection
                                                            .getProtocol()
                                                            .toString()
                                                            .equals(protocol.toString()))
                                        Thread.onSpinWait();

                                    if (connection.isOpen())
                                        connectionEstablishedHandlers.forEach(
                                                handler -> {
                                                    try {
                                                        handler.handle(connection);
                                                    } catch (Exception e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                });
                                }

                                f.channel().closeFuture().sync();
                                connectionLostHandlers.forEach(
                                        handler -> {
                                            try {
                                                handler.handle(connection);
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                                connection = null;
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            } finally {
                                disconnect();
                            }
                        })
                .start();
        return this;
    }

    public boolean isConnected() {
        return !workerGroup.isShutdown()
                || !workerGroup.isShuttingDown()
                || connection != null && connection.isOpen();
    }

    public void disconnect() {
        if (connection != null) connection.disconnect();
        if (workerGroup != null && !workerGroup.isShutdown()) workerGroup.shutdownGracefully();
    }

    public ProtoClient onConnectionEstablished(@NonNull ConnectionEstablishedHandler handler) {
        connectionEstablishedHandlers.add(handler);
        return this;
    }

    public ProtoClient onConnectionLost(@NonNull ConnectionLostHandler handler) {
        this.connectionLostHandlers.add(handler);
        return this;
    }

    public Sender send(@NonNull Object packet) {
        if (connection != null) return connection.send(packet);
        return Sender.NULL;
    }

    public Protocol getCurrentProtocol() {
        return connection == null ? null : connection.getProtocol();
    }

    @FunctionalInterface
    public interface ConnectionEstablishedHandler {
        void handle(ProtoConnection connection) throws Exception;
    }

    @FunctionalInterface
    public interface ConnectionLostHandler {
        void handle(ProtoConnection connection) throws Exception;
    }
}
