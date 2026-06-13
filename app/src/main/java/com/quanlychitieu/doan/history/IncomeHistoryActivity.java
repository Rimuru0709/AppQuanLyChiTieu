package com.quanlychitieu.doan.history;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;
import com.quanlychitieu.doan.navigation.BottomNavHelper;

import java.util.Calendar;

public class IncomeHistoryActivity extends AppCompatActivity {

    LinearLayout layoutTransactions;

    LinearLayout boxTransactionCount;
    LinearLayout boxIncome;
    LinearLayout boxRefund;
    LinearLayout boxAverage;

    TextView btnMonth;
    TextView btnMonthTop;
    TextView btnWallet;
    TextView btnSort;

    EditText edtSearch;

    ImageView imgBack;

    TextView tvTotalIncome;
    TextView tvTransactionCount;
    TextView tvIncome;
    TextView tvRefund;
    TextView tvAverage;

    DatabaseHelper dbHelper;
    SQLiteDatabase database;

    int totalIncome = 0;
    int transactionCount = 0;
    int refund = 0;

    int selectedMonth = 6;
    int selectedYear = 2024;

    String selectedWallet = "Tất cả ví";
    String sortType = "Mới nhất";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_history);

        BottomNavHelper.setup(this);

        imgBack = findViewById(R.id.imgBack);

        layoutTransactions = findViewById(R.id.layoutTransactions);

        boxTransactionCount = findViewById(R.id.boxTransactionCount);
        boxIncome = findViewById(R.id.boxIncome);
        boxRefund = findViewById(R.id.boxRefund);
        boxAverage = findViewById(R.id.boxAverage);

        btnMonth = findViewById(R.id.btnMonth);
        btnMonthTop = findViewById(R.id.btnMonthTop);
        btnWallet = findViewById(R.id.btnWallet);
        btnSort = findViewById(R.id.btnSort);

        edtSearch = findViewById(R.id.edtSearch);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTransactionCount = findViewById(R.id.tvTransactionCount);
        tvIncome = findViewById(R.id.tvIncome);
        tvRefund = findViewById(R.id.tvRefund);
        tvAverage = findViewById(R.id.tvAverage);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        updateMonthText();
        setClickEvents();
        loadIncomeHistory();
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

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            return false;
        });

        boxTransactionCount.setOnClickListener(v ->
                Toast.makeText(this, "Số giao dịch: " + transactionCount, Toast.LENGTH_SHORT).show()
        );

        boxIncome.setOnClickListener(v ->
                Toast.makeText(this, "Tổng thu: " + formatMoney(totalIncome), Toast.LENGTH_SHORT).show()
        );

        boxRefund.setOnClickListener(v ->
                Toast.makeText(this, "Hoàn tiền: " + formatMoney(refund), Toast.LENGTH_SHORT).show()
        );

        boxAverage.setOnClickListener(v ->
                Toast.makeText(this, "Trung bình: " + tvAverage.getText().toString(), Toast.LENGTH_SHORT).show()
        );
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
                    loadIncomeHistory();
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
                    loadIncomeHistory();
                })
                .show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedMonth = month + 1;
                    selectedYear = year;

                    updateMonthText();
                    loadIncomeHistory();
                },
                selectedYear,
                selectedMonth - 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateMonthText() {
        String monthText = "Tháng " + selectedMonth + "/" + selectedYear;

        btnMonth.setText(monthText);
        btnMonthTop.setText(monthText + " ▼");
    }

    private void loadIncomeHistory() {
        layoutTransactions.removeAllViews();

        totalIncome = 0;
        transactionCount = 0;
        refund = 0;

        String monthText = String.format("%02d/%04d", selectedMonth, selectedYear);

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
                    "SELECT title, date, amount FROM transactions " +
                            "WHERE amount > 0 AND substr(date, 4, 7) = ? " +
                            "ORDER BY " + orderBy,
                    new String[]{monthText}
            );
        } else {
            cursor = database.rawQuery(
                    "SELECT title, date, amount FROM transactions " +
                            "WHERE amount > 0 AND substr(date, 4, 7) = ? AND wallet = ? " +
                            "ORDER BY " + orderBy,
                    new String[]{monthText, selectedWallet}
            );
        }

        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String date = cursor.getString(1);
            int amount = cursor.getInt(2);

            totalIncome += amount;
            transactionCount++;

            if (title.toLowerCase().contains("hoàn")) {
                refund += amount;
            }

            addItem(title, date, amount);
        }

        cursor.close();
        updateStatistics();
    }

    private void updateStatistics() {
        int average = 0;

        if (transactionCount > 0) {
            average = totalIncome / transactionCount;
        }

        tvTotalIncome.setText(formatMoney(totalIncome));
        tvTransactionCount.setText(String.valueOf(transactionCount));
        tvIncome.setText(formatMoney(totalIncome));
        tvRefund.setText(formatMoney(refund));
        tvAverage.setText(formatMoney(average));
    }

    private void addItem(String title, String date, int amount) {
        TextView tv = new TextView(this);

        tv.setText(title + "\n" + date + "\n+" + formatMoney(amount));
        tv.setTextSize(16);
        tv.setTextColor(getResources().getColor(android.R.color.black));
        tv.setPadding(24, 18, 24, 18);

        layoutTransactions.addView(tv);
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
        return String.format("%,d đ", Math.abs(money)).replace(",", ".");
    }
}