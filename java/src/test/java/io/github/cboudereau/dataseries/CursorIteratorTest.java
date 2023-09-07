package io.github.cboudereau.dataseries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Union.CursorIterator;
import io.github.cboudereau.dataseries.Union.Cursor;

public class CursorIteratorTest {
    @Test
    public void emptyTest() {
        final List<Integer> empty = Collections.emptyList();
        final var actual = new CursorIterator<Integer>(empty.iterator());
        assertFalse(actual.hasNext());
        assertThrows(NoSuchElementException.class, () -> actual.next());
    }

    @Test
    public void singleCursorTest() {
        final List<Integer> empty = List.of(1);
        final var actual = new CursorIterator<Integer>(empty.iterator());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());
        assertEquals(Union.Cursor.single(1), actual.next());
        assertFalse(actual.hasNext());
        assertThrows(NoSuchElementException.class, () -> actual.next());
    }

    @Test
    public void simplePairCursorTest() {
        final List<Integer> empty = List.of(1, 2);
        final var actual = new CursorIterator<Integer>(empty.iterator());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());

        assertEquals(Union.Cursor.pair(1, 2), actual.next());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());

        assertEquals(Union.Cursor.single(2), actual.next());
        assertFalse(actual.hasNext());
        assertFalse(actual.hasNext());

        assertThrows(NoSuchElementException.class, () -> actual.next());
    }

    @Test
    public void cursorPairTest() {
        final List<Integer> empty = List.of(1, 2, 3, 4, 5);
        final var actual = new CursorIterator<Integer>(empty.iterator());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());

        assertEquals(Cursor.pair(1, 2), actual.next());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());

        assertEquals(Cursor.pair(2, 3), actual.next());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());

        assertEquals(Cursor.pair(3, 4), actual.next());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());

        assertEquals(Cursor.pair(4, 5), actual.next());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());

        assertEquals(Cursor.single(5), actual.next());
        assertFalse(actual.hasNext());
        assertFalse(actual.hasNext());

        assertThrows(NoSuchElementException.class, () -> actual.next());

    }
}
