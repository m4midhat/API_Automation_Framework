package com.entertainer.qa.security;

import com.entertainer.qa.config.ConfigManager;

/**
 * Reads credential values through {@link ConfigManager} and transparently decrypts anything
 * stored as {@code ENC(...)}.
 *
 * <p>Values can arrive from the CLI/CI in either form:
 * <ul>
 *     <li>Encrypted, as checked into {@code config/<env>.properties} (safe to commit) - decrypted
 *     here using the passphrase supplied out-of-band via {@code -DencryptionKey=...} or the
 *     {@code ENCRYPTION_KEY} environment variable.</li>
 *     <li>Plain, injected directly by a CI/CD secret store at run time, e.g.
 *     {@code mvn test -Dbooker.auth.password=$BOOKER_PASSWORD} - used as-is, since the secret
 *     manager has already taken care of encryption at rest.</li>
 * </ul>
 */
public final class CredentialManager {

    private static final String ENCRYPTION_KEY_PROPERTY = "encryptionKey";

    /** Local-dev-only fallback so the suite is runnable out of the box against public demo APIs. */
    private static final String INSECURE_DEFAULT_PASSPHRASE = "local-dev-only-passphrase-change-me";

    private CredentialManager() {
    }

    public static String getCredential(String configKey) {
        String rawValue = ConfigManager.get(configKey);
        if (rawValue == null) {
            throw new IllegalStateException("No value configured for credential key: " + configKey);
        }

        if (!CredentialCipher.isEncrypted(rawValue)) {
            return rawValue;
        }

        return CredentialCipher.decrypt(rawValue, resolvePassphrase());
    }

    private static String resolvePassphrase() {
        String passphrase = ConfigManager.get(ENCRYPTION_KEY_PROPERTY);
        if (passphrase != null) {
            return passphrase;
        }

        System.err.println(
                "[CredentialManager] WARNING: no -DencryptionKey / ENCRYPTION_KEY was supplied; "
                        + "falling back to the insecure local-development passphrase. Never rely on "
                        + "this outside a local run against public sandbox APIs.");
        return INSECURE_DEFAULT_PASSPHRASE;
    }
}
