package io.lucalabs.expenses.utils;

import org.junit.Test;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class NumberFormatterTest {

    @Test
    public void getLongFromString_isCorrect() {
        assertEquals("work with punctuation", 10000L, NumberFormatter.getLongFromString("100.00"));
        assertEquals("works with comma", 10000L, NumberFormatter.getLongFromString("100,00"));
        assertEquals("empty string should be zero", 0, NumberFormatter.getLongFromString(""));
    }
}
