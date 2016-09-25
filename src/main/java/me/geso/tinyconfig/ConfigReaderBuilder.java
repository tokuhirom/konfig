package me.geso.tinyconfig;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigReaderBuilder {
    public static final String DEFAULT_CONFIG_FILE_PREFIX = "config-";
    public static final String DEFAULT_CONFIG_FILE_PROPERTY = "config.file";
    public static final String DEFAULT_CONFIG_PROFILE_PROPERTY = "config.profile";

    private String configFilePrefix;
    private String configFileProperty;
    private String configProfileProperty;

    private ConfigReaderBuilder() {
    }

    public static ConfigReaderBuilder create() {
        return new ConfigReaderBuilder();
    }

    /**
     * Set prefix of configuration file resource.
     */
    public ConfigReaderBuilder setConfigFilePrefix(String configFilePrefix) {
        this.configFilePrefix = configFilePrefix;
        return this;
    }

    public ConfigReaderBuilder setConfigFileProperty(String configFileProperty) {
        this.configFileProperty = configFileProperty;
        return this;
    }

    public ConfigReaderBuilder setConfigProfileProperty(String configProfileProperty) {
        this.configProfileProperty = configProfileProperty;
        return this;
    }

    public ConfigReader build() {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (configFilePrefix == null) {
            configFilePrefix = DEFAULT_CONFIG_FILE_PREFIX;
        }
        if (configFileProperty == null) {
            configFileProperty = DEFAULT_CONFIG_FILE_PROPERTY;
        }
        if (configProfileProperty == null) {
            configProfileProperty = DEFAULT_CONFIG_PROFILE_PROPERTY;
        }
        return new DefaultConfigReader(objectMapper, configFilePrefix, configFileProperty, configProfileProperty);
    }
}
