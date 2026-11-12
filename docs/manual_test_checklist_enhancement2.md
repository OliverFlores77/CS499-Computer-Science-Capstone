# Manual Test Checklist: Enhancement Two (Algorithms and Data Structures)

Run on the Android Studio emulator. Mark each item PASS or FAIL.

## Setup

Clear the app's storage so the database starts empty, create an account, log in, and add these six entries
with the date picker, **in this order** (use Add Weight each time):

| Order | Date | Weight |
|-------|------|--------|
| 1 | 2026-11-02 (Mon) | 155.0 |
| 2 | 2026-11-04 (Wed) | 153.0 |
| 3 | 2026-11-08 (Sun) | 154.0 |
| 4 | 2026-11-09 (Mon) | 152.0 |
| 5 | 2026-11-11 (Wed) | 151.0 |
| 6 | 2026-11-11 (Wed) | 150.5 |

If your emulator's date is before Nov 11, 2026, use any six dates with the same pattern:
three in one Monday-to-Sunday week (including a Sunday) and three in the next week,
with the last two on the same day.

## Sorting

| # | Step | Expected | Result |
|---|------|----------|--------|
| 1 | Open the dashboard | Button reads "Sort: Newest". Rows: Nov 11 (150.5), Nov 11 (151.0), Nov 9, Nov 8, Nov 4, Nov 2 | PASS     |
| 2 | Tap the sort button | Reads "Sort: Oldest". Order reverses (Nov 2 first, Nov 11 151.0, then 150.5) |    PASS    |
| 3 | Tap it again | Back to newest first |  PASS      |

## Filtering

| # | Step | Expected | Result |
|---|------|----------|--------|
| 4 | Tap Filter Dates, pick Nov 4, then Nov 9 | Messages "Pick the start date" and "Now pick the end date". Grid shows 3 rows (Nov 9, 8, 4). Button reads "Clear Filter" | PASS|
| 5 | Tap the sort button while filtered | Still 3 rows, order flips |PASS |
| 6 | Tap Clear Filter | All 6 rows return. Button reads "Filter Dates" |PASS |
| 7 | Filter with start Nov 9 and end Nov 4 (reverse order) | Same 3 rows. Message says Nov 4 to Nov 9 |PASS |
| 8 | Filter Nov 5 to Nov 7 | "No entries between 2026-11-05 and 2026-11-07". Grid is empty |PASS |
| 9 | With the empty grid, tap Weekly Summary | "There are no entries to summarize." |PASS |
| 10 | Try to pick a future date in the filter | Future days cannot be selected |PASS |

## Weekly summary

| # | Step | Expected | Result |
|---|------|----------|--------|
| 11 | Clear the filter, tap Weekly Summary | Week of 2026-11-09: 3 entries, average 151.2 lbs. Week of 2026-11-02: 3 entries, average 154.0 lbs |PASS |
| 12 | Filter Nov 4 to Nov 9, tap Weekly Summary | Week of 2026-11-09: 1 entry, average 152.0 lbs. Week of 2026-11-02: 2 entries, average 153.5 lbs | PASS|

## Data changes

| # | Step | Expected | Result |
|---|------|----------|--------|
| 13 | Clear the filter. Delete the Nov 11 (150.5) row | Row disappears. Current sort stays |PASS |
| 14 | Tap Weekly Summary | Week of 2026-11-09 now shows 2 entries, average 151.5 lbs |PASS |
| 15 | Add a new entry with the date picker, then return | New row appears in the correct position for the current sort |PASS |
| 16 | Log out (long-press the goal weight), log in again | Entries are still there. Sort is back to Newest and no filter is active |PASS |

## Known limitations (planned for later or accepted)

- All users share the same weight entries until the Databases enhancement adds a user_id column.
- The sort order and filter are not saved. They reset on logout, and when the screen is rotated.
- The goal weight is not saved and resets to 150 on logout or restart.
- Typing only "." as a weight crashes the app. Input validation is part of the Databases enhancement.
- Passwords are stored as plain text until the Databases enhancement.
