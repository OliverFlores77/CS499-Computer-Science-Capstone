package com.example.cs360week3oliverflores;

/**
 * Checks what the user typed before it is used or saved, so bad input produces a
 * clear message instead of a crash or bad data.
 *
 * Each method returns null when the input is fine, or a message for the user when it is not.
 *
 * This class uses no Android classes, so it can be unit tested on a computer.
 */
public final class InputValidator {

    /** Lowest and highest weights (lbs) the app accepts, both included. */
    public static final double MIN_WEIGHT = 20.0;
    public static final double MAX_WEIGHT = 1500.0;

    public static final String CREDENTIALS_ERROR = "Please fill out all fields";
    public static final String WEIGHT_ERROR =
            "Enter a weight between " + (int) MIN_WEIGHT + " and " + (int) MAX_WEIGHT + " lbs";

    // Utility class: nothing to instantiate.
    private InputValidator() { }

    /** Both the username and the password must contain something besides spaces. */
    public static String validateCredentials(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            return CREDENTIALS_ERROR;
        }
        return null;
    }

    /**
     * The text must be a real number from MIN_WEIGHT to MAX_WEIGHT.
     * Used for both logged weights and the goal weight.
     */
    public static String validateWeight(String text) {
        if (isBlank(text)) {
            return WEIGHT_ERROR;
        }
        double value;
        try {
            value = Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            // For example a lone "." or letters.
            return WEIGHT_ERROR;
        }
        // NaN and Infinity parse successfully but are not usable weights.
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return WEIGHT_ERROR;
        }
        if (value < MIN_WEIGHT || value > MAX_WEIGHT) {
            return WEIGHT_ERROR;
        }
        return null;
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
