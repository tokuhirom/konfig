package me.geso.konfig;

import lombok.NonNull;

import java.io.IOException;

public interface KonfigReader {
    <T> T read(@NonNull Class<T> klass, @NonNull String profile) throws IOException;

    <T> T read(Class<T> klass) throws IOException;
}
