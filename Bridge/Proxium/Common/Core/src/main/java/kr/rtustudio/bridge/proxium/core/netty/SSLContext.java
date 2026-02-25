package kr.rtustudio.bridge.proxium.core.netty;

import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.*;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

@Slf4j(topic = "Proxium")
public class SSLContext {

    @Getter private static io.netty.handler.ssl.SslContext context;
    private static File privateKey;
    private static File cert;

    public static void initKeystore(String dir) {
        Security.addProvider(new BouncyCastleProvider());

        privateKey = new File(dir + "/Secret/private.pem");
        cert = new File(dir + "/Secret/cert.pem");
    }

    @SneakyThrows
    public static void genKeys() {
        if (privateKey.exists() && cert.exists()) return;

        log.info("Generating TLS Keys");

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        X509Certificate certificate = genCert(kp);

        privateKey.getParentFile().mkdirs();
        @Cleanup JcaPEMWriter privateWriter = new JcaPEMWriter(new FileWriter(privateKey));
        privateWriter.writeObject(kp.getPrivate());

        @Cleanup JcaPEMWriter certWriter = new JcaPEMWriter(new FileWriter(cert));
        certWriter.writeObject(certificate);
    }

    @SneakyThrows
    public static void initContext() {
        PrivateKey key;
        try (FileReader keyReader = new FileReader(privateKey);
                PEMParser pemParser = new PEMParser(keyReader)) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            if (object instanceof PEMKeyPair) {
                key = converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo) {
                key = converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new IllegalStateException("Unknown key format: " + object.getClass());
            }
        }

        X509Certificate certificate;
        try (FileReader certReader = new FileReader(cert);
                PEMParser pemParser = new PEMParser(certReader)) {
            Object object = pemParser.readObject();
            if (object instanceof X509CertificateHolder) {
                certificate =
                        new JcaX509CertificateConverter()
                                .setProvider("BC")
                                .getCertificate((X509CertificateHolder) object);
            } else {
                throw new IllegalStateException("Unknown cert format: " + object.getClass());
            }
        }

        context =
                SslContextBuilder.forServer(key, certificate)
                        .sslProvider(OpenSsl.isAvailable() ? SslProvider.OPENSSL : SslProvider.JDK)
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .applicationProtocolConfig(
                                new ApplicationProtocolConfig(
                                        ApplicationProtocolConfig.Protocol.ALPN,
                                        ApplicationProtocolConfig.SelectorFailureBehavior
                                                .NO_ADVERTISE,
                                        ApplicationProtocolConfig.SelectedListenerFailureBehavior
                                                .ACCEPT,
                                        // ApplicationProtocolNames.HTTP_2,
                                        ApplicationProtocolNames.HTTP_1_1))
                        .build();

        log.info("Initialized TLS Context");
    }

    // From
    // https://stackoverflow.com/questions/29852290/self-signed-x509-certificate-with-bouncy-castle-in-java
    private static X509Certificate genCert(KeyPair keyPair)
            throws OperatorCreationException, CertificateException, IOException {
        Provider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);
        X500Name dnName = new X500Name("CN=PROXIUM");

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.YEAR, 999);
        Date endDate = calendar.getTime();

        ContentSigner contentSigner =
                new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        JcaX509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(
                        dnName,
                        new BigInteger(Long.toString(now)),
                        startDate,
                        endDate,
                        dnName,
                        keyPair.getPublic());
        BasicConstraints basicConstraints = new BasicConstraints(true);
        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);

        return new JcaX509CertificateConverter()
                .setProvider(bcProvider)
                .getCertificate(certBuilder.build(contentSigner));
    }
}
