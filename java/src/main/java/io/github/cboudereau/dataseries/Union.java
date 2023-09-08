package io.github.cboudereau.dataseries;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

final class Union<P extends Comparable<P>, L, R, T> implements Iterator<DataPoint<P, T>> {

    sealed interface Value<T extends Comparable<T>> extends Comparable<Value<T>> permits Value.Fixed, Value.Infinite {

        public static final record Fixed<T extends Comparable<T>>(T value) implements Value<T> {
        }

        public static final record Infinite<T extends Comparable<T>>() implements Value<T> {
        }

        public static <T extends Comparable<T>> Fixed<T> fixed(final T value) {
            return new Fixed<>(value);
        }

        public static <T extends Comparable<T>> Infinite<T> infinite() {
            return new Infinite<>();
        }

        @Override
        default int compareTo(final Value<T> o) {
            switch (this) {
                case final Infinite<T> i -> {
                    switch (o) {
                        case final Infinite<T> i2 -> {
                            return 0;
                        }
                        case final Fixed<T> v -> {
                            return 1;
                        }
                    }
                }
                case final Fixed<T> v -> {
                    switch (o) {
                        case final Infinite<T> i -> {
                            return -1;
                        }
                        case final Fixed<T> v2 -> {
                            return v.value.compareTo(v2.value);
                        }
                    }
                }
            }

            // FIXME : remove this when https://openjdk.org/jeps/433 will be ready (> 17,
            // java 20 at least)
            throw new UnsupportedOperationException();
        }

        default boolean isGreaterThan(final Value<T> o) {
            return this.compareTo(o) > 0;
        }

        default boolean isLessThan(final Value<T> o) {
            return this.compareTo(o) < 0;
        }
    }

    static sealed interface Cursor<T> permits Cursor.Single, Cursor.Pair {
        static final record Single<T>(T v) implements Cursor<T> {
        }

        static final record Pair<T>(T first, T second) implements Cursor<T> {
        }

        static <T> Single<T> single(final T v) {
            return new Single<>(v);
        }

        static <T> Pair<T> pair(final T fst, final T snd) {
            return new Pair<T>(fst, snd);
        }

        default <R> Cursor<R> map(final Function<T, R> f) {
            return switch (this) {
                case final Single<T> s -> Cursor.single(f.apply(s.v));
                case final Pair<T> p -> Cursor.pair(f.apply(p.first), f.apply(p.second));
            };
        }

        static <T extends Comparable<T>> Boolean canOverlap(final Cursor<T> left, final Cursor<T> right) {
            final var fst = left.fst().compareTo(right.fst()) > 0 ? left.fst() : right.fst();
            final var snd = snd(left).compareTo(snd(right)) < 0 ? snd(left) : snd(right);
            return snd.isGreaterThan(Value.fixed(fst));
        }

        default T fst() {
            return switch (this) {
                case final Single<T> s -> s.v;
                case final Pair<T> p -> p.first;
            };
        }

        static <T extends Comparable<T>> Value<T> snd(final Cursor<T> x) {
            return switch (x) {
                case final Single<T> s -> Value.infinite();
                case final Pair<T> p -> Value.fixed(p.second);
            };
        }
    }

    static class CursorIterator<T> implements Iterator<Cursor<T>> {

        private final Iterator<T> iterator;

        private Optional<Cursor<T>> state = Optional.empty();
        private Boolean isPulled = false;
        private Boolean hasNext = true;

        public CursorIterator(final Iterator<T> iterator) {
            this.iterator = iterator;
        }

        private final Optional<Cursor<T>> getState() {
            if (this.state.isPresent()) {
                return switch (this.state.get()) {
                    case final Cursor.Single<T> single -> Optional.empty();
                    case final Cursor.Pair<T> pair ->
                        this.iterator.hasNext() ? Optional.of(Cursor.pair(pair.second(), this.iterator.next()))
                                : Optional.of(Cursor.single(pair.second()));
                };
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

        public final Optional<Cursor<T>> tryNext() {
            if (this.hasNext())
                return Optional.of(this.next());
            return Optional.empty();
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

        private static <L, R> UnionState<L, R> overlapped(final Cursor<L> left, final Cursor<R> right) {
            return new Overlapped<>(left, right);
        }

        private static <L, R> UnionState<L, R> leftOnly(final Cursor<L> left) {
            return new LeftOnly<>(left);
        }

        private static <L, R> UnionState<L, R> rightOnly(final Cursor<R> right) {
            return new RightOnly<>(right);
        }

        private static <L, R> Disjointed<L, R> disjointed(final Cursor<L> left, final Cursor<R> right) {
            return new Disjointed<>(left, right);
        }

        private static <L, R> None<L, R> none() {
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

    public Union(final Iterator<DataPoint<P, L>> left, final Iterator<DataPoint<P, R>> right,
            final Function<UnionResult<L, R>, T> f) {
        this.left = new CursorIterator<>(left);
        this.right = new CursorIterator<>(right);
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

    private final UnionState<DataPoint<P, L>, DataPoint<P, R>> getInitState() {
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

    private final Optional<UnionState<DataPoint<P, L>, DataPoint<P, R>>> getOverlappedState(
            final UnionState.Overlapped<DataPoint<P, L>, DataPoint<P, R>> overlapped) {
        final var leftPoint = overlapped.left.map(x -> x.point());
        final var rightPoint = overlapped.right.map(x -> x.point());
        final var cmp = Cursor.snd(leftPoint).compareTo(Cursor.snd(rightPoint));
        if (cmp < 0) {
            return this.left.tryNext().map(left -> UnionState.overlapped(left, overlapped.right));
        }
        if (cmp > 0) {
            return this.right.tryNext().map(right -> UnionState.overlapped(overlapped.left, right));
        }

        return this.left.tryNext()
                .flatMap(left -> this.right.tryNext().map(right -> UnionState.overlapped(left, right)));
    }

    private final Optional<UnionState<DataPoint<P, L>, DataPoint<P, R>>> getDisjointedState(
            final UnionState.Disjointed<DataPoint<P, L>, DataPoint<P, R>> disjointed) {
        final var leftPoint = disjointed.left.map(x -> x.point());
        final var rightPoint = disjointed.right.map(x -> x.point());
        if (Cursor.canOverlap(leftPoint, rightPoint)) {
            return Optional.of(UnionState.overlapped(disjointed.left, disjointed.right));
        }

        if (Cursor.snd(leftPoint).compareTo(Cursor.snd(rightPoint)) < 0) {
            return this.left.tryNext().map(left -> getUnionState(left, disjointed.right));
        }

        return this.right.tryNext().map(right -> getUnionState(disjointed.left, right));
    }

    private final UnionState<DataPoint<P, L>, DataPoint<P, R>> getState() {
        return switch (this.state) {
            case final UnionState.None<DataPoint<P, L>, DataPoint<P, R>> none -> getInitState();
            case final UnionState.LeftOnly<DataPoint<P, L>, DataPoint<P, R>> left ->
                (this.left.hasNext()) ? UnionState.leftOnly(this.left.next()) : UnionState.none();
            case final UnionState.RightOnly<DataPoint<P, L>, DataPoint<P, R>> right ->
                this.right.hasNext() ? UnionState.rightOnly(this.right.next()) : UnionState.none();
            case final UnionState.Overlapped<DataPoint<P, L>, DataPoint<P, R>> overlapped ->
                getOverlappedState(overlapped).orElseGet(() -> UnionState.none());
            case final UnionState.Disjointed<DataPoint<P, L>, DataPoint<P, R>> disjointed ->
                getDisjointedState(disjointed).orElseGet(() -> UnionState.none());
            // FIXME : remove this when https://openjdk.org/jeps/433 will be ready (> 17,
            // java 20 at least)
            default -> throw new UnsupportedOperationException();
        };
    }

    private final void pull() {
        if (!this.hasNext) {
            return;
        }

        if (this.isPulled)
            return;

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
        this.isPulled = false;
        return switch (this.state) {
            case final UnionState.None<DataPoint<P, L>, DataPoint<P, R>> none -> throw new NoSuchElementException();
            case final UnionState.LeftOnly<DataPoint<P, L>, DataPoint<P, R>> leftOnly -> getLeft(leftOnly);
            case final UnionState.RightOnly<DataPoint<P, L>, DataPoint<P, R>> rightOnly -> getRight(rightOnly);
            case final UnionState.Disjointed<DataPoint<P, L>, DataPoint<P, R>> disjointed -> getDisjointed(disjointed);
            case final UnionState.Overlapped<DataPoint<P, L>, DataPoint<P, R>> overlapped -> getOverlapped(overlapped);
            // FIXME : remove this when https://openjdk.org/jeps/433 will be ready (> 17,
            // java 20 at least)
            default -> throw new UnsupportedOperationException();
        };
    }

    private DataPoint<P, T> getOverlapped(UnionState.Overlapped<DataPoint<P, L>, DataPoint<P, R>> overlapped) {
        final var left = overlapped.left().fst();
        final var right = overlapped.right().fst();
        final var point = (left.point().compareTo(right.point()) > 0) ? left.point() : right.point();
        return Series.datapoint(point, f.apply(UnionResult.both(left.data(), right.data())));
    }

    private DataPoint<P, T> getDisjointed(UnionState.Disjointed<DataPoint<P, L>, DataPoint<P, R>> disjointed) {
        final var left = disjointed.left().fst();
        final var right = disjointed.right().fst();
        if (left.point().compareTo(right.point()) < 0) {
            return Series.datapoint(left.point(), this.f.apply(UnionResult.leftOnly(left.data())));
        }
        return Series.datapoint(right.point(), this.f.apply(UnionResult.rightOnly(right.data())));
    }

    private DataPoint<P, T> getRight(UnionState.RightOnly<DataPoint<P, L>, DataPoint<P, R>> rightOnly) {
        final var right = rightOnly.right.fst();
        return Series.datapoint(right.point(), this.f.apply(UnionResult.rightOnly(right.data())));
    }

    private DataPoint<P, T> getLeft(UnionState.LeftOnly<DataPoint<P, L>, DataPoint<P, R>> leftOnly) {
        final var left = leftOnly.left().fst();
        return Series.datapoint(left.point(), this.f.apply(UnionResult.leftOnly(left.data())));
    }
}
