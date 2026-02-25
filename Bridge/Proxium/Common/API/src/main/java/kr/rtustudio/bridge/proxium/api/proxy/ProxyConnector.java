package kr.rtustudio.bridge.proxium.api.proxy;

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
import kr.rtustudio.bridge.proxium.api.ProxiumAPI;
import kr.rtustudio.bridge.proxium.api.netty.Connection;
import kr.rtustudio.bridge.proxium.api.netty.Sender;
import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.protocol.Side;
import kr.rtustudio.bridge.proxium.api.protocol.handler.InternalConnectionHandler;
import kr.rtustudio.bridge.proxium.api.protocol.handler.ProxyConnectionHandler;
import kr.rtustudio.bridge.proxium.api.proxy.netty.ProxiumTrustManager;
import lombok.Getter;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

import com.google.common.base.internal.Finalizer;

public class ProxyConnector {

    static {
        // This exists so shadow jar does not yeet it
        Class<?> c = Finalizer.class;
    }

    @Getter private final InetSocketAddress address;
    private final SslContext sslContext;
    private final ProxiumTrustManager trustManager;
    private final ArrayList<ConnectionEventHandler> connectionEstablishedHandlers =
            new ArrayList<>();
    private final ArrayList<ConnectionEventHandler> connectionLostHandlers = new ArrayList<>();
    private EventLoopGroup workerGroup = null;
    @Getter private Connection connection = null;

    public ProxyConnector(@NonNull InetSocketAddress address, @NonNull String hostsFile) {
        try {
            this.address = address;
            trustManager =
                    new ProxiumTrustManager(address.getHostName(), address.getPort(), hostsFile);
            this.sslContext = SslContextBuilder.forClient().trustManager(trustManager).build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public ProxyConnector(@NonNull InetSocketAddress address) {
        this(address.getHostName(), address.getPort());
    }

    public ProxyConnector(@NonNull String host, int port, @NonNull String hostsFile) {
        this(new InetSocketAddress(host, port), hostsFile);
    }

    public ProxyConnector(@NonNull String host, int port) {
        this(host, port, ".");
    }

    public ProxyConnector connect(@NonNull Protocol protocol) {
        ProxiumAPI.load(protocol);

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
                                new Connection(
                                        InternalConnectionHandler.getProtocol(), Side.PROXY, ch);
                    }
                });

        ChannelFuture f = b.connect(address);
        new Thread(
                        () -> {
                            try {
                                f.awaitUninterruptibly();
                                if (f.isSuccess()) {
                                    ((ProxyConnectionHandler) connection.getHandler())
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

    public ProxyConnector onConnectionEstablished(@NonNull ConnectionEventHandler handler) {
        connectionEstablishedHandlers.add(handler);
        return this;
    }

    public ProxyConnector onConnectionLost(@NonNull ConnectionEventHandler handler) {
        this.connectionLostHandlers.add(handler);
        return this;
    }

    public ProxyConnector onCertificateRejected(
            @NonNull ProxiumTrustManager.CertificateEventHandler handler) {
        this.trustManager.onCertificateRejected(handler);
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
    public interface ConnectionEventHandler {
        void handle(Connection connection) throws Exception;
    }
}
