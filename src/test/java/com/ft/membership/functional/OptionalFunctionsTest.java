package com.ft.membership.functional;

import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ft.membership.functional.OptionalFunctions.firstPresentOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OptionalFunctionsTest {

    private static final Optional<String> PRESENT = Optional.of("present");
    private static final Optional<String> EMPTY = Optional.empty();
    private static final Optional<String> ALSOPRESENT = Optional.of("also");

    @Test
    public void shouldReturnFirstPresent() {
        assertThat(firstPresentOf(EMPTY), is(EMPTY));
        assertThat(firstPresentOf(EMPTY, EMPTY), is(EMPTY));
        assertThat(firstPresentOf(PRESENT), is(PRESENT));
        assertThat(firstPresentOf(PRESENT, EMPTY), is(PRESENT));
        assertThat(firstPresentOf(EMPTY, PRESENT), is(PRESENT));
        assertThat(firstPresentOf(EMPTY, PRESENT, EMPTY), is(PRESENT));
        assertThat(firstPresentOf(EMPTY, PRESENT, ALSOPRESENT), is(PRESENT));
        assertThat(firstPresentOf(PRESENT, ALSOPRESENT), is(PRESENT));
        assertThat(firstPresentOf(PRESENT, ALSOPRESENT, EMPTY), is(PRESENT));
    }

    @Test
    public void shouldReturnFirstPresentFunction() {
        assertThat(firstPresentOf(() -> EMPTY), is(EMPTY));
        assertThat(firstPresentOf(() -> EMPTY, () -> EMPTY), is(EMPTY));
        assertThat(firstPresentOf(() -> PRESENT), is(PRESENT));
        assertThat(firstPresentOf(() -> PRESENT, () -> EMPTY), is(PRESENT));
        assertThat(firstPresentOf(() -> EMPTY, () -> PRESENT), is(PRESENT));
        assertThat(firstPresentOf(() -> EMPTY, () -> PRESENT, () -> ALSOPRESENT), is(PRESENT));
        assertThat(firstPresentOf(() -> PRESENT, () -> ALSOPRESENT), is(PRESENT));
        assertThat(firstPresentOf(() -> PRESENT, () -> ALSOPRESENT, () -> EMPTY), is(PRESENT));
    }

    @Test
    public void shouldReturnFirstPresentFunctionIsLazy() {
        final AtomicInteger i = new AtomicInteger();

        final Optional<Integer> result = firstPresentOf(Optional::empty, () -> Optional.of(i.incrementAndGet()), () -> Optional.of(i.addAndGet(100)));

        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(1));
        assertThat(i.get(), is(1));
    }
}
