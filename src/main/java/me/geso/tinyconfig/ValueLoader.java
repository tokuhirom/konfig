package me.geso.tinyconfig;

import java.util.List;
import java.util.Optional;

public interface ValueLoader {
    String getName(List<String> path);

    Optional<PathValue> getValue(List<String> path);
}
