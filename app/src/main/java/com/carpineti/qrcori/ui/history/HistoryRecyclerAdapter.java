package com.carpineti.qrcori.ui.history;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.carpineti.qrcori.R;

import java.util.ArrayList;

public class HistoryRecyclerAdapter extends RecyclerView.Adapter<HistoryRecyclerAdapter.HistoryViewHolder> {

    private LayoutInflater layoutInflater;
    private ArrayList<String> data;

    HistoryRecyclerAdapter(Context context, ArrayList<String> data){
        this.layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.history_card_layout, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {

        // bind card components with visited monuments data
        String monument = data.get(position);
        holder.monumentName.setText(monument);

        switch (monument) {
            case "Tempio d'Ercole":
                holder.monumentImg.setImageResource(R.drawable.tempiodercole);
                break;
            case "Tempio di Castore e Polluce":
                holder.monumentImg.setImageResource(R.drawable.tempiocastorepolluce);
                break;
            case "Chiesa di S.Oliva":
                holder.monumentImg.setImageResource(R.drawable.chiesasoliva);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView monumentName;
        ImageView monumentImg;

        // itemView is a single card in History

        public HistoryViewHolder(@NonNull View itemView){
            super(itemView);
            monumentName = itemView.findViewById(R.id.card_text);
            monumentImg = itemView.findViewById(R.id.card_img);
            itemView.setOnClickListener(v -> {

                // navigation logic
                Bundle b = new Bundle();
                String monumInfo = monumentName.getText().toString();
                // we call it qrCodeText to match with MonumentFragment Java code
                b.putString("qrCodeText", monumInfo);
                Navigation.findNavController(itemView).navigate(R.id.action_nav_history_to_nav_monument,
                        b);

            });
        }
    }



}
