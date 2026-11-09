package com.entertainer.qa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central configuration resolver for the framework.
 *
 * <p>Lookup order for any key (highest priority first) so the same codebase works unchanged
 * on a laptop and inside a CI/CD pipeline:
 * <ol>
 *     <li>JVM system property, e.g. {@code mvn test -Dbooker.auth.username=admin}</li>
 *     <li>Environment variable, upper-snake-cased, e.g. {@code BOOKER_AUTH_USERNAME}</li>
 *     <li>{@code config/<env>.properties} on the classpath, where {@code <env>} itself is
 *     resolved the same way via the {@code env} key (default {@code qa})</li>
 * </ol>
 */
public final class ConfigManager {

    private static final String DEFAULT_ENV = "qa";
    private static final Properties fileProperties = new Properties();
    private static final ConcurrentHashMap<String, String> resolvedCache = new ConcurrentHashMap<>();
    private static volatile boolean loaded = false;

    private ConfigManager() {
    }

    public static String getEnv() {
        return resolveWithoutFile("env", DEFAULT_ENV);
    }

    /**
     * Returns the configured value for {@code key}, or {@code null} if it is not set anywhere.
     */
    public static String get(String key) {
        return resolvedCache.computeIfAbsent(key, k -> resolveRaw(k, null));
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        return value != null ? Integer.parseInt(value.trim()) : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return value != null ? Boolean.parseBoolean(value.trim()) : defaultValue;
    }

    private static String resolveRaw(String key, String defaultValue) {
        String fromSystemOrEnv = resolveWithoutFile(key, null);
        if (fromSystemOrEnv != null) {
            return fromSystemOrEnv;
        }

        ensureLoaded();
        String fromFile = fileProperties.getProperty(key);
        if (isSet(fromFile)) {
            return fromFile.trim();
        }

        return defaultValue;
    }

    /**
     * Resolves a key from the system property / environment variable layers only. Used both as
     * the first two steps of {@link #resolveRaw} and, for the {@code env} key itself, as the
     * entire resolution - {@link #ensureLoaded()} needs to know which environment file to load
     * before a file lookup is even possible, so it cannot recurse back into {@link #resolveRaw}.
     */
    private static String resolveWithoutFile(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (isSet(systemProperty)) {
            return systemProperty.trim();
        }

        String envVarName = toEnvVarName(key);
        String envVar = System.getenv(envVarName);
        if (isSet(envVar)) {
            return envVar.trim();
        }

        return defaultValue;
    }

    private static boolean isSet(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String toEnvVarName(String key) {
        return key.toUpperCase().replace('.', '_').replace('-', '_');
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        String env = getEnv();
        String resourcePath = "config/" + env + ".properties";
        try (InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException(
                        "Configuration file not found on classpath: " + resourcePath
                                + ". Pass -Denv=<qa|staging> for an environment that has a matching"
                                + " src/test/resources/config/<env>.properties file.");
            }
            fileProperties.load(stream);
            loaded = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read configuration file: " + resourcePath, e);
        }
    }
}
