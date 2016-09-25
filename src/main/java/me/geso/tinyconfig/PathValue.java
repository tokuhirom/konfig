package me.geso.tinyconfig;

import lombok.Value;

import java.util.List;

@Value
class PathValue {
    private List<String> path;
    private String value;
    private Class<? extends ValueLoader> valueLoader;
}
