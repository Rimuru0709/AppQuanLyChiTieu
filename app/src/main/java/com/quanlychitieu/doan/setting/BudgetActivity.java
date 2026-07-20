package com.quanlychitieu.doan.setting;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetActivity extends AppCompatActivity {

    private ImageView imgBack;

    private AutoCompleteTextView edtCategory;
    private AutoCompleteTextView edtWarningPercent;

    private TextInputEditText edtBudgetAmount;
    private TextInputEditText edtMonth;

    private TextView tvCurrentBudget;

    private MaterialButton btnSaveBudget;
    private MaterialButton btnDeleteBudget;

    private DatabaseHelper dbHelper;

    private int selectedMonth;
    private int selectedYear;

    private String selectedCategory = "";
    private int selectedWarningPercent = 80;

    private final String[] expenseCategories = {
            "Ăn uống",
            "Đi lại",
            "Mua sắm",
            "Giải trí",
            "Hóa đơn",
            "Sức khỏe",
            "Khác"
    };

    private final String[] warningOptions = {
            "70%",
            "80%",
            "90%"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupSafeArea();
        setupCurrentMonth();
        setupDropdowns();
        setupEvents();

        updateMonthText();
        updateCurrentBudgetInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateCurrentBudgetInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

        edtCategory =
                findViewById(R.id.edtCategory);

        edtBudgetAmount =
                findViewById(R.id.edtBudgetAmount);

        edtMonth =
                findViewById(R.id.edtMonth);

        edtWarningPercent =
                findViewById(R.id.edtWarningPercent);

        tvCurrentBudget =
                findViewById(R.id.tvCurrentBudget);

        btnSaveBudget =
                findViewById(R.id.btnSaveBudget);

        btnDeleteBudget =
                findViewById(R.id.btnDeleteBudget);
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
                            dp(22),
                            systemBars.top + dp(8),
                            dp(22),
                            systemBars.bottom + dp(28)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    // =========================================================
    // THÁNG HIỆN TẠI
    // =========================================================

    private void setupCurrentMonth() {
        Calendar calendar =
                Calendar.getInstance();

        selectedMonth =
                calendar.get(Calendar.MONTH) + 1;

        selectedYear =
                calendar.get(Calendar.YEAR);
    }

    private void updateMonthText() {
        if (edtMonth == null) {
            return;
        }

        edtMonth.setText(
                getSelectedMonthText()
        );
    }

    private String getSelectedMonthText() {
        return String.format(
                Locale.getDefault(),
                "%02d/%04d",
                selectedMonth,
                selectedYear
        );
    }

    // =========================================================
    // DROPDOWN
    // =========================================================

    private void setupDropdowns() {
        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        expenseCategories
                );

        edtCategory.setAdapter(
                categoryAdapter
        );

        edtCategory.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedCategory =
                            expenseCategories[position];

                    loadSelectedBudget();
                }
        );

        ArrayAdapter<String> warningAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        warningOptions
                );

        edtWarningPercent.setAdapter(
                warningAdapter
        );

        edtWarningPercent.setText(
                "80%",
                false
        );

        edtWarningPercent.setOnItemClickListener(
                (parent, view, position, id) -> {

                    selectedWarningPercent =
                            parseWarningPercent(
                                    warningOptions[position]
                            );
                }
        );
    }

    // =========================================================
    // SỰ KIỆN
    // =========================================================

    private void setupEvents() {
        if (imgBack != null) {
            imgBack.setOnClickListener(
                    view -> finish()
            );
        }

        if (edtMonth != null) {
            edtMonth.setOnClickListener(
                    view -> showMonthPicker()
            );
        }

        if (btnSaveBudget != null) {
            btnSaveBudget.setOnClickListener(
                    view -> saveBudget()
            );
        }

        if (btnDeleteBudget != null) {
            btnDeleteBudget.setOnClickListener(
                    view -> confirmDeleteBudget()
            );
        }
    }

    // =========================================================
    // CHỌN THÁNG
    // =========================================================

    private void showMonthPicker() {
        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            selectedMonth =
                                    month + 1;

                            selectedYear =
                                    year;

                            updateMonthText();
                            loadSelectedBudget();
                        },
                        selectedYear,
                        selectedMonth - 1,
                        1
                );

        dialog.show();
    }

    // =========================================================
    // LƯU NGÂN SÁCH
    // =========================================================

    private void saveBudget() {
        String category =
                edtCategory.getText()
                        .toString()
                        .trim();

        String amountText =
                edtBudgetAmount.getText() == null
                        ? ""
                        : edtBudgetAmount
                          .getText()
                          .toString()
                          .trim();

        String warningText =
                edtWarningPercent.getText()
                        .toString()
                        .trim();

        if (category.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng chọn danh mục",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (amountText.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng nhập số tiền ngân sách",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int amount;

        try {
            amount =
                    Integer.parseInt(
                            amountText
                    );

        } catch (NumberFormatException exception) {
            Toast.makeText(
                    this,
                    "Số tiền ngân sách không hợp lệ",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (amount <= 0) {
            Toast.makeText(
                    this,
                    "Ngân sách phải lớn hơn 0",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int warningPercent =
                parseWarningPercent(
                        warningText
                );

        String month =
                getSelectedMonthText();

        long result =
                dbHelper.saveBudget(
                        category,
                        amount,
                        month
                );

        if (result == -1) {
            Toast.makeText(
                    this,
                    "Không thể lưu ngân sách",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        dbHelper.saveWarningPercent(
                category,
                warningPercent
        );

        selectedCategory =
                category;

        selectedWarningPercent =
                warningPercent;

        Toast.makeText(
                this,
                "Đã lưu ngân sách",
                Toast.LENGTH_SHORT
        ).show();

        loadSelectedBudget();
    }

    // =========================================================
    // TẢI NGÂN SÁCH ĐÃ LƯU
    // =========================================================

    private void loadSelectedBudget() {
        String category =
                edtCategory.getText()
                        .toString()
                        .trim();

        String month =
                getSelectedMonthText();

        if (category.isEmpty()) {
            updateCurrentBudgetInfo();
            return;
        }

        Cursor cursor = null;

        try {
            cursor =
                    dbHelper.getBudget(
                            category,
                            month
                    );

            if (cursor.moveToFirst()) {
                int amountIndex =
                        cursor.getColumnIndexOrThrow(
                                DatabaseHelper.BUDGET_AMOUNT
                        );

                int amount =
                        cursor.getInt(
                                amountIndex
                        );

                int warningPercent =
                        dbHelper.getWarningPercent(
                                category
                        );

                edtBudgetAmount.setText(
                        String.valueOf(amount)
                );

                edtWarningPercent.setText(
                        warningPercent + "%",
                        false
                );

                selectedCategory =
                        category;

                selectedWarningPercent =
                        warningPercent;

                tvCurrentBudget.setText(
                        "Danh mục: "
                                + category
                                + "\nTháng: "
                                + month
                                + "\nNgân sách: "
                                + formatMoney(amount)
                                + "\nNgưỡng cảnh báo: "
                                + warningPercent
                                + "%"
                );

                btnDeleteBudget.setVisibility(
                        View.VISIBLE
                );

            } else {
                edtBudgetAmount.setText("");

                edtWarningPercent.setText(
                        "80%",
                        false
                );

                selectedWarningPercent = 80;

                tvCurrentBudget.setText(
                        "Chưa có ngân sách cho danh mục "
                                + category
                                + " trong tháng "
                                + month
                                + "."
                );

                btnDeleteBudget.setVisibility(
                        View.GONE
                );
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void updateCurrentBudgetInfo() {
        String category =
                edtCategory == null
                        ? ""
                        : edtCategory
                          .getText()
                          .toString()
                          .trim();

        if (category.isEmpty()) {
            if (tvCurrentBudget != null) {
                tvCurrentBudget.setText(
                        "Chọn danh mục và tháng để xem ngân sách hiện tại."
                );
            }

            if (btnDeleteBudget != null) {
                btnDeleteBudget.setVisibility(
                        View.GONE
                );
            }

            return;
        }

        loadSelectedBudget();
    }

    // =========================================================
    // XÓA NGÂN SÁCH
    // =========================================================

    private void confirmDeleteBudget() {
        String category =
                edtCategory.getText()
                        .toString()
                        .trim();

        String month =
                getSelectedMonthText();

        if (category.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa ngân sách")
                .setMessage(
                        "Bạn có chắc muốn xóa ngân sách "
                                + category
                                + " trong tháng "
                                + month
                                + " không?"
                )
                .setPositiveButton(
                        "Xóa",
                        (dialog, which) ->
                                deleteBudget(
                                        category,
                                        month
                                )
                )
                .setNegativeButton(
                        "Hủy",
                        null
                )
                .show();
    }

    private void deleteBudget(
            String category,
            String month
    ) {
        int deletedRows =
                dbHelper.deleteBudget(
                        category,
                        month
                );

        if (deletedRows > 0) {
            edtBudgetAmount.setText("");

            edtWarningPercent.setText(
                    "80%",
                    false
            );

            selectedWarningPercent = 80;

            btnDeleteBudget.setVisibility(
                    View.GONE
            );

            tvCurrentBudget.setText(
                    "Chưa có ngân sách cho danh mục "
                            + category
                            + " trong tháng "
                            + month
                            + "."
            );

            Toast.makeText(
                    this,
                    "Đã xóa ngân sách",
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            Toast.makeText(
                    this,
                    "Không tìm thấy ngân sách để xóa",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // HÀM HỖ TRỢ
    // =========================================================

    private int parseWarningPercent(
            String value
    ) {
        if (value == null) {
            return 80;
        }

        String normalized =
                value.replace(
                        "%",
                        ""
                ).trim();

        try {
            int percent =
                    Integer.parseInt(
                            normalized
                    );

            if (percent == 70
                    || percent == 80
                    || percent == 90) {

                return percent;
            }

        } catch (NumberFormatException ignored) {
        }

        return 80;
    }

    private String formatMoney(
            int amount
    ) {
        DecimalFormat formatter =
                new DecimalFormat("#,###");

        return formatter
                .format(amount)
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