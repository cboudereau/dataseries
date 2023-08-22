package com.dataseries;

import java.util.function.Function;

public class Series {
    public static final <P, T> DataPoint<P, T> datapoint(final P point, final T data) {
        return new DataPoint<>(point, data);
    }

    public static final <P extends Comparable<P>, L, R, T> IterableSeries<P, T> union(
            final Iterable<DataPoint<P, L>> left, final Iterable<DataPoint<P, R>> right,
            final Function<UnionResult<L, R>, T> f) {
        return () -> new Union<>(left.iterator(), right.iterator(), f);
    }

    public static final <P, T> IterableSeries<P, T> merge(
            final Iterable<DataPoint<P, T>> series) {
        return () -> new Merge<>(series.iterator());
    }
}
