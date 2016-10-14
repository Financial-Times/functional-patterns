package com.ft.membership.functional;

import com.google.common.cache.CacheBuilder;
import org.junit.Test;

import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class CacheTest {

    @Test
    public void should_wrap_simple_function() throws Exception {
        final com.google.common.cache.Cache<Integer, Integer> cache = CacheBuilder.newBuilder().maximumSize(2).recordStats().build();
        final Function<Integer, Integer> f = (Integer x) -> x * 2;

        final Function<Integer, Integer> cachedFn = Cache.cached(cache, f);

        cachedFn.apply(1);

        assertThat(cache.size(), is(1L));

        cachedFn.apply(1);
        assertThat(cache.size(), is(1L));

        cachedFn.apply(2);
        assertThat(cache.size(), is(2L));

        cachedFn.apply(3);
        assertThat(cache.size(), is(2L));

        assertThat(cache.getIfPresent(1), is(nullValue()));
        assertThat(cache.getIfPresent(2), is(f.apply(2)));
        assertThat(cache.getIfPresent(3), is(f.apply(3)));

        assertThat(cache.stats().hitCount(), is(3L));
    }
}
