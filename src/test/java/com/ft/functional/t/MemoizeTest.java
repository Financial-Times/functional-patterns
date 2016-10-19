package com.ft.functional.t;

import com.ft.functional.Memoize;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MemoizeTest {

    @Test
    public void calls_memoized_supplier_only_once() throws Exception {

        final AtomicInteger i = new AtomicInteger(0);

        final Supplier<Integer> f = Memoize.memorize(i::incrementAndGet);

        final Integer apply1 = f.get();
        final Integer apply2 = f.get();

        assertThat(apply1, is(1));
        assertThat(apply2, is(1));

        assertThat(i.get(), is(1));
    }

    @Test
    public void propagates_exception_forever() throws Exception {

        final AtomicInteger i = new AtomicInteger(0);

        // throws due to divide by zero on first run, but then safe...
        final Supplier<Integer> f = Memoize.memorize( () -> 10 / i.getAndIncrement() );

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
