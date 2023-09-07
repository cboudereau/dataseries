package io.github.cboudereau.dataseries.snippets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Series;
import io.github.cboudereau.dataseries.UnionResult;

public class SimpleTest {
    @Test
    public void simple() {
        var s1 = List.of(Series.datapoint(3, 50));
        var s2 = List.of(Series.datapoint(4, 100), Series.datapoint(7, 110));

        var actual = Series.union(s1, s2, x -> x).stream().toArray();

        var expected = List.of(
                Series.datapoint(3, UnionResult.leftOnly(50)),
                Series.datapoint(4, UnionResult.both(50, 100)),
                Series.datapoint(7, UnionResult.both(50, 110))).toArray();

        assertArrayEquals(expected, actual);
    }
}