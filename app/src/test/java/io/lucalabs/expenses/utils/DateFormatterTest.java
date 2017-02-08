package io.lucalabs.expenses.utils;

import org.junit.Test;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DateFormatterTest {

    @Test
    public void dateFormat_isCorrect() {
        assertEquals("date format is correct", "2000-02-15T", DateFormatter.toDateString(2000, 2, 15).substring(0, 11));
        assertNotEquals("does not support Calendar input", "2000-02-15T", DateFormatter.toDateString(2000, Calendar.FEBRUARY, 15).substring(0, 11));
    }
}
