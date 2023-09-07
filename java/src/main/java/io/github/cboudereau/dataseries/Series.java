package io.github.cboudereau.dataseries;

import java.util.function.Function;

/**
 * The entrypoint of the api
 */
public class Series {
    private Series() {

    }

    /**
     * a helper function to create a datapoint
     * 
     * @param <P>   the point type
     * @param <T>   the data type
     * @param point the point
     * @param data  the data
     * @return a datapoint
     */
    public static final <P extends Comparable<P>, T> DataPoint<P, T> datapoint(final P point, final T data) {
        return new DataPoint<>(point, data);
    }

    /**
     * union 2 series and combine union result with the given function
     * 
     * @param <P>   the point type should be common for left and right series
     * @param <L>   the left type
     * @param <R>   the right type
     * @param <T>   the return of the applied function to union result
     * @param left  the left serie
     * @param right the right serie
     * @param f     the function applied to convert union result to T type
     * @return a iterable series
     */
    public static final <P extends Comparable<P>, L, R, T> IterableSeries<P, T> union(
            final Iterable<DataPoint<P, L>> left, final Iterable<DataPoint<P, R>> right,
            final Function<UnionResult<L, R>, T> f) {
        return () -> new Union<>(left.iterator(), right.iterator(), f);
    }

    /**
     * merge a serie to be more compact when contigous events have the same data
     * 
     * @param <P>    the point type
     * @param <T>    the data type
     * @param series the series to merge
     * @return a merged series which have no more duplicated events for the same
     *         data
     */
    public static final <P, T> IterableSeries<P, T> merge(
            final Iterable<DataPoint<P, T>> series) {
        return () -> new Merge<>(series.iterator());
    }
}
