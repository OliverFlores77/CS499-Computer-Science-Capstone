package com.example.cs360week3oliverflores;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Small, plain-Java helper for the dates stored with each weight entry.
 *
 * Dates are stored as ISO text in the form yyyy-MM-dd (for example 2026-06-05).
 * Every part is zero-padded so that ordinary string comparison puts the dates in
 * chronological order. That matters for sorting and date-range filtering.
 * Without padding, "2026-9-5" would wrongly sort AFTER "2026-10-1".
 *
 * This class deliberately uses no Android classes, so it can be unit tested on a
 * computer without an emulator (see DateHelperTest).
 */
public final class DateHelper {

    private static final Pattern ISO_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

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

    /** True if the text looks like yyyy-MM-dd. */
    public static boolean isValidIsoFormat(String date) {
        return date != null && ISO_DATE.matcher(date).matches();
    }

    /**
     * Returns the Monday that starts the week containing the given date, as yyyy-MM-dd.
     * Weeks run Monday through Sunday. Naming a week by its Monday avoids the
     * confusion of week numbers around New Year's Day.
     *
     * @param isoDate a date in yyyy-MM-dd form
     * @throws IllegalArgumentException if the text is not in yyyy-MM-dd form
     */
    public static String weekStart(String isoDate) {
        if (!isValidIsoFormat(isoDate)) {
            throw new IllegalArgumentException("Expected yyyy-MM-dd but got: " + isoDate);
        }
        int year = Integer.parseInt(isoDate.substring(0, 4));
        int month = Integer.parseInt(isoDate.substring(5, 7));
        int day = Integer.parseInt(isoDate.substring(8, 10));

        Calendar date = Calendar.getInstance();
        date.clear();
        date.set(year, month - 1, day);

        // Calendar numbers Sunday=1 ... Saturday=7. This turns that into
        // "days since Monday": Monday=0, Tuesday=1, ... Sunday=6.
        int daysSinceMonday = (date.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        date.add(Calendar.DAY_OF_MONTH, -daysSinceMonday);

        return fromPicker(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH));
    }
}
