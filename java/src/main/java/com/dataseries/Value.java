package com.dataseries;

public sealed interface Value<T extends Comparable<T>> extends Comparable<Value<T>> permits Value.Fixed, Value.Infinite {

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
            case Infinite<T> i -> {
                switch (o) {
                    case Infinite<T> i2 -> {
                        return 0;
                    }
                    case Fixed<T> v -> {
                        return 1;
                    }
                }
            }
            case Fixed<T> v -> {
                switch (o) {
                    case Infinite<T> i -> {
                        return -1;
                    }
                    case Fixed<T> v2 -> {
                        return v.value.compareTo(v2.value);
                    }
                }
            }
        }
    }

    default boolean isGreaterThan(final Value<T> o) {
        return this.compareTo(o) > 0;
    }

    default boolean isLessThan(final Value<T> o) {
        return this.compareTo(o) < 0;
    }
}
