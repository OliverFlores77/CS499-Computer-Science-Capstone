package com.example.cs360week3oliverflores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Creates and talks to the app's SQLite database.
 *
 * Screens should not use this class directly; they go through WeightRepository.
 *
 * Schema (version 2):
 *   users   (_id, username UNIQUE, salt, password_hash, goal_weight)
 *   weights (_id, user_id -> users._id, date, weight)
 *
 * Version 1 stored plain-text passwords and had no link between weights and users.
 * Upgrading from version 1 is handled in migrateToVersion2().
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    /** Returned by getUserId() when no matching user exists. */
    public static final int NO_USER = -1;

    /** Goal weight (lbs) every account starts with. */
    public static final double DEFAULT_GOAL_WEIGHT = 150.0;

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "weightTracker.db";
    private static final int DATABASE_VERSION = 2;

    // Database tables and columns
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_SALT = "salt";
    public static final String COL_PASSWORD_HASH = "password_hash";
    public static final String COL_GOAL_WEIGHT = "goal_weight";

    public static final String TABLE_WEIGHTS = "weights";
    public static final String COL_WEIGHT_ID = "_id";
    public static final String COL_WEIGHT_USER_ID = "user_id";
    public static final String COL_DATE = "date";
    public static final String COL_WEIGHT_VALUE = "weight";

    // Version 1 leftovers, used only while upgrading.
    private static final String LEGACY_COL_PASSWORD = "password";
    /** Holds the weight entries saved before accounts owned their data. Nothing reads it. */
    public static final String TABLE_LEGACY_WEIGHTS = "legacy_weights";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite ignores foreign keys unless they are switched on for each connection.
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ----- Creating the schema -----

    private static String createUsersSql(String tableName) {
        return "CREATE TABLE " + tableName + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                COL_SALT + " TEXT NOT NULL, " +
                COL_PASSWORD_HASH + " TEXT NOT NULL, " +
                COL_GOAL_WEIGHT + " REAL NOT NULL DEFAULT " + DEFAULT_GOAL_WEIGHT + ")";
    }

    private static final String CREATE_WEIGHTS_SQL =
            "CREATE TABLE " + TABLE_WEIGHTS + " (" +
                    COL_WEIGHT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_WEIGHT_USER_ID + " INTEGER NOT NULL, " +
                    COL_DATE + " TEXT NOT NULL, " +
                    COL_WEIGHT_VALUE + " REAL NOT NULL, " +
                    "FOREIGN KEY (" + COL_WEIGHT_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE)";

    // Speeds up "this user's entries, by date".
    private static final String CREATE_WEIGHTS_INDEX_SQL =
            "CREATE INDEX idx_weights_user_date ON " + TABLE_WEIGHTS +
                    " (" + COL_WEIGHT_USER_ID + ", " + COL_DATE + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createUsersSql(TABLE_USERS));
        db.execSQL(CREATE_WEIGHTS_SQL);
        db.execSQL(CREATE_WEIGHTS_INDEX_SQL);
    }

    // ----- Upgrading an existing database -----

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // SQLite runs this whole method in one transaction, so if anything fails the
        // old database is left exactly as it was.
        if (oldVersion < 2) {
            migrateToVersion2(db);
        }
    }

    /**
     * Upgrades a version 1 database without losing anything and without guessing.
     *
     *  1. Old weight entries are kept, untouched, in legacy_weights. Version 1 never
     *     recorded who owned an entry, so they are NOT assigned to any account. That
     *     way no one's data can end up in someone else's account.
     *  2. Every account is copied into the new users table with its plain-text password
     *     replaced by a salted hash. Ids are kept. If a username appears more than once
     *     (version 1 allowed that), the first account is kept and the later ones are skipped.
     *  3. A new, empty weights table is created that is linked to users.
     */
    private void migrateToVersion2(SQLiteDatabase db) {
        // 1. Set the old entries aside.
        db.execSQL("ALTER TABLE " + TABLE_WEIGHTS + " RENAME TO " + TABLE_LEGACY_WEIGHTS);

        // 2. Build the new users table beside the old one, then swap them.
        String newUsersTable = "users_v2";
        db.execSQL(createUsersSql(newUsersTable));

        Cursor oldUsers = db.rawQuery(
                "SELECT " + COL_USER_ID + ", " + COL_USERNAME + ", " + LEGACY_COL_PASSWORD +
                        " FROM " + TABLE_USERS + " ORDER BY " + COL_USER_ID + " ASC", null);
        try {
            while (oldUsers.moveToNext()) {
                String username = oldUsers.getString(1);
                String password = oldUsers.getString(2);
                if (username == null || username.isEmpty() || password == null) {
                    continue; // not a usable account
                }
                String salt = PasswordHasher.generateSalt();

                ContentValues values = new ContentValues();
                values.put(COL_USER_ID, oldUsers.getInt(0));
                values.put(COL_USERNAME, username);
                values.put(COL_SALT, salt);
                values.put(COL_PASSWORD_HASH, PasswordHasher.hash(password, salt));

                // CONFLICT_IGNORE skips a repeated username, so the first account wins.
                db.insertWithOnConflict(newUsersTable, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        } finally {
            oldUsers.close();
        }

        db.execSQL("DROP TABLE " + TABLE_USERS);
        db.execSQL("ALTER TABLE " + newUsersTable + " RENAME TO " + TABLE_USERS);

        // 3. Create the new weights table last, after users is in its final form.
        db.execSQL(CREATE_WEIGHTS_SQL);
        db.execSQL(CREATE_WEIGHTS_INDEX_SQL);
    }

    // ----- Accounts -----

    /**
     * Creates an account. The password is stored only as a salted hash.
     *
     * @return SUCCESS, USERNAME_TAKEN if the username is already used, or ERROR
     */
    public RegisterResult addUser(String username, String password) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String salt = PasswordHasher.generateSalt();

            ContentValues values = new ContentValues();
            values.put(COL_USERNAME, username);
            values.put(COL_SALT, salt);
            values.put(COL_PASSWORD_HASH, PasswordHasher.hash(password, salt));

            long result = db.insertOrThrow(TABLE_USERS, null, values);
            return (result != -1) ? RegisterResult.SUCCESS : RegisterResult.ERROR;
        } catch (SQLiteConstraintException e) {
            // The UNIQUE rule on username rejected a repeated name.
            String message = e.getMessage();
            if (message != null && message.contains("UNIQUE")) {
                return RegisterResult.USERNAME_TAKEN;
            }
            Log.e(TAG, "Could not add user", e);
            return RegisterResult.ERROR;
        } catch (SQLException e) {
            Log.e(TAG, "Could not add user", e);
            return RegisterResult.ERROR;
        }
    }

    /**
     * Checks a username and password. The password is never put into SQL: the user is
     * looked up by username (with a ? placeholder), then the typed password is hashed
     * with that user's salt and compared with the stored hash.
     *
     * @return the user's _id, or NO_USER if the credentials do not match
     */
    public int getUserId(String username, String password) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT " + COL_USER_ID + ", " + COL_SALT + ", " + COL_PASSWORD_HASH +
                    " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + " = ?";
            cursor = db.rawQuery(query, new String[]{username});
            if (cursor.moveToFirst()) {
                String salt = cursor.getString(1);
                String storedHash = cursor.getString(2);
                if (PasswordHasher.verify(password, salt, storedHash)) {
                    return cursor.getInt(0);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Could not look up user", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return NO_USER;
    }

    // ----- Goal weight (saved for each user) -----

    /** Returns the user's goal weight, or the default if the user cannot be found. */
    public double getGoalWeight(int userId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT " + COL_GOAL_WEIGHT + " FROM " + TABLE_USERS +
                            " WHERE " + COL_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Could not read goal weight", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return DEFAULT_GOAL_WEIGHT;
    }

    /** Saves the user's goal weight. Returns true if it was saved. */
    public boolean setGoalWeight(int userId, double goalWeight) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_GOAL_WEIGHT, goalWeight);
            int rows = db.update(TABLE_USERS, values, COL_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)});
            return rows > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Could not save goal weight", e);
            return false;
        }
    }

    // ----- Weight entries (each belongs to one user) -----

    /** Adds a weight entry for a user. Returns true if it was saved. */
    public boolean addWeight(int userId, String date, double weight) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_WEIGHT_USER_ID, userId);
            values.put(COL_DATE, date);
            values.put(COL_WEIGHT_VALUE, weight);
            // Returns -1 if it fails, for example if userId is not a real user.
            long result = db.insert(TABLE_WEIGHTS, null, values);
            return result != -1;
        } catch (SQLException e) {
            Log.e(TAG, "Could not add weight", e);
            return false;
        }
    }

    /**
     * Returns ONLY this user's entries, newest date first.
     * The caller must close the cursor.
     */
    public Cursor getWeightsForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_WEIGHTS +
                        " WHERE " + COL_WEIGHT_USER_ID + " = ?" +
                        " ORDER BY " + COL_DATE + " DESC",
                new String[]{String.valueOf(userId)});
    }

    /**
     * Removes an entry, but only if it belongs to this user, so one user can never
     * delete another user's data.
     */
    public void deleteWeight(int userId, int entryId) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_WEIGHTS,
                    COL_WEIGHT_ID + " = ? AND " + COL_WEIGHT_USER_ID + " = ?",
                    new String[]{String.valueOf(entryId), String.valueOf(userId)});
        } catch (SQLException e) {
            Log.e(TAG, "Could not delete weight", e);
        }
    }
}
