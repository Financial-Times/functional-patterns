package com.ft.membership.functional;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public class OptionalFunctions {

    /**
     * return first non-empty optional
     * @param optionals optionals to check, in order
     * @param <T> type contained in optional
     * @return first non-empty optional, or empty if all are empty.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Optional<T> firstPresentOf(final Optional<T>... optionals) {
        return Arrays.stream(optionals)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    /**
     * return result of first function returning a non-empty optional result; note that evaluation is
     * lazy, meaning that if a function returns a non-empty result, no further functions will be evaluated.
     * <p>
     * The interface uses <code>Supplier</code>, but can be used with <code>Function</code> with extra lambda
     * wrapping:
     * </p>
     * <pre>
     *     Function&lt;String&gt;, Optional&lt;String&gt;&gt; f1 = x -&gt; Optional.empty();
     *     Function&lt;String&gt;, Optional&lt;String&gt;&gt; f2 = x -&gt; Optional.of(x + " World!");
     *     Function&lt;String&gt;, Optional&lt;String&gt;&gt; f3 = x -&gt; {throw new RuntimeException("Bang!");};
     *
     *     String x = "Hello";
     *     Optional&lt;String&gt; r = firstPresentOf(() -&gt; f1(x), () -&gt; f2(x), () -&gt; f3(x));
     * </pre>
     * will return <code>Optional.of("Hello World!")</code>; f3 will not be evaluated.
     *
     * @param functions functions to evaluate, in order of desired evaluation
     * @param <T> type contained in optional result
     * @return first non-empty result, or empty if all results are empty.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Optional<T> firstPresentOf(final Supplier<Optional<T>>... functions) {
        return Arrays.stream(functions)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

}
