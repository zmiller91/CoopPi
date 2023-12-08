package org.example;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PiAuth {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final ContentSigner signer;
    private final X509CertificateHolder certificate;
    private final CMSSignedDataGenerator generator;

    public PiAuth(PrivateKey privateKey, PublicKey publicKey) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.signer = buildSigner();
        this.certificate = buildCertificate();
        this.generator = buildGenerator();

    }

    public CMSSignedData sign(String message) {
        try {
            CMSTypedData data = new CMSProcessableByteArray(message.getBytes());
            return generator.generate(data, true);
        } catch (CMSException e) {
            throw new RuntimeException(e);
        }
    }

    private CMSSignedDataGenerator buildGenerator() {
        try {

            JcaCertStore certs = new JcaCertStore(List.of(certificate));
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(signer, certificate));
            gen.addCertificates(certs);
            return gen;

        } catch (CertificateEncodingException | OperatorCreationException | CMSException e) {
            throw new RuntimeException(e);
        }
    }

    private X509CertificateHolder buildCertificate() {
        return new X509v3CertificateBuilder(
                new X500Name("CN=coop.zackmiller.info"),
                BigInteger.valueOf(12345L), // unique serial number
                new Date(),
                new Date(),
                new X500Name("CN=coop.name"),
                new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSAES_OAEP), publicKey.getEncoded())
        ).build(signer);
    }

    private ContentSigner buildSigner() {
        try {
            return new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privateKey);
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e);
        }
    }
}
