package com.example.cs360week3oliverflores;

import java.util.Calendar;
import java.util.Locale;

/**
 * Small, plain-Java helper for the dates stored with each weight entry.
 *
 * Dates are stored as ISO text in the form yyyy-MM-dd (for example 2026-06-05).
 * Every part is zero-padded so that ordinary string comparison puts the dates in
 * chronological order. That matters later for sorting and date-range filtering.
 * Without padding, "2026-9-5" would wrongly sort AFTER "2026-10-1".
 *
 * This class deliberately uses no Android classes, so it can be unit tested on a
 * computer without an emulator (see DateHelperTest).
 */
public final class DateHelper {

    // Utility class: nothing to instantiate.
    private DateHelper() { }

    /**
     * Builds a yyyy-MM-dd string.
     *
     * @param year  four-digit year, for example 2026
     * @param month month number from 1 (January) to 12 (December)
     * @param day   day of the month from 1 to 31
     */
    public static String format(int year, int month, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    /**
     * Builds a yyyy-MM-dd string from the values Android's DatePickerDialog and
     * Calendar give us. Both count months from 0 (January) to 11 (December),
     * so this adds 1 before formatting.
     */
    public static String fromPicker(int year, int monthZeroBased, int day) {
        return format(year, monthZeroBased + 1, day);
    }

    /** Returns today's date on this device as yyyy-MM-dd. */
    public static String today() {
        Calendar now = Calendar.getInstance();
        return fromPicker(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
    }
}
