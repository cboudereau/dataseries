package com.dataseries;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public final class Union<P extends Comparable<P>, R, L, T> implements Iterator<DataPoint<P, T>> {
    public static sealed interface UnionResult<L, R>
            permits UnionResult.LeftOnly, UnionResult.RightOnly, UnionResult.Both {
        public static record Both<L, R>(L left, R right) implements UnionResult<L, R> {

        }

        public static record LeftOnly<L, R>(L left) implements UnionResult<L, R> {
        }

        public static record RightOnly<L, R>(R right) implements UnionResult<L, R> {

        }

        public static <L, R> LeftOnly<L, R> leftOnly(L left) {
            return new LeftOnly<L, R>(left);
        }

        public static <L, R> RightOnly<L, R> rightOnly(R right) {
            return new RightOnly<L, R>(right);
        }

        public static <L, R> Both<L, R> both(L left, R right) {
            return new Both<L, R>(left, right);
        }
    }

    private static sealed interface UnionState<L, R> permits UnionState.None, UnionState.LeftOnly, UnionState.RightOnly,
            UnionState.Disjointed, UnionState.Overlapped {
        static record None<L, R>() implements UnionState<L, R> {
        }

        static record LeftOnly<L, R>(Cursor<L> left) implements UnionState<L, R> {
        }

        static record RightOnly<L, R>(Cursor<R> right) implements UnionState<L, R> {
        }

        static record Disjointed<L, R>(Cursor<L> left, Cursor<R> right) implements UnionState<L, R> {
        }

        static record Overlapped<L, R>(Cursor<L> left, Cursor<R> right) implements UnionState<L, R> {
        }

        static <L, R> Overlapped<L, R> overlapped(final Cursor<L> left, final Cursor<R> right) {
            return new Overlapped<>(left, right);
        }

        static <L, R> LeftOnly<L, R> leftOnly(final Cursor<L> left) {
            return new LeftOnly<>(left);
        }

        static <L, R> RightOnly<L, R> rightOnly(final Cursor<R> right) {
            return new RightOnly<>(right);
        }

        static <L, R> Disjointed<L, R> disjointed(final Cursor<L> left, final Cursor<R> right) {
            return new Disjointed<>(left, right);
        }

        static <L, R> None<L, R> none() {
            return new None<>();
        }

        default Boolean isNone() {
            return switch (this) {
                case None<L, R> none -> true;
                default -> false;
            };
        }
    }

    private final CursorIterator<DataPoint<P, R>> right;
    private final CursorIterator<DataPoint<P, L>> left;
    private final Function<UnionResult<L, R>, T> f;

    private Boolean isPulled = false;
    private Boolean hasNext = true;
    private UnionState<DataPoint<P, L>, DataPoint<P, R>> state = UnionState.none();

    public Union(final Iterator<DataPoint<P, R>> right, final Iterator<DataPoint<P, L>> left,
            final Function<UnionResult<L, R>, T> f) {
        this.right = new CursorIterator<>(right);
        this.left = new CursorIterator<>(left);
        this.f = f;
    }

    private final static <P extends Comparable<P>, L, R> UnionState<DataPoint<P, L>, DataPoint<P, R>> getUnionState(
            final Cursor<DataPoint<P, L>> left,
            final Cursor<DataPoint<P, R>> right) {
        if (left.fst().point().compareTo(right.fst().point()) == 0) {
            return UnionState.overlapped(left, right);
        }
        return UnionState.disjointed(left, right);
    }

    private final UnionState<DataPoint<P, L>, DataPoint<P, R>> getState() {
        switch (this.state) {
            case final UnionState.None<DataPoint<P, L>, DataPoint<P, R>> none -> {
                final var hasLeft = this.left.hasNext();
                final var hasRight = this.right.hasNext();

                if (hasLeft && hasRight) {
                    final var left = this.left.next();
                    final var right = this.right.next();
                    return getUnionState(left, right);
                }

                if (hasLeft) {
                    return UnionState.leftOnly(this.left.next());
                }

                if (hasRight) {
                    return UnionState.rightOnly(this.right.next());
                }

                return UnionState.none();
            }
            case final UnionState.LeftOnly<DataPoint<P, L>, DataPoint<P, R>> left -> {
                if (this.left.hasNext()) {
                    return UnionState.leftOnly(this.left.next());
                }
                return UnionState.none();
            }
            case final UnionState.RightOnly<DataPoint<P, L>, DataPoint<P, R>> right -> {
                if (this.right.hasNext()) {
                    return UnionState.rightOnly(this.right.next());
                }
                return UnionState.none();
            }
            case final UnionState.Overlapped<DataPoint<P, L>, DataPoint<P, R>> overlapped -> {
                final var cmp = (Cursor.snd(overlapped.left.map(x -> x.point()))
                        .compareTo(Cursor.snd(overlapped.right.map(x -> x.point()))));
                if (cmp < 0) {
                    if (!this.left.hasNext())
                        return UnionState.none();
                    return UnionState.overlapped(this.left.next(), overlapped.right);
                }
                if (cmp > 0) {
                    if (!this.right.hasNext())
                        return UnionState.none();
                    return UnionState.overlapped(overlapped.left, this.right.next());
                }
                if (!this.left.hasNext() || !this.right.hasNext())
                    return UnionState.none();
                return UnionState.overlapped(this.left.next(), this.right.next());
            }
            case final UnionState.Disjointed<DataPoint<P, L>, DataPoint<P, R>> disjointed -> {
                if (Cursor.canOverlap(disjointed.left.map(x -> x.point()), disjointed.right.map(x -> x.point()))) {
                    return UnionState.overlapped(disjointed.left, disjointed.right);
                }

                if (Cursor.snd(disjointed.left.map(x -> x.point()))
                        .compareTo(Cursor.snd(disjointed.right.map(x -> x.point()))) < 0) {
                    if (!this.left.hasNext())
                        return UnionState.none();
                    return getUnionState(this.left.next(), disjointed.right);
                }

                if (!this.right.hasNext())
                    return UnionState.none();
                return getUnionState(disjointed.left, this.right.next());
            }
        }
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
        this.hasNext = !this.state.isNone();
    }

    @Override
    public final boolean hasNext() {
        pull();
        return this.hasNext;
    }

    @Override
    public final DataPoint<P, T> next() {
        pull();
        if (this.state.isNone())
            throw new NoSuchElementException();

        this.isPulled = false;
        switch (this.state) {
            case final UnionState.None<DataPoint<P, L>, DataPoint<P, R>> none -> throw new NoSuchElementException();
            case final UnionState.LeftOnly<DataPoint<P, L>, DataPoint<P, R>> leftOnly -> {
                final var left = leftOnly.left().fst();
                return new DataPoint<>(left.point(), this.f.apply(UnionResult.leftOnly(left.data())));
            }
            case final UnionState.RightOnly<DataPoint<P, L>, DataPoint<P, R>> rightOnly -> {
                final var right = rightOnly.right.fst();
                return new DataPoint<>(right.point(), this.f.apply(UnionResult.rightOnly(right.data())));
            }

            case final UnionState.Disjointed<DataPoint<P, L>, DataPoint<P, R>> disjointed -> {
                final var left = disjointed.left().fst();
                final var right = disjointed.right().fst();
                if (left.point().compareTo(right.point()) < 0) {
                    return new DataPoint<>(left.point(), this.f.apply(UnionResult.leftOnly(left.data())));
                }
                return new DataPoint<>(right.point(), this.f.apply(UnionResult.rightOnly(right.data())));
            }
            case final UnionState.Overlapped<DataPoint<P, L>, DataPoint<P, R>> overlapped -> {
                final var left = overlapped.left().fst();
                final var right = overlapped.right().fst();
                final var point = (left.point().compareTo(right.point()) > 0) ? left.point() : right.point();
                return new DataPoint<>(point, f.apply(UnionResult.both(left.data(), right.data())));
            }
        }
    }
}
