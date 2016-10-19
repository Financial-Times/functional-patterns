package com.ft.functional.t;

import com.ft.functional.StreamFunctions;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StreamFunctionsTest {

    @Test
    public void safeStreamOf_returns_empty_stream_for_null_array() throws Exception {
        //noinspection NullArgumentToVariableArgMethod
        assertThat(StreamFunctions.safeStreamOf((Object)null).count(), is(0L));
    }

    @Test
    public void safeStreamOf_returns_empty_stream_for_empty_array() throws Exception {
        assertThat(StreamFunctions.safeStreamOf(new String[0]).count(), is(0L));
    }

    @Test
    public void safeStreamOf_returns_empty_stream_for_lack_of_array() throws Exception {
        assertThat(StreamFunctions.safeStreamOf().count(), is(0L));
    }

    @Test
    public void safeStreamOf_returns_empty_stream_when_only_null_items() throws Exception {
        assertThat(StreamFunctions.safeStreamOf(null, null).count(), is(0L));
    }

    @Test
    public void safeStreamOf_returns_stream_for_n_non_null_items() throws Exception {
        assertThat(StreamFunctions.safeStreamOf(1).count(), is(1L));
        assertThat(StreamFunctions.safeStreamOf(1, 2).count(), is(2L));
        assertThat(StreamFunctions.safeStreamOf(1, 2, 3).count(), is(3L));
    }

    @Test
    public void flatStreamOf_returns_stream_without_null_items() throws Exception {
        assertThat(StreamFunctions.safeStreamOf(null, 1, null, 2, 3, null).count(), is(3L));
    }
}