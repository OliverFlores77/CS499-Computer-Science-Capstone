package com.example.cs360week3oliverflores;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

/**
 * The single point of contact between the screens (Activities) and the database.
 *
 * Activities call this class and never touch DatabaseHelper directly. That keeps
 * screen code focused on the user interface, and it means database changes only
 * need to be made in one place.
 *
 * Everything a user owns (weight entries, goal weight) is looked up by userId, so one
 * user can never see or change another user's data.
 */
public class WeightRepository {

    /** Returned by authenticateUser() when the username/password do not match. */
    public static final int NO_USER = DatabaseHelper.NO_USER;

    /** Goal weight (lbs) every account starts with. */
    public static final double DEFAULT_GOAL_WEIGHT = DatabaseHelper.DEFAULT_GOAL_WEIGHT;

    private final DatabaseHelper dbHelper;

    public WeightRepository(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
    }

    // ----- Accounts -----

    /**
     * Checks a username and password.
     *
     * @return the user's id if the credentials match, otherwise NO_USER
     */
    public int authenticateUser(String username, String password) {
        return dbHelper.getUserId(username, password);
    }

    /** Creates a new account and says whether it worked, or why not. */
    public RegisterResult registerUser(String username, String password) {
        return dbHelper.addUser(username, password);
    }

    // ----- Goal weight -----

    /** Returns the user's saved goal weight (lbs). */
    public double getGoalWeight(int userId) {
        return dbHelper.getGoalWeight(userId);
    }

    /** Saves the user's goal weight. Returns true if it was saved. */
    public boolean setGoalWeight(int userId, double goalWeight) {
        return dbHelper.setGoalWeight(userId, goalWeight);
    }

    // ----- Weight entries -----

    /**
     * Saves a weight entry for a user.
     *
     * @param date   entry date as yyyy-MM-dd (see DateHelper)
     * @param weight weight in pounds
     * @return true if the entry was saved
     */
    public boolean addWeight(int userId, String date, double weight) {
        return dbHelper.addWeight(userId, date, weight);
    }

    /**
     * Loads this user's weight history as a list of WeightEntry objects.
     * The database cursor is read and closed here, so callers never have to
     * manage it. The order is not guaranteed; callers sort with WeightAnalyzer.
     */
    public List<WeightEntry> getWeightEntries(int userId) {
        List<WeightEntry> entries = new ArrayList<>();
        Cursor cursor = dbHelper.getWeightsForUser(userId);
        try {
            int idColumn = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WEIGHT_ID);
            int dateColumn = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE);
            int weightColumn = cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WEIGHT_VALUE);
            while (cursor.moveToNext()) {
                entries.add(new WeightEntry(
                        cursor.getInt(idColumn),
                        cursor.getString(dateColumn),
                        cursor.getDouble(weightColumn)));
            }
        } finally {
            cursor.close();
        }
        return entries;
    }

    /** Deletes one of this user's weight entries. Entries of other users are never touched. */
    public void deleteWeight(int userId, int entryId) {
        dbHelper.deleteWeight(userId, entryId);
    }
}
