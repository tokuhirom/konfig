package me.geso.konfig;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class PropertyValueLoader implements ValueLoader {
    @Override
    public String getName(List<String> path) {
        return path.stream()
                .collect(Collectors.joining("."));
    }

    @Override
    public Optional<PathValue> getValue(List<String> path) {
        String name = getName(path);
        String property = System.getProperty(name);
        if (log.isTraceEnabled()) {
            log.trace("property value '{}': {}", name, property);
        }
        if (property == null) {
            return Optional.empty();
        } else {
            return Optional.of(new PathValue(path, property, this.getClass()));
        }
    }
}
