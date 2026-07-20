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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;

public class WalletActivity extends AppCompatActivity {

    private ImageView imgBack;

    private TextView btnAddWallet;
    private TextView tvTotalWalletBalance;
    private TextView tvWalletIncome;
    private TextView tvWalletExpense;

    private LinearLayout layoutWallets;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initViews();
        setupSafeArea();
        BottomNavHelper.setup(this);
        setupDatabase();
        setupEvents();

        loadWalletData();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);

        btnAddWallet = findViewById(R.id.btnAddWallet);

        layoutWallets = findViewById(R.id.layoutWallets);

        tvTotalWalletBalance =
                findViewById(R.id.tvTotalWalletBalance);

        tvWalletIncome =
                findViewById(R.id.tvWalletIncome);

        tvWalletExpense =
                findViewById(R.id.tvWalletExpense);
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();
    }

    private void setupEvents() {
        imgBack.setOnClickListener(v -> finish());

        btnAddWallet.setOnClickListener(v ->
                showAddWalletDialog()
        );
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            dp(20),
                            bars.top + dp(12),
                            dp(20),
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

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        database = dbHelper.getReadableDatabase();
        loadWalletData();
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

    private void loadWalletData() {
        if (layoutWallets == null ||
                database == null ||
                !database.isOpen()) {
            return;
        }

        layoutWallets.removeAllViews();

        int totalIncome = getTotalIncomeAllWallets();
        int totalExpense = getTotalExpenseAllWallets();
        int totalBalance = totalIncome - totalExpense;

        tvTotalWalletBalance.setText(
                formatMoney(totalBalance)
        );

        tvWalletIncome.setText(
                "Thu\n" + formatMoney(totalIncome)
        );

        tvWalletExpense.setText(
                "Chi\n" + formatMoney(totalExpense)
        );

        Cursor cursor = null;

        try {
            cursor = dbHelper.getAllWallets();

            if (cursor.getCount() == 0) {
                showEmptyWalletMessage();
                return;
            }

            while (cursor.moveToNext()) {
                String walletName =
                        cursor.getString(0);

                int income =
                        getWalletIncome(walletName);

                int expense =
                        getWalletExpense(walletName);

                int balance =
                        income - expense;

                addWalletCard(
                        walletName,
                        income,
                        expense,
                        balance
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void showEmptyWalletMessage() {
        TextView empty = new TextView(this);

        empty.setText("Chưa có ví nào");
        empty.setTextSize(15);
        empty.setGravity(Gravity.CENTER);

        empty.setPadding(
                0,
                dp(30),
                0,
                dp(30)
        );

        empty.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        layoutWallets.addView(empty);
    }

    private int getTotalIncomeAllWallets() {
        return getTotalByType(
                "INCOME",
                null
        );
    }

    private int getTotalExpenseAllWallets() {
        return getTotalByType(
                "EXPENSE",
                null
        );
    }

    private int getWalletIncome(String walletName) {
        return getTotalByType(
                "INCOME",
                walletName
        );
    }

    private int getWalletExpense(String walletName) {
        return getTotalByType(
                "EXPENSE",
                walletName
        );
    }

    private int getTotalByType(
            String type,
            String walletName
    ) {
        int total = 0;
        Cursor cursor = null;

        try {
            if (walletName == null) {
                cursor = database.rawQuery(
                        "SELECT COALESCE(SUM(ABS(amount)), 0) " +
                                "FROM transactions " +
                                "WHERE type = ?",
                        new String[]{type}
                );
            } else {
                cursor = database.rawQuery(
                        "SELECT COALESCE(SUM(ABS(amount)), 0) " +
                                "FROM transactions " +
                                "WHERE type = ? AND wallet = ?",
                        new String[]{
                                type,
                                walletName
                        }
                );
            }

            if (cursor.moveToFirst() &&
                    !cursor.isNull(0)) {
                total = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }

    private void addWalletCard(
            String walletName,
            int income,
            int expense,
            int balance
    ) {
        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                dp(14)
        );

        card.setLayoutParams(cardParams);

        GradientDrawable cardBackground =
                createWalletBackground(walletName);

        card.setBackground(cardBackground);
        card.setElevation(dp(2));

        LinearLayout topRow =
                new LinearLayout(this);

        topRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView icon = new TextView(this);

        icon.setText(
                getWalletIcon(walletName)
        );

        icon.setTextSize(28);
        icon.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(
                        dp(48),
                        dp(48)
                );

        iconParams.setMargins(
                0,
                0,
                dp(12),
                0
        );

        icon.setLayoutParams(iconParams);

        LinearLayout titleBox =
                new LinearLayout(this);

        titleBox.setOrientation(
                LinearLayout.VERTICAL
        );

        titleBox.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        TextView tvName =
                new TextView(this);

        tvName.setText(walletName);
        tvName.setTextSize(16);
        tvName.setTypeface(
                null,
                Typeface.BOLD
        );

        tvName.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_primary
                )
        );

        TextView tvBalanceLabel =
                new TextView(this);

        tvBalanceLabel.setText("Số dư ví");
        tvBalanceLabel.setTextSize(13);

        tvBalanceLabel.setPadding(
                0,
                dp(3),
                0,
                0
        );

        tvBalanceLabel.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_secondary
                )
        );

        titleBox.addView(tvName);
        titleBox.addView(tvBalanceLabel);

        TextView tvBalance =
                new TextView(this);

        tvBalance.setText(
                formatMoney(balance)
        );

        tvBalance.setTextSize(17);

        tvBalance.setTypeface(
                null,
                Typeface.BOLD
        );

        tvBalance.setGravity(
                Gravity.END
        );

        if (balance >= 0) {
            tvBalance.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.text_primary
                    )
            );
        } else {
            tvBalance.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.delete_color
                    )
            );
        }

        topRow.addView(icon);
        topRow.addView(titleBox);
        topRow.addView(tvBalance);

        LinearLayout moneyRow =
                new LinearLayout(this);

        moneyRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        moneyRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        moneyRow.setPadding(
                0,
                dp(14),
                0,
                0
        );

        TextView tvIncome =
                makeMoneyBox(
                        "Thu\n" + formatMoney(income),
                        Color.parseColor("#16A34A")
                );

        TextView tvExpense =
                makeMoneyBox(
                        "Chi\n" + formatMoney(expense),
                        ContextCompat.getColor(
                                this,
                                R.color.delete_color
                        )
                );

        LinearLayout.LayoutParams leftParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        leftParams.setMargins(
                0,
                0,
                dp(6),
                0
        );

        tvIncome.setLayoutParams(leftParams);

        LinearLayout.LayoutParams rightParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        rightParams.setMargins(
                dp(6),
                0,
                0,
                0
        );

        tvExpense.setLayoutParams(rightParams);

        moneyRow.addView(tvIncome);
        moneyRow.addView(tvExpense);

        card.addView(topRow);
        card.addView(moneyRow);

        card.setOnClickListener(v ->
                showWalletOptions(walletName)
        );

        layoutWallets.addView(card);
    }

    private GradientDrawable createWalletBackground(
            String walletName
    ) {
        GradientDrawable background =
                new GradientDrawable();

        background.setCornerRadius(
                dp(20)
        );

        /*
         * Dùng nền chung theo Light/Dark Mode.
         * Không dùng các màu pastel cố định vì trong
         * Dark Mode chữ sáng có thể bị chìm.
         */
        background.setColor(
                ContextCompat.getColor(
                        this,
                        R.color.card_background
                )
        );

        background.setStroke(
                dp(1),
                ContextCompat.getColor(
                        this,
                        R.color.divider_color
                )
        );

        return background;
    }

    private TextView makeMoneyBox(
            String text,
            int textColor
    ) {
        TextView textView =
                new TextView(this);

        textView.setText(text);
        textView.setTextSize(14);

        textView.setTypeface(
                null,
                Typeface.BOLD
        );

        textView.setTextColor(textColor);
        textView.setGravity(Gravity.CENTER);

        textView.setPadding(
                dp(10),
                dp(10),
                dp(10),
                dp(10)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                ContextCompat.getColor(
                        this,
                        R.color.input_background
                )
        );

        background.setCornerRadius(
                dp(14)
        );

        background.setStroke(
                dp(1),
                ContextCompat.getColor(
                        this,
                        R.color.input_stroke
                )
        );

        textView.setBackground(background);

        return textView;
    }

    private String getWalletIcon(
            String walletName
    ) {
        if ("Ví mặc định".equals(walletName)) {
            return "💳";
        } else if ("Tiết kiệm".equals(walletName)) {
            return "🐷";
        } else if ("Ngân hàng".equals(walletName)) {
            return "🏦";
        } else if ("Momo".equals(walletName)) {
            return "📱";
        } else {
            return "💼";
        }
    }

    private void showAddWalletDialog() {
        EditText edtWallet =
                new EditText(this);

        edtWallet.setHint(
                "Nhập tên ví mới"
        );

        edtWallet.setSingleLine(true);

        edtWallet.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
        );

        edtWallet.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_primary
                )
        );

        edtWallet.setHintTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_hint
                )
        );

        GradientDrawable inputBackground =
                new GradientDrawable();

        inputBackground.setColor(
                ContextCompat.getColor(
                        this,
                        R.color.input_background
                )
        );

        inputBackground.setCornerRadius(
                dp(12)
        );

        inputBackground.setStroke(
                dp(1),
                ContextCompat.getColor(
                        this,
                        R.color.input_stroke
                )
        );

        edtWallet.setBackground(
                inputBackground
        );

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                dp(22),
                dp(6),
                dp(22),
                0
        );

        container.addView(
                edtWallet,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                )
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Thêm ví mới")
                        .setView(container)
                        .setPositiveButton(
                                "Thêm",
                                null
                        )
                        .setNegativeButton(
                                "Hủy",
                                null
                        )
                        .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String walletName =
                        edtWallet.getText()
                                .toString()
                                .trim();

                if (walletName.isEmpty()) {
                    edtWallet.setError(
                            "Vui lòng nhập tên ví"
                    );

                    edtWallet.requestFocus();
                    return;
                }

                dbHelper.insertWallet(
                        walletName
                );

                Toast.makeText(
                        this,
                        "Đã thêm ví mới",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();

                database =
                        dbHelper.getReadableDatabase();

                loadWalletData();
            });
        });

        dialog.show();
    }

    private void showWalletOptions(
            String walletName
    ) {
        if (isDefaultWallet(walletName)) {
            Toast.makeText(
                    this,
                    "Không thể xóa ví mặc định",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] options = {
                "Xóa ví"
        };

        new AlertDialog.Builder(this)
                .setTitle(walletName)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        confirmDeleteWallet(
                                walletName
                        );
                    }
                })
                .show();
    }

    private void confirmDeleteWallet(
            String walletName
    ) {
        if (dbHelper.walletHasTransaction(
                walletName
        )) {
            Toast.makeText(
                    this,
                    "Không thể xóa vì ví này đang có giao dịch",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa ví")
                .setMessage(
                        "Bạn có chắc muốn xóa ví \"" +
                                walletName +
                                "\" không?"
                )
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> {

                            dbHelper.deleteWallet(
                                    walletName
                            );

                            Toast.makeText(
                                    this,
                                    "Đã xóa ví",
                                    Toast.LENGTH_SHORT
                            ).show();

                            database =
                                    dbHelper.getReadableDatabase();

                            loadWalletData();
                        }
                )
                .setNegativeButton(
                        "Hủy",
                        null
                )
                .show();
    }

    private boolean isDefaultWallet(
            String walletName
    ) {
        return "Ví mặc định".equals(walletName)
                || "Tiết kiệm".equals(walletName)
                || "Ngân hàng".equals(walletName)
                || "Momo".equals(walletName);
    }

    private int dp(int value) {
        return Math.round(
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private String formatMoney(int money) {
        DecimalFormat formatter =
                new DecimalFormat("#,###");

        return formatter
                .format(money)
                .replace(",", ".") +
                " đ";
    }
}