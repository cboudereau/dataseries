package com.dataseries;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Iterator;

public class DataPoints {
    public static <P, T> DataPoint<P, T> datapoint(final P point, final T data) {
        return new DataPoint<>(point, data);
    }

    public static <P, T> Stream<DataPoint<P, T>> stream(final Iterator<DataPoint<P, T>> iterator) {
        Iterable<DataPoint<P, T>> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static <P extends Comparable<P>, L, R, T> Iterator<DataPoint<P, T>> union(
            final Iterator<DataPoint<P, L>> left, final Iterator<DataPoint<P, R>> right,
            final Function<UnionResult<L, R>, T> f) {
        return new Union<P, L, R, T>(left, right, f);
    }
}
