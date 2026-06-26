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
import com.quanlychitieu.doan.choosetransaction.ChooseTransactionActivity;
import com.quanlychitieu.doan.database.DatabaseHelper;
import com.quanlychitieu.doan.history.ExpenseHistoryActivity;
import com.quanlychitieu.doan.history.IncomeHistoryActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView tvHello, tvBalance, tvIncome, tvExpense, tvViewAll;
    private LinearLayout btnIncome, btnExpense, btnTransfer, btnWallet, btnGoal, btnAlert;
    private ImageView imgEye;
    private LinearLayout layoutTransactions;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private boolean isBalanceVisible = false;

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
        imgEye = findViewById(R.id.imgEye);
        layoutTransactions = findViewById(R.id.layoutTransactions);

        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnWallet = findViewById(R.id.btnWallet);
        btnGoal = findViewById(R.id.btnGoal);
        btnAlert = findViewById(R.id.btnAlert);
        tvViewAll = findViewById(R.id.tvViewAll);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        loadHomeData();
        hideMoney();
        setupEyeButton();
        setupQuickButtons();
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

        loadRecentTransactions();

        if (isBalanceVisible) {
            showMoney();
        } else {
            hideMoney();
        }
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
        btnIncome.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, IncomeHistoryActivity.class));
        });

        btnExpense.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ExpenseHistoryActivity.class));
        });

        btnTransfer.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng chuyển khoản", Toast.LENGTH_SHORT).show();
        });

        btnWallet.setOnClickListener(v -> {
            Toast.makeText(this, "Ví của tôi", Toast.LENGTH_SHORT).show();
        });

        btnGoal.setOnClickListener(v -> {
            Toast.makeText(this, "Mục tiêu tiết kiệm", Toast.LENGTH_SHORT).show();
        });

        btnAlert.setOnClickListener(v -> {
            Toast.makeText(this, "Cảnh báo chi tiêu", Toast.LENGTH_SHORT).show();
        });

        tvViewAll.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AllTransactionActivity.class));
        });
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
        row.setPadding(0, dp(5), 0, dp(5));

        FrameLayout iconContainer = new FrameLayout(this);

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(dp(38), dp(38));
        containerParams.rightMargin = dp(10);
        iconContainer.setLayoutParams(containerParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);

        ImageView imgIcon = new ImageView(this);

        FrameLayout.LayoutParams iconParams =
                new FrameLayout.LayoutParams(dp(18), dp(18));
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String formatMoney(int money) {
        return String.format("%,d đ", money).replace(",", ".");
    }
}