package com.quanlychitieu.doan.statistic;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

import com.quanlychitieu.doan.R;

public class StatisticActivity extends AppCompatActivity {

    private TextView txtIncome, txtExpense, txtSaving;
    private Spinner spinnerMonth;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        // Ánh xạ View
        txtIncome = findViewById(R.id.txtIncome);
        txtExpense = findViewById(R.id.txtExpense);
        txtSaving = findViewById(R.id.txtSaving);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        pieChart = findViewById(R.id.pieChart); // 3. ĐÃ ÁNH XẠ BIẾN PIECHART (Hãy chắc chắn bên XML id cũng là pieChart)

        // Danh sách tháng
        String[] months = {
                "Tháng 1", "Tháng 2", "Tháng 3",
                "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9",
                "Tháng 10", "Tháng 11", "Tháng 12"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                months
        );

        spinnerMonth.setAdapter(adapter);

        // Mặc định chọn tháng hiện tại (ví dụ Tháng 6)
        spinnerMonth.setSelection(5);

        loadData();
        loadPieChart();
    }

    private void loadData(){
        int income = 10000000;
        int expense = 6500000;
        int saving = income - expense;

        txtIncome.setText(income + " đ");
        txtExpense.setText(expense + " đ");
        txtSaving.setText(saving + " đ");
    }

    private void loadPieChart(){
        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(10000000, "Thu"));
        entries.add(new PieEntry(6500000, "Chi"));

        PieDataSet dataSet = new PieDataSet(entries, "");

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#4CAF50")); // Màu xanh cho Thu
        colors.add(Color.parseColor("#F44336")); // Màu đỏ cho Chi

        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE); // Định dạng chữ trên biểu đồ màu trắng cho dễ nhìn
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);

        Description description = new Description();
        description.setText("");

        pieChart.setDescription(description);
        pieChart.animateY(1000);
        pieChart.invalidate(); // Refresh lại biểu đồ
    }
}