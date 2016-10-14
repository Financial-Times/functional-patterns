package com.ft.membership.functional;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CurryTest {

    @Test
    public void functionToSupplier() throws Exception {
        Function<Integer, Integer> f = (t) -> t + t;

        final Supplier<Integer> curry = Curry.curry(f, 10);

        assertThat(curry.get(), is(20));
    }

    @Test
    public void biFunctionToFunction() throws Exception {
        BiFunction<Integer, Integer, Integer> f = (t,u) -> t * u;

        final Function<Integer, Integer> curry = Curry.curry(f, 10);

        assertThat(curry.apply(10), is(100));
    }


    @Test
    public void biConsumerToConsumer() throws Exception {
        List<Integer> list = new ArrayList<>();

        BiConsumer<Integer, Integer> f = (t, u) -> list.add(t*u);

        final Consumer<Integer> curry = Curry.curry(f, 10);

        curry.accept(10);

        assertThat(list, hasItem(100));
    }

}