package com.quanlychitieu.doan.alltransaction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadSummary();
        loadAllTransactions(edtSearch.getText().toString());
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
                "SELECT SUM(amount) FROM transactions WHERE amount > 0",
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
                "SELECT SUM(amount) FROM transactions WHERE amount < 0",
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
                "SELECT title, date, amount FROM transactions " +
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
            empty.setPadding(0, 40, 0, 40);

            layoutTransactions.addView(empty);
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String date = cursor.getString(1);
            int amount = cursor.getInt(2);

            addTransaction(title, date, amount);
        }

        cursor.close();
    }

    private void addTransaction(String title, String date, int money) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 12, 0, 12);

        FrameLayout iconContainer = new FrameLayout(this);

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(80, 80);
        containerParams.rightMargin = 20;
        iconContainer.setLayoutParams(containerParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);

        ImageView imgIcon = new ImageView(this);

        FrameLayout.LayoutParams iconParams =
                new FrameLayout.LayoutParams(42, 42);
        iconParams.gravity = Gravity.CENTER;
        imgIcon.setLayoutParams(iconParams);

        if (title.contains("Ăn uống")) {
            imgIcon.setImageResource(R.drawable.ic_food);
            bg.setColor(Color.parseColor("#FF4D4D"));
        } else if (title.contains("Lương")) {
            imgIcon.setImageResource(R.drawable.ic_salary);
            bg.setColor(Color.parseColor("#2ECC71"));
        } else if (title.contains("Đi lại")) {
            imgIcon.setImageResource(R.drawable.ic_bus);
            bg.setColor(Color.parseColor("#3498DB"));
        } else if (title.contains("Mua sắm")) {
            imgIcon.setImageResource(R.drawable.ic_shopping);
            bg.setColor(Color.parseColor("#F1C40F"));
        } else if (title.contains("Giải trí")) {
            imgIcon.setImageResource(R.drawable.ic_default);
            bg.setColor(Color.parseColor("#9C27B0"));
        } else if (title.contains("Hóa đơn")) {
            imgIcon.setImageResource(R.drawable.ic_bill);
            bg.setColor(Color.parseColor("#FF9800"));
        } else if (title.contains("Sức khỏe")) {
            imgIcon.setImageResource(R.drawable.ic_heart);
            bg.setColor(Color.parseColor("#FFB3C6"));
        } else if (title.contains("Thưởng")) {
            imgIcon.setImageResource(R.drawable.ic_reward);
            bg.setColor(Color.parseColor("#FB8500"));
        } else if (title.contains("Làm thêm")) {
            imgIcon.setImageResource(R.drawable.ic_work);
            bg.setColor(Color.parseColor("#A2D2FF"));
        } else if (title.contains("Đầu tư")) {
            imgIcon.setImageResource(R.drawable.ic_invest);
            bg.setColor(Color.parseColor("#2A9D8F"));
        } else if (title.contains("Bán hàng")) {
            imgIcon.setImageResource(R.drawable.ic_sell);
            bg.setColor(Color.parseColor("#9D4EDD"));
        } else if (title.contains("Được tặng")) {
            imgIcon.setImageResource(R.drawable.ic_donate);
            bg.setColor(Color.parseColor("#9D6B53"));
        } else {
            imgIcon.setImageResource(R.drawable.ic_dot);
            bg.setColor(Color.parseColor("#ADB5BD"));
        }

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

        if (money > 0) {
            tvMoney.setText("+" + formatMoney(money));
            tvMoney.setTextColor(Color.parseColor("#00A86B"));
        } else {
            tvMoney.setText(formatMoney(money));
            tvMoney.setTextColor(Color.parseColor("#FF3B3B"));
        }

        row.addView(iconContainer);
        row.addView(tvInfo);
        row.addView(tvMoney);

        layoutTransactions.addView(row);
    }

    private String formatMoney(int money) {
        return String.format("%,d đ", money).replace(",", ".");
    }
}