package com.quanlychitieu.doan.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.alltransaction.AllTransactionActivity;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;
import com.quanlychitieu.doan.goal.GoalActivity;
import com.quanlychitieu.doan.history.ExpenseHistoryActivity;
import com.quanlychitieu.doan.history.IncomeHistoryActivity;
import com.quanlychitieu.doan.wallet.WalletActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView tvHello, tvBalance, tvIncome, tvExpense, tvViewAll;
    private LinearLayout btnIncome, btnExpense, btnTransfer, btnWallet, btnGoal;
    private ImageView imgEye;
    private LinearLayout layoutTransactions;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private boolean isBalanceVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupSafeArea();
        BottomNavHelper.setup(this);

        tvHello = findViewById(R.id.tvHello);
        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvViewAll = findViewById(R.id.tvViewAll);

        imgEye = findViewById(R.id.imgEye);
        layoutTransactions = findViewById(R.id.layoutTransactions);

        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnWallet = findViewById(R.id.btnWallet);
        btnGoal = findViewById(R.id.btnGoal);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        loadHomeData();
        showMoney();
        setupEyeButton();
        setupQuickButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        database = dbHelper.getWritableDatabase();

        loadRecentTransactions();

        if (isBalanceVisible) {
            showMoney();
        } else {
            hideMoney();
        }
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

    private void setupEyeButton() {
        imgEye.setOnClickListener(v -> {
            if (isBalanceVisible) {
                hideMoney();
            } else {
                showMoney();
            }
        });
    }

    private void setupQuickButtons() {
        btnIncome.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, IncomeHistoryActivity.class)));

        btnExpense.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ExpenseHistoryActivity.class)));

        btnTransfer.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng chuyển khoản", Toast.LENGTH_SHORT).show());

        btnWallet.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, WalletActivity.class)));

        btnGoal.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, GoalActivity.class)));

        tvViewAll.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, AllTransactionActivity.class)));
    }

    private void loadHomeData() {
        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String fullName = prefs.getString("fullName", "Người dùng");

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

        imgEye.setImageResource(R.drawable.ic_eye_off);
        isBalanceVisible = true;
    }

    private void hideMoney() {
        tvBalance.setText("********");
        tvIncome.setText("Tổng thu\n********");
        tvExpense.setText("Tổng chi\n********");

        imgEye.setImageResource(R.drawable.ic_eye);
        isBalanceVisible = false;
    }

    private int getTotalIncome() {
        int total = 0;

        Cursor cursor = database.rawQuery(
                "SELECT SUM(ABS(amount)) FROM transactions WHERE type='INCOME'",
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
                "SELECT SUM(ABS(amount)) FROM transactions WHERE type='EXPENSE'",
                null
        );

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();
        return total;
    }

    private void loadRecentTransactions() {
        layoutTransactions.removeAllViews();

        Cursor cursor = database.rawQuery(
                "SELECT title, date, amount, type, icon, color " +
                        "FROM transactions " +
                        "ORDER BY id DESC LIMIT 5",
                null
        );

        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String date = cursor.getString(1);
            int amount = cursor.getInt(2);
            String type = cursor.getString(3);
            String iconName = cursor.getString(4);
            String colorCode = cursor.getString(5);

            addTransaction(title, date, amount, type, iconName, colorCode);
        }

        cursor.close();
    }

    private void addTransaction(String title,
                                String date,
                                int money,
                                String type,
                                String iconName,
                                String colorCode) {

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

        iconContainer.setBackground(bg);

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
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String formatMoney(int money) {
        return String.format("%,d đ", money).replace(",", ".");
    }
}