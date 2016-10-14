package com.ft.membership.functional;

import java.util.function.Function;

/**
 * caching for functions.
 */
public class Cache {

    /**
     * returns a function which is backed by the given cache.
     *
     * @param <T>   key type
     * @param <R>   result type
     * @param cache configured cache
     * @param f     function for which results are to be cached, returning R for key T
     * @return function returning R for key T
     */
    public static <T,R> Function<T,R> cached(final com.google.common.cache.Cache<T, R> cache, final Function<T, R> f) {
        return key -> { try { return cache.get(key, () -> f.apply(key)); } catch(Exception e) {throw new RuntimeException(e);} };
    }

    /**
     * returns a function which is backed by the given cache.
     *
     * @param <T>   key type
     * @param <R>   result type
     * @param cache configured cache
     * @param f     function for which results are to be cached, returning R for key T
     * @return function returning R for key T
     * @deprecated use cached(Cache,Function) instead - swapped arguments in 1.8 to be more like 'Currying'.
     */
    @Deprecated
    public static <T,R> Function<T,R> cached(final Function<T, R> f, final com.google.common.cache.Cache<T, R> cache) {
        return cached(cache,f);
    }

}
