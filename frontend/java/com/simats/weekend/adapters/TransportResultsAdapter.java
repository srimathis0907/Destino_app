package com.simats.weekend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.simats.weekend.R;
import com.simats.weekend.models.TransportResult;
import java.text.NumberFormat;
import java.util.List;

public class TransportResultsAdapter extends RecyclerView.Adapter<TransportResultsAdapter.ResultViewHolder> {

    public interface OnTransportSelectListener {
        void onTransportSelected(TransportResult transportResult);
    }

    private final List<TransportResult> results;
    private final OnTransportSelectListener listener;
    private final NumberFormat currencyFormat;

    public TransportResultsAdapter(List<TransportResult> results, OnTransportSelectListener listener, NumberFormat currencyFormat) {
        this.results = results;
        this.listener = listener;
        this.currencyFormat = currencyFormat;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transport_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        TransportResult result = results.get(position);
        holder.tvName.setText(result.getName());
        holder.tvTime.setText(result.getTime());
        holder.tvPrice.setText(currencyFormat.format(result.getPrice()));
        holder.btnSelect.setOnClickListener(v -> listener.onTransportSelected(result));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvPrice;
        Button btnSelect;
        ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_transport_name);
            tvTime = itemView.findViewById(R.id.tv_transport_time);
            tvPrice = itemView.findViewById(R.id.tv_transport_price);
            btnSelect = itemView.findViewById(R.id.btn_select_transport);
        }
    }
}