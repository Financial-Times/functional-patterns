package com.ft.membership.functional;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MemorizeTest {

    @Test
    public void should_call_memorized_supplier_only_once() throws Exception {

        final AtomicInteger i = new AtomicInteger(0);

        final Supplier<Integer> f = Memorize.memorize(i::incrementAndGet);

        final Integer apply1 = f.get();
        final Integer apply2 = f.get();

        assertThat(apply1, is(1));
        assertThat(apply2, is(1));

        assertThat(i.get(), is(1));
    }

    @Test
    public void should_propagate_exception_forever() throws Exception {

        final AtomicInteger i = new AtomicInteger(0);

        // throws due to divide by zero on first run, but then safe...
        final Supplier<Integer> f = Memorize.memorize( () -> 10 / i.getAndIncrement() );

        try {
            f.get(); // first failure

        } catch(Exception e) {
            assertThat(e.getCause(), instanceOf(ArithmeticException.class));
        }

        try {
            f.get(); // cached failure

        } catch(Exception e) {
            assertThat(e.getCause(), instanceOf(ArithmeticException.class));
        }


        assertThat(i.get(), is(1));
    }

}
