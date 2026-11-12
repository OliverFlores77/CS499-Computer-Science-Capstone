package com.example.cs360week3oliverflores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Fills the history grid with WeightEntry rows (date, weight, and a delete button).
 *
 * This replaces the old cursor adapter. It is created once and given a new list
 * whenever the view changes (new data, a different sort, or a filter), instead of
 * building a new adapter each time.
 */
public class WeightEntryAdapter extends BaseAdapter {

    /** Called when the user taps Delete on a row. */
    public interface OnDeleteListener {
        void onDelete(WeightEntry entry);
    }

    private final LayoutInflater inflater;
    private final List<WeightEntry> entries = new ArrayList<>();
    private final OnDeleteListener deleteListener;

    public WeightEntryAdapter(Context context, OnDeleteListener deleteListener) {
        this.inflater = LayoutInflater.from(context);
        this.deleteListener = deleteListener;
    }

    /** Replaces the rows being shown and redraws the grid. */
    public void setEntries(List<WeightEntry> newEntries) {
        entries.clear();
        entries.addAll(newEntries);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return entries.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.grid_item_weight, parent, false);
        }

        final WeightEntry entry = entries.get(position);

        TextView textDate = row.findViewById(R.id.textRowDate);
        TextView textWeight = row.findViewById(R.id.textRowWeight);
        View buttonDelete = row.findViewById(R.id.buttonDeleteRow);

        textDate.setText(entry.getDate());
        textWeight.setText(entry.getWeight() + " lbs");

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteListener.onDelete(entry);
            }
        });

        return row;
    }
}
