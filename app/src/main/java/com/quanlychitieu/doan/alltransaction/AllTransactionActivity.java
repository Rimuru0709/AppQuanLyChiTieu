package com.quanlychitieu.doan.alltransaction;

import android.content.Context;
import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;
import com.quanlychitieu.doan.edittransaction.EditTransactionActivity;

public class AllTransactionActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvTotalTransaction;
    private EditText edtSearch;
    private LinearLayout layoutTransactions;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_transaction);

        initViews();
        setupSafeArea();
        BottomNavHelper.setup(this);
        setupDatabase();
        setupEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtSearch = findViewById(R.id.edtSearch);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvTotalTransaction = findViewById(R.id.tvTotalTransaction);
        layoutTransactions = findViewById(R.id.layoutTransactions);
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();
    }

    private void setupEvents() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        CharSequence s,
                        int start,
                        int count,
                        int after
                ) {
                }

                @Override
                public void onTextChanged(
                        CharSequence s,
                        int start,
                        int before,
                        int count
                ) {
                    loadAllTransactions(s.toString().trim());
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
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            dp(24),
                            systemBars.top + dp(10),
                            dp(24),
                            dp(24)
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
            database = dbHelper.getWritableDatabase();
        }

        loadSummary();

        String keyword = "";

        if (edtSearch != null) {
            keyword = edtSearch.getText().toString().trim();
        }

        loadAllTransactions(keyword);
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
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            hideKeyboard();
        }

        return super.dispatchTouchEvent(event);
    }

    private void loadSummary() {
        if (database == null || !database.isOpen()) {
            return;
        }

        int totalIncome = getTotalIncome();
        int totalExpense = getTotalExpense();
        int totalCount = getTotalCount();

        /*
         * XML mới đã tách tiêu đề và giá trị.
         * Vì vậy chỉ đặt giá trị ở đây.
         */
        tvTotalIncome.setText(formatMoney(totalIncome));
        tvTotalExpense.setText(formatMoney(totalExpense));
        tvTotalTransaction.setText(String.valueOf(totalCount));
    }

    private int getTotalIncome() {
        int total = 0;
        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT SUM(ABS(amount)) " +
                            "FROM transactions " +
                            "WHERE type = 'INCOME'",
                    null
            );

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }

    private int getTotalExpense() {
        int total = 0;
        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT SUM(ABS(amount)) " +
                            "FROM transactions " +
                            "WHERE type = 'EXPENSE'",
                    null
            );

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                total = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }

    private int getTotalCount() {
        int count = 0;
        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM transactions",
                    null
            );

            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    private void loadAllTransactions(String keyword) {
        if (layoutTransactions == null ||
                database == null ||
                !database.isOpen()) {
            return;
        }

        layoutTransactions.removeAllViews();

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT id, title, date, amount, icon, color, type " +
                            "FROM transactions " +
                            "WHERE title LIKE ? " +
                            "ORDER BY id DESC",
                    new String[]{"%" + keyword + "%"}
            );

            if (cursor.getCount() == 0) {
                showEmptyMessage();
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

                addTransaction(
                        id,
                        title,
                        date,
                        amount,
                        iconName,
                        colorCode,
                        type
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showEmptyMessage() {
        TextView empty = new TextView(this);

        empty.setText("Không có giao dịch nào");
        empty.setTextSize(16);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(
                0,
                dp(40),
                0,
                dp(40)
        );

        empty.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        layoutTransactions.addView(empty);
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
        row.setPadding(
                0,
                dp(7),
                0,
                dp(7)
        );

        row.setClickable(true);
        row.setFocusable(true);

        FrameLayout iconContainer =
                new FrameLayout(this);

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        dp(38),
                        dp(38)
                );

        containerParams.setMarginEnd(dp(10));
        iconContainer.setLayoutParams(containerParams);

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

        iconContainer.setBackground(iconBackground);

        ImageView imgIcon =
                new ImageView(this);

        FrameLayout.LayoutParams iconParams =
                new FrameLayout.LayoutParams(
                        dp(18),
                        dp(18)
                );

        iconParams.gravity = Gravity.CENTER;
        imgIcon.setLayoutParams(iconParams);

        int iconResource = getResources().getIdentifier(
                iconName,
                "drawable",
                getPackageName()
        );

        if (iconResource == 0) {
            iconResource = R.drawable.ic_dot;
        }

        imgIcon.setImageResource(iconResource);
        imgIcon.setColorFilter(Color.WHITE);

        iconContainer.addView(imgIcon);

        /*
         * Khối chứa tên và ngày.
         * Tách thành hai TextView để mỗi dòng có màu riêng.
         */
        LinearLayout infoLayout =
                new LinearLayout(this);

        infoLayout.setOrientation(LinearLayout.VERTICAL);

        infoLayout.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView tvTitle =
                new TextView(this);

        tvTitle.setText(title);
        tvTitle.setTextSize(14);
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
        tvDate.setTextSize(12);

        tvDate.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        infoLayout.addView(tvTitle);
        infoLayout.addView(tvDate);

        TextView tvMoney =
                new TextView(this);

        tvMoney.setTextSize(14);
        tvMoney.setTypeface(
                null,
                Typeface.BOLD
        );

        tvMoney.setGravity(Gravity.END);

        if ("INCOME".equalsIgnoreCase(type)) {
            tvMoney.setText(
                    "+" + formatMoney(Math.abs(money))
            );

            tvMoney.setTextColor(
                    Color.parseColor("#00A86B")
            );
        } else {
            tvMoney.setText(
                    "-" + formatMoney(Math.abs(money))
            );

            tvMoney.setTextColor(
                    Color.parseColor("#FF3B3B")
            );
        }

        row.addView(iconContainer);
        row.addView(infoLayout);
        row.addView(tvMoney);

        layoutTransactions.addView(row);

        row.setOnClickListener(v -> {
            Intent intent = new Intent(
                    AllTransactionActivity.this,
                    EditTransactionActivity.class
            );

            intent.putExtra(
                    "transactionId",
                    id
            );

            startActivity(intent);
        });
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
                "%,d đ",
                money
        ).replace(",", ".");
    }
}