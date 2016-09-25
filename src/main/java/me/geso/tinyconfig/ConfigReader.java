package me.geso.tinyconfig;

import lombok.NonNull;

import java.io.IOException;

public interface ConfigReader {
    <T> T read(@NonNull Class<T> klass, @NonNull String profile) throws IOException;

    <T> T read(Class<T> klass) throws IOException;
}
