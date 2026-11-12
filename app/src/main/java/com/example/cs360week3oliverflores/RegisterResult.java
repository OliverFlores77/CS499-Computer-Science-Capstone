package com.example.cs360week3oliverflores;

/** The outcome of trying to create an account. */
public enum RegisterResult {
    /** The account was created. */
    SUCCESS,
    /** Another account already uses that username. */
    USERNAME_TAKEN,
    /** Something else went wrong, so the account was not created. */
    ERROR
}
