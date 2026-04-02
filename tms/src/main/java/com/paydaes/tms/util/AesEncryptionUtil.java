package com.paydaes.tms.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AesEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesEncryptionUtil(
            @Value("${tms.keystore.path}") Resource keystoreResource,
            @Value("${tms.keystore.store-password}") String storePassword,
            @Value("${tms.keystore.key-alias}") String keyAlias,
            @Value("${tms.keystore.key-password}") String keyPassword) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        try (var inputStream = keystoreResource.getInputStream()) {
            keyStore.load(inputStream, storePassword.toCharArray());
        }

        KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                keyAlias,
                new KeyStore.PasswordProtection(keyPassword.toCharArray())
        );

        if (entry == null) {
            throw new IllegalStateException(
                "AES key alias '" + keyAlias + "' not found in keystore. " +
                "Run: keytool -genseckey -alias " + keyAlias +
                " -keyalg AES -keysize 256 -storetype JCEKS -keystore keystore.jceks");
        }

        this.secretKey = entry.getSecretKey();
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            byte[] payload = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, payload, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, payload, GCM_IV_LENGTH, ciphertext.length);

            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedBase64) {
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedBase64);
            byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(payload, GCM_IV_LENGTH, payload.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));

            return new String(cipher.doFinal(ciphertext));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
