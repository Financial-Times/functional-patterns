package com.ft.membership.functional;

import com.ft.membership.functional.Eithers.Either;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.ft.membership.functional.Eithers.Matchers.*;
import static com.ft.membership.functional.Eithers.right;
import static com.ft.membership.functional.Eithers.trying;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "OptionalGetWithoutIsPresent"})
public class EithersTest {

    @Test
    public void isEither() throws Exception {
        assertThat(Eithers.left(5).isLeft(), is(true));
        assertThat(Eithers.left(5).isRight(), is(false));

        assertThat(Eithers.right(5).isLeft(), is(false));
        assertThat(Eithers.right(5).isRight(), is(true));
    }

    @Test
    public void isEitherMatcher() throws Exception {
        assertThat(Eithers.left(5).isLeft(is(5)), is(true));
        assertThat(Eithers.left(5).isLeft(is(10)), is(false));
        assertThat(Eithers.left(5).isRight(is(5)), is(false));

        assertThat(Eithers.left(5).isLeft(5), is(true));
        assertThat(Eithers.left(5).isLeft(10), is(false));
        assertThat(Eithers.left(5).isRight(5), is(false));

        assertThat(Eithers.right(5).isLeft(is(5)), is(false));
        assertThat(Eithers.right(5).isRight(is(5)), is(true));
        assertThat(Eithers.right(5).isRight(is(10)), is(false));

        assertThat(Eithers.right(5).isLeft(5), is(false));
        assertThat(Eithers.right(5).isRight(5), is(true));
        assertThat(Eithers.right(5).isRight(10), is(false));
    }

    @Test
    public void chain_right_through_different_types_of_result() throws Exception {

        Either<Exception, String> result =
                this.<Exception, Integer>functionThatReturnsRight(10)
                        .map(x -> 2 * x)
                        .flatMap(x -> right(x * 10))
                        .map(this::toDouble)
                        .map(Object::toString);

        assertThat(result.isRight(), is(true));
        assertThat(result.right(), is("200.0"));
    }

    @Test
    public void chain_left_from_initial_left() throws Exception {

        Eithers.Either<Exception, Double> result =
                this.<Exception, Integer>functionThatReturnsLeft(new RuntimeException("1"))
                        .map(x -> 2 * x)
                        .flatMap(x -> right(x * 10))
                        .map(this::toDouble);

        assertThat(result.isRight(), is(false));
        assertThat(result.isLeft(), is(true));
        assertThat(result.left(), instanceOf(RuntimeException.class));
        assertThat(result.left().getMessage(), is("1"));
    }

    @Test
    public void chain_left_from_flatmap_left() throws Exception {

        Either<Exception, Double> result =
                this.<Exception, Integer>functionThatReturnsRight(10)
                        .map(x -> 2 * x)
                        .flatMap(x -> this.<Exception, Integer>functionThatReturnsLeft(new ArithmeticException("2")))
                        .map(this::toDouble);

        assertThat(result.isRight(), is(false));
        assertThat(result.isLeft(), is(true));
        assertThat(result.left(), instanceOf(RuntimeException.class));
        assertThat(result.left().getMessage(), is("2"));
    }

    @Test
    public void chain_right_from_trying_when_no_exception() throws Exception {
        int x = 23;

        Either<Exception, Double> result =
                trying(() -> x / 10)
                        .map(this::toDouble);

        assertThat(result.isRight(), is(true));
        assertThat(result.isLeft(), is(false));
        assertThat(result.right(), is(2.0));
    }

    @Test
    public void chain_left_from_trying_exception() throws Exception {
        int x = 23;

        Either<Exception, Double> result =
                trying(() -> x / 0)
                        .map(this::toDouble);

        assertThat(result.isRight(), is(false));
        assertThat(result.isLeft(), is(true));
        assertThat(result.left(), instanceOf(ArithmeticException.class));
    }

    @Test
    public void trying_with_handler() throws Exception {
        Either<String, Integer> right = trying(() -> 23, ex -> Eithers.left("E"));
        assertThat(right.isRight(), is(true));
        assertThat(right.right(), is(23));

        Either<String, Integer> handleLeft = trying(this::supplierThatAlwaysThrows, ex -> Eithers.left("E"));
        assertThat(handleLeft.isLeft(), is(true));
        assertThat(handleLeft.left(), is("E"));

        Either<String, Integer> handlerRight = trying(this::supplierThatAlwaysThrows, ex -> Eithers.right(23));
        assertThat(handlerRight.isRight(), is(true));
        assertThat(handlerRight.right(), is(23));
    }

    @Test
    public void failing_is_left() throws Exception {
        Either<String, Integer> firstLeft = Eithers.failing(() -> "E1", ex -> "E2");
        assertThat(firstLeft.isLeft(), is(true));
        assertThat(firstLeft.left(), is("E1"));

        Either<String, Integer> secondLeft = Eithers.failing(this::supplierThatAlwaysThrows, ex -> "E2");
        assertThat(secondLeft.isLeft(), is(true));
        assertThat(secondLeft.left(), is("E2"));
    }

    @Test
    public void map2_applies_function_when_both_right() throws Exception {
        Either<Exception, Integer> result =
                Eithers.<Exception, Integer>right(42)
                        .map2(Eithers.right(10), (a, b) -> a * b);

        assertThat(result.isLeft(), is(false));
        assertThat(result.isRight(), is(true));
        assertThat(result.right(), is(420));
    }

    @Test
    public void map2_returns_first_left_when_only_first_either_is_left() throws Exception {
        Either<Exception, Integer> result =
                Eithers.<Exception, Integer>left(new ArithmeticException())
                        .map2(Eithers.right(10), (a, b) -> a * b);

        assertThat(result.isRight(), is(false));
        assertThat(result.isLeft(), is(true));
        assertThat(result.left(), instanceOf(ArithmeticException.class));
    }

    @Test
    public void map2_returns_first_left_when_only_second_either_is_left() throws Exception {
        Either<Exception, Integer> result =
                Eithers.<Exception, Integer>right(42)
                        .map2(Eithers.<Exception, Integer>left(new ArithmeticException()), (a, b) -> a * b);

        assertThat(result.isRight(), is(false));
        assertThat(result.isLeft(), is(true));
        assertThat(result.left(), instanceOf(ArithmeticException.class));
    }

    @Test
    public void map2_returns_first_left_when_both_are_left() throws Exception {
        Either<Exception, Integer> result =
                Eithers.<Exception, Integer>left(new ArithmeticException())
                        .map2(Eithers.<Exception, Integer>left(new NullPointerException()), (a, b) -> a * b);

        assertThat(result.isRight(), is(false));
        assertThat(result.isLeft(), is(true));
        assertThat(result.left(), instanceOf(ArithmeticException.class));
    }


    @Test
    public void recovers_from_left() throws Exception {
        Either<String, Integer> result =
                Eithers.<String, Integer>left("Error").recover(__ -> 55);

        assertThat(result.isRight(), is(true));
        assertThat(result.right(), is(55));
    }

    @Test
    public void recover_from_right_is_same_right() throws Exception {
        Either<String, Integer> result =
                Eithers.<String, Integer>right(42).recover(__ -> 55);

        assertThat(result.isRight(), is(true));
        assertThat(result.right(), is(42));
    }

    @Test
    public void fail_with_function_is_left() throws Exception {
        Either<String,Integer> right = Eithers.right(42);
        Either<String,Integer> left = Eithers.left("L");

        assertThat(right.fail(e -> "Fail").isLeft(), is(true));
        assertThat(right.fail(e -> "Fail").left(), is("Fail"));

        assertThat(left.fail(e -> "Fail").isLeft(), is(true));
        assertThat(left.fail(e -> "Fail").left(), is("Fail"));
    }

    @Test
    public void fail_with_supplier_is_left() throws Exception {
        Either<String,Integer> right = Eithers.right(42);
        Either<String,Integer> left = Eithers.left("L");

        assertThat(right.fail(() -> "Fail").isLeft(), is(true));
        assertThat(right.fail(() -> "Fail").left(), is("Fail"));

        assertThat(left.fail(() -> "Fail").isLeft(), is(true));
        assertThat(left.fail(() -> "Fail").left(), is("Fail"));
    }

    @Test
    public void or_else() throws Exception {
        assertThat(Eithers.left("L").orElse("A"), is("A"));
        assertThat(Eithers.right("R").orElse("A"), is("R"));
    }

    @Test
    public void or_else_with_function() throws Exception {
        assertThat(Eithers.left("L").orElse(__ -> "A"), is("A"));
        assertThat(Eithers.right("R").orElse(__ -> "A"), is("R"));
    }

    @Test
    public void or_else_throw() throws Exception {
        final Either<String, String> r = Eithers.right("R");

        assertThat(r.orElseThrow(RuntimeException::new), is("R"));

        try {
            Eithers.<String, String>left("L").orElseThrow(RuntimeException::new);
            fail("Expecting exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("L"));
        }
    }

    @Test
    public void peek_has_no_effect_on_either() throws Exception {
        AtomicInteger integer = new AtomicInteger();

        final Either<String, Object> lefty = Eithers.left("L");
        assertThat(lefty.peek(either -> integer.incrementAndGet()),is(lefty));
        assertThat(integer.get(), is(1));

        final Either<Object, String> righty = Eithers.right("R");
        assertThat(righty.peek(either -> integer.incrementAndGet()), is(righty));
        assertThat(integer.get(), is(2));
    }

    @Test
    public void ifLeft() throws Exception {
        AtomicReference<String> ref = new AtomicReference<>("N");

        Either<String,String> righty = Eithers.right("R");
        Either<String,String> righty1 = righty.ifLeft(ref::set);
        assertThat(ref.get(), is("N"));
        assertThat(righty1, is(righty));

        Either<String, String> lefty = Eithers.left("L");
        Either<String, String> lefty1 = lefty.ifLeft(ref::set);
        assertThat(ref.get(), is("L"));
        assertThat(lefty1, is(lefty));
    }

    @Test
    public void ifRight() throws Exception {
        AtomicReference<String> ref = new AtomicReference<>("N");

        final Either<String, String> lefty = Eithers.left("L");
        final Either<String, String> lefty1 = lefty.ifRight(ref::set);
        assertThat(ref.get(), is("N"));
        assertThat(lefty1, is(lefty));

        final Either<String, String> righty = Eithers.right("R");
        final Either<String, String> righty1 = righty.ifRight(ref::set);
        assertThat(ref.get(), is("R"));
        assertThat(righty1, is(righty));
    }

    @Test
    public void toOptional() throws Exception {
        final Either<Integer,String> left = Eithers.left(76);
        final Either<Integer,String> right = Eithers.right("R");

        assertThat(left.toOptional().isPresent(), is(false));
        assertThat(right.toOptional().isPresent(), is(true));
        assertThat(right.toOptional().get(), is("R"));
    }

    @Test
    public void toOptionalWithTransform() throws Exception {
        final Either<Integer,String> left = Eithers.left(76);
        final Either<Integer,String> right = Eithers.right("R");

        final Function<String, String> t = r -> r + r;
        assertThat(left.toOptional(t).isPresent(), is(false));
        assertThat(right.toOptional(t).isPresent(), is(true));
        assertThat(right.toOptional(t).get(), is("RR"));
    }

    @Test
    public void fromOptional() throws Exception {
        final Optional<String> nonEmpty = Optional.of("R");
        final Optional<String> empty = Optional.empty();

        assertThat(Eithers.fromOptional(nonEmpty,"L").isRight(), is(true));
        assertThat(Eithers.fromOptional(nonEmpty,"L").right(), is("R"));

        assertThat(Eithers.fromOptional(empty,"L").isLeft(), is(true));
        assertThat(Eithers.fromOptional(empty,"L").left(), is("L"));
    }

    @Test
    public void foldLeft() {
        Either<Integer, Integer> left = Eithers.left(1);

        assertThat(left.fold(i -> i + "A", i -> i + "B"), is("1A"));
    }

    @Test
    public void foldRight() {
        Either<Integer, Integer> right = Eithers.right(2);

        assertThat(right.fold(i -> i + "A", i -> i + "B"), is("2B"));

    }

    @Test
    public void matchers() throws Exception {
        final Either<Integer,String> left = Eithers.left(76);
        final Either<Integer,String> right = Eithers.right("R");


        assertThat(left, is(Eithers.Matchers.isLeft()));
        assertThat(left, is(Eithers.Matchers.isLeft(is(76))));
        assertThat(left, is(Eithers.Matchers.isLeft(76)));
        assertThat(left, not(is(Eithers.Matchers.isRight())));
        assertThat(left, not(is(Eithers.Matchers.isLeft(is(77)))));
        assertThat(left, not(is(Eithers.Matchers.isLeft(77))));
        assertThat(left, not(is(Eithers.Matchers.isRight(is("R")))));
        assertThat(left, not(is(Eithers.Matchers.isRight("R"))));

        assertThat(right, is(Eithers.Matchers.isRight()));
        assertThat(right, not(is(Eithers.Matchers.isLeft())));
        assertThat(right, is(Eithers.Matchers.isRight(is("R"))));
        assertThat(right, is(Eithers.Matchers.isRight("R")));
        assertThat(right, not(is(Eithers.Matchers.isRight(is("S")))));
        assertThat(right, not(is(Eithers.Matchers.isRight("S"))));
        assertThat(right, not(Eithers.Matchers.isLeft(is(76))));
        assertThat(right, not(Eithers.Matchers.isLeft(76)));
    }

    @Test
    @Ignore
    public void doc_example() {
        final long someInput = System.currentTimeMillis();

        Either<Exception, String> n =
                trying(() -> functionThatMightThrow(someInput))
                        .map(x -> x * 100)
                        .map(x -> String.format("The number of the day is %.2f", x));

        // now handle any exceptions
        if (n.isRight()) {
            // success; value in n.right()
            System.out.println(n.right());
        } else {
            // failure; exception in n.left()
            System.out.println(n.left().getMessage());
        }

        // or using Case
        System.out.println(
                Case.<String>match(n)
                        .when(Left).apply(() -> n.left().getMessage())
                        .when(isRight(0.0)).apply(() -> "nada")
                        .when(isRight(containsString(".0"))).apply(() -> n.right().split(".")[0])
                        .when(Right).apply(n::right)
                        .get()
        );
    }

    private Double functionThatMightThrow(long input) throws ArithmeticException {
        final Random random = new Random();
        return input / random.nextDouble();
    }

    private Double toDouble(Number a) {
        return a.doubleValue();
    }

    private <E, R> Either<E, R> functionThatReturnsRight(R rightValue) {
        return Eithers.right(rightValue);
    }

    private <E, R> Either<E, R> functionThatReturnsLeft(E leftValue) {
        return Eithers.left(leftValue);
    }

    private <T> T supplierThatAlwaysThrows() {
        throw new RuntimeException("SEx");
    }
}