package com.quanlychitieu.doan.choosetransaction;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Calendar;

public class ChooseTransactionActivity extends AppCompatActivity {

    private LinearLayout layoutCategory;
    private TextView tvCategoryName, tvWallet;
    private TextView tabChiTieu, tabThuNhap;
    private ImageView imgCategoryIcon;
    private EditText edtAmount, edtDate, edtNote, edtOtherCategory;
    private Button btnSave;

    private DatabaseHelper databaseHelper;

    private String transactionType = "EXPENSE";
    private String selectedWallet = "Ví mặc định";

    private String selectedIcon = "ic_food";
    private String selectedColor = "#FF3131";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_transaction);

        setupSafeArea();

        databaseHelper = new DatabaseHelper(this);

        layoutCategory = findViewById(R.id.layoutCategory);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        imgCategoryIcon = findViewById(R.id.imgCategoryIcon);

        tabChiTieu = findViewById(R.id.tabChiTieu);
        tabThuNhap = findViewById(R.id.tabThuNhap);

        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtNote = findViewById(R.id.edtNote);
        edtOtherCategory = findViewById(R.id.edtOtherCategory);

        tvWallet = findViewById(R.id.tvWallet);
        tvWallet.setText(selectedWallet);

        btnSave = findViewById(R.id.btnSave);

        ImageView imgback = findViewById(R.id.imgback);
        imgback.setOnClickListener(v -> finish());

        setCurrentDate();
        setExpenseMode();

        tabChiTieu.setOnClickListener(v -> setExpenseMode());
        tabThuNhap.setOnClickListener(v -> setIncomeMode());

        layoutCategory.setOnClickListener(v -> showCategoryDialog());
        edtDate.setOnClickListener(v -> showDatePicker());
        tvWallet.setOnClickListener(v -> showWalletDialog());
        btnSave.setOnClickListener(v -> saveTransaction());

        edtAmount.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            return false;
        });

        edtNote.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            return false;
        });

        edtOtherCategory.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            return false;
        });
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hideKeyboard();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(
                    getCurrentFocus().getWindowToken(),
                    0
            );
            getCurrentFocus().clearFocus();
        }
    }

    private void setExpenseMode() {
        transactionType = "EXPENSE";

        tabChiTieu.setBackgroundResource(R.drawable.bg_tab_selected);
        tabThuNhap.setBackgroundResource(R.drawable.bg_tab_unselected);

        tabChiTieu.setTextColor(Color.WHITE);
        tabThuNhap.setTextColor(Color.parseColor("#111827"));

        tvCategoryName.setText("Ăn uống");
        imgCategoryIcon.setImageResource(R.drawable.ic_food);
        setIconBackgroundColor("#FF3131");

        selectedIcon = "ic_food";
        selectedColor = "#FF3131";

        edtOtherCategory.setVisibility(View.GONE);
        edtOtherCategory.setText("");
    }

    private void setIncomeMode() {
        transactionType = "INCOME";

        tabThuNhap.setBackgroundResource(R.drawable.bg_tab_selected);
        tabChiTieu.setBackgroundResource(R.drawable.bg_tab_unselected);

        tabThuNhap.setTextColor(Color.WHITE);
        tabChiTieu.setTextColor(Color.parseColor("#111827"));

        tvCategoryName.setText("Lương");
        imgCategoryIcon.setImageResource(R.drawable.ic_salary);
        setIconBackgroundColor("#2ECC71");

        selectedIcon = "ic_salary";
        selectedColor = "#2ECC71";

        edtOtherCategory.setVisibility(View.GONE);
        edtOtherCategory.setText("");
    }

    private void setIconBackgroundColor(String color) {
        GradientDrawable bg = (GradientDrawable) imgCategoryIcon.getBackground();
        bg.setColor(Color.parseColor(color));
    }

    private void setCurrentDate() {
        String currentDate = new SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
        ).format(new Date());

        edtDate.setText(currentDate);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(
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

        datePickerDialog.show();
    }

    private void showWalletDialog() {
        Cursor cursor = databaseHelper.getAllWallets();

        ArrayList<String> walletList = new ArrayList<>();

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

    private void showCategoryDialog() {
        String[] categories;

        if (transactionType.equals("INCOME")) {
            categories = new String[]{
                    "Lương",
                    "Thưởng",
                    "Làm thêm",
                    "Đầu tư",
                    "Bán hàng",
                    "Được tặng",
                    "Khác"
            };
        } else {
            categories = new String[]{
                    "Ăn uống",
                    "Đi lại",
                    "Mua sắm",
                    "Giải trí",
                    "Hóa đơn",
                    "Sức khỏe",
                    "Khác"
            };
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn danh mục");

        builder.setItems(categories, (dialog, which) -> {
            String selectedCategory = categories[which];
            tvCategoryName.setText(selectedCategory);

            if (selectedCategory.equals("Khác")) {
                edtOtherCategory.setVisibility(View.VISIBLE);
            } else {
                edtOtherCategory.setVisibility(View.GONE);
                edtOtherCategory.setText("");
            }

            if (transactionType.equals("INCOME")) {
                setIncomeCategoryIcon(which);
            } else {
                setExpenseCategoryIcon(which);
            }
        });

        builder.show();
    }

    private void setExpenseCategoryIcon(int which) {
        switch (which) {
            case 0:
                imgCategoryIcon.setImageResource(R.drawable.ic_food);
                setIconBackgroundColor("#FF3131");
                selectedIcon = "ic_food";
                selectedColor = "#FF3131";
                break;
            case 1:
                imgCategoryIcon.setImageResource(R.drawable.ic_bus);
                setIconBackgroundColor("#2196F3");
                selectedIcon = "ic_bus";
                selectedColor = "#2196F3";
                break;
            case 2:
                imgCategoryIcon.setImageResource(R.drawable.ic_shopping);
                setIconBackgroundColor("#FF9800");
                selectedIcon = "ic_shopping";
                selectedColor = "#FF9800";
                break;
            case 3:
                imgCategoryIcon.setImageResource(R.drawable.ic_default);
                setIconBackgroundColor("#9C27B0");
                selectedIcon = "ic_default";
                selectedColor = "#9C27B0";
                break;
            case 4:
                imgCategoryIcon.setImageResource(R.drawable.ic_bill);
                setIconBackgroundColor("#FF9800");
                selectedIcon = "ic_bill";
                selectedColor = "#FF9800";
                break;
            case 5:
                imgCategoryIcon.setImageResource(R.drawable.ic_heart);
                setIconBackgroundColor("#FFB3C6");
                selectedIcon = "ic_heart";
                selectedColor = "#FFB3C6";
                break;
            default:
                imgCategoryIcon.setImageResource(R.drawable.ic_dot);
                setIconBackgroundColor("#ADB5BD");
                selectedIcon = "ic_dot";
                selectedColor = "#ADB5BD";
                break;
        }
    }

    private void setIncomeCategoryIcon(int which) {
        switch (which) {
            case 0:
                imgCategoryIcon.setImageResource(R.drawable.ic_salary);
                setIconBackgroundColor("#2ECC71");
                selectedIcon = "ic_salary";
                selectedColor = "#2ECC71";
                break;
            case 1:
                imgCategoryIcon.setImageResource(R.drawable.ic_reward);
                setIconBackgroundColor("#FB8500");
                selectedIcon = "ic_reward";
                selectedColor = "#FB8500";
                break;
            case 2:
                imgCategoryIcon.setImageResource(R.drawable.ic_work);
                setIconBackgroundColor("#A2D2FF");
                selectedIcon = "ic_work";
                selectedColor = "#A2D2FF";
                break;
            case 3:
                imgCategoryIcon.setImageResource(R.drawable.ic_invest);
                setIconBackgroundColor("#2A9D8F");
                selectedIcon = "ic_invest";
                selectedColor = "#2A9D8F";
                break;
            case 4:
                imgCategoryIcon.setImageResource(R.drawable.ic_sell);
                setIconBackgroundColor("#9D4EDD");
                selectedIcon = "ic_sell";
                selectedColor = "#9D4EDD";
                break;
            case 5:
                imgCategoryIcon.setImageResource(R.drawable.ic_donate);
                setIconBackgroundColor("#9D6B53");
                selectedIcon = "ic_donate";
                selectedColor = "#9D6B53";
                break;
            default:
                imgCategoryIcon.setImageResource(R.drawable.ic_dot);
                setIconBackgroundColor("#ADB5BD");
                selectedIcon = "ic_dot";
                selectedColor = "#ADB5BD";
                break;
        }
    }

    private void saveTransaction() {
        String amountText = edtAmount.getText().toString().trim();
        String category = tvCategoryName.getText().toString();
        String date = edtDate.getText().toString();
        String wallet = selectedWallet;

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.equals("Khác")) {
            String otherCategory = edtOtherCategory.getText().toString().trim();

            if (otherCategory.isEmpty()) {
                edtOtherCategory.setError("Nhập tên danh mục khác");
                return;
            }

            category = otherCategory;
            selectedIcon = "ic_dot";
            selectedColor = "#ADB5BD";
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

        databaseHelper.insertTransaction(
                category,
                date,
                amount,
                wallet,
                transactionType,
                selectedIcon,
                selectedColor
        );

        Toast.makeText(this, "Lưu giao dịch thành công", Toast.LENGTH_SHORT).show();
        finish();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}