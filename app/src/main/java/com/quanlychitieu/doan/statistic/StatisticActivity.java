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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

public class StatisticActivity extends AppCompatActivity {

    private TextView tvMonth;
    private TextView tvIncome;
    private TextView tvExpense;
    private TextView tvSaving;

    private TextView tvIncomeCount;
    private TextView tvExpenseCount;
    private TextView tvTopCategory;
    private TextView tvAverageExpense;

    private ImageView imgBack;

    private DonutChartView donutChart;
    private BarChartView barChart;
    private LinearLayout layoutLegend;

    private DatabaseHelper dbHelper;

    private int selectedMonth;
    private int selectedYear;

    /*
     * Đây là màu dữ liệu biểu đồ nên có thể giữ cố định.
     * Không cần đổi theo chế độ sáng/tối.
     */
    private final int[] chartColors = {
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

        initViews();
        setupSafeArea();
        BottomNavHelper.setup(this);
        setupDatabase();
        setupCurrentMonth();
        setupEvents();

        updateMonthText();
        loadData();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);

        tvMonth = findViewById(R.id.tvMonth);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvSaving = findViewById(R.id.tvSaving);

        donutChart = findViewById(R.id.donutChart);
        barChart = findViewById(R.id.barChart);
        layoutLegend = findViewById(R.id.layoutLegend);

        tvIncomeCount = findViewById(R.id.tvIncomeCount);
        tvExpenseCount = findViewById(R.id.tvExpenseCount);
        tvTopCategory = findViewById(R.id.tvTopCategory);
        tvAverageExpense = findViewById(R.id.tvAverageExpense);
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
    }

    private void setupCurrentMonth() {
        Calendar calendar = Calendar.getInstance();

        selectedMonth =
                calendar.get(Calendar.MONTH) + 1;

        selectedYear =
                calendar.get(Calendar.YEAR);
    }

    private void setupEvents() {
        imgBack.setOnClickListener(v -> finish());

        tvMonth.setOnClickListener(v ->
                showMonthPicker()
        );
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            dp(16),
                            bars.top + dp(12),
                            dp(16),
                            dp(20)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper != null) {
            loadData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void showMonthPicker() {
        DatePickerDialog dialog =
                new DatePickerDialog(
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
        tvMonth.setText(
                "Tháng " +
                        selectedMonth +
                        "/" +
                        selectedYear +
                        " ▼"
        );
    }

    private void loadData() {
        SQLiteDatabase database =
                dbHelper.getReadableDatabase();

        String monthText =
                String.format(
                        Locale.getDefault(),
                        "/%02d/%d",
                        selectedMonth,
                        selectedYear
                );

        int totalIncome = 0;
        int totalExpense = 0;

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT type, SUM(ABS(amount)) " +
                            "FROM transactions " +
                            "WHERE date LIKE ? " +
                            "GROUP BY type",
                    new String[]{
                            "%" + monthText
                    }
            );

            while (cursor.moveToNext()) {
                String type = cursor.getString(0);
                int amount = cursor.getInt(1);

                if ("INCOME".equals(type)) {
                    totalIncome = amount;
                } else if ("EXPENSE".equals(type)) {
                    totalExpense = amount;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        tvIncome.setText(
                "Tổng thu\n" +
                        formatMoney(totalIncome)
        );

        tvExpense.setText(
                "Tổng chi\n" +
                        formatMoney(totalExpense)
        );

        tvSaving.setText(
                "Tiết kiệm\n" +
                        formatMoney(
                                totalIncome - totalExpense
                        )
        );

        loadCategoryChart(
                database,
                monthText,
                totalExpense
        );

        loadBarChart(
                database,
                monthText
        );

        loadQuickStats(
                database,
                monthText,
                totalExpense
        );
    }

    private void loadCategoryChart(
            SQLiteDatabase database,
            String monthText,
            int totalExpense
    ) {
        layoutLegend.removeAllViews();

        if (totalExpense == 0) {
            int emptyColor =
                    ContextCompat.getColor(
                            this,
                            R.color.text_secondary
                    );

            donutChart.setData(
                    new float[]{100f},
                    new int[]{emptyColor},
                    0
            );

            addLegend(
                    "Chưa có dữ liệu",
                    100f,
                    emptyColor
            );

            return;
        }

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT title, SUM(ABS(amount)) " +
                            "FROM transactions " +
                            "WHERE type = 'EXPENSE' " +
                            "AND date LIKE ? " +
                            "GROUP BY title " +
                            "ORDER BY SUM(ABS(amount)) DESC",
                    new String[]{
                            "%" + monthText
                    }
            );

            float[] values = new float[10];
            int[] colors = new int[10];

            int index = 0;

            while (cursor.moveToNext() &&
                    index < 10) {

                String title =
                        cursor.getString(0);

                int amount =
                        cursor.getInt(1);

                float percent =
                        amount * 100f / totalExpense;

                int color =
                        chartColors[
                                index % chartColors.length
                                ];

                values[index] = percent;
                colors[index] = color;

                addLegend(
                        title,
                        percent,
                        color
                );

                index++;
            }

            float[] finalValues =
                    new float[index];

            int[] finalColors =
                    new int[index];

            System.arraycopy(
                    values,
                    0,
                    finalValues,
                    0,
                    index
            );

            System.arraycopy(
                    colors,
                    0,
                    finalColors,
                    0,
                    index
            );

            donutChart.setData(
                    finalValues,
                    finalColors,
                    totalExpense
            );
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void loadBarChart(
            SQLiteDatabase database,
            String monthText
    ) {
        int[] dailyExpense =
                new int[31];

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT date, SUM(ABS(amount)) " +
                            "FROM transactions " +
                            "WHERE type = 'EXPENSE' " +
                            "AND date LIKE ? " +
                            "GROUP BY date",
                    new String[]{
                            "%" + monthText
                    }
            );

            while (cursor.moveToNext()) {
                String date =
                        cursor.getString(0);

                int amount =
                        cursor.getInt(1);

                try {
                    int day =
                            Integer.parseInt(
                                    date.substring(0, 2)
                            );

                    if (day >= 1 && day <= 31) {
                        dailyExpense[day - 1] =
                                amount;
                    }
                } catch (Exception ignored) {
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        barChart.setData(dailyExpense);
    }

    private void loadQuickStats(
            SQLiteDatabase database,
            String monthText,
            int totalExpense
    ) {
        int incomeCount =
                getTransactionCount(
                        database,
                        "INCOME",
                        monthText
                );

        int expenseCount =
                getTransactionCount(
                        database,
                        "EXPENSE",
                        monthText
                );

        String topCategory =
                getTopExpenseCategory(
                        database,
                        monthText
                );

        int average =
                totalExpense / 31;

        tvIncomeCount.setText(
                "⬇ Thu: " + incomeCount
        );

        tvExpenseCount.setText(
                "⬆ Chi: " + expenseCount
        );

        tvTopCategory.setText(
                "🔥 Danh mục chi nhiều nhất: " +
                        topCategory
        );

        tvAverageExpense.setText(
                "📊 Trung bình/ngày: " +
                        formatMoney(average)
        );
    }

    private int getTransactionCount(
            SQLiteDatabase database,
            String type,
            String monthText
    ) {
        int count = 0;
        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT COUNT(*) " +
                            "FROM transactions " +
                            "WHERE type = ? " +
                            "AND date LIKE ?",
                    new String[]{
                            type,
                            "%" + monthText
                    }
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    private String getTopExpenseCategory(
            SQLiteDatabase database,
            String monthText
    ) {
        String topCategory =
                "Chưa có";

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT title, SUM(ABS(amount)) " +
                            "FROM transactions " +
                            "WHERE type = 'EXPENSE' " +
                            "AND date LIKE ? " +
                            "GROUP BY title " +
                            "ORDER BY SUM(ABS(amount)) DESC " +
                            "LIMIT 1",
                    new String[]{
                            "%" + monthText
                    }
            );

            if (cursor.moveToFirst()) {
                topCategory =
                        cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return topCategory;
    }

    private void addLegend(
            String name,
            float percent,
            int color
    ) {
        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                0,
                dp(4),
                0,
                dp(4)
        );

        TextView bullet =
                new TextView(this);

        bullet.setText("●");
        bullet.setTextSize(15);
        bullet.setTextColor(color);

        TextView content =
                new TextView(this);

        content.setText(
                "  " +
                        name +
                        "   " +
                        Math.round(percent) +
                        "%"
        );

        content.setTextSize(14);

        /*
         * Màu chữ lấy từ colors.xml nên tự đổi
         * theo Light Mode và Dark Mode.
         */
        content.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_primary
                )
        );

        row.addView(bullet);
        row.addView(content);

        layoutLegend.addView(row);
    }

    private int dp(int value) {
        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private String formatMoney(int money) {
        DecimalFormat formatter =
                new DecimalFormat("#,###");

        return formatter
                .format(money)
                .replace(",", ".") +
                " đ";
    }
}