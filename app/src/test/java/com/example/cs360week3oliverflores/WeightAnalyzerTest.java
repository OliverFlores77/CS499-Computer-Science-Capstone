package com.example.cs360week3oliverflores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * Local unit tests for WeightAnalyzer, WeekSummary, and WeightEntry.
 * They run on the development computer; no emulator is needed.
 *
 * Sample data used by most tests (Monday-Sunday weeks):
 *   id 1  2026-11-02 (Mon)  155.0      week of 2026-11-02
 *   id 2  2026-11-04 (Wed)  153.0      week of 2026-11-02
 *   id 3  2026-11-08 (Sun)  154.0      week of 2026-11-02
 *   id 4  2026-11-09 (Mon)  152.0      week of 2026-11-09
 *   id 5  2026-11-11 (Wed)  151.0      week of 2026-11-09
 *   id 6  2026-11-11 (Wed)  150.5      week of 2026-11-09 (saved after id 5, same day)
 */
public class WeightAnalyzerTest {

    private List<WeightEntry> sample() {
        List<WeightEntry> list = new ArrayList<>();
        list.add(new WeightEntry(3, "2026-11-08", 154.0));
        list.add(new WeightEntry(5, "2026-11-11", 151.0));
        list.add(new WeightEntry(1, "2026-11-02", 155.0));
        list.add(new WeightEntry(6, "2026-11-11", 150.5));
        list.add(new WeightEntry(2, "2026-11-04", 153.0));
        list.add(new WeightEntry(4, "2026-11-09", 152.0));
        return list;
    }

    private String ids(List<WeightEntry> list) {
        StringBuilder text = new StringBuilder();
        for (WeightEntry entry : list) {
            text.append(entry.getId());
        }
        return text.toString();
    }

    // ----- sorting -----

    @Test
    public void sortNewestFirst_ordersByDateThenLaterEntryFirst() {
        List<WeightEntry> list = sample();
        WeightAnalyzer.sortNewestFirst(list);
        // Nov 11 (id 6 saved after id 5), Nov 9, Nov 8, Nov 4, Nov 2
        assertEquals("654321", ids(list));
    }

    @Test
    public void sortOldestFirst_ordersByDateThenEarlierEntryFirst() {
        List<WeightEntry> list = sample();
        WeightAnalyzer.sortOldestFirst(list);
        assertEquals("123456", ids(list));
    }

    @Test
    public void sort_emptyListDoesNothing() {
        List<WeightEntry> list = new ArrayList<>();
        WeightAnalyzer.sortNewestFirst(list);
        assertEquals(0, list.size());
    }

    // ----- filtering -----

    @Test
    public void filterByDateRange_includesBothEndDates() {
        List<WeightEntry> result = WeightAnalyzer.filterByDateRange(sample(), "2026-11-04", "2026-11-09");
        WeightAnalyzer.sortOldestFirst(result);
        assertEquals("234", ids(result)); // Nov 4, Nov 8, Nov 9
    }

    @Test
    public void filterByDateRange_noMatchesReturnsEmptyListNotNull() {
        List<WeightEntry> result = WeightAnalyzer.filterByDateRange(sample(), "2026-11-05", "2026-11-07");
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void filterByDateRange_swapsDatesGivenInWrongOrder() {
        List<WeightEntry> result = WeightAnalyzer.filterByDateRange(sample(), "2026-11-09", "2026-11-04");
        assertEquals(3, result.size());
    }

    @Test
    public void filterByDateRange_doesNotChangeTheOriginalList() {
        List<WeightEntry> original = sample();
        WeightAnalyzer.filterByDateRange(original, "2026-11-04", "2026-11-09");
        assertEquals(6, original.size());
    }

    // ----- weekly summaries -----

    @Test
    public void summarizeByWeek_groupsIntoMondayToSundayWeeksNewestFirst() {
        List<WeekSummary> weeks = WeightAnalyzer.summarizeByWeek(sample());
        assertEquals(2, weeks.size());

        // Newest week first: the week of Nov 9 holds ids 4, 5, 6.
        assertEquals("2026-11-09", weeks.get(0).getWeekStart());
        assertEquals(3, weeks.get(0).getEntryCount());
        assertEquals((152.0 + 151.0 + 150.5) / 3, weeks.get(0).getAverageWeight(), 0.0001);

        // The week of Nov 2 holds ids 1, 2, 3 (Sunday Nov 8 stays in this week).
        assertEquals("2026-11-02", weeks.get(1).getWeekStart());
        assertEquals(3, weeks.get(1).getEntryCount());
        assertEquals(154.0, weeks.get(1).getAverageWeight(), 0.0001);
    }

    @Test
    public void summarizeByWeek_emptyListGivesNoWeeks() {
        assertEquals(0, WeightAnalyzer.summarizeByWeek(new ArrayList<WeightEntry>()).size());
    }

    @Test
    public void summarizeByWeek_skipsEntriesWithBadDates() {
        List<WeightEntry> list = new ArrayList<>();
        list.add(new WeightEntry(1, "not-a-date", 150.0));
        list.add(new WeightEntry(2, "2026-11-09", 152.0));
        List<WeekSummary> weeks = WeightAnalyzer.summarizeByWeek(list);
        assertEquals(1, weeks.size());
        assertEquals(1, weeks.get(0).getEntryCount());
    }

    @Test
    public void weekSummary_toStringReadsClearly() {
        assertEquals("Week of 2026-11-02: 3 entries, average 154.0 lbs",
                new WeekSummary("2026-11-02", 3, 154.0).toString());
        assertEquals("Week of 2026-11-09: 1 entry, average 152.0 lbs",
                new WeekSummary("2026-11-09", 1, 152.0).toString());
    }

    @Test
    public void weightEntry_keepsItsValues() {
        WeightEntry entry = new WeightEntry(7, "2026-11-09", 152.5);
        assertEquals(7, entry.getId());
        assertEquals("2026-11-09", entry.getDate());
        assertTrue(entry.getWeight() == 152.5);
    }
}
