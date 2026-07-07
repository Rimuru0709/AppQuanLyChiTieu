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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {

    private ImageView imgBack, imgCategory;
    private TextView tabExpense, tabIncome, tvWallet, tvCategory;
    private LinearLayout layoutCategory;
    private EditText edtAmount, edtDate, edtOtherCategory;
    private Button btnSave, btnDelete;

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

        setupSafeArea();

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        transactionId = getIntent().getIntExtra("transactionId", -1);

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

        edtOtherCategory.setVisibility(View.GONE);

        edtAmount.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edtAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                edtAmount.clearFocus();
                return true;
            }
            return false;
        });

        imgBack.setOnClickListener(v -> finish());
        edtDate.setOnClickListener(v -> showDatePicker());
        tvWallet.setOnClickListener(v -> showWalletDialog());
        layoutCategory.setOnClickListener(v -> showCategoryDialog());

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

        btnSave.setOnClickListener(v -> updateTransaction());
        btnDelete.setOnClickListener(v -> confirmDelete());

        if (transactionId != -1) {
            loadTransactionDetail();
        } else {
            Toast.makeText(this, "Không tìm thấy giao dịch", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(dp(24), bars.top + dp(12), dp(24), dp(30));

            return insets;
        });
    }

    private void loadTransactionDetail() {
        Cursor cursor = database.rawQuery(
                "SELECT title, date, amount, wallet, type, icon, color FROM transactions WHERE id=?",
                new String[]{String.valueOf(transactionId)}
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
            edtAmount.setText(String.valueOf(Math.abs(amount)));
            tvWallet.setText(selectedWallet);

            updateTabUI();
            updateCategoryUI();
        } else {
            Toast.makeText(this, "Không tìm thấy giao dịch", Toast.LENGTH_SHORT).show();
            finish();
        }

        cursor.close();
    }

    private void updateTransaction() {
        String amountText = edtAmount.getText().toString().trim();
        String date = edtDate.getText().toString().trim();

        if (amountText.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryName.equals("Khác")) {
            String otherCategory = edtOtherCategory.getText().toString().trim();

            if (otherCategory.isEmpty()) {
                edtOtherCategory.setError("Nhập tên danh mục khác");
                return;
            }

            categoryName = otherCategory;
            iconName = "ic_dot";
            colorCode = "#ADB5BD";
        }

        int amount;

        try {
            amount = Integer.parseInt(amountText);
        } catch (Exception e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amount <= 0) {
            Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(this, "Đã cập nhật giao dịch", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có chắc muốn xóa giao dịch này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteTransaction(transactionId);
                    Toast.makeText(this, "Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateTabUI() {
        if ("EXPENSE".equals(transactionType)) {
            tabExpense.setBackgroundResource(R.drawable.bg_tab_selected);
            tabIncome.setBackgroundResource(R.drawable.bg_tab_unselected);

            tabExpense.setTextColor(Color.WHITE);
            tabIncome.setTextColor(Color.parseColor("#111827"));
        } else {
            tabIncome.setBackgroundResource(R.drawable.bg_tab_selected);
            tabExpense.setBackgroundResource(R.drawable.bg_tab_unselected);

            tabIncome.setTextColor(Color.WHITE);
            tabExpense.setTextColor(Color.parseColor("#111827"));
        }
    }

    private void updateCategoryUI() {
        tvCategory.setText(categoryName);

        int iconRes = getResources().getIdentifier(iconName, "drawable", getPackageName());

        if (iconRes == 0) {
            iconRes = R.drawable.ic_dot;
        }

        imgCategory.setImageResource(iconRes);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);

        try {
            bg.setColor(Color.parseColor(colorCode));
        } catch (Exception e) {
            bg.setColor(Color.parseColor("#ADB5BD"));
        }

        imgCategory.setBackground(bg);
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
                "Ăn uống", "Đi lại", "Mua sắm", "Giải trí", "Hóa đơn", "Sức khỏe", "Khác"
        };

        new AlertDialog.Builder(this)
                .setTitle("Chọn danh mục chi tiêu")
                .setItems(categories, (dialog, which) -> {
                    categoryName = categories[which];

                    if (categoryName.equals("Khác")) {
                        iconName = "ic_dot";
                        colorCode = "#ADB5BD";
                        edtOtherCategory.setVisibility(View.VISIBLE);
                        edtOtherCategory.requestFocus();
                    } else {
                        edtOtherCategory.setVisibility(View.GONE);
                        edtOtherCategory.setText("");

                        if (categoryName.equals("Ăn uống")) {
                            iconName = "ic_food";
                            colorCode = "#FF3131";
                        } else if (categoryName.equals("Đi lại")) {
                            iconName = "ic_bus";
                            colorCode = "#2196F3";
                        } else if (categoryName.equals("Mua sắm")) {
                            iconName = "ic_shopping";
                            colorCode = "#FF9800";
                        } else if (categoryName.equals("Giải trí")) {
                            iconName = "ic_default";
                            colorCode = "#9C27B0";
                        } else if (categoryName.equals("Hóa đơn")) {
                            iconName = "ic_bill";
                            colorCode = "#FF9800";
                        } else if (categoryName.equals("Sức khỏe")) {
                            iconName = "ic_heart";
                            colorCode = "#FFB3C6";
                        }
                    }

                    updateCategoryUI();
                })
                .show();
    }

    private void showIncomeCategoryDialog() {
        String[] categories = {
                "Lương", "Thưởng", "Làm thêm", "Đầu tư", "Bán hàng", "Được tặng", "Khác"
        };

        new AlertDialog.Builder(this)
                .setTitle("Chọn danh mục thu nhập")
                .setItems(categories, (dialog, which) -> {
                    categoryName = categories[which];

                    if (categoryName.equals("Khác")) {
                        iconName = "ic_dot";
                        colorCode = "#ADB5BD";
                        edtOtherCategory.setVisibility(View.VISIBLE);
                        edtOtherCategory.requestFocus();
                    } else {
                        edtOtherCategory.setVisibility(View.GONE);
                        edtOtherCategory.setText("");

                        if (categoryName.equals("Lương")) {
                            iconName = "ic_salary";
                            colorCode = "#2ECC71";
                        } else if (categoryName.equals("Thưởng")) {
                            iconName = "ic_reward";
                            colorCode = "#FB8500";
                        } else if (categoryName.equals("Làm thêm")) {
                            iconName = "ic_work";
                            colorCode = "#A2D2FF";
                        } else if (categoryName.equals("Đầu tư")) {
                            iconName = "ic_invest";
                            colorCode = "#2A9D8F";
                        } else if (categoryName.equals("Bán hàng")) {
                            iconName = "ic_sell";
                            colorCode = "#9D4EDD";
                        } else if (categoryName.equals("Được tặng")) {
                            iconName = "ic_donate";
                            colorCode = "#9D6B53";
                        }
                    }

                    updateCategoryUI();
                })
                .show();
    }

    private void showWalletDialog() {
        ArrayList<String> walletList = new ArrayList<>();

        Cursor cursor = dbHelper.getAllWallets();

        while (cursor.moveToNext()) {
            walletList.add(cursor.getString(0));
        }

        cursor.close();

        if (walletList.isEmpty()) {
            walletList.add("Ví mặc định");
        }

        String[] wallets = walletList.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Chọn ví")
                .setItems(wallets, (dialog, which) -> {
                    selectedWallet = wallets[which];
                    tvWallet.setText(selectedWallet);
                })
                .show();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                    );
                    edtDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();

            if (view != null) {
                hideKeyboard();
                view.clearFocus();
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View view = getCurrentFocus();

        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}