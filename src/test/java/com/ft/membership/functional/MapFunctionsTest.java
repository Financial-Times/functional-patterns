package com.ft.membership.functional;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class MapFunctionsTest {

    @Test
    public void testMapValues() throws Exception {
        final ImmutableMap<String, Integer> input = ImmutableMap.of("a", 1, "b", 2, "c", 3);
        final Map<String, Integer> output = MapFunctions.mapValues(input, e -> e.getValue() * 10);
        assertThat(output).containsOnly(entry("a", 10),entry("b", 20), entry("c",30));
    }

    @Test
    public void testFlattenValues() throws Exception {
        final ImmutableMap<String, Optional<String>> input = ImmutableMap.of("a", Optional.of("A"), "b", Optional.empty(), "c", Optional.of("C"));
        final Map<String, String> output = MapFunctions.flattenValues(input);
        assertThat(output).containsOnly(entry("a", "A"),entry("c","C"));
    }
}