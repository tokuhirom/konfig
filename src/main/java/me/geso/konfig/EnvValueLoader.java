package me.geso.konfig;

import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class EnvValueLoader implements ValueLoader {
    @Override
    public String getName(List<String> path) {
        return path.stream()
                .map(it -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, it))
                .collect(Collectors.joining("_"));
    }

    @Override
    public Optional<String> getValue(List<String> path) {
        String name = getName(path);
        String env = System.getenv(name);
        if (log.isTraceEnabled()) {
            log.trace("environment variable '{}': {}", name, env);
        }
        return Optional.ofNullable(env);
    }
}
