package com.simats.weekend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MonthFilterFragment extends Fragment {

    interface MonthListener { void onMonthSelected(String month); }
    private MonthListener listener;

    public void setListener(MonthListener listener) { this.listener = listener; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle s) {
        ListView listView = new ListView(getContext());
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, months);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) {
                listener.onMonthSelected(months[position].substring(0, 3));
            }
        });
        return listView;
    }
}