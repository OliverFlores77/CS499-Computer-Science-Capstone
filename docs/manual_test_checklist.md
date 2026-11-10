# Manual Test Checklist: Enhancement One

Tested on the Android Studio emulator (Pixel 10) after installing the enhanced app.
The app's storage was cleared first so the database started empty.

## Login and session

| # | Step | Expected | Result |
|---|------|----------|--------|
| 1 | Create Account, fields empty | "Please fill out all fields" | PASS |
| 2 | Create account user1 | "Account Created Successfully!" | PASS |
| 3 | Log in, wrong password | "Invalid Credentials" | PASS |
| 4 | Log in as user1 | Dashboard opens | PASS |
| 5 | Back button on dashboard | Goes to home screen, not login | PASS |
| 6 | Reopen the app | Login screen (no auto-login) | PASS |

## Date picker

| # | Step | Expected | Result |
|---|------|----------|--------|
| 7 | Dashboard > Add Data | Button shows today's date | PASS |
| 8 | Tap the date button | Calendar opens on today | PASS |
| 9 | Try a future date | Future days not selectable | PASS |
| 10 | Pick June 5 | Button shows 2026-06-05 | PASS |
| 11 | Enter weight, Save Entry | "Weight Logged!", back to dashboard | PASS |
| 12 | Check the history grid | Row shows the picked date | PASS |
| 13 | Add entry with another date | Newest date listed first | PASS |

## Delete and logout

| # | Step | Expected | Result |
|---|------|----------|--------|
| 14 | Tap delete on a row | "Weight Entry Deleted" | PASS |
| 15 | Long-press the goal weight | "Logged out", login appears | PASS |
| 16 | Back button on login | Goes to home screen | PASS |
| 17 | Log in again | Dashboard opens, entries remain | PASS |

## Known limitations (planned for later enhancements)

- All users share the same weight entries until the Databases enhancement adds a user_id column.
- The goal weight is not saved and resets to 150 on logout or restart.
- Typing only "." as a weight crashes the app. Input validation is part of the Databases enhancement.
- Passwords are stored as plain text until the Databases enhancement.
