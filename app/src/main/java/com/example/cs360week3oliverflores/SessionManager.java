package com.example.cs360week3oliverflores;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Remembers which user is logged in so every screen knows whose data it is
 * working with.
 *
 * Only the user's database id is stored (never the password). The id is kept in
 * SharedPreferences, which is private to this app.
 *
 * Typical flow:
 *   LoginActivity    -> saveUserId(id) after a successful login
 *   Other Activities -> isLoggedIn() / getUserId() to find the current user
 *   Logout           -> clear()
 */
public class SessionManager {

    /** Returned by getUserId() when nobody is logged in. */
    public static final int NO_USER = -1;

    private static final String PREFS_NAME = "weight_tracker_session";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        // Application context avoids holding on to an Activity.
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Saves the id of the user who just logged in. */
    public void saveUserId(int userId) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }

    /** Returns the logged-in user's id, or NO_USER if nobody is logged in. */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, NO_USER);
    }

    /** True when a user is currently logged in. */
    public boolean isLoggedIn() {
        return getUserId() != NO_USER;
    }

    /** Ends the session (logout). */
    public void clear() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }
}
