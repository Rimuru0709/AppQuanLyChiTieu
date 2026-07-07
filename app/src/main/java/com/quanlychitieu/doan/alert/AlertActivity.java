package com.quanlychitieu.doan.alert;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AlertActivity extends AppCompatActivity {

    private ImageView imgBack;
    private RecyclerView rvBudgetAlerts;

    private TextView    tvGoalAlert;

    private View layoutLargeTransactionAlert, layoutLowWalletAlert;

    private SwitchCompat swGoalAlert;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private ArrayList<AlertModel> alertList;
    private AlertAdapter alertAdapter;

    private int largeTransactionLimit;
    private int lowWalletLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        setupSafeArea();
        BottomNavHelper.setup(this);

        imgBack = findViewById(R.id.imgBack);
        rvBudgetAlerts = findViewById(R.id.rvBudgetAlerts);

        layoutLargeTransactionAlert = findViewById(R.id.layoutLargeTransactionAlert);
        layoutLowWalletAlert = findViewById(R.id.layoutLowWalletAlert);

        tvGoalAlert = findViewById(R.id.tvGoalAlert);

        swGoalAlert = findViewById(R.id.swGoalAlert);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        alertList = new ArrayList<>();
        alertAdapter = new AlertAdapter(this, alertList);

        rvBudgetAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvBudgetAlerts.setAdapter(alertAdapter);
        rvBudgetAlerts.setNestedScrollingEnabled(false);

        imgBack.setOnClickListener(v -> finish());

        layoutLargeTransactionAlert.setOnClickListener(v ->
                showInputLimitDialog("transaction_limit"));

        layoutLowWalletAlert.setOnClickListener(v ->
                showInputLimitDialog("wallet_limit"));



        loadAlertSettings();
        loadAlerts();

        swGoalAlert.setOnCheckedChangeListener((buttonView, isChecked) ->
                loadGoalAlert());
    }

    @Override
    protected void onResume() {
        super.onResume();

        database = dbHelper.getReadableDatabase();

        loadAlertSettings();
        loadAlerts();
    }

    private void loadAlertSettings() {
        largeTransactionLimit =
                dbHelper.getAlertSettingValue("transaction_limit", 5000000);

        lowWalletLimit =
                dbHelper.getAlertSettingValue("wallet_limit", 500000);
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    dp(18),
                    bars.top + dp(12),
                    dp(18),
                    dp(24)
            );

            return insets;
        });
    }    private void loadAlerts() {
        loadCategoryExpenseAlerts();
        loadLargeTransactionAlert();
        loadLowWalletAlert();
        loadGoalAlert();
    }

    private void loadCategoryExpenseAlerts() {
        alertList.clear();

        String monthText = getCurrentMonthText();

        Cursor cursor = database.rawQuery(
                "SELECT title, icon, color, SUM(ABS(amount)) " +
                        "FROM transactions " +
                        "WHERE type='EXPENSE' AND date LIKE ? " +
                        "GROUP BY title, icon, color " +
                        "ORDER BY SUM(ABS(amount)) DESC",
                new String[]{"%" + monthText}
        );

        if (cursor.getCount() == 0) {
            int warningPercent = 80;

            alertList.add(new AlertModel(
                    R.drawable.ic_dot,
                    "#ADB5BD",
                    "Chưa có dữ liệu",
                    "Ngân sách: 0 đ",
                    "Đã chi: 0 đ",
                    warningPercent + "%",
                    true,
                    warningPercent
            ));

            cursor.close();
            alertAdapter.notifyDataSetChanged();
            return;
        }

        while (cursor.moveToNext()) {
            String category = cursor.getString(0);
            String iconName = cursor.getString(1);
            String colorCode = cursor.getString(2);
            int used = cursor.getInt(3);

            int budget = getDefaultBudget(category);
            int warningPercent = dbHelper.getWarningPercent(category);

            int iconRes = getResources().getIdentifier(
                    iconName,
                    "drawable",
                    getPackageName()
            );

            if (iconRes == 0) {
                iconRes = R.drawable.ic_dot;
            }

            alertList.add(new AlertModel(
                    iconRes,
                    colorCode,
                    category,
                    "Ngân sách: " + formatMoney(budget),
                    "Đã chi: " + formatMoney(used),
                    warningPercent + "%",
                    true,
                    warningPercent
            ));
        }

        cursor.close();
        alertAdapter.notifyDataSetChanged();
    }

    private void loadLargeTransactionAlert() {
        Cursor cursor = database.rawQuery(
                "SELECT title, amount FROM transactions " +
                        "WHERE ABS(amount) >= ? " +
                        "ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(largeTransactionLimit)}
        );

    }

    private void loadLowWalletAlert() {
        Cursor cursor = dbHelper.getAllWallets();

        String lowWalletName = null;
        int lowBalance = 0;

        while (cursor.moveToNext()) {
            String walletName = cursor.getString(0);
            int balance = getWalletBalance(walletName);

            if (balance < lowWalletLimit) {
                lowWalletName = walletName;
                lowBalance = balance;
                break;
            }
        }

        cursor.close();
    }

    private void loadGoalAlert() {
        if (!swGoalAlert.isChecked()) {
            tvGoalAlert.setText("Đã tắt cảnh báo mục tiêu tiết kiệm.");
            return;
        }

        Cursor cursor = database.rawQuery(
                "SELECT name, targetAmount, savedAmount FROM goals ORDER BY id DESC LIMIT 1",
                null
        );

        if (cursor.moveToFirst()) {
            String name = cursor.getString(0);
            int targetAmount = cursor.getInt(1);
            int savedAmount = cursor.getInt(2);
            int remain = targetAmount - savedAmount;

            if (remain <= 0) {
                tvGoalAlert.setText("Mục tiêu \"" + name + "\" đã hoàn thành.");
            } else {
                tvGoalAlert.setText(
                        "Mục tiêu \"" + name + "\" còn thiếu " + formatMoney(remain)
                );
            }
        } else {
            tvGoalAlert.setText("Chưa có mục tiêu tiết kiệm nào.");
        }

        cursor.close();
    }    private void showInputLimitDialog(String key) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập số tiền");

        if (key.equals("transaction_limit")) {
            input.setText(String.valueOf(largeTransactionLimit));
        } else {
            input.setText(String.valueOf(lowWalletLimit));
        }

        new AlertDialog.Builder(this)
                .setTitle(key.equals("transaction_limit")
                        ? "Nhập ngưỡng giao dịch lớn"
                        : "Nhập ngưỡng số dư ví thấp")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String text = input.getText().toString().trim();

                    if (text.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int value;

                    try {
                        value = Integer.parseInt(text);
                    } catch (Exception e) {
                        Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value <= 0) {
                        Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dbHelper.saveAlertSettingValue(key, value);
                    database = dbHelper.getReadableDatabase();

                    loadAlertSettings();
                    loadLargeTransactionAlert();
                    loadLowWalletAlert();

                    Toast.makeText(this, "Đã lưu cài đặt", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private int getWalletBalance(String walletName) {
        int balance = 0;

        Cursor cursor = database.rawQuery(
                "SELECT SUM(CASE " +
                        "WHEN type='INCOME' THEN ABS(amount) " +
                        "WHEN type='EXPENSE' THEN -ABS(amount) " +
                        "ELSE 0 END) " +
                        "FROM transactions WHERE wallet=?",
                new String[]{walletName}
        );

        if (cursor.moveToFirst()) {
            balance = cursor.getInt(0);
        }

        cursor.close();
        return balance;
    }

    private int getDefaultBudget(String category) {
        if (category.equals("Ăn uống")) return 2000000;
        if (category.equals("Đi lại")) return 1000000;
        if (category.equals("Mua sắm")) return 3000000;
        if (category.equals("Giải trí")) return 1000000;
        if (category.equals("Hóa đơn")) return 1500000;
        if (category.equals("Sức khỏe")) return 1000000;

        return 1000000;
    }

    private String getCurrentMonthText() {
        Calendar calendar = Calendar.getInstance();

        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        return String.format("%02d/%04d", month, year);
    }

    private String formatMoney(int money) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(money).replace(",", ".") + " đ";
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}