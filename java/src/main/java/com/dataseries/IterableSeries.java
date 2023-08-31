package com.dataseries;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface IterableSeries<P, T> extends Iterable<DataPoint<P, T>> {
    /**
     * Convert to a conventional stream
     */
    public default Stream<DataPoint<P, T>> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

}