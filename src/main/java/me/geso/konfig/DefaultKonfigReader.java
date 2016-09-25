package me.geso.konfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class DefaultKonfigReader implements KonfigReader {
    public static final String KONFIG_FILE_PROPERTY = "konfig.file";
    public static final String KONFIG_PROFILE_PROPERTY = "konfig.profile";
    private static final Set<Class<?>> SUPPORTED_TYPES =
            ImmutableSet.of(
                    Long.class,
                    long.class,
                    Integer.class,
                    int.class,
                    Short.class,
                    short.class,
                    Double.class,
                    double.class,
                    Float.class,
                    float.class,
                    String.class
            );

    private final ObjectMapper objectMapper;
    private final List<ValueLoader> valueLoaders = ImmutableList.of(
            new EnvValueLoader(),
            new PropertyValueLoader()
    );

    public DefaultKonfigReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T read(@NonNull Class<T> klass, @NonNull String profile) throws IOException {
        Object config = readInternal(Object.class, profile);
        replaceValues(klass, config);
        byte[] bytes = this.objectMapper.writeValueAsBytes(config);
        return this.objectMapper.readValue(bytes, klass);
    }

    @Override
    public <T> T read(Class<T> klass) throws IOException {
        return read(klass, getProfile());
    }

    private void replaceValues(Class<?> klass, Object config) {
        doReplace(klass, config, Collections.emptyList());
    }

    private <T> void doReplace(Class<?> klass, Object config, List<String> path) {
        log.trace("replacing config for {}",
                klass);
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(klass, Object.class);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                Class<?> propertyType = propertyDescriptor.getPropertyType();
                if (SUPPORTED_TYPES.contains(propertyType)) {
                    writeValue(propertyDescriptor, config, path);
                } else if (propertyType.isPrimitive()) {
                    log.trace("Ignore non-supported primitive type '{}' for '{}.{}'",
                            propertyType, path, propertyDescriptor.getName());
                } else if (Primitives.isWrapperType(propertyType)) {
                    log.trace("Ignore non-supported wrapper type: {}",
                            propertyType);
                } else {
                    readValue(propertyDescriptor, config, path, klass);
                }

            }
        } catch (IntrospectionException e) {
            log.info("Can't read bean info: {}({})", klass, e.getMessage());
        }
    }

    private void readValue(PropertyDescriptor propertyDescriptor, Object config, List<String> path, Class<?> klass) {
        List<String> newPath = ImmutableList.<String>builder()
                .addAll(path)
                .add(propertyDescriptor.getName())
                .build();

        if (config instanceof Map) {
            Object child = ((Map) config).get(propertyDescriptor.getName());
            log.trace("Handling child: {} => {}", klass.getName(), propertyDescriptor.getName());
            doReplace(propertyDescriptor.getPropertyType(), child, newPath);
        } else {
            log.trace("{}: {} is not a map", newPath, config);
        }
    }

    private void writeValue(PropertyDescriptor propertyDescriptor, Object config, List<String> path) {
        Method writeMethod = propertyDescriptor.getWriteMethod();
        if (writeMethod == null) {
            log.trace("There's no writer method. Path:{}, Property:{}",
                    path, propertyDescriptor.getName());
            return;
        }

        List<String> newPath = ImmutableList.<String>builder()
                .addAll(path)
                .add(propertyDescriptor.getName())
                .build();

        log.trace("Writing value: {}", newPath);
        valueLoaders.stream()
                .map(valueLoader -> valueLoader.getValue(newPath))
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get)
                .ifPresent(it -> setValue(config, newPath, it));
    }

    private void setValue(Object config, List<String> path, String value) {
        log.trace("Putting value: {} -> {}", path, value);

        String last = path.get(path.size() - 1);

        if (config instanceof Map) {
            // last item. Just set a value.
            log.info("Put value: {} -> {}", path, value);
            ((Map) config).put(last, value);
        } else {
            log.info("Cannot set value for {}. There's non-Map value in a path. Value: {}", path, value);
            return;
        }
    }

    private <T> T readInternal(Class<T> klass, String profile) throws IOException {
        String configFile = System.getProperty(KONFIG_FILE_PROPERTY);
        if (configFile != null) {
            log.info("Reading configuration from " + configFile);
            return objectMapper.readValue(new File(configFile), klass);
        }

        String resourceName = "konfig-" + profile + ".yml";
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream != null) {
                log.info("Reading configuration from resource: " + resourceName);
                return objectMapper.readValue(inputStream, klass);
            }
        }

        throw new IllegalStateException("There's no `konfig.file` system profile property and no resource named `" + resourceName + "`.");
    }

    private String getProfile() {
        return System.getProperty(KONFIG_PROFILE_PROPERTY, "local");
    }

}
