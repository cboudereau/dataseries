package io.github.cboudereau.dataseries.snippets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Series;
import io.github.cboudereau.dataseries.UnionResult;

public class IntersectionTest {
    @Test
    public void intersection() {
        final var s1 = List.of(Series.datapoint(3, 50));
        final var s2 = List.of(Series.datapoint(4, 100), Series.datapoint(7, 110));

        final var actual = Series.union(s1, s2, IntersectionTest::toTuple).stream().filter(x -> x.data().isPresent())
                .map(x -> Series.datapoint(x.point(), x.data().get())).toArray();

        final var expected = List.of(
                Series.datapoint(4, new Tuple<>(50, 100)),
                Series.datapoint(7, new Tuple<>(50, 110))).toArray();

        assertArrayEquals(expected, actual);
    }

    private static record Tuple<L, R>(L fst, R snd) {
    }

    private static <L, R> Optional<Tuple<L, R>> toTuple(UnionResult<L, R> unionResult) {
        return switch (unionResult) {
            case final UnionResult.LeftOnly<L, R> x -> Optional.empty();
            case final UnionResult.RightOnly<L, R> x -> Optional.empty();
            case final UnionResult.Both<L, R> both -> Optional.of(new Tuple<L, R>(both.left(), both.right()));
        };
    }
}