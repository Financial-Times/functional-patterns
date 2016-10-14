package com.ft.membership.functional;

import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.Uninterruptibles;

import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * a higher-order function which when applied, will invoke the supplied function at most once
 * in order to return its value, and return the same value thereafter.
 *
 * <p>NB if the function throws an Exception, it will be wrapped in a RuntimeException and
 * thrown to the caller; all future invocations will throw the same, wrapped exception.</p>
 * <p>
 * The returned function is thread-safe if the inner function is also thread safe.
 * </p>
 */
 public class Memorize {

    /**
     * memorise a {@code Supplier}.
     * @param inner a supplier to memorise
     * @param <R> type returned by supplier
     * @return memorised supplier
     */
    public static <R> Supplier<R> memorize(final Supplier<R> inner) {
        /* NB we don't use the easy-to-use updateAndGet() here because it doesn't
         * guarantee that the updating function will be called only once in a
         * multi-threaded situation... we do!
         */
        final AtomicInteger isSet = new AtomicInteger();
        final SettableFuture<R> completable = SettableFuture.create();

        return () -> {

            if (isSet.compareAndSet(0, 1)) try {
                // not complete, calculate value
                final R value = inner.get();
                completable.set(value);
                return value;

            } catch (Exception e) {
                completable.setException(e);
                throw new RuntimeException(e);

            }
            else try {
                // complete, or being calculated by another thread...
                return Uninterruptibles.getUninterruptibly(completable);

            } catch (ExecutionException error) {
                throw new RuntimeException(error.getCause());
            }
        };
    }
}
