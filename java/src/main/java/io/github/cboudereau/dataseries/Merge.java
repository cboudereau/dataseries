package io.github.cboudereau.dataseries;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

final class Merge<P, T> implements Iterator<DataPoint<P, T>> {
    final Iterator<DataPoint<P, T>> series;

    private Boolean hasNext = true;
    private Boolean isPulled = false;

    private Optional<DataPoint<P, T>> current = Optional.empty();
    private Optional<DataPoint<P, T>> entry = Optional.empty();

    public Merge(final Iterator<DataPoint<P, T>> series) {
        this.series = series;
    }

    private final void pull() {
        if (this.isPulled)
            return;

        this.isPulled = true;
        pullEntry();
    }

    private final void pullEntry() {
        while (this.series.hasNext()) {
            final var next = this.series.next();

            if (this.current.isEmpty()) {
                this.current = Optional.of(next);
                continue;
            }

            if (this.current.map(x -> x.data().equals(next.data())).orElse(false)) {
                continue;
            }

            this.entry = this.current;
            this.current = Optional.of(next);
            return;
        }

        if (this.current.isPresent()) {
            this.entry = this.current;
            this.current = Optional.empty();
            return;
        }

        this.hasNext = false;
        this.entry = Optional.empty();
        return;
    }

    @Override
    public final boolean hasNext() {
        pull();
        return this.hasNext;
    }

    @Override
    public final DataPoint<P, T> next() {
        pull();
        if (this.entry.isEmpty())
            throw new NoSuchElementException();

        this.isPulled = false;
        return this.entry.get();
    }

}
