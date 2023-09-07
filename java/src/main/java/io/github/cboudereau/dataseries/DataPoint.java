package io.github.cboudereau.dataseries;

/**
 * Data point record to store data at any given point.
 * 
 * @param <P>   the point type
 * @param <T>   the data type
 * @param point the point value
 * @param data  the data value
 */
public record DataPoint<P, T>(P point, T data) {

}
