package io.github.cboudereau.dataseries;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class MergeTest {

    @Test
    public void PullMergeWithEmptyValueTest() {
        final List<DataPoint<Integer, String>> x = List.of();
        final var iterator = Series.merge(x).iterator();
        assertThrows(NoSuchElementException.class, () -> iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void MergeWithEmptyValueTest() {
        final List<DataPoint<Integer, String>> x = List.of();
        final var expected = new Object[] {};
        final var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void MergeWithSameValueTest() {
        final List<DataPoint<Integer, Optional<Integer>>> x = List.of(
                Series.datapoint(1, Optional.of(10)),
                Series.datapoint(5, Optional.of(10)),
                Series.datapoint(10, Optional.empty()));

        final var expected = List.of(Series.datapoint(1, Optional.of(10)), Series.datapoint(10, Optional.empty())).toArray();

        final var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void MergeWithDifferentValueTest() {
        final List<DataPoint<Integer, Optional<Integer>>> x = List.of(
                Series.datapoint(1, Optional.of(10)),
                Series.datapoint(5, Optional.of(100)),
                Series.datapoint(10, Optional.empty()));

        final var expected = List.of(
                Series.datapoint(1, Optional.of(10)),
                Series.datapoint(5, Optional.of(100)),
                Series.datapoint(10, Optional.empty())).toArray();

        final var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }
}
