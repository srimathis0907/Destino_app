package com.simats.weekend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.weekend.models.FolderItem;
import java.util.ArrayList;
import java.util.List;

public class FolderSelectionAdapter extends RecyclerView.Adapter<FolderSelectionAdapter.ViewHolder> implements Filterable {

    public interface OnFolderSelectedListener {
        void onFolderSelected(String folderName);
    }

    private List<FolderItem> folderList;
    private List<FolderItem> folderListFiltered;
    private OnFolderSelectedListener listener;

    public FolderSelectionAdapter(List<FolderItem> folderList, OnFolderSelectedListener listener) {
        this.folderList = folderList;
        this.folderListFiltered = new ArrayList<>(folderList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FolderItem folder = folderListFiltered.get(position);
        holder.folderName.setText(folder.getName());
        holder.itemView.setOnClickListener(v -> listener.onFolderSelected(folder.getName()));
    }

    @Override
    public int getItemCount() {
        return folderListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    folderListFiltered = new ArrayList<>(folderList);
                } else {
                    List<FolderItem> filteredList = new ArrayList<>();
                    for (FolderItem row : folderList) {
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    folderListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = folderListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                folderListFiltered = (ArrayList<FolderItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(android.R.id.text1);
        }
    }
}