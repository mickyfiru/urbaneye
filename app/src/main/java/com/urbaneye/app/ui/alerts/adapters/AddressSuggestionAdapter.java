package com.urbaneye.app.ui.alerts.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.urbaneye.app.R;
import com.urbaneye.app.repositories.geocoding.AddressSuggestion;

import java.util.ArrayList;
import java.util.List;

public class AddressSuggestionAdapter extends RecyclerView.Adapter<AddressSuggestionAdapter.SuggestionViewHolder> {
    public interface OnSuggestionClickListener {
        void onSuggestionClick(AddressSuggestion suggestion);
    }

    private final List<AddressSuggestion> suggestions = new ArrayList<>();
    private final OnSuggestionClickListener listener;

    public AddressSuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        AddressSuggestion suggestion = suggestions.get(position);
        holder.name.setText(suggestion.name);
        holder.address.setText(suggestion.address);
        holder.distance.setText(suggestion.distanceLabel());
        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(suggestion));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public void submitList(List<AddressSuggestion> newSuggestions) {
        suggestions.clear();
        if (newSuggestions != null) suggestions.addAll(newSuggestions);
        notifyDataSetChanged();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView address;
        final TextView distance;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.suggestionNameText);
            address = itemView.findViewById(R.id.suggestionAddressText);
            distance = itemView.findViewById(R.id.suggestionDistanceText);
        }
    }
}
