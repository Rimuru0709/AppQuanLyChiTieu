package com.quanlychitieu.doan.history;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import com.quanlychitieu.doan.export.ReportExporter;

import java.util.Calendar;

public class ExpenseHistoryActivity extends AppCompatActivity {

    LinearLayout layoutTransactions;

    TextView btnMonth, btnMonthTop, btnWallet, btnSort, btnExport;
    EditText edtSearch;
    ImageView imgBack;
    TextView tvTotalIncome;

    DatabaseHelper dbHelper;
    SQLiteDatabase database;

    int totalExpense = 0;
    int transactionCount = 0;

    int selectedMonth;
    int selectedYear;

    String selectedWallet = "Tất cả ví";
    String sortType = "Mới nhất";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        setupSafeArea();

        BottomNavHelper.setup(this);

        imgBack = findViewById(R.id.imgBack);
        layoutTransactions = findViewById(R.id.layoutTransactions);

        btnMonth = findViewById(R.id.btnMonth);
        btnMonthTop = findViewById(R.id.btnMonthTop);
        btnWallet = findViewById(R.id.btnWallet);
        btnSort = findViewById(R.id.btnSort);
        btnExport = findViewById(R.id.btnExport);

        edtSearch = findViewById(R.id.edtSearch);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedYear = calendar.get(Calendar.YEAR);

        updateMonthText();
        setClickEvents();
        loadExpenseHistory();
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    dp(18),
                    systemBars.top + dp(10),
                    dp(18),
                    dp(18)
            );

            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenseHistory();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hideKeyboard();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setClickEvents() {
        imgBack.setOnClickListener(v -> finish());

        btnMonth.setOnClickListener(v -> showDatePicker());
        btnMonthTop.setOnClickListener(v -> showDatePicker());
        btnWallet.setOnClickListener(v -> showWalletDialog());
        btnSort.setOnClickListener(v -> showSortDialog());
        btnExport.setOnClickListener(v -> showExportDialog());

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            loadExpenseHistory();
            return false;
        });
    }

    private void showExportDialog() {
        String[] options = {"Xuất PDF", "Xuất Excel (.xlsx)"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn định dạng báo cáo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        ReportExporter.exportExpensePdf(
                                this,
                                database,
                                selectedMonth,
                                selectedYear,
                                selectedWallet,
                                totalExpense,
                                transactionCount
                        );
                    } else {
                        ReportExporter.exportExpenseExcel(
                                this,
                                database,
                                selectedMonth,
                                selectedYear,
                                selectedWallet
                        );
                    }
                })
                .show();
    }

    private void showSortDialog() {
        String[] options = {
                "Mới nhất",
                "Cũ nhất",
                "Số tiền cao nhất",
                "Số tiền thấp nhất"
        };

        new AlertDialog.Builder(this)
                .setTitle("Sắp xếp")
                .setItems(options, (dialog, which) -> {
                    sortType = options[which];
                    btnSort.setText(sortType + "  ☷");
                    loadExpenseHistory();
                })
                .show();
    }

    private void showWalletDialog() {
        String[] wallets = {
                "Tất cả ví",
                "Ví mặc định",
                "Tiết kiệm",
                "Ngân hàng",
                "Momo"
        };

        new AlertDialog.Builder(this)
                .setTitle("Chọn ví")
                .setItems(wallets, (dialog, which) -> {
                    selectedWallet = wallets[which];
                    btnWallet.setText(selectedWallet);
                    loadExpenseHistory();
                })
                .show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedMonth = month + 1;
                    selectedYear = year;

                    updateMonthText();
                    loadExpenseHistory();
                },
                selectedYear,
                selectedMonth - 1,
                1
        );

        datePickerDialog.show();
    }

    private void updateMonthText() {
        String monthText = "Tháng " + selectedMonth + "/" + selectedYear;
        btnMonth.setText(monthText);
        btnMonthTop.setText(monthText + " ▼");
    }

    private void loadExpenseHistory() {
        layoutTransactions.removeAllViews();

        totalExpense = 0;
        transactionCount = 0;

        String monthText = String.format("%02d/%04d", selectedMonth, selectedYear);
        String keyword = edtSearch.getText().toString().trim().toLowerCase();

        String orderBy = "id DESC";

        if (sortType.equals("Cũ nhất")) {
            orderBy = "id ASC";
        } else if (sortType.equals("Số tiền cao nhất")) {
            orderBy = "amount DESC";
        } else if (sortType.equals("Số tiền thấp nhất")) {
            orderBy = "amount ASC";
        }

        Cursor cursor;

        if (selectedWallet.equals("Tất cả ví")) {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, icon, color FROM transactions " +
                            "WHERE type='EXPENSE' AND substr(date, 4, 7) = ? " +
                            "ORDER BY " + orderBy,
                    new String[]{monthText}
            );
        } else {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, icon, color FROM transactions " +
                            "WHERE type='EXPENSE' AND substr(date, 4, 7) = ? AND wallet = ? " +
                            "ORDER BY " + orderBy,
                    new String[]{monthText, selectedWallet}
            );
        }

        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String date = cursor.getString(1);
            int amount = cursor.getInt(2);
            String iconName = cursor.getString(3);
            String colorCode = cursor.getString(4);

            if (!keyword.isEmpty() && !title.toLowerCase().contains(keyword)) {
                continue;
            }

            totalExpense += Math.abs(amount);
            transactionCount++;

            addItem(title, date, amount, iconName, colorCode);
        }

        cursor.close();
        updateStatistics();
    }

    private void updateStatistics() {
        tvTotalIncome.setText(formatMoney(totalExpense));
    }

    private void addItem(String title, String date, int amount, String iconName, String colorCode) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        cardParams.setMargins(0, dp(6), 0, dp(10));
        card.setLayoutParams(cardParams);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dp(16));
        cardBg.setStroke(dp(1), Color.parseColor("#E5E7EB"));
        card.setBackground(cardBg);
        card.setElevation(dp(2));

        ImageView imgIcon = new ImageView(this);

        int iconRes = getResources().getIdentifier(iconName, "drawable", getPackageName());
        if (iconRes == 0) {
            iconRes = R.drawable.ic_dot;
        }

        imgIcon.setImageResource(iconRes);
        imgIcon.setColorFilter(Color.WHITE);
        imgIcon.setPadding(dp(9), dp(9), dp(9), dp(9));

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);

        try {
            iconBg.setColor(Color.parseColor(colorCode));
        } catch (Exception e) {
            iconBg.setColor(Color.parseColor("#ADB5BD"));
        }

        imgIcon.setBackground(iconBg);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(dp(46), dp(46));
        iconParams.setMargins(0, 0, dp(12), 0);
        imgIcon.setLayoutParams(iconParams);

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#111827"));

        TextView tvDate = new TextView(this);
        tvDate.setText(date);
        tvDate.setTextSize(13);
        tvDate.setTextColor(Color.parseColor("#6B7280"));

        textBox.addView(tvTitle);
        textBox.addView(tvDate);

        TextView tvAmount = new TextView(this);
        tvAmount.setText("-" + formatMoney(Math.abs(amount)));
        tvAmount.setTextSize(15);
        tvAmount.setTypeface(null, Typeface.BOLD);
        tvAmount.setTextColor(Color.parseColor("#EF4444"));
        tvAmount.setGravity(Gravity.END);

        card.addView(imgIcon);
        card.addView(textBox);
        card.addView(tvAmount);

        layoutTransactions.addView(card);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(
                    getCurrentFocus().getWindowToken(),
                    0
            );
            getCurrentFocus().clearFocus();
        }
    }

    private String formatMoney(int money) {
        return String.format("%,d đ", money).replace(",", ".");
    }
}