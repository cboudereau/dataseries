package com.dataseries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CursorTest {
    @Test
    public void fstTest() {
        assertEquals((Integer) 1, Cursor.single(1).fst());
        assertEquals((Integer) 1, Cursor.pair(1, 2).fst());
    }

    @Test
    public void sndTest() {
        assertEquals(Value.fixed(2), Cursor.snd(Cursor.pair(1, 2)));
        assertEquals(Value.infinite(), Cursor.snd(Cursor.single(1)));
    }

    @Test
    public void mapTest() {
        assertEquals(Cursor.single("1"), Cursor.single(1).map(x -> x.toString()));
        assertEquals(Cursor.pair("1", "2"), Cursor.pair(1, 2).map(x -> x.toString()));
    }

    @Test
    public void canOverlapTest() {
        assertTrue(Cursor.canOverlap(Cursor.single(1), Cursor.single(1)));
        assertTrue(Cursor.canOverlap(Cursor.single(2), Cursor.single(1)));
        assertTrue(Cursor.canOverlap(Cursor.single(1), Cursor.single(2)));

        assertTrue(Cursor.canOverlap(Cursor.pair(1, 2), Cursor.pair(1, 2)));
        assertTrue(Cursor.canOverlap(Cursor.pair(1, 3), Cursor.pair(1, 2)));
        assertTrue(Cursor.canOverlap(Cursor.pair(1, 3), Cursor.pair(2, 3)));
        assertTrue(Cursor.canOverlap(Cursor.pair(2, 3), Cursor.pair(1, 3)));

        assertFalse(Cursor.canOverlap(Cursor.pair(1, 1), Cursor.pair(1, 1)));
        assertFalse(Cursor.canOverlap(Cursor.pair(1, 2), Cursor.pair(2, 3)));
    }
}
