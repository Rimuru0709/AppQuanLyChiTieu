package com.quanlychitieu.doan.history;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;
import com.quanlychitieu.doan.edittransaction.EditTransactionActivity;
import com.quanlychitieu.doan.export.ReportExporter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ExpenseHistoryActivity extends AppCompatActivity {

    private LinearLayout layoutTransactions;

    private TextView btnMonth;
    private TextView btnMonthTop;
    private TextView btnWallet;
    private TextView btnSort;
    private TextView btnExport;
    private TextView tvTotalIncome;

    private EditText edtSearch;
    private ImageView imgBack;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private int totalExpense = 0;
    private int transactionCount = 0;

    private int selectedMonth;
    private int selectedYear;

    private String selectedWallet = "Tất cả ví";
    private String sortType = "Mới nhất";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_history);

        initViews();
        setupSafeArea();
        BottomNavHelper.setup(this);
        setupDatabase();
        setupCurrentMonth();
        setupClickEvents();

        updateMonthText();
        loadExpenseHistory();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);

        layoutTransactions =
                findViewById(R.id.layoutTransactions);

        btnMonth = findViewById(R.id.btnMonth);
        btnMonthTop = findViewById(R.id.btnMonthTop);
        btnWallet = findViewById(R.id.btnWallet);
        btnSort = findViewById(R.id.btnSort);
        btnExport = findViewById(R.id.btnExport);

        edtSearch = findViewById(R.id.edtSearch);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();
    }

    private void setupCurrentMonth() {
        Calendar calendar = Calendar.getInstance();

        selectedMonth =
                calendar.get(Calendar.MONTH) + 1;

        selectedYear =
                calendar.get(Calendar.YEAR);
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    view.setPadding(
                            dp(18),
                            systemBars.top + dp(10),
                            dp(18),
                            dp(18)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    private void setupClickEvents() {
        imgBack.setOnClickListener(v -> finish());

        btnMonth.setOnClickListener(v ->
                showDatePicker()
        );

        btnMonthTop.setOnClickListener(v ->
                showDatePicker()
        );

        btnWallet.setOnClickListener(v ->
                showWalletDialog()
        );

        btnSort.setOnClickListener(v ->
                showSortDialog()
        );

        btnExport.setOnClickListener(v ->
                showExportDialog()
        );

        edtSearch.setOnEditorActionListener(
                (view, actionId, event) -> {
                    hideKeyboard();
                    loadExpenseHistory();
                    return false;
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper != null) {
            database = dbHelper.getReadableDatabase();
        }

        loadExpenseHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (database != null && database.isOpen()) {
            database.close();
        }

        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() ==
                MotionEvent.ACTION_DOWN) {

            hideKeyboard();
        }

        return super.dispatchTouchEvent(event);
    }

    private void showExportDialog() {
        String[] options = {
                "Xuất PDF",
                "Xuất Excel (.xlsx)"
        };

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

                    btnSort.setText(
                            sortType + "  ☷"
                    );

                    loadExpenseHistory();
                })
                .show();
    }

    private void showWalletDialog() {
        ArrayList<String> walletList =
                new ArrayList<>();

        walletList.add("Tất cả ví");

        Cursor cursor = null;

        try {
            cursor = dbHelper.getAllWallets();

            while (cursor.moveToNext()) {
                walletList.add(
                        cursor.getString(0)
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String[] wallets =
                walletList.toArray(new String[0]);

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
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
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
        String monthText =
                "Tháng " +
                        selectedMonth +
                        "/" +
                        selectedYear;

        btnMonth.setText(monthText);
        btnMonthTop.setText(
                monthText + " ▼"
        );
    }

    private void loadExpenseHistory() {
        if (layoutTransactions == null ||
                database == null ||
                !database.isOpen()) {
            return;
        }

        layoutTransactions.removeAllViews();

        totalExpense = 0;
        transactionCount = 0;

        String monthText =
                String.format(
                        Locale.getDefault(),
                        "%02d/%04d",
                        selectedMonth,
                        selectedYear
                );

        String keyword =
                edtSearch.getText()
                        .toString()
                        .trim()
                        .toLowerCase(Locale.getDefault());

        String orderBy = "id DESC";

        if ("Cũ nhất".equals(sortType)) {
            orderBy = "id ASC";
        } else if ("Số tiền cao nhất".equals(sortType)) {
            orderBy = "ABS(amount) DESC";
        } else if ("Số tiền thấp nhất".equals(sortType)) {
            orderBy = "ABS(amount) ASC";
        }

        Cursor cursor = null;

        try {
            if ("Tất cả ví".equals(selectedWallet)) {
                cursor = database.rawQuery(
                        "SELECT id, title, date, amount, icon, color " +
                                "FROM transactions " +
                                "WHERE type = 'EXPENSE' " +
                                "AND substr(date, 4, 7) = ? " +
                                "ORDER BY " + orderBy,
                        new String[]{monthText}
                );
            } else {
                cursor = database.rawQuery(
                        "SELECT id, title, date, amount, icon, color " +
                                "FROM transactions " +
                                "WHERE type = 'EXPENSE' " +
                                "AND substr(date, 4, 7) = ? " +
                                "AND wallet = ? " +
                                "ORDER BY " + orderBy,
                        new String[]{
                                monthText,
                                selectedWallet
                        }
                );
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String date = cursor.getString(2);
                int amount = cursor.getInt(3);
                String iconName = cursor.getString(4);
                String colorCode = cursor.getString(5);

                if (!keyword.isEmpty() &&
                        !title.toLowerCase(
                                Locale.getDefault()
                        ).contains(keyword)) {
                    continue;
                }

                totalExpense += Math.abs(amount);
                transactionCount++;

                addItem(
                        id,
                        title,
                        date,
                        amount,
                        iconName,
                        colorCode
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (transactionCount == 0) {
            showEmptyMessage();
        }

        updateStatistics();
    }

    private void updateStatistics() {
        tvTotalIncome.setText(
                formatMoney(totalExpense)
        );
    }

    private void showEmptyMessage() {
        TextView emptyView =
                new TextView(this);

        emptyView.setText(
                "Không có khoản chi nào"
        );

        emptyView.setTextSize(15);
        emptyView.setGravity(Gravity.CENTER);

        emptyView.setPadding(
                0,
                dp(30),
                0,
                dp(30)
        );

        emptyView.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        layoutTransactions.addView(emptyView);
    }

    private void addItem(
            int id,
            String title,
            String date,
            int amount,
            String iconName,
            String colorCode
    ) {
        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.CENTER_VERTICAL
        );

        card.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                dp(6),
                0,
                dp(10)
        );

        card.setLayoutParams(cardParams);

        GradientDrawable cardBackground =
                new GradientDrawable();

        cardBackground.setColor(
                ContextCompat.getColor(
                        this,
                        R.color.card_background
                )
        );

        cardBackground.setCornerRadius(
                dp(16)
        );

        cardBackground.setStroke(
                dp(1),
                ContextCompat.getColor(
                        this,
                        R.color.divider_color
                )
        );

        card.setBackground(cardBackground);
        card.setElevation(dp(2));

        ImageView imgIcon =
                new ImageView(this);

        int iconResource =
                getResources().getIdentifier(
                        iconName,
                        "drawable",
                        getPackageName()
                );

        if (iconResource == 0) {
            iconResource = R.drawable.ic_dot;
        }

        imgIcon.setImageResource(iconResource);
        imgIcon.setColorFilter(Color.WHITE);

        imgIcon.setPadding(
                dp(9),
                dp(9),
                dp(9),
                dp(9)
        );

        GradientDrawable iconBackground =
                new GradientDrawable();

        iconBackground.setShape(
                GradientDrawable.OVAL
        );

        try {
            iconBackground.setColor(
                    Color.parseColor(colorCode)
            );
        } catch (Exception exception) {
            iconBackground.setColor(
                    Color.parseColor("#ADB5BD")
            );
        }

        imgIcon.setBackground(iconBackground);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        dp(46),
                        dp(46)
                );

        iconParams.setMargins(
                0,
                0,
                dp(12),
                0
        );

        imgIcon.setLayoutParams(iconParams);

        LinearLayout textBox =
                new LinearLayout(this);

        textBox.setOrientation(
                LinearLayout.VERTICAL
        );

        textBox.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView tvTitle =
                new TextView(this);

        tvTitle.setText(title);
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        tvTitle.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_primary
                )
        );

        TextView tvDate =
                new TextView(this);

        tvDate.setText(date);
        tvDate.setTextSize(13);

        tvDate.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        textBox.addView(tvTitle);
        textBox.addView(tvDate);

        TextView tvAmount =
                new TextView(this);

        tvAmount.setText(
                "-" +
                        formatMoney(
                                Math.abs(amount)
                        )
        );

        tvAmount.setTextSize(15);
        tvAmount.setTypeface(
                null,
                Typeface.BOLD
        );

        tvAmount.setGravity(Gravity.END);

        tvAmount.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.delete_color
                )
        );

        card.addView(imgIcon);
        card.addView(textBox);
        card.addView(tvAmount);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ExpenseHistoryActivity.this,
                    EditTransactionActivity.class
            );

            intent.putExtra(
                    "transactionId",
                    id
            );

            startActivity(intent);
        });

        layoutTransactions.addView(card);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        View currentView = getCurrentFocus();

        if (inputMethodManager != null &&
                currentView != null) {

            inputMethodManager.hideSoftInputFromWindow(
                    currentView.getWindowToken(),
                    0
            );

            currentView.clearFocus();
        }
    }

    private int dp(int value) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private String formatMoney(int money) {
        return String.format(
                Locale.getDefault(),
                "%,d đ",
                money
        ).replace(",", ".");
    }
}