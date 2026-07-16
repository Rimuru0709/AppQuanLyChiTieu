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
import com.quanlychitieu.doan.notification.NotificationModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlertActivity extends AppCompatActivity {

    private static final String KEY_TRANSACTION_LIMIT =
            "transaction_limit";

    private static final String KEY_WALLET_LIMIT =
            "wallet_limit";

    private ImageView imgBack;
    private RecyclerView rvBudgetAlerts;

    private TextView tvGoalAlert;

    private View layoutLargeTransactionAlert;
    private View layoutLowWalletAlert;

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

        initViews();
        setupDatabase();
        setupRecyclerView();
        setupEvents();

        loadAlertSettings();
        loadAlerts();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper != null) {
            database =
                    dbHelper.getReadableDatabase();
        }

        loadAlertSettings();
        loadAlerts();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (database != null
                && database.isOpen()) {

            database.close();
        }

        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // =========================================================
    // KHỞI TẠO VIEW
    // =========================================================

    private void initViews() {
        imgBack =
                findViewById(R.id.imgBack);

        rvBudgetAlerts =
                findViewById(R.id.rvBudgetAlerts);

        layoutLargeTransactionAlert =
                findViewById(
                        R.id.layoutLargeTransactionAlert
                );

        layoutLowWalletAlert =
                findViewById(
                        R.id.layoutLowWalletAlert
                );

        tvGoalAlert =
                findViewById(R.id.tvGoalAlert);

        swGoalAlert =
                findViewById(R.id.swGoalAlert);
    }

    private void setupDatabase() {
        dbHelper =
                new DatabaseHelper(this);

        database =
                dbHelper.getReadableDatabase();
    }

    private void setupRecyclerView() {
        alertList =
                new ArrayList<>();

        alertAdapter =
                new AlertAdapter(
                        this,
                        alertList
                );

        rvBudgetAlerts.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvBudgetAlerts.setAdapter(
                alertAdapter
        );

        rvBudgetAlerts.setNestedScrollingEnabled(
                false
        );
    }

    private void setupEvents() {
        if (imgBack != null) {
            imgBack.setOnClickListener(
                    view -> finish()
            );
        }

        if (layoutLargeTransactionAlert != null) {
            layoutLargeTransactionAlert
                    .setOnClickListener(
                            view ->
                                    showInputLimitDialog(
                                            KEY_TRANSACTION_LIMIT
                                    )
                    );
        }

        if (layoutLowWalletAlert != null) {
            layoutLowWalletAlert
                    .setOnClickListener(
                            view ->
                                    showInputLimitDialog(
                                            KEY_WALLET_LIMIT
                                    )
                    );
        }

        if (swGoalAlert != null) {
            swGoalAlert.setOnCheckedChangeListener(
                    (buttonView, isChecked) ->
                            loadGoalAlert()
            );
        }
    }

    // =========================================================
    // SAFE AREA
    // =========================================================

    private void setupSafeArea() {
        View content =
                findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets bars =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
                            );

                    view.setPadding(
                            dp(18),
                            bars.top + dp(12),
                            dp(18),
                            dp(24)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    // =========================================================
    // TẢI CÀI ĐẶT CẢNH BÁO
    // =========================================================

    private void loadAlertSettings() {
        largeTransactionLimit =
                dbHelper.getAlertSettingValue(
                        KEY_TRANSACTION_LIMIT,
                        5_000_000
                );

        lowWalletLimit =
                dbHelper.getAlertSettingValue(
                        KEY_WALLET_LIMIT,
                        500_000
                );
    }

    private void loadAlerts() {
        if (database == null
                || !database.isOpen()) {

            return;
        }

        loadCategoryExpenseAlerts();
        loadLargeTransactionAlert();
        loadLowWalletAlert();
        loadGoalAlert();
    }

    // =========================================================
    // CẢNH BÁO NGÂN SÁCH THEO DANH MỤC
    // =========================================================

    private void loadCategoryExpenseAlerts() {
        alertList.clear();

        if (database == null
                || !database.isOpen()) {

            return;
        }

        String monthText =
                getCurrentMonthText();

        Cursor cursor = null;

        try {
            /*
             * Đọc danh sách từ bảng budgets.
             *
             * LEFT JOIN transactions giúp ngân sách vẫn xuất hiện
             * khi danh mục chưa có giao dịch chi tiêu.
             */
            cursor = database.rawQuery(
                    "SELECT "
                            + "b."
                            + DatabaseHelper.BUDGET_CATEGORY
                            + ", "

                            + "b."
                            + DatabaseHelper.BUDGET_AMOUNT
                            + ", "

                            + "COALESCE(MAX(t."
                            + DatabaseHelper.TRANSACTION_ICON
                            + "), ''), "

                            + "COALESCE(MAX(t."
                            + DatabaseHelper.TRANSACTION_COLOR
                            + "), ''), "

                            + "COALESCE(SUM(ABS(t."
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ")), 0) "

                            + "FROM "
                            + DatabaseHelper.TABLE_BUDGET
                            + " b "

                            + "LEFT JOIN "
                            + DatabaseHelper.TABLE_TRANSACTION
                            + " t "

                            + "ON t."
                            + DatabaseHelper.TRANSACTION_CATEGORY
                            + " = b."
                            + DatabaseHelper.BUDGET_CATEGORY
                            + " "

                            + "AND t."
                            + DatabaseHelper.TRANSACTION_TYPE
                            + " = ? "

                            + "AND t."
                            + DatabaseHelper.TRANSACTION_DATE
                            + " LIKE ? "

                            + "WHERE b."
                            + DatabaseHelper.BUDGET_MONTH
                            + " = ? "

                            + "GROUP BY "
                            + "b."
                            + DatabaseHelper.BUDGET_CATEGORY
                            + ", "
                            + "b."
                            + DatabaseHelper.BUDGET_AMOUNT
                            + " "

                            + "ORDER BY "
                            + "COALESCE(SUM(ABS(t."
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ")), 0) DESC",

                    new String[]{
                            "EXPENSE",
                            "%" + monthText,
                            monthText
                    }
            );

            if (!cursor.moveToFirst()) {
                alertList.add(
                        new AlertModel(
                                R.drawable.ic_dot,
                                "#ADB5BD",
                                "Chưa thiết lập ngân sách",
                                "Vào Cài đặt → Ngân sách",
                                "Tháng " + monthText,
                                "0%",
                                false,
                                0
                        )
                );

                alertAdapter.notifyDataSetChanged();
                return;
            }

            do {
                String category =
                        cursor.getString(0);

                int budgetAmount =
                        cursor.getInt(1);

                String iconName =
                        cursor.getString(2);

                String colorCode =
                        cursor.getString(3);

                int usedAmount =
                        cursor.getInt(4);

                if (category == null
                        || category.trim().isEmpty()) {

                    category = "Khác";
                }

                if (iconName == null
                        || iconName.trim().isEmpty()) {

                    iconName =
                            getDefaultCategoryIcon(
                                    category
                            );
                }

                if (colorCode == null
                        || colorCode.trim().isEmpty()) {

                    colorCode =
                            getDefaultCategoryColor(
                                    category
                            );
                }

                int warningPercent =
                        dbHelper.getWarningPercent(
                                category
                        );

                boolean alertEnabled =
                        dbHelper.isCategoryAlertEnabled(
                                category
                        );

                int usedPercent = 0;

                if (budgetAmount > 0) {
                    usedPercent =
                            Math.round(
                                    usedAmount
                                            * 100f
                                            / budgetAmount
                            );
                }

                int iconResource =
                        getResources().getIdentifier(
                                iconName,
                                "drawable",
                                getPackageName()
                        );

                if (iconResource == 0) {
                    iconResource =
                            R.drawable.ic_dot;
                }

                boolean reachedWarning =
                        budgetAmount > 0
                                && usedPercent
                                >= warningPercent;

                alertList.add(
                        new AlertModel(
                                iconResource,
                                colorCode,
                                category,
                                "Ngân sách: "
                                        + formatMoney(
                                        budgetAmount
                                ),
                                "Đã chi: "
                                        + formatMoney(
                                        usedAmount
                                ),
                                warningPercent + "%",
                                alertEnabled,
                                warningPercent
                        )
                );

                /*
                 * Chỉ tạo thông báo khi:
                 * - cảnh báo đang bật;
                 * - có ngân sách;
                 * - mức chi đạt ngưỡng.
                 */
                if (alertEnabled
                        && reachedWarning) {

                    String notificationTitle =
                            "Cảnh báo ngân sách";

                    String notificationMessage;

                    if (usedPercent >= 100) {
                        notificationMessage =
                                "Bạn đã sử dụng "
                                        + usedPercent
                                        + "% ngân sách "
                                        + category
                                        + " và đã vượt ngân sách tháng này.";

                    } else {
                        notificationMessage =
                                "Bạn đã sử dụng "
                                        + usedPercent
                                        + "% ngân sách "
                                        + category
                                        + " trong tháng này.";
                    }

                    insertAlertNotificationIfNeeded(
                            notificationTitle,
                            notificationMessage
                    );
                }

            } while (cursor.moveToNext());

        } catch (Exception exception) {
            exception.printStackTrace();

            Toast.makeText(
                    this,
                    "Không thể tải cảnh báo ngân sách",
                    Toast.LENGTH_SHORT
            ).show();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        alertAdapter.notifyDataSetChanged();
    }

    // =========================================================
    // CẢNH BÁO GIAO DỊCH LỚN
    // =========================================================

    private void loadLargeTransactionAlert() {
        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT "
                            + DatabaseHelper.TRANSACTION_TITLE
                            + ", "
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ", "
                            + DatabaseHelper.TRANSACTION_DATE

                            + " FROM "
                            + DatabaseHelper.TABLE_TRANSACTION

                            + " WHERE ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ") >= ? "

                            + "ORDER BY "
                            + DatabaseHelper.TRANSACTION_ID
                            + " DESC "

                            + "LIMIT 1",

                    new String[]{
                            String.valueOf(
                                    largeTransactionLimit
                            )
                    }
            );

            if (!cursor.moveToFirst()) {
                return;
            }

            String transactionTitle =
                    cursor.getString(0);

            int amount =
                    Math.abs(
                            cursor.getInt(1)
                    );

            String transactionDate =
                    cursor.getString(2);

            String title =
                    "Cảnh báo giao dịch lớn";

            String message =
                    "Giao dịch \""
                            + transactionTitle
                            + "\" có giá trị "
                            + formatMoney(amount)
                            + " vào ngày "
                            + transactionDate
                            + ".";

            insertAlertNotificationIfNeeded(
                    title,
                    message
            );

        } catch (Exception exception) {
            exception.printStackTrace();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================================
    // CẢNH BÁO SỐ DƯ VÍ THẤP
    // =========================================================

    private void loadLowWalletAlert() {
        Cursor cursor =
                dbHelper.getAllWallets();

        try {
            while (cursor.moveToNext()) {
                String walletName =
                        cursor.getString(0);

                /*
                 * Không cảnh báo ví chưa từng có giao dịch.
                 */
                if (!dbHelper.walletHasTransaction(
                        walletName
                )) {
                    continue;
                }

                int balance =
                        getWalletBalance(
                                walletName
                        );

                if (balance < lowWalletLimit) {
                    String title =
                            "Cảnh báo số dư ví thấp";

                    String message =
                            "Số dư ví \""
                                    + walletName
                                    + "\" hiện còn "
                                    + formatMoney(balance)
                                    + ", thấp hơn mức cảnh báo "
                                    + formatMoney(
                                    lowWalletLimit
                            )
                                    + ".";

                    insertAlertNotificationIfNeeded(
                            title,
                            message
                    );
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================================
    // CẢNH BÁO MỤC TIÊU TIẾT KIỆM
    // =========================================================

    private void loadGoalAlert() {
        if (swGoalAlert == null
                || tvGoalAlert == null) {

            return;
        }

        if (!swGoalAlert.isChecked()) {
            tvGoalAlert.setText(
                    "Đã tắt cảnh báo mục tiêu tiết kiệm."
            );

            return;
        }

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT "
                            + DatabaseHelper.GOAL_NAME
                            + ", "
                            + DatabaseHelper.GOAL_TARGET_AMOUNT
                            + ", "
                            + DatabaseHelper.GOAL_SAVED_AMOUNT

                            + " FROM "
                            + DatabaseHelper.TABLE_GOAL

                            + " ORDER BY "
                            + DatabaseHelper.GOAL_ID
                            + " DESC "

                            + "LIMIT 1",
                    null
            );

            if (!cursor.moveToFirst()) {
                tvGoalAlert.setText(
                        "Chưa có mục tiêu tiết kiệm nào."
                );

                return;
            }

            String name =
                    cursor.getString(0);

            int targetAmount =
                    cursor.getInt(1);

            int savedAmount =
                    cursor.getInt(2);

            int remain =
                    targetAmount - savedAmount;

            if (remain <= 0) {
                tvGoalAlert.setText(
                        "Mục tiêu \""
                                + name
                                + "\" đã hoàn thành."
                );

                insertAlertNotificationIfNeeded(
                        "Hoàn thành mục tiêu tiết kiệm",
                        "Bạn đã hoàn thành mục tiêu \""
                                + name
                                + "\"."
                );

                return;
            }

            tvGoalAlert.setText(
                    "Mục tiêu \""
                            + name
                            + "\" còn thiếu "
                            + formatMoney(remain)
            );

            int progress = 0;

            if (targetAmount > 0) {
                progress =
                        Math.round(
                                savedAmount
                                        * 100f
                                        / targetAmount
                        );
            }

            if (progress >= 80) {
                insertAlertNotificationIfNeeded(
                        "Cảnh báo mục tiêu tiết kiệm",
                        "Bạn đã hoàn thành "
                                + progress
                                + "% mục tiêu \""
                                + name
                                + "\". Còn thiếu "
                                + formatMoney(remain)
                                + "."
                );
            }

        } catch (Exception exception) {
            exception.printStackTrace();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================================
    // LƯU CẢNH BÁO VÀO NOTIFICATIONS
    // =========================================================

    private void insertAlertNotificationIfNeeded(
            String title,
            String message
    ) {
        if (notificationExistsToday(
                title,
                message
        )) {
            return;
        }

        dbHelper.insertNotification(
                title,
                message,
                NotificationModel.TYPE_ALERT,
                formatCurrentDateTime()
        );
    }

    private boolean notificationExistsToday(
            String title,
            String message
    ) {
        Cursor cursor = null;

        try {
            String today =
                    getCurrentDateText();

            cursor = database.rawQuery(
                    "SELECT COUNT(*) "
                            + "FROM "
                            + DatabaseHelper.TABLE_NOTIFICATION
                            + " WHERE "
                            + DatabaseHelper.NOTIFICATION_TITLE
                            + " = ? "
                            + "AND "
                            + DatabaseHelper.NOTIFICATION_MESSAGE
                            + " = ? "
                            + "AND "
                            + DatabaseHelper.NOTIFICATION_CREATED_AT
                            + " LIKE ?",

                    new String[]{
                            title,
                            message,
                            "%" + today + "%"
                    }
            );

            return cursor.moveToFirst()
                    && cursor.getInt(0) > 0;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================================
    // HỘP THOẠI CÀI ĐẶT NGƯỠNG
    // =========================================================

    private void showInputLimitDialog(
            String key
    ) {
        EditText input =
                new EditText(this);

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
        );

        input.setHint(
                "Nhập số tiền"
        );

        if (KEY_TRANSACTION_LIMIT.equals(key)) {
            input.setText(
                    String.valueOf(
                            largeTransactionLimit
                    )
            );

        } else {
            input.setText(
                    String.valueOf(
                            lowWalletLimit
                    )
            );
        }

        new AlertDialog.Builder(this)
                .setTitle(
                        KEY_TRANSACTION_LIMIT.equals(key)
                                ? "Nhập ngưỡng giao dịch lớn"
                                : "Nhập ngưỡng số dư ví thấp"
                )
                .setView(input)
                .setPositiveButton(
                        "Lưu",
                        (dialog, which) -> {

                            String text =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (text.isEmpty()) {
                                Toast.makeText(
                                        this,
                                        "Vui lòng nhập số tiền",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            int value;

                            try {
                                value =
                                        Integer.parseInt(
                                                text
                                        );

                            } catch (NumberFormatException exception) {
                                Toast.makeText(
                                        this,
                                        "Số tiền không hợp lệ",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            if (value <= 0) {
                                Toast.makeText(
                                        this,
                                        "Số tiền phải lớn hơn 0",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            dbHelper.saveAlertSettingValue(
                                    key,
                                    value
                            );

                            database =
                                    dbHelper.getReadableDatabase();

                            loadAlertSettings();
                            loadCategoryExpenseAlerts();
                            loadLargeTransactionAlert();
                            loadLowWalletAlert();
                            loadGoalAlert();

                            Toast.makeText(
                                    this,
                                    "Đã lưu cài đặt",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .setNegativeButton(
                        "Hủy",
                        null
                )
                .show();
    }

    // =========================================================
    // TÍNH SỐ DƯ VÍ
    // =========================================================

    private int getWalletBalance(
            String walletName
    ) {
        int balance = 0;

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT SUM(CASE "

                            + "WHEN "
                            + DatabaseHelper.TRANSACTION_TYPE
                            + " = 'INCOME' "

                            + "THEN ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ") "

                            + "WHEN "
                            + DatabaseHelper.TRANSACTION_TYPE
                            + " = 'EXPENSE' "

                            + "THEN -ABS("
                            + DatabaseHelper.TRANSACTION_AMOUNT
                            + ") "

                            + "ELSE 0 END) "

                            + "FROM "
                            + DatabaseHelper.TABLE_TRANSACTION

                            + " WHERE "
                            + DatabaseHelper.TRANSACTION_WALLET
                            + " = ?",

                    new String[]{
                            walletName
                    }
            );

            if (cursor.moveToFirst()
                    && !cursor.isNull(0)) {

                balance =
                        cursor.getInt(0);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return balance;
    }

    // =========================================================
    // ICON MẶC ĐỊNH THEO DANH MỤC
    // =========================================================

    private String getDefaultCategoryIcon(
            String category
    ) {
        if ("Ăn uống".equals(category)) {
            return "ic_food";
        }

        if ("Đi lại".equals(category)) {
            return "ic_transport";
        }

        if ("Mua sắm".equals(category)) {
            return "ic_shopping";
        }

        if ("Giải trí".equals(category)) {
            return "ic_entertainment";
        }

        if ("Hóa đơn".equals(category)) {
            return "ic_bill";
        }

        if ("Sức khỏe".equals(category)) {
            return "ic_health";
        }

        return "ic_dot";
    }

    // =========================================================
    // MÀU MẶC ĐỊNH THEO DANH MỤC
    // =========================================================

    private String getDefaultCategoryColor(
            String category
    ) {
        if ("Ăn uống".equals(category)) {
            return "#FF3131";
        }

        if ("Đi lại".equals(category)) {
            return "#2196F3";
        }

        if ("Mua sắm".equals(category)) {
            return "#FF9800";
        }

        if ("Giải trí".equals(category)) {
            return "#9C27B0";
        }

        if ("Hóa đơn".equals(category)) {
            return "#FF9800";
        }

        if ("Sức khỏe".equals(category)) {
            return "#FFB3C6";
        }

        return "#ADB5BD";
    }

    // =========================================================
    // HÀM HỖ TRỢ NGÀY THÁNG VÀ TIỀN
    // =========================================================

    private String getCurrentMonthText() {
        Calendar calendar =
                Calendar.getInstance();

        int month =
                calendar.get(Calendar.MONTH) + 1;

        int year =
                calendar.get(Calendar.YEAR);

        return String.format(
                Locale.getDefault(),
                "%02d/%04d",
                month,
                year
        );
    }

    private String getCurrentDateText() {
        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        return formatter.format(
                new Date()
        );
    }

    private String formatCurrentDateTime() {
        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "HH:mm - dd/MM/yyyy",
                        Locale.getDefault()
                );

        return formatter.format(
                new Date()
        );
    }

    private String formatMoney(
            int money
    ) {
        DecimalFormat formatter =
                new DecimalFormat("#,###");

        return formatter
                .format(money)
                .replace(",", ".")
                + " đ";
    }

    private int dp(
            int value
    ) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}