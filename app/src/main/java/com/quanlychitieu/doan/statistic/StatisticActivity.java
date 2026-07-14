package com.quanlychitieu.doan.statistic;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
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
     * Đây là màu dữ liệu biểu đồ.
     * Giữ cố định để các danh mục dễ phân biệt.
     */
    private final int[] chartColors = {
            Color.parseColor("#FF3131"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#4285F4"),
            Color.parseColor("#16A34A"),
            Color.parseColor("#6D28D9"),
            Color.parseColor("#ADB5BD"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#14B8A6"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#6366F1")
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

    // =========================================================
    // KHỞI TẠO VIEW
    // =========================================================

    private void initViews() {
        imgBack =
                findViewById(R.id.imgBack);

        tvMonth =
                findViewById(R.id.tvMonth);

        tvIncome =
                findViewById(R.id.tvIncome);

        tvExpense =
                findViewById(R.id.tvExpense);

        tvSaving =
                findViewById(R.id.tvSaving);

        donutChart =
                findViewById(R.id.donutChart);

        barChart =
                findViewById(R.id.barChart);

        layoutLegend =
                findViewById(R.id.layoutLegend);

        tvIncomeCount =
                findViewById(R.id.tvIncomeCount);

        tvExpenseCount =
                findViewById(R.id.tvExpenseCount);

        tvTopCategory =
                findViewById(R.id.tvTopCategory);

        tvAverageExpense =
                findViewById(R.id.tvAverageExpense);
    }

    private void setupDatabase() {
        dbHelper =
                new DatabaseHelper(this);
    }

    private void setupCurrentMonth() {
        Calendar calendar =
                Calendar.getInstance();

        selectedMonth =
                calendar.get(Calendar.MONTH) + 1;

        selectedYear =
                calendar.get(Calendar.YEAR);
    }

    private void setupEvents() {
        if (imgBack != null) {
            imgBack.setOnClickListener(
                    view -> finish()
            );
        }

        if (tvMonth != null) {
            tvMonth.setOnClickListener(
                    view -> showMonthPicker()
            );
        }
    }

    // =========================================================
    // SAFE AREA
    // =========================================================

    private void setupSafeArea() {
        View content =
                findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets bars =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
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

    // =========================================================
    // CHỌN THÁNG
    // =========================================================

    private void showMonthPicker() {
        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            selectedMonth =
                                    month + 1;

                            selectedYear =
                                    year;

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
        if (tvMonth == null) {
            return;
        }

        tvMonth.setText(
                "Tháng "
                        + selectedMonth
                        + "/"
                        + selectedYear
                        + " ▼"
        );
    }

    // =========================================================
    // TẢI TOÀN BỘ DỮ LIỆU THỐNG KÊ
    // =========================================================

    private void loadData() {
        if (dbHelper == null) {
            return;
        }

        SQLiteDatabase database =
                dbHelper.getReadableDatabase();

        String monthText =
                String.format(
                        Locale.getDefault(),
                        "%02d/%04d",
                        selectedMonth,
                        selectedYear
                );

        int totalIncome =
                getTotalAmountByType(
                        database,
                        "INCOME",
                        monthText
                );

        int totalExpense =
                getTotalAmountByType(
                        database,
                        "EXPENSE",
                        monthText
                );

        int saving =
                totalIncome - totalExpense;

        tvIncome.setText(
                "Tổng thu\n"
                        + formatMoney(totalIncome)
        );

        tvExpense.setText(
                "Tổng chi\n"
                        + formatMoney(totalExpense)
        );

        tvSaving.setText(
                "Tiết kiệm\n"
                        + formatMoney(saving)
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

    // =========================================================
    // TỔNG THU / TỔNG CHI
    // =========================================================

    private int getTotalAmountByType(
            SQLiteDatabase database,
            String type,
            String monthText
    ) {
        int total = 0;

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT SUM(ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ")) "
                            + "FROM "
                            + DatabaseHelper.TABLE_TRANSACTION
                            + " WHERE "
                            + DatabaseHelper.TRANSACTION_TYPE
                            + " = ? "
                            + "AND substr("
                            + DatabaseHelper.TRANSACTION_DATE
                            + ", 4, 7) = ?",
                    new String[]{
                            type,
                            monthText
                    }
            );

            if (cursor.moveToFirst()
                    && !cursor.isNull(0)) {

                total =
                        cursor.getInt(0);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }

    // =========================================================
    // BIỂU ĐỒ TRÒN THEO DANH MỤC
    // =========================================================

    private void loadCategoryChart(
            SQLiteDatabase database,
            String monthText,
            int totalExpense
    ) {
        if (layoutLegend == null
                || donutChart == null) {

            return;
        }

        layoutLegend.removeAllViews();

        if (totalExpense <= 0) {
            showEmptyDonutChart();
            return;
        }

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT "
                            + DatabaseHelper.TRANSACTION_CATEGORY
                            + ", "
                            + "SUM(ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ")) "
                            + "FROM "
                            + DatabaseHelper.TABLE_TRANSACTION
                            + " WHERE "
                            + DatabaseHelper.TRANSACTION_TYPE
                            + " = ? "
                            + "AND substr("
                            + DatabaseHelper.TRANSACTION_DATE
                            + ", 4, 7) = ? "
                            + "GROUP BY "
                            + DatabaseHelper.TRANSACTION_CATEGORY
                            + " "
                            + "ORDER BY SUM(ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ")) DESC",
                    new String[]{
                            "EXPENSE",
                            monthText
                    }
            );

            float[] values =
                    new float[10];

            int[] colors =
                    new int[10];

            int index = 0;

            while (cursor.moveToNext()
                    && index < 10) {

                String category =
                        cursor.getString(0);

                int amount =
                        cursor.getInt(1);

                if (category == null
                        || category.trim().isEmpty()) {

                    category = "Khác";
                }

                float percent =
                        amount * 100f
                                / totalExpense;

                int color =
                        chartColors[
                                index
                                        % chartColors.length
                                ];

                values[index] =
                        percent;

                colors[index] =
                        color;

                addLegend(
                        category,
                        percent,
                        color
                );

                index++;
            }

            if (index == 0) {
                showEmptyDonutChart();
                return;
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

    private void showEmptyDonutChart() {
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
    }

    // =========================================================
    // BIỂU ĐỒ CỘT THEO NGÀY
    // =========================================================

    private void loadBarChart(
            SQLiteDatabase database,
            String monthText
    ) {
        if (barChart == null) {
            return;
        }

        int[] dailyExpense =
                new int[31];

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT "
                            + DatabaseHelper.TRANSACTION_DATE
                            + ", "
                            + "SUM(ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ")) "
                            + "FROM "
                            + DatabaseHelper.TABLE_TRANSACTION
                            + " WHERE "
                            + DatabaseHelper.TRANSACTION_TYPE
                            + " = ? "
                            + "AND substr("
                            + DatabaseHelper.TRANSACTION_DATE
                            + ", 4, 7) = ? "
                            + "GROUP BY "
                            + DatabaseHelper.TRANSACTION_DATE
                            + " "
                            + "ORDER BY "
                            + DatabaseHelper.TRANSACTION_DATE
                            + " ASC",
                    new String[]{
                            "EXPENSE",
                            monthText
                    }
            );

            while (cursor.moveToNext()) {
                String date =
                        cursor.getString(0);

                int amount =
                        cursor.getInt(1);

                if (date == null
                        || date.length() < 2) {

                    continue;
                }

                try {
                    int day =
                            Integer.parseInt(
                                    date.substring(0, 2)
                            );

                    if (day >= 1
                            && day <= 31) {

                        dailyExpense[
                                day - 1
                                ] = amount;
                    }

                } catch (NumberFormatException ignored) {
                }
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        barChart.setData(
                dailyExpense
        );
    }

    // =========================================================
    // THỐNG KÊ NHANH
    // =========================================================

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

        int daysInMonth =
                getDaysInSelectedMonth();

        int averageExpense = 0;

        if (daysInMonth > 0) {
            averageExpense =
                    totalExpense / daysInMonth;
        }

        tvIncomeCount.setText(
                "⬇ Thu: "
                        + incomeCount
        );

        tvExpenseCount.setText(
                "⬆ Chi: "
                        + expenseCount
        );

        tvTopCategory.setText(
                "🔥 Danh mục chi nhiều nhất: "
                        + topCategory
        );

        tvAverageExpense.setText(
                "📊 Trung bình/ngày: "
                        + formatMoney(
                        averageExpense
                )
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
                    "SELECT COUNT(*) "
                            + "FROM "
                            + DatabaseHelper.TABLE_TRANSACTION
                            + " WHERE "
                            + DatabaseHelper.TRANSACTION_TYPE
                            + " = ? "
                            + "AND substr("
                            + DatabaseHelper.TRANSACTION_DATE
                            + ", 4, 7) = ?",
                    new String[]{
                            type,
                            monthText
                    }
            );

            if (cursor.moveToFirst()) {
                count =
                        cursor.getInt(0);
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
                    "SELECT "
                            + DatabaseHelper.TRANSACTION_CATEGORY
                            + ", "
                            + "SUM(ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ")) "
                            + "FROM "
                            + DatabaseHelper.TABLE_TRANSACTION
                            + " WHERE "
                            + DatabaseHelper.TRANSACTION_TYPE
                            + " = ? "
                            + "AND substr("
                            + DatabaseHelper.TRANSACTION_DATE
                            + ", 4, 7) = ? "
                            + "GROUP BY "
                            + DatabaseHelper.TRANSACTION_CATEGORY
                            + " "
                            + "ORDER BY SUM(ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ")) DESC "
                            + "LIMIT 1",
                    new String[]{
                            "EXPENSE",
                            monthText
                    }
            );

            if (cursor.moveToFirst()) {
                topCategory =
                        cursor.getString(0);

                if (topCategory == null
                        || topCategory.trim().isEmpty()) {

                    topCategory =
                            "Khác";
                }
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return topCategory;
    }

    private int getDaysInSelectedMonth() {
        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.YEAR,
                selectedYear
        );

        calendar.set(
                Calendar.MONTH,
                selectedMonth - 1
        );

        calendar.set(
                Calendar.DAY_OF_MONTH,
                1
        );

        return calendar.getActualMaximum(
                Calendar.DAY_OF_MONTH
        );
    }

    // =========================================================
    // CHÚ THÍCH BIỂU ĐỒ
    // =========================================================

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
                Gravity.CENTER_VERTICAL
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
                "  "
                        + name
                        + "   "
                        + Math.round(percent)
                        + "%"
        );

        content.setTextSize(14);

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

    // =========================================================
    // HÀM HỖ TRỢ
    // =========================================================

    private int dp(int value) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private String formatMoney(
            int money
    ) {
        DecimalFormat formatter =
                new DecimalFormat("#,###");

        return formatter
                .format(money)
                .replace(",", ".")
                + " đ";
    }
}