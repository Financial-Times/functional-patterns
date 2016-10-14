package com.ft.membership.functional.t;

import com.ft.membership.functional.Case;
import org.junit.Test;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.ft.membership.functional.Case.Matchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class CaseTest {

    @Test
    public void match_with_hamcrest_matchers() {
        Integer a = 1;
        String b = "c";

        Integer result = Case.<Integer>match(a, b)
                .when(is(1), is("")).apply(() -> 1)
                .when(is(2), is("c")).apply(() -> 2)
                .when(is(1), startsWith("c")).apply(() -> 3)
                .when(is(1), is("c")).apply(() -> 4)
                .orElse(0);

        assertThat(result, is(3));
    }

    @Test
    public void return_first_match() {
        Integer a = 1;
        String b = "c";

        Integer result = Case.<Integer>match(a, b)
                .when(is(1), is("c")).apply(() -> 1)
                .when(is(1), is("c")).apply(() -> 2)
                .when(is(1), is("c")).apply(() -> 3)
                .orElse(0);

        assertThat(result, is(1));
    }

    @Test
    public void match_literal_objects_with_object_equals() {
        Integer a = 2;
        String b = "c";

        Integer result = Case.<Integer>match(a, b)
                .when(1, 2).apply(() -> 1)
                .when(2, "c").apply(() -> 2)
                .when("x", "c").apply(() -> 3)
                .orElse(0);

        assertThat(result, is(2));
    }

    @Test
    public void match_with_predicates() {
        Integer a = 1;
        String b = "c";

        Integer result = Case.<Integer>match(a, b)
                // urgh
                .when((Predicate<Integer>) (x -> x < 2), (Predicate) Objects::nonNull).apply(() -> 1)
                .orElse(0);

        assertThat(result, is(1));
    }

    @Test
    public void null_matcher_should_not_match_anything_nor_die_horribly() throws Exception {
        Integer a = null;
        Integer b = 2;
        Integer c = 3;

        Integer result = Case.<Integer>match(a, b, c)
                .when(null, 2, 3).apply(() -> 1)
                .orElse(0);

        assertThat(result, is(0));
    }

    @Test
    public void match_null_objects_with_Any() {
        Integer a = 2;
        String b = null;

        Integer result = Case.<Integer>match(a, b)
                .when(1, Any).apply(() -> 1)
                .when(2, Any).apply(() -> 2)
                .when(2, "c").apply(() -> 3)
                .orElse(0);

        assertThat(result, is(2));
    }

    @Test
    public void match_null_objects_with___() {
        Integer a = 2;
        String b = null;

        Integer result = Case.<Integer>match(a, b)
                .when(1, __).apply(() -> 1)
                .when(2, __).apply(() -> 2)
                .when(2, "c").apply(() -> 3)
                .orElse(0);

        assertThat(result, is(2));
    }

    @Test
    public void match_null_objects_with_Null() {
        Integer a = 2;
        String b = null;

        Integer result = Case.<Integer>match(a, b)
                .when(1,Null).apply(() -> 1)
                .when(2, Null).apply(() -> 2)
                .when(2, "c").apply(() -> 3)
                .orElse(0);

        assertThat(result, is(2));
    }

    @Test
    public void match_non_null_objects_with_Ref() {
        Integer a = 2;
        String b = "c";

        Integer result = Case.<Integer>match(a, b)
                .when(1,Null).apply(() -> 1)
                .when(2, Null).apply(() -> 2)
                .when(2, Ref).apply(() -> 3)
                .orElse(0);

        assertThat(result, is(3));
    }

    @Test
    public void fall_through_to_default_when_no_matches() {
        Integer a = 3;
        String b = "x";

        Integer result = Case.<Integer>match(a, b)
                .when(1,__).apply(() -> 1)
                .when(2, __).apply(() -> 2)
                .when(__, "c").apply(() -> 3)
                .orElse(0);

        assertThat(result, is(0));
    }

    @Test(expected = RuntimeException.class)
    public void fall_through_to_throw_when_no_matches() {
        Integer a = 3;
        String b = "x";

        Case.<Integer>match(a, b)
                .when(1,__).apply(() -> 1)
                .orElseThrow(RuntimeException::new);
    }

    @Test(expected = Case.NonMatchingCaseException.class)
    public void fall_through_to_default_throw_when_no_matches() {
        Integer a = 3;
        String b = "x";

        Case.<Integer>match(a, b)
                .when(1,__).apply(() -> 1)
                .orElseThrow();
    }

    @Test
    public void fall_through_to_supplier_when_no_matches() {
        Integer a = 3;
        String b = "x";

        Integer result = Case.<Integer>match(a, b)
                .when(1,__).apply(() -> 1)
                .orElseGet(() -> 8);

        assertThat(result, is(8));
    }

    @Test
    public void match_optional_empty() {
        Optional<String> a = Optional.empty();

        Integer result = Case.<Integer>match(a)
                .when(None).apply(() -> 1)
                .orElse(0);

        assertThat(result, is(1));
    }

    @Test
    public void match_optional_present() {
        Optional<String> a = Optional.of("Hello");

        Integer result = Case.<Integer>match(a)
                .when(None).apply(() -> 1)
                .when(Some).apply(() -> 2)
                .orElse(0);

        assertThat(result, is(2));
    }

    @Test
    public void no_illegal_argument_on_single_null_value() {
        Object a = null;
        Case.<Integer>match(a)
                .when(__).apply(() -> 1)
                .orElse(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegal_argument_on_null_varargs_array_value() {
        Object[] a = null;
        Case.<Integer>match(a)
                .when(__).apply(() -> 1)
                .orElse(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void require_same_number_of_matchers_as_values() {
        Integer a = 1;

        Case.<Integer>match(a)
                .when(__, 1).apply(() -> 1)
                .orElse(0);

    }


    @Test(expected = IllegalArgumentException.class)
    public void require_same_number_of_matchers_as_values_2() {
        Integer a = 1;

        Case.<Integer>match(a,5)
                .when(__, 1, 2).apply(() -> 1)
                .orElse(0);

    }

    @Test
    public void pass_template_args_to_function() throws Exception {
        Integer a = 3;
        String b = "x";

        Integer result = Case.<Integer>match(a, b, 19.4)
                .when(3, __, __).applyValues((args) -> args.length)
                .orElseGet(() -> 8);

        assertThat(result, is(3));
    }

    @Test
    public void example_with_template_args_as_literal_matchers_because_null_doesnt_match_null() throws Exception {
        Integer a = null;
        Integer b = 2;
        Integer c = null;

        Integer result = Case.<Integer>match(a, b, c)
                .when(a, Null, Null).apply(() -> 1)
                .when(Null, b, Null).apply(() -> 2)
                .when(Null, Null, c).apply(() -> 3)
                .orElse(0);

        assertThat(result, is(2));
    }

    @Test
    public void example_with_optional_template_args_as_literal_matchers() throws Exception {
        Optional<Integer> a = Optional.empty();
        Optional<Integer> b = Optional.of(2);
        Optional<Integer> c = Optional.empty();

        Integer result = Case.<Integer>match(a, b, c)
                .when(a, None, None).apply(() -> 1)
                .when(None, b, None).apply(() -> 2)
                .when(None, None, c).apply(() -> 3)
                .orElse(0);

        assertThat(result, is(2));
    }

    @Test
    public void example_simple_calculator() {
        String op = "add";
        Integer a = 3, b = 2;
        Integer result = Case.<Integer>match( op, a, b )
                .when( "square", Ref, Null ).apply(() -> a * a)
                .when( "cube",   Ref, Null ).apply( () -> a * a * a )
                .when( "add",    Ref, Ref  ).apply( () -> a + b )
                .when( "sub",    Ref, Ref  ).apply( () -> a - b )
                .when( "div",    Ref, 0    ).thenThrow(() -> new IllegalArgumentException("divide by zero"))
                .when( "div",    Ref, Ref  ).apply( () -> a / b )
                .map(r -> {
                    System.out.println("Either: " + r);
                    return r;
                })
                .orElseThrow( () -> new IllegalArgumentException("no matching operation" ) );

        assertThat(result, is(5));
    }
}