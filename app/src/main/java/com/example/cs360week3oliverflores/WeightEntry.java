package com.example.cs360week3oliverflores;

/**
 * One logged weight: its database id, the date (yyyy-MM-dd), and the weight in pounds.
 *
 * A small, read-only model class. The rest of the app works with lists of these
 * objects instead of raw database cursors. It uses no Android classes, so it can
 * be unit tested on a computer.
 */
public class WeightEntry {

    private final int id;
    private final String date;
    private final double weight;

    public WeightEntry(int id, String date, double weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    public int getId() { return id; }

    /** Entry date as yyyy-MM-dd (see DateHelper). */
    public String getDate() { return date; }

    /** Weight in pounds. */
    public double getWeight() { return weight; }
}
