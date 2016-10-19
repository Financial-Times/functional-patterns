package com.ft.functional;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapFunctions {

    /**
     * transforms entries of a map using a function applied to entries, returning new values, leaving keys the same.
     * @param map map to re-map
     * @param fn function to apply to entries
     * @param <K> type of keys
     * @param <V> type of values
     * @return map with re-mapped values
     */
    public static <K, V> Map<K, V> mapValues(Map<K, V> map, Function<Map.Entry<K, V>, V> fn) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, fn::apply));
    }

    /**
     * transforms map, removing entries with empty Optional values.
     * @param map input map with Optional&lt;V&gt; values
     * @param <K> type of key
     * @param <V> type held in Optional value
     * @return map containing only entries with present values
     */
    public static <K, V> Map<K, V> flattenValues(Map<K, Optional<V>> map) {
        return
                map
                        .entrySet()
                        .stream()
                        .filter(e -> e.getValue().isPresent())
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));

    }
}
