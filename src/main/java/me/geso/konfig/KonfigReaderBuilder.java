package me.geso.konfig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class KonfigReaderBuilder {
    private ObjectMapper objectMapper;

    public KonfigReader build() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper(new YAMLFactory());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return new DefaultKonfigReader(objectMapper);
    }
}
