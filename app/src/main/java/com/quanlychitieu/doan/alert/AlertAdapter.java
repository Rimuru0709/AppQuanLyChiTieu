package com.quanlychitieu.doan.alert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quanlychitieu.doan.R;

import java.util.ArrayList;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    Context context;
    ArrayList<AlertModel> list;

    public AlertAdapter(Context context, ArrayList<AlertModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_alert, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AlertModel model = list.get(position);

        holder.imgIcon.setImageResource(model.getIcon());

        holder.txtCategory.setText(model.getCategory());

        holder.txtBudget.setText(model.getBudget());

        holder.txtUsed.setText(model.getUsed());

        holder.txtPercent.setText(model.getPercent());

        holder.swAlert.setChecked(model.isEnable());

        holder.swAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {

            model.setEnable(isChecked);

            if (isChecked) {

                Toast.makeText(context,
                        "Đã bật cảnh báo cho " + model.getCategory(),
                        Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(context,
                        "Đã tắt cảnh báo cho " + model.getCategory(),
                        Toast.LENGTH_SHORT).show();

            }

        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView txtCategory, txtBudget, txtUsed, txtPercent;
        Switch swAlert;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgIcon);

            txtCategory = itemView.findViewById(R.id.txtCategory);

            txtBudget = itemView.findViewById(R.id.txtBudget);

            txtUsed = itemView.findViewById(R.id.txtUsed);

            txtPercent = itemView.findViewById(R.id.txtPercent);

            swAlert = itemView.findViewById(R.id.swAlert);
        }
    }
}