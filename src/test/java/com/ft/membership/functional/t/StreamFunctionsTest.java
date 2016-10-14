package com.ft.membership.functional.t;

import org.junit.Test;

import static com.ft.membership.functional.StreamFunctions.safeStreamOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static com.ft.membership.functional.StreamFunctions.*;

public class StreamFunctionsTest {

    @Test
    public void safeStreamOf_returns_empty_stream_for_null_array() throws Exception {
        //noinspection NullArgumentToVariableArgMethod
        assertThat(safeStreamOf((Object)null).count(), is(0L));
    }

    @Test
    public void safeStreamOf_returns_empty_stream_for_empty_array() throws Exception {
        assertThat(safeStreamOf(new String[0]).count(), is(0L));
    }

    @Test
    public void safeStreamOf_returns_empty_stream_for_lack_of_array() throws Exception {
        assertThat(safeStreamOf().count(), is(0L));
    }

    @Test
    public void safeStreamOf_returns_empty_stream_when_only_null_items() throws Exception {
        assertThat(safeStreamOf(null, null).count(), is(0L));
    }

    @Test
    public void safeStreamOf_returns_stream_for_n_non_null_items() throws Exception {
        assertThat(safeStreamOf(1).count(), is(1L));
        assertThat(safeStreamOf(1, 2).count(), is(2L));
        assertThat(safeStreamOf(1, 2, 3).count(), is(3L));
    }

    @Test
    public void flatStreamOf_returns_stream_without_null_items() throws Exception {
        assertThat(safeStreamOf(null, 1, null, 2, 3, null).count(), is(3L));
    }
}