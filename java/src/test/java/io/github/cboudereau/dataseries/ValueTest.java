package io.github.cboudereau.dataseries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Union.Value;

public class ValueTest {

    @Test
    public void compareToTest() {
        assertEquals(0, Value.infinite().compareTo(Value.infinite()));
        assertEquals(0, Value.fixed(1).compareTo(Value.fixed(1)));
        assertTrue(Value.fixed(1).isLessThan(Value.infinite()));
        assertTrue(Value.fixed(2).isGreaterThan(Value.fixed(1)));
        assertTrue(Value.fixed(1).isLessThan(Value.fixed(2)));
        assertFalse(Value.fixed(2).isLessThan(Value.fixed(1)));
    }
}
