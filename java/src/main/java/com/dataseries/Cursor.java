package com.dataseries;

import java.util.function.Function;

public sealed interface Cursor<T> permits Cursor.Single, Cursor.Pair {
    public static final record Single<T>(T v) implements Cursor<T> {
    }

    public static final record Pair<T>(T first, T second) implements Cursor<T> {
    }

    public static <T> Single<T> single(final T v) {
        return new Single<>(v);
    }

    public static <T> Pair<T> pair(final T fst, final T snd) {
        return new Pair<T>(fst, snd);
    }

    default <R> Cursor<R> map(final Function<T, R> f) {
        switch (this) {
            case final Single<T> s -> {
                return Cursor.single(f.apply(s.v));
            }
            case final Pair<T> p -> {
                return Cursor.pair(f.apply(p.first), f.apply(p.second));
            }
        }
    }

    static <T extends Comparable<T>> Boolean canOverlap(final Cursor<T> left, final Cursor<T> right) {
        final var fst = left.fst().compareTo(right.fst()) > 0 ? left.fst() : right.fst();
        final var snd = snd(left).compareTo(snd(right)) < 0 ? snd(left) : snd(right);
        return snd.isGreaterThan(Value.fixed(fst));
    }

    default T fst() {
        switch (this) {
            case final Single<T> s -> {
                return s.v;
            }
            case final Pair<T> p -> {
                return p.first;
            }
        }
    }

    static <T extends Comparable<T>> Value<T> snd(final Cursor<T> x) {
        switch (x) {
            case final Single<T> s -> {
                return Value.infinite();
            }
            case final Pair<T> p -> {
                return Value.fixed(p.second);
            }
        }
    }
}
