package com.company.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Resolution order (highest wins):
 *   1. JVM property   -Dbase.url=...
 *   2. Env variable   BASE_URL  (key upper-cased, dots -> underscores)
 *   3. File           config/{env}.properties   (env = -Denv or TEST_ENV, default "qa")
 * Secrets (passwords) must come from 1 or 2 only, never from the file.
 */
public final class ConfigReader {

    private static final Properties FILE_PROPS = load();

    private ConfigReader() { }

    private static Properties load() {
        String env = System.getProperty("env", System.getenv().getOrDefault("TEST_ENV", "qa"));
        Properties props = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config/" + env + ".properties")) {
            if (in == null) {
                throw new IllegalStateException("No config file for env '" + env + "'");
            }
            props.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return props;
    }

    public static String get(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) value = System.getenv(key.toUpperCase().replace('.', '_'));
        if (value == null) value = FILE_PROPS.getProperty(key);
        return value != null ? value : defaultValue;
    }

    public static String get(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new IllegalStateException("Missing config '" + key + "' (set -D" + key
                    + " or env " + key.toUpperCase().replace('.', '_') + ")");
        }
        return value;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(get(key, String.valueOf(defaultValue)));
    }
}
