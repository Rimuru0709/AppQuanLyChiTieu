package com.quanlychitieu.doan.wallet;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;

public class WalletActivity extends AppCompatActivity {

    private ImageView imgBack;
    private TextView btnAddWallet, tvTotalWalletBalance, tvWalletIncome, tvWalletExpense;
    private LinearLayout layoutWallets;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        setupSafeArea();
        BottomNavHelper.setup(this);

        imgBack = findViewById(R.id.imgBack);
        btnAddWallet = findViewById(R.id.btnAddWallet);
        layoutWallets = findViewById(R.id.layoutWallets);
        tvTotalWalletBalance = findViewById(R.id.tvTotalWalletBalance);
        tvWalletIncome = findViewById(R.id.tvWalletIncome);
        tvWalletExpense = findViewById(R.id.tvWalletExpense);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        imgBack.setOnClickListener(v -> finish());
        btnAddWallet.setOnClickListener(v -> showAddWalletDialog());

        loadWalletData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        database = dbHelper.getReadableDatabase();
        loadWalletData();
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    dp(20),
                    bars.top + dp(12),
                    dp(20),
                    dp(24)
            );

            return insets;
        });
    }

    private void loadWalletData() {
        layoutWallets.removeAllViews();

        int totalIncome = getTotalIncomeAllWallets();
        int totalExpense = getTotalExpenseAllWallets();
        int totalBalance = totalIncome - totalExpense;

        tvTotalWalletBalance.setText(formatMoney(totalBalance));
        tvWalletIncome.setText("Thu\n" + formatMoney(totalIncome));
        tvWalletExpense.setText("Chi\n" + formatMoney(totalExpense));

        Cursor cursor = dbHelper.getAllWallets();

        if (cursor.getCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có ví nào");
            empty.setTextColor(Color.parseColor("#6B7280"));
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(30), 0, dp(30));
            layoutWallets.addView(empty);
            cursor.close();
            return;
        }

        while (cursor.moveToNext()) {
            String walletName = cursor.getString(0);

            int income = getWalletIncome(walletName);
            int expense = getWalletExpense(walletName);
            int balance = income - expense;

            addWalletCard(walletName, income, expense, balance);
        }

        cursor.close();
    }

    private int getTotalIncomeAllWallets() {
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

    private int getTotalExpenseAllWallets() {
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

    private int getWalletIncome(String walletName) {
        int total = 0;

        Cursor cursor = database.rawQuery(
                "SELECT SUM(ABS(amount)) FROM transactions " +
                        "WHERE type='INCOME' AND wallet=?",
                new String[]{walletName}
        );

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();
        return total;
    }

    private int getWalletExpense(String walletName) {
        int total = 0;

        Cursor cursor = database.rawQuery(
                "SELECT SUM(ABS(amount)) FROM transactions " +
                        "WHERE type='EXPENSE' AND wallet=?",
                new String[]{walletName}
        );

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();
        return total;
    }

    private void addWalletCard(String walletName, int income, int expense, int balance) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        cardParams.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(cardParams);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));

        if (walletName.equals("Ví mặc định")) {
            bg.setColor(Color.parseColor("#EAF1FF"));
        } else if (walletName.equals("Tiết kiệm")) {
            bg.setColor(Color.parseColor("#ECFDF5"));
        } else if (walletName.equals("Ngân hàng")) {
            bg.setColor(Color.parseColor("#FFF7ED"));
        } else if (walletName.equals("Momo")) {
            bg.setColor(Color.parseColor("#F5E8FF"));
        } else {
            bg.setColor(Color.WHITE);
        }

        bg.setStroke(dp(1), Color.parseColor("#E5E7EB"));
        card.setBackground(bg);
        card.setElevation(dp(2));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView icon = new TextView(this);
        icon.setText(getWalletIcon(walletName));
        icon.setTextSize(28);
        icon.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(dp(48), dp(48));
        iconParams.setMargins(0, 0, dp(12), 0);
        icon.setLayoutParams(iconParams);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));

        TextView tvName = new TextView(this);
        tvName.setText(walletName);
        tvName.setTextColor(Color.parseColor("#111827"));
        tvName.setTextSize(16);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvBalanceLabel = new TextView(this);
        tvBalanceLabel.setText("Số dư ví");
        tvBalanceLabel.setTextColor(Color.parseColor("#6B7280"));
        tvBalanceLabel.setTextSize(13);
        tvBalanceLabel.setPadding(0, dp(3), 0, 0);

        titleBox.addView(tvName);
        titleBox.addView(tvBalanceLabel);

        TextView tvBalance = new TextView(this);
        tvBalance.setText(formatMoney(balance));
        tvBalance.setTextColor(balance >= 0 ? Color.parseColor("#111827") : Color.parseColor("#EF4444"));
        tvBalance.setTextSize(17);
        tvBalance.setTypeface(null, Typeface.BOLD);
        tvBalance.setGravity(Gravity.END);

        topRow.addView(icon);
        topRow.addView(titleBox);
        topRow.addView(tvBalance);

        LinearLayout moneyRow = new LinearLayout(this);
        moneyRow.setOrientation(LinearLayout.HORIZONTAL);
        moneyRow.setGravity(Gravity.CENTER_VERTICAL);
        moneyRow.setPadding(0, dp(14), 0, 0);

        TextView tvIncome = makeMoneyBox("Thu\n" + formatMoney(income), "#16A34A");
        TextView tvExpense = makeMoneyBox("Chi\n" + formatMoney(expense), "#EF4444");

        LinearLayout.LayoutParams leftParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        leftParams.setMargins(0, 0, dp(6), 0);
        tvIncome.setLayoutParams(leftParams);

        LinearLayout.LayoutParams rightParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        rightParams.setMargins(dp(6), 0, 0, 0);
        tvExpense.setLayoutParams(rightParams);

        moneyRow.addView(tvIncome);
        moneyRow.addView(tvExpense);

        card.addView(topRow);
        card.addView(moneyRow);

        card.setOnClickListener(v -> showWalletOptions(walletName));

        layoutWallets.addView(card);
    }

    private TextView makeMoneyBox(String text, String color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.parseColor(color));
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(10), dp(10), dp(10), dp(10));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1), Color.parseColor("#E5E7EB"));

        tv.setBackground(bg);
        return tv;
    }

    private String getWalletIcon(String walletName) {
        if (walletName.equals("Ví mặc định")) {
            return "💳";
        } else if (walletName.equals("Tiết kiệm")) {
            return "🐷";
        } else if (walletName.equals("Ngân hàng")) {
            return "🏦";
        } else if (walletName.equals("Momo")) {
            return "📱";
        } else {
            return "💼";
        }
    }

    private void showAddWalletDialog() {
        EditText edtWallet = new EditText(this);
        edtWallet.setHint("Nhập tên ví mới");
        edtWallet.setSingleLine(true);
        edtWallet.setPadding(dp(16), dp(12), dp(16), dp(12));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm ví mới")
                .setView(edtWallet)
                .setPositiveButton("Thêm", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String walletName = edtWallet.getText().toString().trim();

                if (walletName.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập tên ví", Toast.LENGTH_SHORT).show();
                    return;
                }

                dbHelper.insertWallet(walletName);

                Toast.makeText(this, "Đã thêm ví mới", Toast.LENGTH_SHORT).show();

                dialog.dismiss();

                database = dbHelper.getReadableDatabase();
                loadWalletData();
            });
        });

        dialog.show();
    }
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String formatMoney(int money) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(money).replace(",", ".") + " đ";
    }

    private void showWalletOptions(String walletName) {
        if (isDefaultWallet(walletName)) {
            Toast.makeText(this, "Không thể xóa ví mặc định", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"Xóa ví"};

        new AlertDialog.Builder(this)
                .setTitle(walletName)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        confirmDeleteWallet(walletName);
                    }
                })
                .show();
    }

    private void confirmDeleteWallet(String walletName) {
        if (dbHelper.walletHasTransaction(walletName)) {
            Toast.makeText(this, "Không thể xóa vì ví này đang có giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa ví")
                .setMessage("Bạn có chắc muốn xóa ví \"" + walletName + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteWallet(walletName);

                    Toast.makeText(this, "Đã xóa ví", Toast.LENGTH_SHORT).show();

                    database = dbHelper.getReadableDatabase();
                    loadWalletData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean isDefaultWallet(String walletName) {
        return walletName.equals("Ví mặc định")
                || walletName.equals("Tiết kiệm")
                || walletName.equals("Ngân hàng")
                || walletName.equals("Momo");
    }
}