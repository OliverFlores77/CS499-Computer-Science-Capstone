package com.example.cs360week3oliverflores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Local unit tests for DateHelper. These run on the development computer
 * (no emulator needed) because DateHelper uses no Android classes.
 */
public class DateHelperTest {

    @Test
    public void format_padsSingleDigitMonthAndDay() {
        assertEquals("2026-06-05", DateHelper.format(2026, 6, 5));
    }

    @Test
    public void format_keepsTwoDigitMonthAndDay() {
        assertEquals("2026-12-31", DateHelper.format(2026, 12, 31));
    }

    @Test
    public void fromPicker_convertsZeroBasedMonthToCalendarMonth() {
        // Android's picker gives January as 0 and December as 11.
        assertEquals("2026-01-31", DateHelper.fromPicker(2026, 0, 31));
        assertEquals("2026-12-09", DateHelper.fromPicker(2026, 11, 9));
    }

    @Test
    public void formattedDates_sortChronologicallyAsText() {
        // Zero padding is what makes plain text comparison match calendar order.
        assertTrue("2026-06-05".compareTo("2026-06-13") < 0);
        assertTrue("2026-09-05".compareTo("2026-10-01") < 0);
        assertTrue("2025-12-31".compareTo("2026-01-01") < 0);
    }

    @Test
    public void today_hasIsoFormat() {
        assertTrue(DateHelper.today().matches("\\d{4}-\\d{2}-\\d{2}"));
    }
}
