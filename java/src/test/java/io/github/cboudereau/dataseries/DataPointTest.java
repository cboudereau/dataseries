package io.github.cboudereau.dataseries;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DataPointTest {
    @Test
    public void datapointTest() {
        final var x = Series.datapoint(1, "hello");
        final var y = Series.datapoint(1, "hello");
        assertEquals(x, y);
    }
}
