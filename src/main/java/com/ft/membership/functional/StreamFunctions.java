package com.ft.membership.functional;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

public class StreamFunctions {

    /**
     * returns a stream of non-null values from possibly null arguments.
     * <pre>
     *     safeStreamOf(null) == Stream.empty()
     *     safeStreamOf(null, null) == Stream.empty()
     *     safeStreamOf(null, "a", null, "b") == Stream.of("a","b")
     * </pre>
     *
     * @param values items for stream, which may be null, or some of which may be null
     * @param <T> type of stream items
     * @return stream of the non-null values
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Stream<T> safeStreamOf(@Nullable final T... values) {
        if(values == null || values.length == 0) return Stream.empty();
        return Stream.of(values).filter(Objects::nonNull);
    }
}
