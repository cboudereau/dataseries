package com.dataseries;

public sealed interface UnionResult<L, R>
        permits UnionResult.LeftOnly, UnionResult.RightOnly, UnionResult.Both {
    public static record Both<L, R>(L left, R right) implements UnionResult<L, R> {

    }

    public static record LeftOnly<L, R>(L left) implements UnionResult<L, R> {
    }

    public static record RightOnly<L, R>(R right) implements UnionResult<L, R> {

    }

    public static <L, R> UnionResult.LeftOnly<L, R> leftOnly(L left) {
        return new UnionResult.LeftOnly<L, R>(left);
    }

    public static <L, R> UnionResult.RightOnly<L, R> rightOnly(R right) {
        return new UnionResult.RightOnly<L, R>(right);
    }

    public static <L, R> UnionResult.Both<L, R> both(L left, R right) {
        return new UnionResult.Both<L, R>(left, right);
    }
}