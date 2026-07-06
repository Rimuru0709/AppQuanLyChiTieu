package com.quanlychitieu.doan.alltransaction;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;
import com.quanlychitieu.doan.edittransaction.EditTransactionActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

public class AllTransactionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalIncome, tvTotalExpense, tvTotalTransaction;
    private EditText edtSearch;
    private LinearLayout layoutTransactions;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transaction);

        setupSafeArea();
        BottomNavHelper.setup(this);

        btnBack = findViewById(R.id.btnBack);
        edtSearch = findViewById(R.id.edtSearch);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTotalTransaction = findViewById(R.id.tvTotalTransaction);
        layoutTransactions = findViewById(R.id.layoutTransactions);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        btnBack.setOnClickListener(v -> finish());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadAllTransactions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            edtSearch.clearFocus();
            return false;
        });
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    dp(24),
                    systemBars.top + dp(10),
                    dp(24),
                    dp(24)
            );

            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadSummary();
        loadAllTransactions(edtSearch.getText().toString());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hideKeyboard();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void loadSummary() {
        int totalIncome = getTotalIncome();
        int totalExpense = getTotalExpense();
        int totalCount = getTotalCount();

        tvTotalIncome.setText("Tổng thu\n" + formatMoney(totalIncome));
        tvTotalExpense.setText("Tổng chi\n" + formatMoney(totalExpense));
        tvTotalTransaction.setText("Tổng giao dịch\n" + totalCount);
    }

    private int getTotalIncome() {
        int total = 0;

        Cursor cursor = database.rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE type='INCOME'",
                null
        );

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();
        return total;
    }

    private int getTotalExpense() {
        int total = 0;

        Cursor cursor = database.rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE type='EXPENSE'",
                null
        );

        if (cursor.moveToFirst()) {
            total = Math.abs(cursor.getInt(0));
        }

        cursor.close();
        return total;
    }

    private int getTotalCount() {
        int count = 0;

        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM transactions",
                null
        );

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }

    private void loadAllTransactions(String keyword) {
        layoutTransactions.removeAllViews();

        Cursor cursor = database.rawQuery(
                "SELECT id, title, date, amount, icon, color, type FROM transactions " +
                        "WHERE title LIKE ? " +
                        "ORDER BY id DESC",
                new String[]{"%" + keyword + "%"}
        );

        if (cursor.getCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("Không có giao dịch nào");
            empty.setTextSize(16);
            empty.setTextColor(Color.GRAY);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(40), 0, dp(40));

            layoutTransactions.addView(empty);
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String date = cursor.getString(2);
            int amount = cursor.getInt(3);
            String iconName = cursor.getString(4);
            String colorCode = cursor.getString(5);
            String type = cursor.getString(6);

            addTransaction(id, title, date, amount, iconName, colorCode, type);
        }

        cursor.close();
    }

    private void addTransaction(
            int id,
            String title,
            String date,
            int money,
            String iconName,
            String colorCode,
            String type

    ) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(5), 0, dp(5));

        FrameLayout iconContainer = new FrameLayout(this);

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(dp(38), dp(38));
        containerParams.rightMargin = dp(10);
        iconContainer.setLayoutParams(containerParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);

        try {
            bg.setColor(Color.parseColor(colorCode));
        } catch (Exception e) {
            bg.setColor(Color.parseColor("#ADB5BD"));
        }

        ImageView imgIcon = new ImageView(this);

        FrameLayout.LayoutParams iconParams =
                new FrameLayout.LayoutParams(dp(18), dp(18));
        iconParams.gravity = Gravity.CENTER;
        imgIcon.setLayoutParams(iconParams);

        int iconRes = getResources().getIdentifier(
                iconName,
                "drawable",
                getPackageName()
        );

        if (iconRes == 0) {
            iconRes = R.drawable.ic_dot;
        }

        imgIcon.setImageResource(iconRes);
        imgIcon.setColorFilter(Color.WHITE);

        iconContainer.setBackground(bg);
        iconContainer.addView(imgIcon);

        TextView tvInfo = new TextView(this);
        tvInfo.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        tvInfo.setText(title + "\n" + date);
        tvInfo.setTextSize(14);
        tvInfo.setTextColor(Color.parseColor("#222222"));

        TextView tvMoney = new TextView(this);
        tvMoney.setTextSize(14);
        tvMoney.setTypeface(null, Typeface.BOLD);

        if ("INCOME".equals(type)) {
            tvMoney.setText("+" + formatMoney(Math.abs(money)));
            tvMoney.setTextColor(Color.parseColor("#00A86B"));
        } else {
            tvMoney.setText("-" + formatMoney(Math.abs(money)));
            tvMoney.setTextColor(Color.parseColor("#FF3B3B"));
        }

        row.addView(iconContainer);
        row.addView(tvInfo);
        row.addView(tvMoney);

        layoutTransactions.addView(row);

        row.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AllTransactionActivity.this,
                    EditTransactionActivity.class
            );

            intent.putExtra("transactionId", id);

            startActivity(intent);

        });
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String formatMoney(int money) {
        return String.format("%,d đ", money).replace(",", ".");
    }
}