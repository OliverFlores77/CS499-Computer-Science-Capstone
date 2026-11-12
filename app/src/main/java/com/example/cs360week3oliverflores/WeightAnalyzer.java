package com.example.cs360week3oliverflores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The algorithms behind the history screen: sorting, filtering, and weekly summaries.
 *
 * Everything here works on a List of WeightEntry objects, which is why the history
 * is loaded into memory instead of being handed straight from the database to the grid.
 * That lets the user change the view without re-querying the database, and it gives
 * each step a clear, analyzable cost. (n = number of entries)
 *
 *   sorting   with a Comparator     O(n log n) time
 *   filtering with one loop         O(n) time
 *   grouping  with a HashMap        O(n) time on average, O(n) extra space
 *
 * Plain Java with no Android classes, so it can be unit tested on a computer.
 */
public final class WeightAnalyzer {

    // Utility class: nothing to instantiate.
    private WeightAnalyzer() { }

    // yyyy-MM-dd dates compare correctly as text. If two entries share a date,
    // the one saved later (higher id) counts as newer.
    private static final Comparator<WeightEntry> NEWEST_FIRST = new Comparator<WeightEntry>() {
        @Override
        public int compare(WeightEntry a, WeightEntry b) {
            int byDate = b.getDate().compareTo(a.getDate());
            if (byDate != 0) {
                return byDate;
            }
            return Integer.compare(b.getId(), a.getId());
        }
    };

    private static final Comparator<WeightEntry> OLDEST_FIRST = new Comparator<WeightEntry>() {
        @Override
        public int compare(WeightEntry a, WeightEntry b) {
            int byDate = a.getDate().compareTo(b.getDate());
            if (byDate != 0) {
                return byDate;
            }
            return Integer.compare(a.getId(), b.getId());
        }
    };

    private static final Comparator<WeekSummary> NEWEST_WEEK_FIRST = new Comparator<WeekSummary>() {
        @Override
        public int compare(WeekSummary a, WeekSummary b) {
            return b.getWeekStart().compareTo(a.getWeekStart());
        }
    };

    /** Sorts the list in place, newest date first. O(n log n). */
    public static void sortNewestFirst(List<WeightEntry> entries) {
        Collections.sort(entries, NEWEST_FIRST);
    }

    /** Sorts the list in place, oldest date first. O(n log n). */
    public static void sortOldestFirst(List<WeightEntry> entries) {
        Collections.sort(entries, OLDEST_FIRST);
    }

    /**
     * Returns a NEW list holding only the entries dated from startDate through
     * endDate, inclusive. The original list is not changed. O(n): one pass.
     *
     * If the two dates are given in the wrong order they are swapped, so a range
     * is never "empty" just because the user picked the end date first.
     * If nothing falls in the range, the result is an empty list (never null).
     */
    public static List<WeightEntry> filterByDateRange(List<WeightEntry> entries,
                                                      String startDate, String endDate) {
        if (startDate.compareTo(endDate) > 0) {
            String swap = startDate;
            startDate = endDate;
            endDate = swap;
        }

        List<WeightEntry> result = new ArrayList<>();
        for (WeightEntry entry : entries) {
            String date = entry.getDate();
            if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0) {
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Groups entries by week (Monday through Sunday) and returns one summary per
     * week, newest week first.
     *
     * A HashMap keyed by the week's starting Monday collects each week's entries in a
     * single pass, so grouping is O(n) on average. Sorting the (much smaller) list of
     * weeks afterward costs O(w log w), where w is the number of weeks.
     * Entries whose date is not in yyyy-MM-dd form are skipped.
     */
    public static List<WeekSummary> summarizeByWeek(List<WeightEntry> entries) {
        Map<String, List<WeightEntry>> weeks = new HashMap<>();

        for (WeightEntry entry : entries) {
            if (!DateHelper.isValidIsoFormat(entry.getDate())) {
                continue;
            }
            String key = DateHelper.weekStart(entry.getDate());
            List<WeightEntry> group = weeks.get(key);
            if (group == null) {
                group = new ArrayList<>();
                weeks.put(key, group);
            }
            group.add(entry);
        }

        List<WeekSummary> summaries = new ArrayList<>();
        for (Map.Entry<String, List<WeightEntry>> week : weeks.entrySet()) {
            double total = 0;
            for (WeightEntry entry : week.getValue()) {
                total += entry.getWeight();
            }
            int count = week.getValue().size();
            summaries.add(new WeekSummary(week.getKey(), count, total / count));
        }

        Collections.sort(summaries, NEWEST_WEEK_FIRST);
        return summaries;
    }
}
