# Manual Test Checklist: Enhancement Three (Databases)

Run on the Android Studio emulator. Mark each item PASS or FAIL.

## Part A. Seed the OLD database (do this BEFORE copying in the new files)

Use the Enhancement Two app that is already installed. Do not uninstall it.

1. Optional: clear the app's storage first so the data is predictable.
2. Create account `user1` with password `pass1`.
3. Create account `user2` with password `pass2`.
4. Create `user1` again with password `other`. The old version allows this.
5. Log in as `user1` and add 3 weights on different dates. Write them down.

Now switch to the `enhancement-3` branch, copy in the new files, and press Run.
**Do not uninstall the app.** Installing over it is what runs the migration.

## Part B. Migration (old data upgraded)

| # | Step | Expected | Result |
|---|------|----------|--------|
| 1 | App starts after the install | Login screen appears, no crash | PASS     |
| 2 | Database Inspector > users | 2 rows: user1 and user2. Columns are username, salt, password_hash, goal_weight. No "password" column |   PASS     |
| 3 | Look at password_hash | Long hex text (64 characters), not the real password | PASS       |
| 4 | Database Inspector > legacy_weights | The 3 old entries are there, unchanged |   PASS     |
| 5 | Database Inspector > weights | Empty |  PASS      |
| 6 | Log in as user1 / pass1 | Works. History is empty |   PASS     |
| 7 | Log in as user1 / other | "Invalid Credentials" (the duplicate was skipped) |  PASS      |
| 8 | Log in as user2 / pass2 | Works. History is empty |   PASS     |

## Part C. Accounts

| # | Step | Expected | Result |
|---|------|----------|--------|
| 9 | Create Account with both fields empty | "Please fill out all fields" |PASS |
| 10 | Create Account with only spaces in the username | "Please fill out all fields" |PASS |
| 11 | Create `user3` / `pass1` | "Account Created Successfully!" | PASS|
| 12 | Create `user3` again | "That username is already taken" | PASS|

## Part D. Each user sees only their own data

| # | Step | Expected | Result |
|---|------|----------|--------|
| 13 | As user1, add 2 weights | Both appear |PASS |
| 14 | Log out, log in as user2 | History is empty |PASS |
| 15 | As user2, add 1 weight | Only that 1 weight appears |PASS |
| 16 | Log out, log in as user1 | Still exactly 2 weights | PASS|
| 17 | As user1, try sort, filter, and Weekly Summary | They work and only use user1's entries |PASS |
| 18 | Delete one of user1's entries | It disappears. Log in as user2 and confirm user2's entry is still there |PASS |
| 19 | Database Inspector > weights | Each row has a user_id that matches its owner |PASS |

## Part E. Goal weight saved per user

| # | Step | Expected | Result |
|---|------|----------|--------|
| 20 | As user1, tap the goal weight, enter 145, Save | Label shows 145.0 lbs |PASS |
| 21 | Log out and log in as user1 | Still 145.0 lbs |PASS |
| 22 | Log in as user2 | Shows 150.0 lbs (own goal) |PASS |
| 23 | Close the app completely, reopen, log in as user1 | Still 145.0 lbs |PASS |
| 24 | Open the goal dialog, type only "." and tap Save | Message under the field. The dialog stays open. No crash |PASS |
| 25 | Try goal 5, then try an empty field | Same message each time |PASS |
| 26 | Tap Cancel in the dialog | Goal is unchanged |PASS |
| 27 | Optional: turn on the SMS switch and log a weight at or below user1's goal | Compared against user1's own goal (permission prompt or alert) |PASS |

## Part F. Input validation

| # | Step | Expected | Result |
|---|------|----------|--------|
| 28 | Add Weight: type only "." and Save | Message under the field. No crash. Nothing saved |PASS |
| 29 | Try 0, then 2000 | Message each time. Nothing saved |PASS |
| 30 | Try an empty field | Message. Nothing saved | PASS|
| 31 | Try 150.5 | "Weight Logged!" and the entry appears |PASS |

## Part G. Password storage

| # | Step | Expected | Result |
|---|------|----------|--------|
| 32 | Database Inspector > users: compare user1 and user3 (both use pass1) | Different salt and different password_hash |PASS |
| 33 | Look at every table | The text "pass1" appears nowhere |PASS |

## Part H. Fresh install

| # | Step | Expected | Result |
|---|------|----------|--------|
| 34 | Uninstall the app, then Run | Starts normally | PASS|
| 35 | Create an account, log in, add a weight, set a goal | All work. Database Inspector shows no legacy_weights table | PASS|

## Known limitations (accepted for this project)

- Weight entries saved before this version have no owner, so they are kept in legacy_weights and are not shown to anyone.
- There is no way to change or reset a password.
- Usernames are case-sensitive, so "User1" and "user1" are different accounts.
- The sort order and filter reset when the user logs out or rotates the screen.
