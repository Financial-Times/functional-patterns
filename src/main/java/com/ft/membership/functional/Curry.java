package com.ft.membership.functional;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Currying: supply an initial fixed argument to a function/consumer, returning a function/consumer that takes one
 * less argument.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Currying">Currying</a>
 */
public class Curry {

    public static <T,R> Supplier<R> curry(final Function<T,R> f, T t) {
        return () -> f.apply(t);
    }

    public static <T,U,R> Function<U,R> curry(final BiFunction<T,U,R> f, T t) {
        return (u) -> f.apply(t,u);
    }

    public static <T,U> Consumer<U> curry(final BiConsumer<T,U> f, T t) {
        return (u) -> f.accept(t,u);
    }
}
