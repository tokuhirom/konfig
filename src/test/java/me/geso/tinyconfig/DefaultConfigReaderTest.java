package me.geso.tinyconfig;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class DefaultConfigReaderTest {
    @Before
    public void before() {
        System.clearProperty(ConfigReaderBuilder.DEFAULT_CONFIG_FILE_PROPERTY);
        System.clearProperty(ConfigReaderBuilder.DEFAULT_CONFIG_PROFILE_PROPERTY);
    }

    @Test
    public void read() throws Exception {
        ConfigReader reader = ConfigReaderBuilder.create()
                .build();
        ConfigFile config = reader.read(ConfigFile.class);
        assertThat(config.getEnv())
                .isEqualTo("local");
    }

    @Test
    public void readWithReleaseProfile() throws Exception {
        ConfigReader reader = ConfigReaderBuilder.create()
                .build();
        System.setProperty(ConfigReaderBuilder.DEFAULT_CONFIG_PROFILE_PROPERTY,
                "release");
        ConfigFile config = reader.read(ConfigFile.class);
        assertThat(config.getEnv())
                .isEqualTo("release");
    }

    @Test
    public void rewriteByProperty() throws Exception {
        ConfigReader reader = ConfigReaderBuilder.create()
                .build();
        System.setProperty("dataSource.uri", "jdbc:pg:");
        ConfigFile config = reader.read(ConfigFile.class);
        assertThat(config.getEnv())
                .isEqualTo("local");
        assertThat(config.getDataSource().getUri())
                .isEqualTo("jdbc:pg:");
    }

    @Test
    public void rewriteByEnv() throws Exception {
        ConfigReader reader = ConfigReaderBuilder.create()
                .build();
        setEnv(ImmutableMap.<String, String>builder()
                .put("DATA_SOURCE_URI", "jdbc:pg:")
                .build());
        ConfigFile config = reader.read(ConfigFile.class);
        assertThat(config.getEnv())
                .isEqualTo("local");
        assertThat(config.getDataSource().getUri())
                .isEqualTo("jdbc:pg:");
    }

    @Test
    public void rewriteFromEmpty() throws Exception {
        ConfigReader reader = ConfigReaderBuilder.create()
                .build();
        System.setProperty("dataSource.uri", "jdbc:pg:");
        ConfigFile config = reader.read(ConfigFile.class, "empty");
        assertThat(config.getDataSource().getUri())
                .isEqualTo("jdbc:pg:");
    }

    @Data
    public static class ConfigFile {
        private String env;
        private DataSourceConfig dataSource;

        @Data
        public static class DataSourceConfig {
            private String uri;
        }
    }

    // Hack: http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java
    @SuppressWarnings("unchecked")
    private static void setEnv(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
                Class[] classes = Collections.class.getDeclaredClasses();
                Map<String, String> env = System.getenv();
                for (Class cl : classes) {
                    if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                        Field field = cl.getDeclaredField("m");
                        field.setAccessible(true);
                        Object obj = field.get(env);
                        Map<String, String> map = (Map<String, String>) obj;
                        map.clear();
                        map.putAll(newenv);
                    }
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}