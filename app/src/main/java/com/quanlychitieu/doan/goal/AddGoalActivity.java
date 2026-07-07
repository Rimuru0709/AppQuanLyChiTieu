package com.quanlychitieu.doan.goal;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddGoalActivity extends AppCompatActivity {

    private ImageView imgBack;
    private TextView tvTitle, tvWallet;
    private TextView tvGoalAmount, tvSaved, tvRemain;
    private EditText edtName, edtTargetAmount, edtSavedAmount, edtDeadline;
    private ProgressBar progressGoal;
    private SwitchCompat swAuto;
    private Button btnSave, btnDelete;

    private DatabaseHelper dbHelper;
    private int goalId = -1;
    private String selectedWallet = "Ví mặc định";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        setupSafeArea();

        dbHelper = new DatabaseHelper(this);

        imgBack = findViewById(R.id.imgBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvWallet = findViewById(R.id.tvWallet);

        tvGoalAmount = findViewById(R.id.tvGoalAmount);
        tvSaved = findViewById(R.id.tvSaved);
        tvRemain = findViewById(R.id.tvRemain);
        progressGoal = findViewById(R.id.progressGoal);

        edtName = findViewById(R.id.edtName);
        edtTargetAmount = findViewById(R.id.edtTargetAmount);
        edtSavedAmount = findViewById(R.id.edtSavedAmount);
        edtDeadline = findViewById(R.id.edtDeadline);

        swAuto = findViewById(R.id.swAuto);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        setupKeyboardDone();

        goalId = getIntent().getIntExtra("goalId", -1);

        imgBack.setOnClickListener(v -> finish());
        edtDeadline.setOnClickListener(v -> showDatePicker());
        tvWallet.setOnClickListener(v -> showWalletDialog());

        setupLivePreview();

        if (goalId != -1) {
            tvTitle.setText("Sửa mục tiêu");
            btnDelete.setVisibility(View.VISIBLE);
            loadGoalDetail(goalId);
        } else {
            updatePreview();
        }

        btnSave.setOnClickListener(v -> saveGoal());

        btnDelete.setOnClickListener(v -> {
            dbHelper.deleteGoal(goalId);
            Toast.makeText(this, "Đã xóa mục tiêu", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupKeyboardDone() {
        edtName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edtTargetAmount.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edtSavedAmount.setImeOptions(EditorInfo.IME_ACTION_DONE);

        edtName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                edtName.clearFocus();
                return true;
            }
            return false;
        });

        edtTargetAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                edtTargetAmount.clearFocus();
                return true;
            }
            return false;
        });

        edtSavedAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                edtSavedAmount.clearFocus();
                return true;
            }
            return false;
        });
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    dp(22),
                    bars.top + dp(12),
                    dp(22),
                    dp(26)
            );

            return insets;
        });
    }    private void setupLivePreview() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtTargetAmount.addTextChangedListener(watcher);
        edtSavedAmount.addTextChangedListener(watcher);
    }

    private void updatePreview() {
        int targetAmount = parseInt(edtTargetAmount.getText().toString().trim());
        int savedAmount = parseInt(edtSavedAmount.getText().toString().trim());

        int remain = targetAmount - savedAmount;
        if (remain < 0) remain = 0;

        int percent = 0;
        if (targetAmount > 0) {
            percent = (int) ((savedAmount * 100.0) / targetAmount);
        }
        if (percent > 100) percent = 100;

        tvGoalAmount.setText(formatMoney(targetAmount));
        tvSaved.setText("Đã tiết kiệm: " + formatMoney(savedAmount));
        tvRemain.setText("Còn lại: " + formatMoney(remain));
        progressGoal.setProgress(percent);
    }

    private void loadGoalDetail(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT name, targetAmount, savedAmount, deadline, wallet, autoSave FROM goals WHERE id=?",
                new String[]{String.valueOf(id)}
        );

        if (cursor.moveToFirst()) {
            edtName.setText(cursor.getString(0));
            edtTargetAmount.setText(String.valueOf(cursor.getInt(1)));
            edtSavedAmount.setText(String.valueOf(cursor.getInt(2)));
            edtDeadline.setText(cursor.getString(3));

            selectedWallet = cursor.getString(4);
            tvWallet.setText(selectedWallet);

            swAuto.setChecked(cursor.getInt(5) == 1);
            updatePreview();
        }

        cursor.close();
    }

    private void saveGoal() {
        String name = edtName.getText().toString().trim();
        String targetText = edtTargetAmount.getText().toString().trim();
        String savedText = edtSavedAmount.getText().toString().trim();
        String deadline = edtDeadline.getText().toString().trim();

        if (name.isEmpty() || targetText.isEmpty() || deadline.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int targetAmount = parseInt(targetText);
        int savedAmount = savedText.isEmpty() ? 0 : parseInt(savedText);

        if (targetAmount <= 0) {
            Toast.makeText(this, "Số tiền mục tiêu phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }

        int autoSave = swAuto.isChecked() ? 1 : 0;

        if (goalId == -1) {
            dbHelper.insertGoal(
                    name,
                    targetAmount,
                    savedAmount,
                    deadline,
                    selectedWallet,
                    autoSave
            );

            if (savedAmount > 0) {
                addGoalSavingTransaction(name, savedAmount);
            }

            Toast.makeText(this, "Đã thêm mục tiêu", Toast.LENGTH_SHORT).show();

        } else {
            dbHelper.updateGoal(
                    goalId,
                    name,
                    targetAmount,
                    savedAmount,
                    deadline,
                    selectedWallet,
                    autoSave
            );

            Toast.makeText(this, "Đã cập nhật mục tiêu", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void addGoalSavingTransaction(String goalName, int savedAmount) {
        dbHelper.insertTransaction(
                "Tiết kiệm mục tiêu - " + goalName,
                getToday(),
                -savedAmount,
                selectedWallet,
                "EXPENSE",
                "ic_target",
                "#FF9800"
        );
    }    private String getToday() {
        Calendar calendar = Calendar.getInstance();

        return String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
        );
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
                    edtDeadline.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
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

    private int parseInt(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatMoney(int money) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(money).replace(",", ".") + " đ";
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