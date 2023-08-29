package com.dataseries;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class MergeTest {

    @Test
    public void PullMergeWithEmptyValueTest() {
        List<DataPoint<Integer, String>> x = List.of();
        assertThrows(NoSuchElementException.class, () -> Series.merge(x).iterator().next());
    }

    @Test
    public void MergeWithEmptyValueTest() {
        List<DataPoint<Integer, String>> x = List.of();
        var expected = new Object[] {};
        var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void MergeWithSameValueTest() {
        List<DataPoint<Integer, Optional<Integer>>> x = List.of(
                Series.datapoint(1, Optional.of(10)),
                Series.datapoint(5, Optional.of(10)),
                Series.datapoint(10, Optional.empty()));

        var expected = List.of(Series.datapoint(1, Optional.of(10)), Series.datapoint(10, Optional.empty())).toArray();

        var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void MergeWithDifferentValueTest() {
        List<DataPoint<Integer, Optional<Integer>>> x = List.of(
                Series.datapoint(1, Optional.of(10)),
                Series.datapoint(5, Optional.of(100)),
                Series.datapoint(10, Optional.empty()));

        var expected = List.of(
                Series.datapoint(1, Optional.of(10)),
                Series.datapoint(5, Optional.of(100)),
                Series.datapoint(10, Optional.empty())).toArray();

        var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }
}
