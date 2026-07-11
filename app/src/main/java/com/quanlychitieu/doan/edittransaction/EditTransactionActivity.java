package com.quanlychitieu.doan.edittransaction;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {

    private ImageView imgBack;
    private ImageView imgCategory;

    private TextView tabExpense;
    private TextView tabIncome;
    private TextView tvWallet;
    private TextView tvCategory;

    private LinearLayout layoutCategory;

    private EditText edtAmount;
    private EditText edtDate;
    private EditText edtOtherCategory;

    private Button btnSave;
    private Button btnDelete;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private int transactionId = -1;

    private String transactionType = "EXPENSE";
    private String selectedWallet = "Ví mặc định";

    private String categoryName = "Ăn uống";
    private String iconName = "ic_food";
    private String colorCode = "#FF3131";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);

        initViews();
        setupSafeArea();
        setupDatabase();
        setupEvents();

        transactionId = getIntent().getIntExtra(
                "transactionId",
                -1
        );

        edtOtherCategory.setVisibility(View.GONE);

        if (transactionId != -1) {
            loadTransactionDetail();
        } else {
            Toast.makeText(
                    this,
                    "Không tìm thấy giao dịch",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        imgCategory = findViewById(R.id.imgCategory);

        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);

        tvWallet = findViewById(R.id.tvWallet);
        tvCategory = findViewById(R.id.tvCategory);

        layoutCategory = findViewById(R.id.layoutCategory);

        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtOtherCategory = findViewById(R.id.edtOtherCategory);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupDatabase() {
        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();
    }

    private void setupEvents() {
        edtAmount.setImeOptions(
                EditorInfo.IME_ACTION_DONE
        );

        edtAmount.setOnEditorActionListener(
                (view, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        hideKeyboard();
                        edtAmount.clearFocus();
                        return true;
                    }

                    return false;
                }
        );

        imgBack.setOnClickListener(v -> finish());

        edtDate.setOnClickListener(v ->
                showDatePicker()
        );

        tvWallet.setOnClickListener(v ->
                showWalletDialog()
        );

        layoutCategory.setOnClickListener(v ->
                showCategoryDialog()
        );

        tabExpense.setOnClickListener(v -> {
            transactionType = "EXPENSE";

            setDefaultCategoryByType();

            edtOtherCategory.setVisibility(View.GONE);
            edtOtherCategory.setText("");

            updateTabUI();
            updateCategoryUI();
        });

        tabIncome.setOnClickListener(v -> {
            transactionType = "INCOME";

            setDefaultCategoryByType();

            edtOtherCategory.setVisibility(View.GONE);
            edtOtherCategory.setText("");

            updateTabUI();
            updateCategoryUI();
        });

        btnSave.setOnClickListener(v ->
                updateTransaction()
        );

        btnDelete.setOnClickListener(v ->
                confirmDelete()
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
                            dp(24),
                            bars.top + dp(12),
                            dp(24),
                            dp(30)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    private void loadTransactionDetail() {
        Cursor cursor = null;

        try {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, wallet, type, icon, color " +
                            "FROM transactions WHERE id = ?",
                    new String[]{
                            String.valueOf(transactionId)
                    }
            );

            if (cursor.moveToFirst()) {
                categoryName = cursor.getString(0);
                String date = cursor.getString(1);
                int amount = cursor.getInt(2);
                selectedWallet = cursor.getString(3);
                transactionType = cursor.getString(4);
                iconName = cursor.getString(5);
                colorCode = cursor.getString(6);

                tvCategory.setText(categoryName);
                edtDate.setText(date);
                edtAmount.setText(
                        String.valueOf(Math.abs(amount))
                );
                tvWallet.setText(selectedWallet);

                updateTabUI();
                updateCategoryUI();
            } else {
                Toast.makeText(
                        this,
                        "Không tìm thấy giao dịch",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateTransaction() {
        String amountText =
                edtAmount.getText().toString().trim();

        String date =
                edtDate.getText().toString().trim();

        if (amountText.isEmpty() || date.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if ("Khác".equals(categoryName)) {
            String otherCategory =
                    edtOtherCategory
                            .getText()
                            .toString()
                            .trim();

            if (otherCategory.isEmpty()) {
                edtOtherCategory.setError(
                        "Nhập tên danh mục khác"
                );
                return;
            }

            categoryName = otherCategory;
            iconName = "ic_dot";
            colorCode = "#ADB5BD";
        }

        int amount;

        try {
            amount = Integer.parseInt(amountText);
        } catch (Exception exception) {
            Toast.makeText(
                    this,
                    "Số tiền không hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (amount <= 0) {
            Toast.makeText(
                    this,
                    "Số tiền phải lớn hơn 0",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        dbHelper.updateTransaction(
                transactionId,
                categoryName,
                date,
                amount,
                selectedWallet,
                transactionType,
                iconName,
                colorCode
        );

        Toast.makeText(
                this,
                "Đã cập nhật giao dịch",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage(
                        "Bạn có chắc muốn xóa giao dịch này không?"
                )
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) -> {

                            dbHelper.deleteTransaction(
                                    transactionId
                            );

                            Toast.makeText(
                                    this,
                                    "Đã xóa giao dịch",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .setNegativeButton(
                        "Hủy",
                        null
                )
                .show();
    }

    private void updateTabUI() {
        int normalTextColor =
                ContextCompat.getColor(
                        this,
                        R.color.text_primary
                );

        if ("EXPENSE".equals(transactionType)) {
            tabExpense.setBackgroundResource(
                    R.drawable.bg_tab_selected
            );

            tabIncome.setBackgroundResource(
                    R.drawable.bg_tab_unselected
            );

            tabExpense.setTextColor(Color.WHITE);
            tabIncome.setTextColor(normalTextColor);

        } else {
            tabIncome.setBackgroundResource(
                    R.drawable.bg_tab_selected
            );

            tabExpense.setBackgroundResource(
                    R.drawable.bg_tab_unselected
            );

            tabIncome.setTextColor(Color.WHITE);
            tabExpense.setTextColor(normalTextColor);
        }
    }

    private void updateCategoryUI() {
        tvCategory.setText(categoryName);

        tvCategory.setTextColor(
                ContextCompat.getColor(
                        this,
                        R.color.text_primary
                )
        );

        int iconResource =
                getResources().getIdentifier(
                        iconName,
                        "drawable",
                        getPackageName()
                );

        if (iconResource == 0) {
            iconResource = R.drawable.ic_dot;
        }

        imgCategory.setImageResource(iconResource);
        imgCategory.setColorFilter(Color.WHITE);

        GradientDrawable background =
                new GradientDrawable();

        background.setShape(
                GradientDrawable.OVAL
        );

        try {
            background.setColor(
                    Color.parseColor(colorCode)
            );
        } catch (Exception exception) {
            background.setColor(
                    Color.parseColor("#ADB5BD")
            );
        }

        imgCategory.setBackground(background);
    }

    private void setDefaultCategoryByType() {
        if ("EXPENSE".equals(transactionType)) {
            categoryName = "Ăn uống";
            iconName = "ic_food";
            colorCode = "#FF3131";
        } else {
            categoryName = "Lương";
            iconName = "ic_salary";
            colorCode = "#2ECC71";
        }
    }

    private void showCategoryDialog() {
        if ("EXPENSE".equals(transactionType)) {
            showExpenseCategoryDialog();
        } else {
            showIncomeCategoryDialog();
        }
    }

    private void showExpenseCategoryDialog() {
        String[] categories = {
                "Ăn uống",
                "Đi lại",
                "Mua sắm",
                "Giải trí",
                "Hóa đơn",
                "Sức khỏe",
                "Khác"
        };

        new AlertDialog.Builder(this)
                .setTitle("Chọn danh mục chi tiêu")
                .setItems(categories, (dialog, which) -> {
                    categoryName = categories[which];

                    if ("Khác".equals(categoryName)) {
                        iconName = "ic_dot";
                        colorCode = "#ADB5BD";

                        edtOtherCategory.setVisibility(
                                View.VISIBLE
                        );

                        edtOtherCategory.requestFocus();
                    } else {
                        edtOtherCategory.setVisibility(
                                View.GONE
                        );

                        edtOtherCategory.setText("");

                        switch (categoryName) {
                            case "Ăn uống":
                                iconName = "ic_food";
                                colorCode = "#FF3131";
                                break;

                            case "Đi lại":
                                iconName = "ic_bus";
                                colorCode = "#2196F3";
                                break;

                            case "Mua sắm":
                                iconName = "ic_shopping";
                                colorCode = "#FF9800";
                                break;

                            case "Giải trí":
                                iconName = "ic_default";
                                colorCode = "#9C27B0";
                                break;

                            case "Hóa đơn":
                                iconName = "ic_bill";
                                colorCode = "#FF9800";
                                break;

                            case "Sức khỏe":
                                iconName = "ic_heart";
                                colorCode = "#FFB3C6";
                                break;
                        }
                    }

                    updateCategoryUI();
                })
                .show();
    }

    private void showIncomeCategoryDialog() {
        String[] categories = {
                "Lương",
                "Thưởng",
                "Làm thêm",
                "Đầu tư",
                "Bán hàng",
                "Được tặng",
                "Khác"
        };

        new AlertDialog.Builder(this)
                .setTitle("Chọn danh mục thu nhập")
                .setItems(categories, (dialog, which) -> {
                    categoryName = categories[which];

                    if ("Khác".equals(categoryName)) {
                        iconName = "ic_dot";
                        colorCode = "#ADB5BD";

                        edtOtherCategory.setVisibility(
                                View.VISIBLE
                        );

                        edtOtherCategory.requestFocus();
                    } else {
                        edtOtherCategory.setVisibility(
                                View.GONE
                        );

                        edtOtherCategory.setText("");

                        switch (categoryName) {
                            case "Lương":
                                iconName = "ic_salary";
                                colorCode = "#2ECC71";
                                break;

                            case "Thưởng":
                                iconName = "ic_reward";
                                colorCode = "#FB8500";
                                break;

                            case "Làm thêm":
                                iconName = "ic_work";
                                colorCode = "#A2D2FF";
                                break;

                            case "Đầu tư":
                                iconName = "ic_invest";
                                colorCode = "#2A9D8F";
                                break;

                            case "Bán hàng":
                                iconName = "ic_sell";
                                colorCode = "#9D4EDD";
                                break;

                            case "Được tặng":
                                iconName = "ic_donate";
                                colorCode = "#9D6B53";
                                break;
                        }
                    }

                    updateCategoryUI();
                })
                .show();
    }

    private void showWalletDialog() {
        ArrayList<String> walletList =
                new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = dbHelper.getAllWallets();

            while (cursor.moveToNext()) {
                walletList.add(
                        cursor.getString(0)
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (walletList.isEmpty()) {
            walletList.add("Ví mặc định");
        }

        String[] wallets =
                walletList.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Chọn ví")
                .setItems(wallets, (dialog, which) -> {
                    selectedWallet = wallets[which];
                    tvWallet.setText(selectedWallet);

                    tvWallet.setTextColor(
                            ContextCompat.getColor(
                                    this,
                                    R.color.text_primary
                            )
                    );
                })
                .show();
    }

    private void showDatePicker() {
        Calendar calendar =
                Calendar.getInstance();

        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            String selectedDate =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year
                                    );

                            edtDate.setText(selectedDate);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        dialog.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View currentView = getCurrentFocus();

            if (currentView != null) {
                hideKeyboard();
                currentView.clearFocus();
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        View currentView = getCurrentFocus();

        if (currentView != null &&
                inputMethodManager != null) {

            inputMethodManager.hideSoftInputFromWindow(
                    currentView.getWindowToken(),
                    0
            );
        }
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

    private int dp(int value) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}