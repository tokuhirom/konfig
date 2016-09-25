package me.geso.konfig;

import java.io.IOException;

public interface KonfigReader {
    <T> T read(Class<T> klass) throws IOException;
}
