package com.dataseries;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CursorIterator<T> implements Iterator<Cursor<T>> {

    private final Iterator<T> iterator;

    private Optional<Cursor<T>> state = Optional.empty();
    private Boolean isPulled = false;
    private Boolean hasNext = true;

    public CursorIterator(final Iterator<T> iterator) {
        this.iterator = iterator;
    }

    private final Optional<Cursor<T>> getState() {
        if (this.state.isPresent()) {
            switch (this.state.get()) {
                case Cursor.Single<T> single -> Optional.empty();
                case Cursor.Pair<T> pair -> {
                    if (!this.iterator.hasNext())
                        return Optional.of(Cursor.single(pair.second()));
                    return Optional.of(Cursor.pair(pair.second(), this.iterator.next()));
                }
            }
        }

        if (!this.iterator.hasNext())
            return Optional.empty();
        final var current = this.iterator.next();

        if (!this.iterator.hasNext())
            return Optional.of(Cursor.single(current));

        return Optional.of(Cursor.pair(current, this.iterator.next()));
    }

    private final void pull() {
        if (this.isPulled)
            return;

        if (!this.hasNext) {
            this.isPulled = true;
            return;
        }

        this.isPulled = true;
        this.state = getState();
        this.hasNext = this.state.isPresent();
    }

    @Override
    public final boolean hasNext() {
        pull();
        return this.hasNext;
    }

    @Override
    public final Cursor<T> next() {
        pull();
        if (this.state.isEmpty())
            throw new NoSuchElementException();

        this.isPulled = false;
        return this.state.get();
    }
}
