package com.ft.membership.functional;

import com.google.common.base.MoreObjects;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;

/**
 * An Either <a href="https://en.wikipedia.org/wiki/Monad_(functional_programming)">monad</a>: error handling, functional-style.
 * <p>
 * Delay exceptional handling using a error-or-result chaining, rather than nested try/throw/catch blocks:
 *</p>
 * <pre>
 *     import com.ft.membership.functional.Eithers.Either;
 *     import com.ft.membership.functional.Case;
 *
 *     import static com.ft.membership.functional.Eithers.*;
 *     import static com.ft.membership.functional.Eithers.Matchers.*;
 *
 *     Either&lt;Exception,String&gt; n =
 *         trying( () -&gt; methodThatMightThrow( someInput ) )
 *             .map( x -&gt; x * 100 )
 *             .map( x -&gt; String.format("The number of the day is %.2f", x) );
 *
 *     // we can now handle success, or any exceptions:
 *     if(n.isRight()) {
 *         // success; value in n.right()
 *         System.out.println(n.right());
 *     } else {
 *         // failure; exception in n.left()
 *         System.out.println(n.left().getMessage());
 *     }
 *
 *     // or using com.ft.membership.functional.Case:
 *     System.out.println(
 *         Case.&lt;String&gt;match(n)
 *             .when(isLeft()).apply(() -&gt; n.left().getMessage())
 *             .orElse(n::right)
 *         );
 *
 * </pre>
 *
 * <p>Whereas {@link java.util.Optional Optional} can yield a result, or nothing, Either can yield a result
 * <em>right</em>, or another value <em>left</em>. Left is used, by convention, for the exceptional case,
 * but may, or may not, actually be an instance of {@link java.lang.Exception}.</p>
 *
 * <p>The <em>right</em> path yields the result only if all operations succeed, the <em>left</em> path returns the exceptional value
 * from the first failed operation. The type of the <em>right</em> path can vary along the chain, since only the last operation
 * yields a result, the type of the  <em>left</em> path cannot vary since the chain can potentially fail, at any link, and fall-through.</p>
 *
 * <dl>
 *     <dt>right()</dt><dd>yields the happy-path 'right' result</dd>
 *     <dt>left()</dt><dd>yields the unhappy-path or exceptional result</dd>
 * </dl>
 *
 * @see <a href="http://fsharpforfunandprofit.com/rop/">Railway Oriented Programming</a>
 */
public class Eithers {

    /**
     * wrap the value in a Left
     * @param left value for left
     * @param <E> type of left result
     * @param <R> type of right result
     * @return a Left Either.
     */
    public static <E,R> Either<E,R> left(E left) {
        return new Either.Left<>(left);
    }

    /**
     * wrap the value in a Right
     * @param right value for right
     * @param <E> type of left result
     * @param <R> type of right result
     * @return a Right Either.
     */
    public static <E,R> Either<E,R> right(R right) {
        return new Either.Right<>(right);
    }

    /**
     * tries calling Supplier f to get a Right value, wrapping any thrown exception
     * as a Left.
     * @param f no-arg function to call
     * @param <R> type of right result
     * @return an Either
     */
    public static <R> Either<Exception,R> trying(final Supplier<R> f) {
        try {
            return right(f.get());
        } catch(Exception e) {
            return left(e);
        }
    }

    /**
     * tries calling Supplier f to get a Right value, handling any thrown exception
     * by returning the result of applying h to the caught exception.
     * @param f no-arg function to call to generate Right value.
     * @param h handler to call on exception to generate alternative Either.
     * @param <E> type of left result
     * @param <R> type of right result
     * @return an Either
     */
    public static <E,R> Either<E,R> trying(final Supplier<R> f, final Function<Exception, Either<E,R>> h) {
        try {
            return right(f.get());
        } catch(Exception e) {
            return h.apply(e);
        }
    }

    /**
     * tries calling Supplier f to get a Left value, handling any thrown exception
     * by returning the Left result of applying handler h to the caught exception.
     * <p>
     * Use case is when you want want to get a Left, but calculating the Left may also fail.
     * </p>
     * @param f no-arg function to call to generate Left value.
     * @param h function to call with exception to generate alternative Left.
     * @param <E> type of left result
     * @param <R> type of right result
     * @return an Either
     */
    public static <E,R> Either<E,R> failing(final Supplier<E> f, final Function<Exception, E> h) {
        try {
            return left(f.get());
        } catch(Exception e) {
            return left(h.apply(e));
        }
    }

    /**
     * convenience method to convert Optional&lt;R&gt; to a Right value if non-empty, or to given
     * Left value if empty.
     * @param rightWhenSome source of right value when Optional is non-empty
     * @param leftWhenNone left value when Optional is empty
     * @param <E> type of right result
     * @param <R> type of left result
     * @return an Either
     */
    public static <E,R> Either<E,R> fromOptional(final Optional<R> rightWhenSome, final E leftWhenNone) {
        return rightWhenSome.map(Eithers::<E, R>right).orElse(Eithers.left(leftWhenNone));
    }

    /**
     * Hamcrest matchers that may be useful in Case etc.
     */
    public static final class Matchers {

        public static Matcher<Eithers.Either> isLeft() { return Left; }
        public static <V> Matcher<V> isLeft(V v) { return new LeftMatcher(is(v)); }
        public static <V> Matcher<V> isLeft(Matcher<V> m) { return new LeftMatcher(m); }
        public static Matcher<Eithers.Either> isRight() { return Right; }
        public static <V> Matcher<V> isRight(Matcher<V> m) { return new RightMatcher(m); }
        public static <V> Matcher<V> isRight(V v) { return new RightMatcher(is(v)); }

        public static final Matcher<Eithers.Either> Left = new BaseMatcher<Eithers.Either>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Left");
            }

            @Override
            public boolean matches(Object item) {
                return item instanceof Either.Left;
            }
        };

        public static final Matcher<Eithers.Either> Right = new BaseMatcher<Eithers.Either>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Right");
            }

            @Override
            public boolean matches(Object item) {
                return item instanceof Either.Right;
            }
        };

        public static class LeftMatcher<V> extends TypeSafeMatcher<Eithers.Either> {
            private final Matcher<V> m;

            public LeftMatcher(Matcher<V> m) {
                this.m = m;
            }

            @Override
            protected boolean matchesSafely(final Either item) {
                return item.isLeft() && m.matches(item.left());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Left").appendDescriptionOf(m);
            }
        }

        public static class RightMatcher<V> extends TypeSafeMatcher<Eithers.Either> {
            private final Matcher<V> m;

            public RightMatcher(Matcher<V> m) {
                this.m = m;
            }

            @Override
            protected boolean matchesSafely(final Either item) {
                return item.isRight() && m.matches(item.right());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Right").appendDescriptionOf(m);
            }
        }


    }

    public interface Either<E,R> {

        default E left() {
            throw new IllegalStateException("Not left");
        }

        default R right() {
            throw new IllegalStateException("Not right");
        }

        default boolean isLeft() {
            return this instanceof Left;
        }

        default boolean isLeft(Matcher<E> m) {
            return Matchers.isLeft(m).matches(this);
        }

        default boolean isLeft(E e) {
            return Matchers.isLeft(e).matches(this);
        }

        default boolean isRight() {
            return this instanceof Right;
        }

        default boolean isRight(Matcher<R> m) {
            return Matchers.isRight(m).matches(this);
        }

        default boolean isRight(R r) {
            return Matchers.isRight(r).matches(this);
        }

        /**
         * Applies `fl` if this is a `Left` or `fr` if this is a `Right`.
         *
         * @param fl the function to apply if this is a `Left`
         * @param fr the function to apply if this is a `Right`
         * @param <T> type of function result
         * @return the result of applying the appropriate function
         */
        default <T> T fold(Function<E, T> fl, Function<R, T> fr) {
            if(isRight())
                return fr.apply(right());

            return fl.apply(left());
        }

        /**
         * return the result of applying function f to the current right result, wrapped in a Right Either,
         * if currently on right path, otherwise return this Either.
         * @param f to apply if right leaning
         * @param <T> type of function result
         * @return either Right wrapped result of function, or current Left.
         */
        default <T> Either<E,T> map(Function<R, T> f) {
            if(isRight())
                return Eithers.right(f.apply(right()));

            return Eithers.left(left());
        }

        /**
         * return the Either result of applying function f to the current right result,
         * if currently on the right path, otherwise return this Either.
         * @param f function to map over right values
         * @param <T> type of function right result
         * @return the Either result of applying function, or current Left.
         */
        default <T> Either<E,T> flatMap(Function<R, Either<E,T>> f) {
            if(isRight())
                return f.apply(right());

            return Eithers.left(left());
        }

        /**
         * applies a two-argument function to the right values of this and another Either, if both are Right,
         * or returns first Left of the two.
         * @param other another Either
         * @param f function to call if both Eithers are Right
         * @param <S> type of this Either's right value
         * @param <T> type of the other Either's right value
         * @return an Either which will be the result of f(this.right(),other.right()) if both arguments are Right.
         */
        default <S,T> Either<E,T> map2(Either<E,S> other, BiFunction<R,S,T> f) {
            return flatMap( t -> other.map( o -> f.apply(t,o) ) );
        }

        /**
         * recovers from a Left value by providing a Right value, or keeps right value if already a Right.
         * @param f function providing a right value, of the same type as this Either's R.
         * @return a Right&lt;R&gt;
         */
        default Either<E,R> recover(Function<E,R> f) {
            if(isLeft())
                return Eithers.right( f.apply(left()) );

            return this;
        }

        /**
         * fails to the left, regardless of current direction.
         * @param f a function which is always called to provide a new left value.
         * @return a Left&lt;E&gt;
         */
        default Either<E,R> fail(Function<Either<E,R>,E> f) {
            return Eithers.left( f.apply(this) );
        }

        /**
         * fails to the left, regardless of current direction.
         * @param f a function which is always called to provide the new left value.
         * @return a Left&lt;E&gt;
         */
        default Either<E,R> fail(Supplier<E> f) {
            return Eithers.left( f.get() );
        }

        /**
         * returns the Either's right value, or if Left, an alternative value supplied by the function.
         * @param f function which returns an alternative value, given the Left value.
         * @return a value
         */
        default R orElse(Function<E,R> f) {
            if(isLeft())
                return f.apply(left());

            return right();
        }

        /**
         * returns the Either's right value, or if Left, an alternative value.
         * @param value alternative value if Left
         * @return a value
         */
        default R orElse(R value) {
            if(isLeft())
                return value;

            return right();
        }

        /**
         * convenience method return Right as an Optional, or Optional.empty() if Left.
         * @return Optional&lt;R&gt; of Right, or empty when Left.
         */
        default Optional<R> toOptional() {
            return map(Optional::of).orElse(Optional.empty());
        }

        /**
         * returns result of applying a function to Right as an Optional,
         * or Optional.empty() if Left.
         * @param t transformation function
         * @param <T> type of result
         * @return Optional&lt;T&gt; of <i>t(Right)</i>, or empty when Left.
         */
        default <T> Optional<T> toOptional(Function<R,T> t) {
            return fold(l -> Optional.<T>empty(), r -> Optional.ofNullable(t.apply(r)));
        }

        /**
         * returns the Either's right value, or if Left, throws the Exception generated by the function,
         * which is passed the left value.
         * @param exceptionFunction function that returns an Exception
         * @param <X> exception type
         * @return the right value of the Either
         * @throws X exception
         */
        default <X extends Throwable> R orElseThrow(Function<E, ? extends X> exceptionFunction) throws X {
            if(isLeft()) {
                throw exceptionFunction.apply(left());
            }
            return right();
        }

        /**
         * apply the function f to the Either, but otherwise continue the current path.
         * @param f function with possible side-effect
         * @return this Either
         */
        default Either<E,R> peek(Consumer<Either<E,R>> f) {
            f.accept(this);
            return this;
        }

        /**
         * apply the left value to the Consumer function if Left, then continue the current path.
         * @param f void function to apply to left value
         * @return this Either
         */
        default Either<E,R> ifLeft(Consumer<E> f) {
            if(isLeft()) f.accept(left());
            return this;
        }

        /**
         * apply the right value to the Consumer function if Right, then continue the current path.
         * @param f void function to apply to right value
         * @return this Either
         */
        default Either<E,R> ifRight(Consumer<R> f) {
            if(isRight()) f.accept(right());
            return this;
        }

        class Right<E, R> implements Either<E, R> {

            private final R right;

            public Right(R right) {
                this.right = right;
            }

            @Override
            public R right() {
                return right;
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this).addValue(right).toString();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Right<?, ?> right1 = (Right<?, ?>) o;
                return Objects.equals(right, right1.right);
            }

            @Override
            public int hashCode() {
                return Objects.hash(right);
            }
        }

        class Left<E, R> implements Either<E, R> {
            private final E left;

            public Left(E left) {
                this.left = left;
            }

            @Override
            public E left() {
                return left;
            }

            @Override
            public String toString() {
                return MoreObjects.toStringHelper(this).addValue(left).toString();
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Left<?, ?> left1 = (Left<?, ?>) o;
                return Objects.equals(left, left1.left);
            }

            @Override
            public int hashCode() {
                return Objects.hash(left);
            }
        }
    }
}
