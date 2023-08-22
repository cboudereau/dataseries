package com.dataseries;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Series {
    public static final <P, T> DataPoint<P, T> datapoint(final P point, final T data) {
        return new DataPoint<>(point, data);
    }

    public static final <P, T> Stream<DataPoint<P, T>> stream(final Iterable<DataPoint<P, T>> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    public static final <P extends Comparable<P>, L, R, T> Iterable<DataPoint<P, T>> union(
            final Iterable<DataPoint<P, L>> left, final Iterable<DataPoint<P, R>> right,
            final Function<UnionResult<L, R>, T> f) {
        return () -> new Union<P, L, R, T>(left.iterator(), right.iterator(), f);
    }
}
