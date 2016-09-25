package me.geso.konfig;

import java.util.List;
import java.util.Optional;

public interface ValueLoader {
    String getName(List<String> path);

    Optional<String> getValue(List<String> path);
}
