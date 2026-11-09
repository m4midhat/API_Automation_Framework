package com.entertainer.qa.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption for credentials at rest (properties files, CI/CD secret stores).
 *
 * <p>Deliberately built on the JDK's own {@code javax.crypto} rather than a third-party crypto
 * library: encryption here is a utility concern, not a QA tool choice, so it stays out of scope
 * of the testing-tool stack the framework otherwise restricts itself to.
 *
 * <p>Ciphertext is stored as {@code ENC(<base64>)}, where the base64 payload is
 * {@code salt (16 bytes) | iv (12 bytes) | GCM ciphertext+tag}. The secret passphrase itself is
 * never stored in the repository &mdash; it is supplied at run time via the {@code encryptionKey}
 * system property or the {@code ENCRYPTION_KEY} environment variable (see {@link CredentialManager}),
 * exactly the kind of value a CI/CD pipeline injects from its own secret store.
 */
public final class CredentialCipher {

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";

    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_DERIVATION_ITERATIONS = 65_536;
    private static final int KEY_LENGTH_BITS = 256;

    private static final int SALT_LENGTH_BYTES = 16;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CredentialCipher() {
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.trim().startsWith(ENC_PREFIX) && value.trim().endsWith(ENC_SUFFIX);
    }

    public static String encrypt(String plainText, String passphrase) {
        try {
            byte[] salt = new byte[SALT_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(salt);
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            SecretKeySpec key = deriveKey(passphrase, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[salt.length + iv.length + cipherText.length];
            System.arraycopy(salt, 0, payload, 0, salt.length);
            System.arraycopy(iv, 0, payload, salt.length, iv.length);
            System.arraycopy(cipherText, 0, payload, salt.length + iv.length, cipherText.length);

            return ENC_PREFIX + Base64.getEncoder().encodeToString(payload) + ENC_SUFFIX;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encrypt credential", e);
        }
    }

    public static String decrypt(String encryptedValue, String passphrase) {
        String base64Payload = unwrap(encryptedValue);
        try {
            byte[] payload = Base64.getDecoder().decode(base64Payload);

            byte[] salt = new byte[SALT_LENGTH_BYTES];
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            byte[] cipherText = new byte[payload.length - salt.length - iv.length];
            System.arraycopy(payload, 0, salt, 0, salt.length);
            System.arraycopy(payload, salt.length, iv, 0, iv.length);
            System.arraycopy(payload, salt.length + iv.length, cipherText, 0, cipherText.length);

            SecretKeySpec key = deriveKey(passphrase, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to decrypt credential - wrong encryptionKey, or the value is not a"
                            + " CredentialCipher-encrypted ENC(...) string.", e);
        }
    }

    private static String unwrap(String encryptedValue) {
        String trimmed = encryptedValue.trim();
        if (!isEncrypted(trimmed)) {
            throw new IllegalArgumentException("Value is not in ENC(...) format: " + encryptedValue);
        }
        return trimmed.substring(ENC_PREFIX.length(), trimmed.length() - ENC_SUFFIX.length());
    }

    private static SecretKeySpec deriveKey(String passphrase, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, KEY_DERIVATION_ITERATIONS, KEY_LENGTH_BITS);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * CLI helper for generating {@code ENC(...)} values to paste into a {@code config/*.properties}
     * file or a CI/CD secret variable - no IDE required, which matters for a Jenkins/Azure DevOps
     * agent preparing a pipeline.
     *
     * <pre>
     *   mvn -q compile
     *   java -cp target/classes com.entertainer.qa.security.CredentialCipher encrypt password123 myPassphrase
     * </pre>
     */
    public static void main(String[] args) {
        if (args.length != 3 || (!args[0].equals("encrypt") && !args[0].equals("decrypt"))) {
            System.out.println("Usage: CredentialCipher <encrypt|decrypt> <value> <passphrase>");
            return;
        }
        String operation = args[0];
        String value = args[1];
        String passphrase = args[2];

        String result = operation.equals("encrypt") ? encrypt(value, passphrase) : decrypt(value, passphrase);
        System.out.println(result);
    }
}
