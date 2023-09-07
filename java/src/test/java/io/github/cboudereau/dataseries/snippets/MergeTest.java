package io.github.cboudereau.dataseries.snippets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Series;

public class MergeTest {
    @Test
    public void contiguousTest() {
        final var s1 = List.of(Series.datapoint(1, 100), Series.datapoint(3, 100));
        final var actual = Series.merge(s1);

        final var expected = List.of(Series.datapoint(1, 100));

        assertArrayEquals(expected.toArray(), actual.stream().toArray());
    }
}
