package me.geso.konfig;

import lombok.Value;

import java.util.List;

@Value
class PathValue {
    private List<String> path;
    private String value;
    private Class<? extends ValueLoader> valueLoader;
}
