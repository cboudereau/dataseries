package io.github.cboudereau.dataseries.snippets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Series;
import io.github.cboudereau.dataseries.UnionResult;

public class SamplesTest {
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

    @Test
    public void intersection() {
        var s1 = List.of(Series.datapoint(3, 50));
        var s2 = List.of(Series.datapoint(4, 100), Series.datapoint(7, 110));

        var actual = Series.union(s1, s2, SamplesTest::toTuple).stream().filter(x -> x.data().isPresent())
                .map(x -> Series.datapoint(x.point(), x.data().get())).toArray();

        var expected = List.of(
                Series.datapoint(4, new Tuple<>(50, 100)),
                Series.datapoint(7, new Tuple<>(50, 110))).toArray();

        assertArrayEquals(expected, actual);
    }

    private static record Tuple<L, R>(L fst, R snd) {
    }

    private static <L, R> Optional<Tuple<L, R>> toTuple(UnionResult<L, R> unionResult) {
        switch (unionResult) {
            case UnionResult.LeftOnly<L, R> x -> {
                return Optional.empty();
            }
            case UnionResult.RightOnly<L, R> x -> {
                return Optional.empty();
            }

            case UnionResult.Both<L, R> both -> {
                return Optional.of(new Tuple<L, R>(both.left(), both.right()));
            }
        }
    }
}