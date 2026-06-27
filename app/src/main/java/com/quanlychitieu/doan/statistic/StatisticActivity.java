package com.quanlychitieu.doan.statistic;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;
import java.util.Calendar;

public class StatisticActivity extends AppCompatActivity {

    TextView tvMonth, tvIncome, tvExpense, tvSaving;
    ImageView imgBack;

    DonutChartView donutChart;
    BarChartView barChart;
    LinearLayout layoutLegend;

    DatabaseHelper dbHelper;

    int selectedMonth, selectedYear;

    int[] colors = {
            Color.parseColor("#FF3131"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#4285F4"),
            Color.parseColor("#16A34A"),
            Color.parseColor("#6D28D9"),
            Color.parseColor("#ADB5BD")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        setupSafeArea();
        BottomNavHelper.setup(this);

        imgBack = findViewById(R.id.imgBack);
        tvMonth = findViewById(R.id.tvMonth);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvSaving = findViewById(R.id.tvSaving);
        donutChart = findViewById(R.id.donutChart);
        barChart = findViewById(R.id.barChart);
        layoutLegend = findViewById(R.id.layoutLegend);

        imgBack.setOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);

        Calendar calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedYear = calendar.get(Calendar.YEAR);

        updateMonthText();
        loadData();

        tvMonth.setOnClickListener(v -> showMonthPicker());
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    dp(16),
                    bars.top + dp(12),
                    dp(16),
                    dp(20)
            );

            return insets;
        });
    }

    private void showMonthPicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedMonth = month + 1;
                    selectedYear = year;

                    updateMonthText();
                    loadData();
                },
                selectedYear,
                selectedMonth - 1,
                1
        );

        dialog.show();
    }

    private void updateMonthText() {
        tvMonth.setText("Tháng " + selectedMonth + "/" + selectedYear + " ▼");
    }

    private void loadData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String monthText = String.format("/%02d/%d", selectedMonth, selectedYear);

        int totalIncome = 0;
        int totalExpense = 0;

        Cursor c1 = db.rawQuery(
                "SELECT type, SUM(amount) FROM transactions " +
                        "WHERE date LIKE ? " +
                        "GROUP BY type",
                new String[]{"%" + monthText}
        );

        while (c1.moveToNext()) {
            String type = c1.getString(0);
            int amount = c1.getInt(1);

            if ("INCOME".equals(type)) {
                totalIncome = amount;
            } else if ("EXPENSE".equals(type)) {
                totalExpense = amount;
            }
        }

        c1.close();

        tvIncome.setText("Tổng thu\n" + formatMoney(totalIncome));
        tvExpense.setText("Tổng chi\n" + formatMoney(totalExpense));
        tvSaving.setText("Tiết kiệm\n" + formatMoney(totalIncome - totalExpense));

        loadCategoryChart(db, monthText, totalExpense);
        loadBarChart(db, monthText);
    }

    private void loadCategoryChart(SQLiteDatabase db, String monthText, int totalExpense) {
        layoutLegend.removeAllViews();

        if (totalExpense == 0) {
            donutChart.setData(new float[]{100}, new int[]{Color.parseColor("#D1D5DB")}, 0);
            addLegend("Chưa có dữ liệu", 100, Color.parseColor("#D1D5DB"));
            return;
        }

        Cursor c = db.rawQuery(
                "SELECT title, SUM(amount) FROM transactions " +
                        "WHERE type='EXPENSE' AND date LIKE ? " +
                        "GROUP BY title " +
                        "ORDER BY SUM(amount) DESC",
                new String[]{"%" + monthText}
        );

        float[] values = new float[10];
        int[] chartColors = new int[10];

        int index = 0;

        while (c.moveToNext() && index < 10) {
            String title = c.getString(0);
            int amount = c.getInt(1);

            float percent = amount * 100f / totalExpense;
            int color = colors[index % colors.length];

            values[index] = percent;
            chartColors[index] = color;

            addLegend(title, percent, color);

            index++;
        }

        c.close();

        float[] finalValues = new float[index];
        int[] finalColors = new int[index];

        for (int i = 0; i < index; i++) {
            finalValues[i] = values[i];
            finalColors[i] = chartColors[i];
        }

        donutChart.setData(finalValues, finalColors, totalExpense);
    }

    private void loadBarChart(SQLiteDatabase db, String monthText) {
        int[] dailyExpense = new int[31];

        Cursor c = db.rawQuery(
                "SELECT date, SUM(amount) FROM transactions " +
                        "WHERE type='EXPENSE' AND date LIKE ? " +
                        "GROUP BY date",
                new String[]{"%" + monthText}
        );

        while (c.moveToNext()) {
            String date = c.getString(0);
            int amount = c.getInt(1);

            try {
                int day = Integer.parseInt(date.substring(0, 2));
                if (day >= 1 && day <= 31) {
                    dailyExpense[day - 1] = amount;
                }
            } catch (Exception ignored) {
            }
        }

        c.close();

        barChart.setData(dailyExpense);
    }

    private void addLegend(String name, float percent, int color) {
        TextView tv = new TextView(this);
        tv.setText("●  " + name + "   " + Math.round(percent) + "%");
        tv.setTextSize(14);
        tv.setTextColor(color);
        tv.setPadding(0, 8, 0, 8);

        layoutLegend.addView(tv);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String formatMoney(int money) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(money).replace(",", ".") + " đ";
    }
}