package com.example.cs360week3oliverflores;

import java.util.Locale;

/**
 * Summary of the weight entries that fall in one week (Monday through Sunday):
 * which week, how many entries, and their average weight.
 * Plain Java with no Android classes, so it can be unit tested on a computer.
 */
public class WeekSummary {

    private final String weekStart;
    private final int entryCount;
    private final double averageWeight;

    public WeekSummary(String weekStart, int entryCount, double averageWeight) {
        this.weekStart = weekStart;
        this.entryCount = entryCount;
        this.averageWeight = averageWeight;
    }

    /** The Monday that starts this week, as yyyy-MM-dd. */
    public String getWeekStart() { return weekStart; }

    public int getEntryCount() { return entryCount; }

    public double getAverageWeight() { return averageWeight; }

    /** Text shown to the user, for example "Week of 2026-11-09: 3 entries, average 151.2 lbs". */
    @Override
    public String toString() {
        String noun = (entryCount == 1) ? "entry" : "entries";
        return String.format(Locale.US, "Week of %s: %d %s, average %.1f lbs",
                weekStart, entryCount, noun, averageWeight);
    }
}
