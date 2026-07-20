package com.quanlychitieu.doan.transfer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TransferActivity extends AppCompatActivity {

    private ImageView imgBack;

    private LinearLayout layoutFromWallet;
    private LinearLayout layoutToWallet;

    private TextView tvFromWalletName;
    private TextView tvFromWalletBalance;
    private TextView tvToWalletName;
    private TextView tvToWalletBalance;

    private EditText edtAmount;
    private EditText edtDate;
    private EditText edtNote;

    private Button btnTransfer;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private final ArrayList<String> walletList = new ArrayList<>();

    private String selectedFromWallet = "";
    private String selectedToWallet = "";

    private int fromWalletBalance = 0;
    private int toWalletBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        setupSafeArea();
        BottomNavHelper.setup(this);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        bindViews();
        setupKeyboard();
        setupClickEvents();

        edtDate.setText(getToday());

        loadWalletList();
        setDefaultWallets();
        updateWalletInformation();
    }

    private void bindViews() {
        imgBack = findViewById(R.id.imgBack);

        layoutFromWallet = findViewById(R.id.layoutFromWallet);
        layoutToWallet = findViewById(R.id.layoutToWallet);

        tvFromWalletName = findViewById(R.id.tvFromWalletName);
        tvFromWalletBalance = findViewById(R.id.tvFromWalletBalance);

        tvToWalletName = findViewById(R.id.tvToWalletName);
        tvToWalletBalance = findViewById(R.id.tvToWalletBalance);

        edtAmount = findViewById(R.id.edtAmount);
        edtDate = findViewById(R.id.edtDate);
        edtNote = findViewById(R.id.edtNote);

        btnTransfer = findViewById(R.id.btnTransfer);
    }

    private void setupClickEvents() {
        imgBack.setOnClickListener(v -> finish());

        layoutFromWallet.setOnClickListener(v ->
                showWalletDialog(true)
        );

        layoutToWallet.setOnClickListener(v ->
                showWalletDialog(false)
        );

        edtDate.setOnClickListener(v -> showDatePicker());

        btnTransfer.setOnClickListener(v -> performTransfer());
    }

    private void setupKeyboard() {
        edtAmount.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edtNote.setImeOptions(EditorInfo.IME_ACTION_DONE);

        edtAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                edtAmount.clearFocus();
                return true;
            }

            return false;
        });

        edtNote.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                edtNote.clearFocus();
                return true;
            }

            return false;
        });
    }

    private void setupSafeArea() {
        View content = findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            v.setPadding(
                    dp(22),
                    bars.top + dp(12),
                    dp(22),
                    dp(30)
            );

            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        database = dbHelper.getWritableDatabase();

        loadWalletList();

        if (selectedFromWallet.isEmpty()
                || !walletList.contains(selectedFromWallet)) {
            setDefaultWallets();
        }

        if (!selectedToWallet.isEmpty()
                && !walletList.contains(selectedToWallet)) {
            selectedToWallet = "";
        }

        updateWalletInformation();
    }

    private void loadWalletList() {
        walletList.clear();

        Cursor cursor = dbHelper.getAllWallets();

        while (cursor.moveToNext()) {
            String walletName = cursor.getString(0);

            if (walletName != null && !walletName.trim().isEmpty()) {
                walletList.add(walletName);
            }
        }

        cursor.close();
    }

    private void setDefaultWallets() {
        if (walletList.isEmpty()) {
            selectedFromWallet = "";
            selectedToWallet = "";
            return;
        }

        selectedFromWallet = walletList.get(0);

        if (walletList.size() >= 2) {
            selectedToWallet = walletList.get(1);
        } else {
            selectedToWallet = "";
        }
    }

    private void showWalletDialog(boolean chooseFromWallet) {
        if (walletList.isEmpty()) {
            Toast.makeText(
                    this,
                    "Chưa có ví nào để lựa chọn",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ArrayList<String> selectableWallets = new ArrayList<>();

        for (String wallet : walletList) {
            if (chooseFromWallet) {
                if (!wallet.equals(selectedToWallet)) {
                    selectableWallets.add(wallet);
                }
            } else {
                if (!wallet.equals(selectedFromWallet)) {
                    selectableWallets.add(wallet);
                }
            }
        }

        if (selectableWallets.isEmpty()) {
            Toast.makeText(
                    this,
                    "Cần có ít nhất hai ví khác nhau để chuyển khoản",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String[] wallets = selectableWallets.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle(
                        chooseFromWallet
                                ? "Chọn ví nguồn"
                                : "Chọn ví nhận"
                )
                .setItems(wallets, (dialog, which) -> {
                    if (chooseFromWallet) {
                        selectedFromWallet = wallets[which];

                        if (selectedFromWallet.equals(selectedToWallet)) {
                            selectedToWallet = "";
                        }
                    } else {
                        selectedToWallet = wallets[which];
                    }

                    updateWalletInformation();
                })
                .show();
    }

    private void updateWalletInformation() {
        if (selectedFromWallet.isEmpty()) {
            tvFromWalletName.setText("Chọn ví nguồn");
            tvFromWalletBalance.setText("Số dư: 0 đ");
            fromWalletBalance = 0;
        } else {
            fromWalletBalance = getWalletBalance(selectedFromWallet);

            tvFromWalletName.setText(selectedFromWallet);
            tvFromWalletBalance.setText(
                    "Số dư: " + formatMoney(fromWalletBalance)
            );
        }

        if (selectedToWallet.isEmpty()) {
            tvToWalletName.setText("Chọn ví nhận");
            tvToWalletBalance.setText("Số dư: 0 đ");
            toWalletBalance = 0;
        } else {
            toWalletBalance = getWalletBalance(selectedToWallet);

            tvToWalletName.setText(selectedToWallet);
            tvToWalletBalance.setText(
                    "Số dư: " + formatMoney(toWalletBalance)
            );
        }
    }

    private int getWalletBalance(String walletName) {
        if (walletName == null || walletName.trim().isEmpty()) {
            return 0;
        }

        int balance = 0;

        Cursor cursor = database.rawQuery(
                "SELECT COALESCE(SUM(CASE " +
                        "WHEN type='INCOME' THEN ABS(amount) " +
                        "WHEN type='EXPENSE' THEN -ABS(amount) " +
                        "ELSE 0 END), 0) " +
                        "FROM transactions WHERE wallet=?",
                new String[]{walletName}
        );

        if (cursor.moveToFirst()) {
            balance = cursor.getInt(0);
        }

        cursor.close();

        return balance;
    }

    private void performTransfer() {
        hideKeyboard();

        String amountText = edtAmount.getText()
                .toString()
                .trim();

        String date = edtDate.getText()
                .toString()
                .trim();

        String note = edtNote.getText()
                .toString()
                .trim();

        if (selectedFromWallet.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng chọn ví nguồn",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedToWallet.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng chọn ví nhận",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (selectedFromWallet.equals(selectedToWallet)) {
            Toast.makeText(
                    this,
                    "Ví nguồn và ví nhận không được giống nhau",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (amountText.isEmpty()) {
            edtAmount.setError("Vui lòng nhập số tiền");
            edtAmount.requestFocus();
            return;
        }

        int amount;

        try {
            amount = Integer.parseInt(amountText);
        } catch (NumberFormatException e) {
            edtAmount.setError("Số tiền không hợp lệ");
            edtAmount.requestFocus();
            return;
        }

        if (amount <= 0) {
            edtAmount.setError("Số tiền phải lớn hơn 0");
            edtAmount.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(
                    this,
                    "Vui lòng chọn ngày chuyển khoản",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        fromWalletBalance = getWalletBalance(selectedFromWallet);

        if (fromWalletBalance < amount) {
            Toast.makeText(
                    this,
                    "Ví nguồn không đủ số dư",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (note.isEmpty()) {
            note = "Chuyển từ "
                    + selectedFromWallet
                    + " đến "
                    + selectedToWallet;
        }

        showTransferConfirmation(amount, date, note);
    }

    private void showTransferConfirmation(
            int amount,
            String date,
            String note
    ) {
        String message =
                "Từ ví: " + selectedFromWallet +
                        "\nĐến ví: " + selectedToWallet +
                        "\nSố tiền: " + formatMoney(amount) +
                        "\nNgày: " + date +
                        "\nNội dung: " + note;

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận chuyển khoản")
                .setMessage(message)
                .setPositiveButton("Chuyển", (dialog, which) ->
                        saveTransfer(amount, date, note)
                )
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveTransfer(
            int amount,
            String date,
            String note
    ) {
        database = dbHelper.getWritableDatabase();
        database.beginTransaction();

        try {
            ContentValues expenseValues = new ContentValues();
            expenseValues.put(
                    "title",
                    "Chuyển khoản đến " + selectedToWallet
            );
            expenseValues.put("date", date);
            expenseValues.put("amount", -Math.abs(amount));
            expenseValues.put("wallet", selectedFromWallet);
            expenseValues.put("type", "EXPENSE");
            expenseValues.put("icon", "ic_transfer");
            expenseValues.put("color", "#00A6C7");

            long expenseResult = database.insert(
                    DatabaseHelper.TABLE_TRANSACTION,
                    null,
                    expenseValues
            );

            ContentValues incomeValues = new ContentValues();
            incomeValues.put(
                    "title",
                    "Nhận chuyển khoản từ " + selectedFromWallet
            );
            incomeValues.put("date", date);
            incomeValues.put("amount", Math.abs(amount));
            incomeValues.put("wallet", selectedToWallet);
            incomeValues.put("type", "INCOME");
            incomeValues.put("icon", "ic_transfer");
            incomeValues.put("color", "#00A6C7");

            long incomeResult = database.insert(
                    DatabaseHelper.TABLE_TRANSACTION,
                    null,
                    incomeValues
            );

            if (expenseResult == -1 || incomeResult == -1) {
                throw new IllegalStateException(
                        "Không thể lưu giao dịch chuyển khoản"
                );
            }

            database.setTransactionSuccessful();

            Toast.makeText(
                    this,
                    "Chuyển khoản thành công",
                    Toast.LENGTH_SHORT
            ).show();

            clearTransferForm();
            updateWalletInformation();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "Chuyển khoản thất bại",
                    Toast.LENGTH_SHORT
            ).show();
        } finally {
            database.endTransaction();
        }
    }

    private void clearTransferForm() {
        edtAmount.setText("");
        edtNote.setText("");
        edtDate.setText(getToday());
        edtAmount.clearFocus();
        edtNote.clearFocus();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
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

        dialog.show();
    }

    private String getToday() {
        Calendar calendar = Calendar.getInstance();

        return String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
        );
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

        if (currentView != null && inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    currentView.getWindowToken(),
                    0
            );
        }
    }

    private String formatMoney(int money) {
        DecimalFormat formatter = new DecimalFormat("#,###");

        return formatter.format(money)
                .replace(",", ".") + " đ";
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}