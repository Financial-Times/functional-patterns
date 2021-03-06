package com.ft.functional;

import com.google.common.base.Preconditions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A simple Scala-like case matcher (without proper decomposition)
 * to replace "pyramid of doom" of nested if statements with the
 * "monadic bind" functional pattern:
 *
 * <pre>
 * if(x != null)
 *    if(y != null)
 *      if(z != null)
 *         r = f(x,y,z)
 *      else
 *         r = g(x,y)
 *    else
 *      if(z != null)
 *         r = h(x,z)
 *      else
 *         r = i(x)
 * </pre>
 * <p>etc. could be re-written (using a literal style making it easier to read):</p>
 * <pre>
 * r = Case.match( x, y, z )
 *     .when( x,    y,     z    ).apply( () -&gt; f(x,y,z) )
 *     .when( x,    y,     Null ).apply( () -&gt; g(x,y) )
 *     .when( x,    Null,  z    ).apply( () -&gt; h(x,z) )
 *     .when( x,    Null,  Null ).apply( () -&gt; i(x) )
 *     .orElseThrow()
 * </pre>
 *
 * <p>Arguments to {@link Case.WhenClause#when} can be either literals, which will be matched using
 * {@link java.lang.Object#equals} (NB a <code>null</code> matcher never matches),
 * a {@link java.util.function.Predicate} (although awkward to use), or Hamcrest matchers,
 * including the provided <code>Ref</code> (non-null), <code>Null</code> (null), <code>Some</code>
 * (non-empty {@link java.util.Optional}), <code>None</code> (empty {@link java.util.Optional})
 * or <code>Any</code> (anything at all, alias <code>__</code>).</p>
 *
 * <p>The {@link java.util.function.Supplier} given to {@link Case.ApplyClause#apply} will only
 * be called when the corresponding when-clause matches, and, like a case statement, at most one
 * when-clause will be matched.</p>
 *
 * @param <R> type of result of expression.
 */
public class Case<R> {

    private final Object[] values;

    public static <R> Case<R> match(final Object... values) {
        Preconditions.checkArgument(values != null, "null varargs array");
        return new Case<>(values);
    }

    public static class NonMatchingCaseException extends RuntimeException {
        NonMatchingCaseException() {
            super("No clauses matched the input.");
        }
    }

    public static class Matchers {
        /**
         * matches anything, including nulls
         **/
        public static final Matcher<?> Any = new BaseMatcher<Object>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("any");
            }

            @Override
            public boolean matches(Object item) {
                return true;
            }
        };

        public static final Matcher<?> __ = Any;

        /**
         * matches empty Optionals
         **/
        public static final Matcher<Optional<?>> None = new BaseMatcher<Optional<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("any");
            }

            @Override
            public boolean matches(Object item) {
                return item instanceof Optional && !((Optional) item).isPresent();
            }
        };

        /**
         * matches present (non-empty) Optionals
         **/
        public static final Matcher<Optional<?>> Some = new BaseMatcher<Optional<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("any");
            }

            @Override
            public boolean matches(Object item) {
                return item instanceof Optional && ((Optional) item).isPresent();
            }
        };

        /**
         * matches null
         **/
        public static final Matcher<Optional<?>> Null = new BaseMatcher<Optional<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("null reference");
            }

            @Override
            public boolean matches(Object item) {
                return item == null;
            }
        };

        /**
         * matches non-null
         **/
        public static final Matcher<Optional<?>> Ref = new BaseMatcher<Optional<?>>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("non-null reference");
            }

            @Override
            public boolean matches(Object item) {
                return item != null;
            }
        };
    }

    protected Case(final Object... values) {
        this.values = values;
    }

    public ApplyClause<R> when(final Object... matchers) {
        return new WhenMatcher<>(this).when(matchers);
    }

    public interface ApplyClause<R> {
        ResultChain<R> apply(final Supplier<R> supplier);

        // method name avoids spurious type ambiguity
        ResultChain<R> applyValues(final Function<Object[],R> functionTakingValues);

        <X extends Throwable> ResultChain<R> thenThrow(final Supplier<? extends X> exceptionSupplier) throws X;
    }

    public interface WhenClause<R> {
        ApplyClause<R> when(final Object... matchers);
    }

    public static abstract class Result<R> {

        public abstract Optional<R> result();

        public R get() {
            return result().get();
        }
        public  R orElse(R other) {
            return result().orElse(other);
        }

        public  R orElseGet(final Supplier<R> supplier) {
            return result().orElseGet(supplier);
        }
        public  <X extends Throwable> R orElseThrow(final Supplier<? extends X> exceptionSupplier) throws X {
            return result().orElseThrow(exceptionSupplier);
        }
        public  <X extends Throwable> R orElseThrow() throws X {
            return result().orElseThrow(NonMatchingCaseException::new);
        }

        public  <U> Optional<U> map(final Function<R, U> f) {
            return result().map(f);
        }
        public  <U> Optional<U> flatMap(final Function<R, Optional<U>> f) {
            return result().flatMap(f);
        }

    }

    public static abstract class ResultChain<R> extends Result<R> implements WhenClause<R> {}

    private static class WhenMatcher<R> extends ResultChain<R> {
        private final Case<R> templates;

        WhenMatcher(final Case<R> templates) {
            this.templates = templates;
        }

        @Override
        public Optional<R> result() {
            return Optional.empty();
        }

        @Override
        public ApplyClause<R> when(final Object... matchers) {
            Preconditions.checkNotNull(matchers, "matchers required");
            Preconditions.checkArgument(matchers.length == templates.values.length,
                    String.format("number of matchers (%d) does not match number of input values (%d)", matchers.length, templates.values.length)
            );

            // DO Matching
            if(matches(templates.values, matchers)) {
                return new ActiveApplyClause<>(templates);
            } else {
                return new PassiveApplyClause<>(templates);
            }
        }

        @SuppressWarnings("unchecked")
        private boolean matches(final Object[] values, final Object[] matchers) {
            for(int i = 0; i < matchers.length; i++) {
                if(matchers[i] instanceof Matcher) {
                    if (!((Matcher) matchers[i]).matches(values[i])) {
                        return false;
                    }
                } else if(matchers[i] instanceof Predicate) {
                    if(!((Predicate)matchers[i]).test(values[i])) {
                        return false;
                    }
                } else if(matchers[i] == null) {
                    return false;

                } else if(!matchers[i].equals(values[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class ResultPassthruChain<R> extends ResultChain<R> implements ApplyClause<R> {

        private final Optional<R> result;

        ResultPassthruChain(final Optional<R> result) {
            this.result = result;
        }

        @Override
        public Optional<R> result() {
            return result;
        }

        @Override
        public ApplyClause<R> when(final Object... matchers) {
            return this;
        }

        @Override
        public ResultChain<R> apply(final Supplier<R> supplier) {
            return this;
        }

        @Override
        public ResultChain<R> applyValues(final Function<Object[], R> functionTakingValues) {
            return this;
        }

        @Override
        public <X extends Throwable> ResultChain<R> thenThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return this;
        }

    }

    private static class PassiveApplyClause<R> implements ApplyClause<R> {
        private final Case<R> templates;

        PassiveApplyClause(final Case<R> templates) {
            this.templates = templates;
        }

        @Override
        public ResultChain<R> apply(final Supplier<R> supplier) {
            return new WhenMatcher<>(templates);
        }

        @Override
        public ResultChain<R> applyValues(final Function<Object[], R> functionTakingValues) {
            return new WhenMatcher<>(templates);
        }

        @Override
        public <X extends Throwable> ResultChain<R> thenThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return new WhenMatcher<>(templates);
        }

    }

    private static class ActiveApplyClause<R> implements ApplyClause<R> {

        private final Case<R> templates;

        ActiveApplyClause(final Case<R> templates) {
            this.templates = templates;
        }

        @Override
        public ResultChain<R> apply(final Supplier<R> supplier) {
            return new ResultPassthruChain<>(Optional.of(supplier.get()));
        }

        @Override
        public ResultChain<R> applyValues(final Function<Object[], R> functionTakingValues) {
            return new ResultPassthruChain<>(Optional.of(functionTakingValues.apply(templates.values)));
        }

        @Override
        public <X extends Throwable> ResultChain<R> thenThrow(Supplier<? extends X> exceptionSupplier) throws X {
            throw exceptionSupplier.get();
        }

    }
}
