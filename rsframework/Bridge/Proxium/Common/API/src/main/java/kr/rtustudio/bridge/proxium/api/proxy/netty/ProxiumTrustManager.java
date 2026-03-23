package kr.rtustudio.bridge.proxium.api.proxy.netty;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.EmptyArrays;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;

import javax.net.ssl.X509TrustManager;

/**
 * This trust manager is based off the {@link
 * io.netty.handler.ssl.util.FingerprintTrustManagerFactory} but modified to function in the manner
 * that ssh does (By always trusting the first connection and saving its fingerprint).
 */
public class ProxiumTrustManager implements X509TrustManager {

    private static final FastThreadLocal<MessageDigest> localMessageDigest =
            new FastThreadLocal<>() {

                @Override
                protected MessageDigest initialValue() {
                    try {
                        return MessageDigest.getInstance("SHA256");
                    } catch (NoSuchAlgorithmException e) {
                        throw new IllegalArgumentException("Unsupported hash algorithm", e);
                    }
                }
            };
    private static final Object lock = new Object();
    private final File hostsFile;
    private final String hostId;
    private final ArrayList<CertificateEventHandler> certificateRejectionHandlers =
            new ArrayList<>();
    private byte[] trusted = null;

    public ProxiumTrustManager(String host, int port, String file) {
        hostsFile = new File(file + File.separator + "known_hosts");
        this.hostId = host + ":" + port;
        if (!hostsFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(hostsFile))) {
            reader.lines()
                    .filter(l -> l.startsWith(host + ":" + port))
                    .findFirst()
                    .ifPresent(
                            l -> {
                                try {
                                    String hex = l.split("=")[1].strip();
                                    if (hex.length() % 2 == 0 && !hex.isEmpty()) {
                                        trusted = HexFormat.of().parseHex(hex);
                                    }
                                } catch (Exception ignored) {
                                }
                            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onCertificateRejected(@NonNull CertificateEventHandler handler) {
        certificateRejectionHandlers.add(handler);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String s) {
        checkTrusted(chain);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String s) {
        checkTrusted(chain);
    }

    @SneakyThrows
    private void checkTrusted(X509Certificate[] chain) {
        MessageDigest md = localMessageDigest.get();
        md.reset();
        byte[] fingerprint = md.digest(chain[0].getEncoded());

        if (trusted == null) {
            synchronized (lock) {
                hostsFile.getParentFile().mkdirs();
                hostsFile.createNewFile();

                @Cleanup
                BufferedWriter writer = new BufferedWriter(new FileWriter(hostsFile, true));
                writer.append(hostId)
                        .append("=")
                        .append(HexFormat.of().formatHex(fingerprint))
                        .append("\n");
                trusted = fingerprint;
            }
            return;
        }

        if (Arrays.equals(trusted, fingerprint)) return;

        certificateRejectionHandlers.forEach(handler -> handler.handle(trusted, fingerprint));
        throw new CertificateException(
                "proxium-proxy-cert-error:"
                        + hostId
                        + ":"
                        + HexFormat.of().formatHex(trusted)
                        + "!="
                        + HexFormat.of().formatHex(fingerprint));
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return EmptyArrays.EMPTY_X509_CERTIFICATES;
    }

    @FunctionalInterface
    public interface CertificateEventHandler {
        void handle(byte[] expected, byte[] actual);
    }
}
