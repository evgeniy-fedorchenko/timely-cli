package com.efedorchenko.timely;

import java.io.IOException;
import java.util.Properties;

/**
 * Метаданные приложения, загружаются из application.properties.
 */
public final class AppProperties {

    public static final String PROPERTIES_PATH = "/application.properties";

    private static final String VERSION;

    static {
        var props = new Properties();
        try (var is = AppProperties.class.getResourceAsStream(PROPERTIES_PATH)) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException _) { }

        VERSION = props.getProperty("version", "dev");
    }

    private AppProperties() {}

    public static String version() {
        return VERSION;
    }
}