package com.dataseries;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * IterableSeries wrap DataPoint and have a default useful method to use with the java stream api
 * 
 * @param <P> the point type
 * @param <T> the data type
 */
public interface IterableSeries<P, T> extends Iterable<DataPoint<P, T>> {
    /**
     * Convert to a conventional stream
     * @return a datapoint stream
     */
    public default Stream<DataPoint<P, T>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

}