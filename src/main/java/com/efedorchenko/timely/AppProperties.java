package com.efedorchenko.timely;

import java.io.IOException;
import java.util.Properties;

/**
 * Метаданные приложения, загружаются из application.properties.
 */
public final class AppProperties {

    public static final String PROPERTIES_PATH = "/application.properties";

    private static final String VERSION;
    private static final String DAEMON_HOST;
    private static final int DAEMON_PORT;
    private static final int CONNECT_TIMEOUT_MS;
    private static final int READ_TIMEOUT_MS;

    static {
        var props = new Properties();
        try (var is = AppProperties.class.getResourceAsStream(PROPERTIES_PATH)) {
            if (is == null) {
                throw new ExceptionInInitializerError("Resource not found: " + PROPERTIES_PATH);
            }
            props.load(is);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        VERSION = props.getProperty("version", "dev");
        DAEMON_HOST = requireNonBlank(props, "daemon.host");
        DAEMON_PORT = requireInt(props, "daemon.port", 1, 65535);
        CONNECT_TIMEOUT_MS = requireInt(props, "daemon.connect-timeout-ms", 1, null);
        READ_TIMEOUT_MS = requireInt(props, "daemon.read-timeout-ms", 1, null);
    }

    private static String requireNonBlank(Properties props, String key) {
        var value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ExceptionInInitializerError("Missing or blank property: " + key);
        }
        return value;
    }

    private static int requireInt(Properties props, String key, Integer min, Integer max) {
        var raw = props.getProperty(key);
        if (raw == null || raw.isBlank()) {
            throw new ExceptionInInitializerError("Missing or blank property: " + key);
        }
        int value;
        try {
            value = Integer.parseInt(raw.strip());
        } catch (NumberFormatException _) {
            throw new ExceptionInInitializerError("Property " + key + " must be an integer, got: " + raw);
        }
        if (min != null && value < min) {
            throw new ExceptionInInitializerError("Property " + key + " must be >= " + min + ", got: " + value);
        }
        if (max != null && value > max) {
            throw new ExceptionInInitializerError("Property " + key + " must be <= " + max + ", got: " + value);
        }
        return value;
    }

    private AppProperties() {}

    public static String version() {
        return VERSION;
    }

    public static String daemonHost() {
        return DAEMON_HOST;
    }

    public static int daemonPort() {
        return DAEMON_PORT;
    }

    public static int connectTimeoutMs() {
        return CONNECT_TIMEOUT_MS;
    }

    public static int readTimeoutMs() {
        return READ_TIMEOUT_MS;
    }
}