package com.quanlychitieu.doan.alert;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.util.ArrayList;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private Context context;
    private ArrayList<AlertModel> list;

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
        holder.imgIcon.setColorFilter(Color.WHITE);
        holder.imgIcon.setPadding(dp(9), dp(9), dp(9), dp(9));

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);

        try {
            iconBg.setColor(Color.parseColor(model.getColor()));
        } catch (Exception e) {
            iconBg.setColor(Color.parseColor("#ADB5BD"));
        }

        holder.imgIcon.setBackground(iconBg);

        holder.txtCategory.setText(model.getCategory());
        holder.txtBudget.setText(model.getBudget());
        holder.txtUsed.setText(model.getUsed());
        holder.txtPercent.setText(model.getPercent());

        holder.txtPercent.setOnClickListener(v -> showPercentDialog(model, holder));

        holder.swAlert.setOnCheckedChangeListener(null);
        holder.swAlert.setChecked(model.isEnable());

        holder.swAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            model.setEnable(isChecked);

            if (isChecked) {
                Toast.makeText(
                        context,
                        "Đã bật cảnh báo cho " + model.getCategory(),
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        context,
                        "Đã tắt cảnh báo cho " + model.getCategory(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void showPercentDialog(AlertModel model, ViewHolder holder) {
        String[] options = {"70%", "80%", "90%", "100%"};

        new AlertDialog.Builder(context)
                .setTitle("Chọn mức cảnh báo")
                .setItems(options, (dialog, which) -> {
                    int percent = Integer.parseInt(options[which].replace("%", ""));

                    model.setWarningPercent(percent);
                    model.setPercent(options[which]);
                    holder.txtPercent.setText(options[which]);

                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    dbHelper.saveWarningPercent(model.getCategory(), percent);

                    Toast.makeText(
                            context,
                            "Đã lưu mức cảnh báo " + options[which],
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private int dp(int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

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