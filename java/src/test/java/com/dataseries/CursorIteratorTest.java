package com.dataseries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class CursorIteratorTest {
    @Test
    public void emptyTest() {
        List<Integer> empty = Collections.emptyList();
        var actual = new CursorIterator<Integer>(empty.iterator());
        assertFalse(actual.hasNext());
        assertThrows(NoSuchElementException.class, () -> actual.next());
    }

    @Test
    public void singleCursorTest() {
        List<Integer> empty = List.of(1);
        var actual = new CursorIterator<Integer>(empty.iterator());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());
        assertEquals(Cursor.single(1), actual.next());
        assertFalse(actual.hasNext());
        assertThrows(NoSuchElementException.class, () -> actual.next());
    }

    @Test
    public void simplePairCursorTest() {
        List<Integer> empty = List.of(1, 2);
        var actual = new CursorIterator<Integer>(empty.iterator());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());
        
        assertEquals(Cursor.pair(1, 2), actual.next());
        assertTrue(actual.hasNext());
        assertTrue(actual.hasNext());
        
        assertEquals(Cursor.single(2), actual.next());
        assertFalse(actual.hasNext());
        assertFalse(actual.hasNext());
        
        assertThrows(NoSuchElementException.class, () -> actual.next());
    }

    @Test
    public void cursorPairTest(){
        List<Integer> empty = List.of(1, 2, 3, 4, 5);
        var actual = new CursorIterator<Integer>(empty.iterator());
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
