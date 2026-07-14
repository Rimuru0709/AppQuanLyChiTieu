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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.quanlychitieu.doan.transfer.TransferActivity;
import com.quanlychitieu.doan.wallet.WalletActivity;
import com.quanlychitieu.doan.notification.NotificationActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView tvHello;
    private TextView tvBalance;
    private TextView tvIncome;
    private TextView tvExpense;
    private TextView tvViewAll;

    private LinearLayout btnIncome;
    private LinearLayout btnExpense;
    private LinearLayout btnTransfer;
    private LinearLayout btnWallet;
    private LinearLayout btnGoal;

    private ImageView imgEye;
    private ImageView imgBell;

    private View viewNotificationDot;

    private LinearLayout layoutTransactions;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private boolean isBalanceVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupSafeArea();
        BottomNavHelper.setup(this);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        loadHomeData();
        showMoney();
        setupEyeButton();
        setupQuickButtons();
        updateNotificationDot();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        database = dbHelper.getWritableDatabase();

        loadRecentTransactions();

        if (isBalanceVisible) {
            showMoney();
        } else {
            hideMoney();
        }

        updateNotificationDot();
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

    private void initViews() {
        tvHello = findViewById(R.id.tvHello);
        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvViewAll = findViewById(R.id.tvViewAll);

        imgEye = findViewById(R.id.imgEye);
        imgBell = findViewById(R.id.imgBell);

        viewNotificationDot = findViewById(R.id.viewNotificationDot);

        layoutTransactions = findViewById(R.id.layoutTransactions);

        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnWallet = findViewById(R.id.btnWallet);
        btnGoal = findViewById(R.id.btnGoal);
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

    private void setupEyeButton() {
        if (imgEye == null) {
            return;
        }

        imgEye.setOnClickListener(v -> {
            if (isBalanceVisible) {
                hideMoney();
            } else {
                showMoney();
            }
        });
    }

    private void setupQuickButtons() {
        if (btnIncome != null) {
            btnIncome.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    IncomeHistoryActivity.class
                            )
                    )
            );
        }

        if (btnExpense != null) {
            btnExpense.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    ExpenseHistoryActivity.class
                            )
                    )
            );
        }

        if (btnTransfer != null) {
            btnTransfer.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    TransferActivity.class
                            )
                    )
            );
        }

        if (btnWallet != null) {
            btnWallet.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    WalletActivity.class
                            )
                    )
            );
        }

        if (btnGoal != null) {
            btnGoal.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    GoalActivity.class
                            )
                    )
            );
        }

        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v ->
                    startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    AllTransactionActivity.class
                            )
                    )
            );
        }

        if (imgBell != null) {
            imgBell.setOnClickListener(v -> {
                Intent intent = new Intent(
                        HomeActivity.this,
                        NotificationActivity.class
                );

                startActivity(intent);
            });
        }
    }

    private void loadHomeData() {
        SharedPreferences preferences =
                getSharedPreferences(
                        "UserData",
                        MODE_PRIVATE
                );

        String fullName = preferences.getString(
                "fullName",
                "Người dùng"
        );

        if (fullName == null || fullName.trim().isEmpty()) {
            fullName = "Người dùng";
        }

        tvHello.setText(
                "Xin chào, " + fullName.trim() + "! 👋"
        );

        loadRecentTransactions();
    }

    private void showMoney() {
        if (database == null || !database.isOpen()) {
            return;
        }

        int totalIncome = getTotalIncome();
        int totalExpense = getTotalExpense();
        int balance = totalIncome - totalExpense;

        tvBalance.setText(formatMoney(balance));

        tvIncome.setText(
                "Tổng thu\n" + formatMoney(totalIncome)
        );

        tvExpense.setText(
                "Tổng chi\n" + formatMoney(totalExpense)
        );

        imgEye.setImageResource(R.drawable.ic_eye_off);
        isBalanceVisible = true;
    }

    private void hideMoney() {
        tvBalance.setText("********");

        tvIncome.setText(
                "Tổng thu\n********"
        );

        tvExpense.setText(
                "Tổng chi\n********"
        );

        imgEye.setImageResource(R.drawable.ic_eye);
        isBalanceVisible = false;
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

    private void loadRecentTransactions() {
        if (layoutTransactions == null
                || database == null
                || !database.isOpen()) {
            return;
        }

        layoutTransactions.removeAllViews();

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT category, date, amount, type, icon, color " +
                            "FROM transactions " +
                            "ORDER BY id DESC " +
                            "LIMIT 5",
                    null
            );

            while (cursor.moveToNext()) {

                String category = cursor.getString(0);

                String date = cursor.getString(1);

                int amount = cursor.getInt(2);

                String type = cursor.getString(3);

                String iconName = cursor.getString(4);

                String colorCode = cursor.getString(5);

                if (category == null
                        || category.trim().isEmpty()) {

                    category = "Khác";
                }

                addTransaction(
                        category,
                        date,
                        amount,
                        type,
                        iconName,
                        colorCode
                );
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void addTransaction(
            String title,
            String date,
            int money,
            String type,
            String iconName,
            String colorCode
    ) {
        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(
                0,
                dp(6),
                0,
                dp(6)
        );

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

        ImageView imageIcon =
                new ImageView(this);

        FrameLayout.LayoutParams iconParams =
                new FrameLayout.LayoutParams(
                        dp(18),
                        dp(18)
                );

        iconParams.gravity = Gravity.CENTER;
        imageIcon.setLayoutParams(iconParams);

        int iconResource = getResources().getIdentifier(
                iconName,
                "drawable",
                getPackageName()
        );

        if (iconResource == 0) {
            iconResource = R.drawable.ic_dot;
        }

        imageIcon.setImageResource(iconResource);
        imageIcon.setColorFilter(Color.WHITE);

        iconContainer.addView(imageIcon);

        TextView tvInfo =
                new TextView(this);

        tvInfo.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        tvInfo.setText(
                title + "\n" + date
        );

        tvInfo.setTextSize(14);
        tvInfo.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_primary
                )
        );

        TextView tvMoney =
                new TextView(this);

        tvMoney.setTextSize(14);
        tvMoney.setGravity(Gravity.END);

        if ("INCOME".equals(type)) {
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
        row.addView(tvInfo);
        row.addView(tvMoney);

        layoutTransactions.addView(row);
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

    private void updateNotificationDot() {
        if (viewNotificationDot == null) {
            return;
        }

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        int unreadCount =
                dbHelper.getUnreadNotificationCount();

        viewNotificationDot.setVisibility(
                unreadCount > 0
                        ? View.VISIBLE
                        : View.GONE
        );

        /*
         * Đảm bảo chấm đỏ luôn nằm trên icon chuông.
         */
        viewNotificationDot.bringToFront();
    }
}