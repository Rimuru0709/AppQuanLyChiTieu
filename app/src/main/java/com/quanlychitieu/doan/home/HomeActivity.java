package com.quanlychitieu.doan.home;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;

import com.quanlychitieu.doan.addtransaction.AddTransactionActivity;
import com.quanlychitieu.doan.category.CategoryActivity;
import com.quanlychitieu.doan.setting.SettingActivity;
import com.quanlychitieu.doan.statistic.StatisticActivity;
import com.quanlychitieu.doan.choosetransaction.ChooseTransactionActivity;

import android.content.SharedPreferences;

public class HomeActivity extends AppCompatActivity {

    private TextView tvHello, tvBalance, tvIncome, tvExpense;
    private TextView navHome, navStatistic, navAdd, navCategory, navSetting;
    private ImageView imgEye;
    private LinearLayout layoutTransactions;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private boolean isBalanceVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        tvHello = findViewById(R.id.tvHello);
        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        imgEye = findViewById(R.id.imgEye);
        layoutTransactions = findViewById(R.id.layoutTransactions);

        navHome = findViewById(R.id.navHome);
        navStatistic = findViewById(R.id.navStatistic);
        navAdd = findViewById(R.id.navAdd);
        navCategory = findViewById(R.id.navCategory);
        navSetting = findViewById(R.id.navSetting);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        loadHomeData();
        hideMoney();
        setupEyeButton();
        setupBottomNavigation();
    }

    private void setupEyeButton() {
        imgEye.setOnClickListener(v -> {
            if (isBalanceVisible) {
                hideMoney();
            } else {
                showMoney();
            }
        });
    }

    private void setupBottomNavigation() {
        navHome.setTextColor(Color.parseColor("#0057FF"));

        navStatistic.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, StatisticActivity.class));
        });

        navAdd.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AddTransactionActivity.class));
        });

        navCategory.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CategoryActivity.class));
        });

        navSetting.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SettingActivity.class));
        });

        navAdd.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ChooseTransactionActivity.class));
        });
    }

    private void loadHomeData() {
        SharedPreferences prefs =
                getSharedPreferences("UserData", MODE_PRIVATE);

        String fullName =
                prefs.getString("fullName", "Người dùng");

        tvHello.setText("Xin chào, " + fullName + "! 👋");

        loadRecentTransactions();
    }

    private void showMoney() {
        int totalIncome = getTotalIncome();
        int totalExpense = getTotalExpense();
        int balance = totalIncome - totalExpense;

        tvBalance.setText(formatMoney(balance));
        tvIncome.setText("Tổng thu\n" + formatMoney(totalIncome));
        tvExpense.setText("Tổng chi\n" + formatMoney(totalExpense));

        imgEye.setImageResource(R.drawable.ic_eye);
        isBalanceVisible = true;
    }

    private void hideMoney() {
        tvBalance.setText("******");
        tvIncome.setText("Tổng thu\n******");
        tvExpense.setText("Tổng chi\n******");

        imgEye.setImageResource(R.drawable.ic_eye_off);
        isBalanceVisible = false;
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

    private void loadRecentTransactions() {
        layoutTransactions.removeAllViews();

        Cursor cursor = database.rawQuery(
                "SELECT title, date, amount FROM transactions ORDER BY id DESC LIMIT 5",
                null
        );

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
        } else {
            imgIcon.setImageResource(R.drawable.ic_default);
            bg.setColor(Color.parseColor("#9B59B6"));
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
        String sign = money > 0 ? "+" : "-";
        tvMoney.setText(sign + formatMoney(money));
        tvMoney.setTextSize(14);

        if (money > 0) {
            tvMoney.setTextColor(Color.parseColor("#00A86B"));
        } else {
            tvMoney.setTextColor(Color.parseColor("#FF3B3B"));
        }

        row.addView(iconContainer);
        row.addView(tvInfo);
        row.addView(tvMoney);

        layoutTransactions.addView(row);
    }

    private String formatMoney(int money) {
        return String.format("%,d đ", Math.abs(money)).replace(",", ".");
    }
}