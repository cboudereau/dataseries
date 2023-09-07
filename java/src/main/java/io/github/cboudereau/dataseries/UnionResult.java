package io.github.cboudereau.dataseries;

/**
 * The Union result algebraic data type (sum type)
 * 
 * @param <R> the right data
 * @param <L> the left data
 */
public sealed interface UnionResult<L, R> permits UnionResult.LeftOnly, UnionResult.RightOnly, UnionResult.Both {
    /**
     * Both record
     * 
     * @param <L>   the left type
     * @param <R>   the right type
     * @param right the right value
     * @param left  the left value
     */
    public static final record Both<L, R>(L left, R right) implements UnionResult<L, R> {

    }

    /**
     * Left only record
     * 
     * @param <L>  the left type
     * @param <R>  the right type
     * @param left the left value
     */
    public static final record LeftOnly<L, R>(L left) implements UnionResult<L, R> {
    }

    /**
     * Right only record
     * 
     * @param <L>   the left type
     * @param <R>   the right type
     * @param right the right value
     */
    public static final record RightOnly<L, R>(R right) implements UnionResult<L, R> {

    }

    /**
     * A left only union result when there is no right data for the given point
     * 
     * @param <L>  the left type
     * @param <R>  the right type
     * @param left the left data
     * @return A left only union result when there is no right data for the given
     *         point
     */
    public static <L, R> UnionResult.LeftOnly<L, R> leftOnly(final L left) {
        return new UnionResult.LeftOnly<L, R>(left);
    }

    /**
     * A right only union result when there is no left data for the given point
     * 
     * @param <L>   the left type
     * @param <R>   the right type
     * @param right the right data
     * @return A right only union result when there is no left data for the given
     *         point
     */
    public static <L, R> UnionResult.RightOnly<L, R> rightOnly(final R right) {
        return new UnionResult.RightOnly<L, R>(right);
    }

    /**
     * A union of both (left and right)
     * 
     * @param <L>   the left type
     * @param <R>   the right type
     * @param left  the left data
     * @param right the right data
     * @return A union of both (left and right)
     */
    public static <L, R> UnionResult.Both<L, R> both(final L left, final R right) {
        return new UnionResult.Both<L, R>(left, right);
    }
}