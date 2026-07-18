package com.quanlychitieu.doan.home;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.alltransaction.AllTransactionActivity;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;
import com.quanlychitieu.doan.goal.GoalActivity;
import com.quanlychitieu.doan.history.ExpenseHistoryActivity;
import com.quanlychitieu.doan.history.IncomeHistoryActivity;
import com.quanlychitieu.doan.notification.NotificationActivity;
import com.quanlychitieu.doan.setting.AccountStorage;
import com.quanlychitieu.doan.transfer.TransferActivity;
import com.quanlychitieu.doan.wallet.WalletActivity;

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

    private FirebaseAuth auth;

    private boolean isBalanceVisible = true;

    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();

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

    // =========================================================
    // ON RESUME
    // =========================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        database = dbHelper.getWritableDatabase();

        /*
         * Cập nhật lại tên người dùng khi:
         * - Đăng nhập thành công.
         * - Đăng xuất.
         * - Đổi tên trong màn hình tài khoản.
         */
        updateGreeting();

        loadRecentTransactions();

        if (isBalanceVisible) {
            showMoney();
        } else {
            hideMoney();
        }

        updateNotificationDot();
    }

    // =========================================================
    // ON DESTROY
    // Khi người dùng đóng ứng dụng sẽ tự động giải phóng bộ nhớ
    // =========================================================

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

    // =========================================================
    // ÁNH XẠ VIEW
    // =========================================================

    private void initViews() {
        tvHello = findViewById(R.id.tvHello);
        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvViewAll = findViewById(R.id.tvViewAll);

        imgEye = findViewById(R.id.imgEye);
        imgBell = findViewById(R.id.imgBell);

        viewNotificationDot =
                findViewById(R.id.viewNotificationDot);

        layoutTransactions =
                findViewById(R.id.layoutTransactions);

        btnIncome = findViewById(R.id.btnIncome);
        btnExpense = findViewById(R.id.btnExpense);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnWallet = findViewById(R.id.btnWallet);
        btnGoal = findViewById(R.id.btnGoal);
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

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
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

    // =========================================================
    // TẢI DỮ LIỆU HOME
    // =========================================================

    private void loadHomeData() {
        updateGreeting();
        loadRecentTransactions();
    }

    // =========================================================
    // CẬP NHẬT LỜI CHÀO
    // =========================================================

    private void updateGreeting() {
        if (tvHello == null) {
            return;
        }

        FirebaseUser currentUser =
                auth.getCurrentUser();

        /*
         * Chưa đăng nhập:
         * giữ nguyên chữ Người dùng.
         */
        if (currentUser == null) {
            tvHello.setText(
                    "Xin chào, Người dùng! 👋"
            );

            return;
        }

        String userId =
                currentUser.getUid();

        /*
         * Lấy tên đã lưu trong AccountStorage
         * theo UID của tài khoản Firebase.
         */
        String fullName =
                AccountStorage.getFullName(
                        HomeActivity.this,
                        userId
                );

        /*
         * Nếu AccountStorage chưa có tên,
         * lấy displayName từ Firebase.
         */
        if (fullName == null
                || fullName.trim().isEmpty()) {

            fullName =
                    currentUser.getDisplayName();
        }

        /*
         * Nếu Firebase cũng chưa có tên,
         * tiếp tục hiện Người dùng.
         */
        if (fullName == null
                || fullName.trim().isEmpty()) {

            fullName = "Người dùng";
        }

        tvHello.setText(
                "Xin chào, "
                        + fullName.trim()
                        + "! 👋"
        );
    }

    // =========================================================
    // NÚT ẨN / HIỆN SỐ DƯ
    // =========================================================

    private void setupEyeButton() {
        if (imgEye == null) {
            return;
        }

        imgEye.setOnClickListener(
                view -> {

                    if (isBalanceVisible) {
                        hideMoney();
                    } else {
                        showMoney();
                    }
                }
        );
    }

    // =========================================================
    // SỰ KIỆN CÁC NÚT CHỨC NĂNG
    // =========================================================

    private void setupQuickButtons() {
        if (btnIncome != null) {
            btnIncome.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    IncomeHistoryActivity.class
                            )
                    )
            );
        }

        if (btnExpense != null) {
            btnExpense.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    ExpenseHistoryActivity.class
                            )
                    )
            );
        }

        if (btnTransfer != null) {
            btnTransfer.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    TransferActivity.class
                            )
                    )
            );
        }

        if (btnWallet != null) {
            btnWallet.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    WalletActivity.class
                            )
                    )
            );
        }

        if (btnGoal != null) {
            btnGoal.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    GoalActivity.class
                            )
                    )
            );
        }

        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(
                    view -> startActivity(
                            new Intent(
                                    HomeActivity.this,
                                    AllTransactionActivity.class
                            )
                    )
            );
        }

        if (imgBell != null) {
            imgBell.setOnClickListener(
                    view -> {

                        Intent intent =
                                new Intent(
                                        HomeActivity.this,
                                        NotificationActivity.class
                                );

                        startActivity(intent);
                    }
            );
        }
    }

    // =========================================================
    // HIỂN THỊ SỐ TIỀN
    // =========================================================

    private void showMoney() {
        if (database == null
                || !database.isOpen()) {

            return;
        }

        int totalIncome =
                getTotalIncome();

        int totalExpense =
                getTotalExpense();

        int balance =
                totalIncome - totalExpense;

        if (tvBalance != null) {
            tvBalance.setText(
                    formatMoney(balance)
            );
        }

        if (tvIncome != null) {
            tvIncome.setText(
                    "Tổng thu\n"
                            + formatMoney(totalIncome)
            );
        }

        if (tvExpense != null) {
            tvExpense.setText(
                    "Tổng chi\n"
                            + formatMoney(totalExpense)
            );
        }

        if (imgEye != null) {
            imgEye.setImageResource(
                    R.drawable.ic_eye_off
            );
        }

        isBalanceVisible = true;
    }

    // =========================================================
    // ẨN SỐ TIỀN
    // =========================================================

    private void hideMoney() {
        if (tvBalance != null) {
            tvBalance.setText("********");
        }

        if (tvIncome != null) {
            tvIncome.setText(
                    "Tổng thu\n********"
            );
        }

        if (tvExpense != null) {
            tvExpense.setText(
                    "Tổng chi\n********"
            );
        }

        if (imgEye != null) {
            imgEye.setImageResource(
                    R.drawable.ic_eye
            );
        }

        isBalanceVisible = false;
    }

    // =========================================================
    // TÍNH TỔNG THU
    // =========================================================

    private int getTotalIncome() {
        int total = 0;

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT SUM(ABS(amount)) "
                            + "FROM transactions "
                            + "WHERE type = 'INCOME'",
                    null
            );

            if (cursor.moveToFirst()
                    && !cursor.isNull(0)) {

                total = cursor.getInt(0);
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }

    // =========================================================
    // TÍNH TỔNG CHI
    // =========================================================

    private int getTotalExpense() {
        int total = 0;

        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT SUM(ABS(amount)) "
                            + "FROM transactions "
                            + "WHERE type = 'EXPENSE'",
                    null
            );

            if (cursor.moveToFirst()
                    && !cursor.isNull(0)) {

                total = cursor.getInt(0);
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return total;
    }

    // =========================================================
    // TẢI GIAO DỊCH GẦN ĐÂY
    // =========================================================

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
                    "SELECT category, date, amount, type, icon, color "
                            + "FROM transactions "
                            + "ORDER BY id DESC "
                            + "LIMIT 5",
                    null
            );

            while (cursor.moveToNext()) {
                String category =
                        cursor.getString(0);

                String date =
                        cursor.getString(1);

                int amount =
                        cursor.getInt(2);

                String type =
                        cursor.getString(3);

                String iconName =
                        cursor.getString(4);

                String colorCode =
                        cursor.getString(5);

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

    // =========================================================
    // THÊM MỘT DÒNG GIAO DỊCH
    // =========================================================

    private void addTransaction(
            String title,
            String date,
            int money,
            String type,
            String iconName,
            String colorCode
    ) {
        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

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

        containerParams.setMarginEnd(
                dp(10)
        );

        iconContainer.setLayoutParams(
                containerParams
        );

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

        iconContainer.setBackground(
                iconBackground
        );

        ImageView imageIcon =
                new ImageView(this);

        FrameLayout.LayoutParams iconParams =
                new FrameLayout.LayoutParams(
                        dp(18),
                        dp(18)
                );

        iconParams.gravity =
                Gravity.CENTER;

        imageIcon.setLayoutParams(
                iconParams
        );

        int iconResource = 0;

        if (iconName != null
                && !iconName.trim().isEmpty()) {

            iconResource =
                    getResources().getIdentifier(
                            iconName,
                            "drawable",
                            getPackageName()
                    );
        }

        if (iconResource == 0) {
            iconResource =
                    R.drawable.ic_dot;
        }

        imageIcon.setImageResource(
                iconResource
        );

        imageIcon.setColorFilter(
                Color.WHITE
        );

        iconContainer.addView(
                imageIcon
        );

        TextView tvInfo =
                new TextView(this);

        tvInfo.setLayoutParams(
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        String displayDate =
                date == null
                        ? ""
                        : date;

        tvInfo.setText(
                title
                        + "\n"
                        + displayDate
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
                    "+"
                            + formatMoney(
                            Math.abs(money)
                    )
            );

            tvMoney.setTextColor(
                    Color.parseColor("#00A86B")
            );

        } else {
            tvMoney.setText(
                    "-"
                            + formatMoney(
                            Math.abs(money)
                    )
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

    // =========================================================
    // CHẤM ĐỎ THÔNG BÁO
    // =========================================================

    private void updateNotificationDot() {
        if (viewNotificationDot == null) {
            return;
        }

        if (dbHelper == null) {
            dbHelper =
                    new DatabaseHelper(this);
        }

        int unreadCount =
                dbHelper.getUnreadNotificationCount();

        viewNotificationDot.setVisibility(
                unreadCount > 0
                        ? View.VISIBLE
                        : View.GONE
        );

        /*
         * Đảm bảo chấm đỏ nằm phía trên icon chuông.
         */
        viewNotificationDot.bringToFront();
    }

    // =========================================================
    // ĐỔI DP THÀNH PX
    // =========================================================

    private int dp(int value) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =========================================================
    // ĐỊNH DẠNG TIỀN
    // =========================================================

    private String formatMoney(int money) {
        return String.format(
                "%,d đ",
                money
        ).replace(",", ".");
    }
}