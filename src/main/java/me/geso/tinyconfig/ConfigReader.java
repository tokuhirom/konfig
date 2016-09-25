package me.geso.tinyconfig;

import lombok.NonNull;

import java.io.IOException;

public interface ConfigReader {
    /**
     * Read configuration file.
     *
     * @param klass   configuration mapping class
     * @param profile active profile name.
     * @return parsed result.
     * @throws IOException Throws IOException if ConfigReader can't read configuration file.
     */
    <T> T read(@NonNull Class<T> klass, @NonNull String profile) throws IOException;

    /**
     * Read configuration file. Detect active profile from system property.
     *
     * @param klass configuration mapping class
     * @return parsed result.
     * @throws IOException Throws IOException if ConfigReader can't read configuration file.
     */
    <T> T read(Class<T> klass) throws IOException;
}
