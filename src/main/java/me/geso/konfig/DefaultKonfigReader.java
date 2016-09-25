package me.geso.konfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

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

    public DefaultKonfigReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T read(Class<T> klass) throws IOException {
        T config = readInternal(klass, getProfile());
        replaceValuesBySystemProperty(config);
        return config;
    }

    private <T> void replaceValuesBySystemProperty(T config) {
        doReplace(config, null);
    }

    private <T> void doReplace(T config, String prefix) {
        Class<?> aClass = config.getClass();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(aClass, Object.class);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                Method writeMethod = propertyDescriptor.getWriteMethod();
                if (writeMethod != null) {
                    Class<?>[] parameterTypes = writeMethod.getParameterTypes();
                    if (parameterTypes.length == 1) {
                        Class<?> parameterType = parameterTypes[0];
                        if (SUPPORTED_TYPES.contains(parameterType)) {
                            rewriteByProperty(config, prefix, propertyDescriptor.getName(), writeMethod);
                            rewriteByEnv(config, prefix, propertyDescriptor.getName(), writeMethod);
                        } else {
                            log.debug("Skip non-primitive property: prefix:{} writer:{}",
                                    prefix,
                                    writeMethod.getName());
                        }
                    }
                }

                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    Class<?> returnType = readMethod.getReturnType();
                    if (!(returnType.isPrimitive() || SUPPORTED_TYPES.contains(returnType))) {
                        try {
                            Object child = readMethod.invoke(config);
                            String newPrefix = (prefix == null ? "" : prefix + ".") + propertyDescriptor.getName();
                            log.trace("Handling child: {} => {}", aClass.getName(), propertyDescriptor.getName());
                            doReplace(child, newPrefix);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            log.debug("Cannot access property: {}.{}: {}", aClass.getCanonicalName(), readMethod.getName(), e.getMessage());
                        }
                    } else {
                        log.trace("Skip primitive types for reading: {}.{}", aClass.getName(), propertyDescriptor.getName());
                    }
                }
            }
        } catch (IntrospectionException e) {
            log.info("Can't read bean info: {}({})", aClass, e.getMessage());
        }
    }


    private <T> void rewriteByEnv(T config, String prefix, String name, Method writeMethod) {
        String envName = (prefix == null ? "" : prefix + "_") + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
        String value = System.getenv(envName);
        if (value == null) {
            return;
        }

        setValue(config, writeMethod, "Env: " + envName, value);
    }

    private <T> void rewriteByProperty(T config, String prefix, String name, Method writeMethod) {
        String propertyName = (prefix == null ? "" : prefix + ".") + name;
        String value = System.getProperty(propertyName);
        log.trace("Reading property: {}. Value: {}",
                propertyName,
                value);
        if (value != null) {
            setValue(config, writeMethod, "Property: " + propertyName, value);
        }
    }

    private <T> void setValue(T config, Method writeMethod, String name, String value) {
        Class<?>[] parameterTypes = writeMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            log.debug("This is not a writer method: {}", writeMethod);
            return;
        }
        Class<?> parameterType = parameterTypes[0];
        try {
            if (parameterType == String.class) {
                writeMethod.invoke(config, value);
            } else if (parameterType == short.class || parameterType == Short.class) {
                writeMethod.invoke(config, Short.valueOf(value));
            } else if (parameterType == int.class || parameterType == Integer.class) {
                writeMethod.invoke(config, Integer.valueOf(value));
            } else if (parameterType == long.class || parameterType == Long.class) {
                writeMethod.invoke(config, Long.valueOf(value));
            } else if (parameterType == float.class || parameterType == Float.class) {
                writeMethod.invoke(config, Float.valueOf(value));
            } else if (parameterType == double.class || parameterType == Double.class) {
                writeMethod.invoke(config, Double.valueOf(value));
            } else {
                log.info("Can't set property: {}. parameter type '{}' is not supported. Supported types are 'int', 'short', 'long', and 'String'", value, parameterType);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.info("Cannot set value: {} <- {}. Type: {}({})",
                    name,
                    value,
                    parameterType.getName(),
                    e.getMessage());
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
