package com.example.cs360week3oliverflores;

import android.content.Context;
import android.database.Cursor;

/**
 * The single point of contact between the screens (Activities) and the database.
 *
 * Activities call this class and never touch DatabaseHelper directly. That keeps
 * screen code focused on the user interface, and it means database changes only
 * need to be made in one place.
 *
 * NOTE FOR LATER ENHANCEMENTS: the userId parameters below are already part of
 * the method signatures so the Activities will not need to change again. Today the
 * weights table has no user_id column, so userId is not yet used for weight
 * queries. The Databases enhancement will add that column and pass userId through.
 */
public class WeightRepository {

    /** Returned by authenticateUser() when the username/password do not match. */
    public static final int NO_USER = DatabaseHelper.NO_USER;

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

    /** Creates a new account. Returns true if it was saved. */
    public boolean registerUser(String username, String password) {
        return dbHelper.addUser(username, password);
    }

    // ----- Weight entries -----

    /**
     * Saves a weight entry.
     *
     * @param userId the logged-in user (not yet stored; see note at top of class)
     * @param date   entry date as yyyy-MM-dd (see DateHelper)
     * @param weight weight in pounds
     * @return true if the entry was saved
     */
    public boolean addWeight(int userId, String date, double weight) {
        return dbHelper.addWeight(date, weight);
    }

    /**
     * Returns the weight history for the history grid, newest date first.
     * The caller is responsible for the cursor's lifecycle.
     *
     * @param userId the logged-in user (not yet used; see note at top of class)
     */
    public Cursor getWeights(int userId) {
        return dbHelper.getAllWeights();
    }

    /** Deletes one weight entry by its id. */
    public void deleteWeight(int entryId) {
        dbHelper.deleteWeight(entryId);
    }
}
