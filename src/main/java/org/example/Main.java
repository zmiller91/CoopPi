package org.example;

import com.google.common.collect.Streams;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        PrivateKey privateKey = loadPrivateKey();
        PublicKey publicKey = loadPublicKey();
        PiAuth piAuth = new PiAuth(privateKey, publicKey);

        CMSSignedData signed = piAuth.sign("Hello World!");
        System.out.println(new String((byte[]) ((CMSTypedData) signed.getSignedContent()).getContent()));

        // READING

        CMSSignedData signedData = new CMSSignedData(signed.getEncoded());
        System.out.println(signedData.isDetachedSignature());
        SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(publicKey);

        boolean valid = Streams.stream(signedData.getSignerInfos().iterator())
                .allMatch(info -> verify(info, verifier));

        System.out.println(valid);
        System.out.println(new String((byte[])signedData.getSignedContent().getContent()));
    }

    private static boolean verify(SignerInformation signerInfo, SignerInformationVerifier verifier) {
        try {
            return signerInfo.verify(verifier);
        } catch (CMSException e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyPair generateKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        return keyGen.generateKeyPair();
    }

    private static PrivateKey loadPrivateKey() throws Exception {
        String raw = Files.readString(Paths.get("private_key"));
        byte[] bytes = Base64.getDecoder().decode(raw);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }

    private static PublicKey loadPublicKey() throws Exception {
        String raw = Files.readString(Paths.get("public_key"));
        byte[] bytes = Base64.getDecoder().decode(raw);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }
}