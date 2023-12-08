package org.example;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.ocsp.Signature;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.*;

import static org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

public class Main {

    private static final byte[] DATA_TO_BE_SIGNED = "data".getBytes();
    private static final String SHA_1_WITH_RSA = "SHA1WithRSA";
    private static final boolean DATA_NOT_ATTACHED = false;
    private static final String COLLECTION_STORE_TYPE = "Collection";

    public static void main(String[] args) throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = keyGen.generateKeyPair();

        PublicKey publicKey = pair.getPublic();
        PrivateKey privateKey = pair.getPrivate();

        System.out.println(decode(pair.getPrivate().getEncoded()));
        System.out.println(decode(pair.getPublic().getEncoded()));

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        X509Certificate[] serverChain = new X509Certificate[1];

        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                new X500Name("CN=coop.zackmiller.info"),
                BigInteger.valueOf(12345L), // unique serial number
                new Date(),
                new Date(),
                new X500Name("CN=coop.name"),
                new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSAES_OAEP), publicKey.getEncoded())
        );

        ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(privateKey);
        X509CertificateHolder cert = certificateBuilder.build(sha1Signer);
        Store certs = new JcaCertStore(Arrays.asList(cert));

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()).build(sha1Signer, cert));
        gen.addCertificates(certs);

        CMSTypedData data = new CMSProcessableByteArray("message".getBytes());
        CMSSignedData signed = gen.generate(data, true);
        System.out.println(new String((byte[]) ((CMSTypedData) signed.getSignedContent()).getContent()));

        // READING
        CMSSignedData signedData = new CMSSignedData(signed.getEncoded());
        System.out.println(new String((byte[])signedData.getSignedContent().getContent()));

        SignerInformationVerifier verify = new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(publicKey);
        System.out.println(signedData.getSignerInfos().iterator().next().verify(verify));


//        Store<X509CertificateHolder> certificates = signed.getCertificates();
//        SignerInformationStore signerStore = signedData.getSignerInfos();
//        Iterator<SignerInformation> signers = signerStore.getSigners().iterator();
//        while(signers.hasNext()) {
//            SignerInformation signer = signers.next();
//            Collection<X509CertificateHolder> certificateCollection = certificates.getMatches(signer.getSID());
//
//        }
//
//        System.out.println(new String(signed.getEncoded()));

//        X509V3CertificateGenerator serverCertGen = new X509V3CertificateGenerator();
//        serverCertGen.setSerialNumber(new BigInteger("123456789"));
//// X509Certificate caCert=null;
//        serverCertGen.setIssuerDN(new X500Principal("CN=coop"));
//        serverCertGen.setNotBefore(new Date());
//        serverCertGen.setNotAfter(new Date());
//        serverCertGen.setSubjectDN(new X500Principal("CN=coop"));
//        serverCertGen.setPublicKey(publicKey);
//        serverCertGen.setSignatureAlgorithm("MD5WithRSA");
//        serverCertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
//                new SubjectKeyIdentifierStructure(publicKey));
//        serverChain[0] = serverCertGen.generateX509Certificate(privateKey, "BC"); // note: private key of CA
//        System.out.println(new String(serverChain[0].getEncoded()));


//        PrivateKey privateKey = privateKey("C:\\Users\\zmiller\\.ssh\\id_rsa");
//        PublicKey publicKey = publicKey("C:\\Users\\zmiller\\.ssh\\id_rsa.pub");

    }

    private static void signature() {
//        PrivateKey pk = new PrivateK
    }

    private static String decode(byte[] key) {
        StringBuffer retString = new StringBuffer();
        for (int i = 0; i < key.length; ++i) {
            retString.append(Integer.toHexString(0x0100 + (key[i] & 0x00FF)).substring(1));
        }
        return retString.toString();
    }

    private static PrivateKey privateKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        System.out.println(new String(keyBytes));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private static PublicKey publicKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}