package com.quanlychitieu.doan.history;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;
import com.quanlychitieu.doan.database.DatabaseHelper;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class IncomeHistoryActivity extends AppCompatActivity {

    LinearLayout layoutTransactions;
    LinearLayout boxTransactionCount, boxIncome, boxRefund, boxAverage;

    TextView btnMonth, btnMonthTop, btnWallet, btnSort, btnExport;
    EditText edtSearch;
    ImageView imgBack;

    TextView tvTotalIncome, tvTransactionCount, tvIncome, tvRefund, tvAverage;

    DatabaseHelper dbHelper;
    SQLiteDatabase database;

    int totalIncome = 0;
    int transactionCount = 0;
    int refund = 0;

    int selectedMonth;
    int selectedYear;

    String selectedWallet = "Tất cả ví";
    String sortType = "Mới nhất";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_history);

        BottomNavHelper.setup(this);

        imgBack = findViewById(R.id.imgBack);
        layoutTransactions = findViewById(R.id.layoutTransactions);

        boxTransactionCount = findViewById(R.id.boxTransactionCount);
        boxIncome = findViewById(R.id.boxIncome);
        boxRefund = findViewById(R.id.boxRefund);
        boxAverage = findViewById(R.id.boxAverage);

        btnMonth = findViewById(R.id.btnMonth);
        btnMonthTop = findViewById(R.id.btnMonthTop);
        btnWallet = findViewById(R.id.btnWallet);
        btnSort = findViewById(R.id.btnSort);
        btnExport = findViewById(R.id.btnExport);

        edtSearch = findViewById(R.id.edtSearch);

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTransactionCount = findViewById(R.id.tvTransactionCount);
        tvIncome = findViewById(R.id.tvIncome);
        tvRefund = findViewById(R.id.tvRefund);
        tvAverage = findViewById(R.id.tvAverage);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH) + 1;
        selectedYear = calendar.get(Calendar.YEAR);

        updateMonthText();
        setClickEvents();
        loadIncomeHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadIncomeHistory();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            hideKeyboard();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setClickEvents() {
        imgBack.setOnClickListener(v -> finish());

        btnMonth.setOnClickListener(v -> showDatePicker());
        btnMonthTop.setOnClickListener(v -> showDatePicker());
        btnWallet.setOnClickListener(v -> showWalletDialog());
        btnSort.setOnClickListener(v -> showSortDialog());
        btnExport.setOnClickListener(v -> showExportDialog());

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            loadIncomeHistory();
            return false;
        });

        boxTransactionCount.setOnClickListener(v ->
                Toast.makeText(this, "Số giao dịch: " + transactionCount, Toast.LENGTH_SHORT).show()
        );

        boxIncome.setOnClickListener(v ->
                Toast.makeText(this, "Tổng thu: " + formatMoney(totalIncome), Toast.LENGTH_SHORT).show()
        );

        boxRefund.setOnClickListener(v ->
                Toast.makeText(this, "Hoàn tiền: " + formatMoney(refund), Toast.LENGTH_SHORT).show()
        );

        boxAverage.setOnClickListener(v ->
                Toast.makeText(this, "Trung bình: " + tvAverage.getText().toString(), Toast.LENGTH_SHORT).show()
        );
    }

    private void showExportDialog() {
        String[] options = {"Xuất PDF", "Xuất Excel (.xlsx)"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn định dạng báo cáo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        exportPdf();
                    } else {
                        exportExcel();
                    }
                })
                .show();
    }

    private void showSortDialog() {
        String[] options = {
                "Mới nhất",
                "Cũ nhất",
                "Số tiền cao nhất",
                "Số tiền thấp nhất"
        };

        new AlertDialog.Builder(this)
                .setTitle("Sắp xếp")
                .setItems(options, (dialog, which) -> {
                    sortType = options[which];
                    btnSort.setText(sortType + "  ☷");
                    loadIncomeHistory();
                })
                .show();
    }

    private void showWalletDialog() {
        String[] wallets = {
                "Tất cả ví",
                "Ví mặc định",
                "Tiết kiệm",
                "Ngân hàng",
                "Momo"
        };

        new AlertDialog.Builder(this)
                .setTitle("Chọn ví")
                .setItems(wallets, (dialog, which) -> {
                    selectedWallet = wallets[which];
                    btnWallet.setText(selectedWallet);
                    loadIncomeHistory();
                })
                .show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedMonth = month + 1;
                    selectedYear = year;

                    updateMonthText();
                    loadIncomeHistory();
                },
                selectedYear,
                selectedMonth - 1,
                1
        );

        datePickerDialog.show();
    }

    private void updateMonthText() {
        String monthText = "Tháng " + selectedMonth + "/" + selectedYear;

        btnMonth.setText(monthText);
        btnMonthTop.setText(monthText + " ▼");
    }

    private void loadIncomeHistory() {
        layoutTransactions.removeAllViews();

        totalIncome = 0;
        transactionCount = 0;
        refund = 0;

        String monthText = String.format("%02d/%04d", selectedMonth, selectedYear);
        String keyword = edtSearch.getText().toString().trim().toLowerCase();

        String orderBy = "id DESC";

        if (sortType.equals("Cũ nhất")) {
            orderBy = "id ASC";
        } else if (sortType.equals("Số tiền cao nhất")) {
            orderBy = "amount DESC";
        } else if (sortType.equals("Số tiền thấp nhất")) {
            orderBy = "amount ASC";
        }

        Cursor cursor;

        if (selectedWallet.equals("Tất cả ví")) {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, icon, color FROM transactions " +
                            "WHERE amount > 0 AND substr(date, 4, 7) = ? " +
                            "ORDER BY " + orderBy,
                    new String[]{monthText}
            );
        } else {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, icon, color FROM transactions " +
                            "WHERE amount > 0 AND substr(date, 4, 7) = ? AND wallet = ? " +
                            "ORDER BY " + orderBy,
                    new String[]{monthText, selectedWallet}
            );
        }

        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String date = cursor.getString(1);
            int amount = cursor.getInt(2);
            String iconName = cursor.getString(3);
            String colorCode = cursor.getString(4);

            if (!keyword.isEmpty() && !title.toLowerCase().contains(keyword)) {
                continue;
            }

            totalIncome += amount;
            transactionCount++;

            if (title.toLowerCase().contains("hoàn")) {
                refund += amount;
            }

            addItem(title, date, amount, iconName, colorCode);
        }

        cursor.close();
        updateStatistics();
    }

    private void updateStatistics() {
        int average = 0;

        if (transactionCount > 0) {
            average = totalIncome / transactionCount;
        }

        tvTotalIncome.setText(formatMoney(totalIncome));
        tvTransactionCount.setText(String.valueOf(transactionCount));
        tvIncome.setText(formatMoney(totalIncome));
        tvRefund.setText(formatMoney(refund));
        tvAverage.setText(formatMoney(average));
    }

    private void addItem(String title, String date, int amount, String iconName, String colorCode) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        cardParams.setMargins(dp(0), dp(6), dp(0), dp(10));
        card.setLayoutParams(cardParams);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dp(16));
        cardBg.setStroke(dp(1), Color.parseColor("#E5E7EB"));
        card.setBackground(cardBg);
        card.setElevation(dp(2));

        ImageView imgIcon = new ImageView(this);

        int iconRes = getResources().getIdentifier(
                iconName,
                "drawable",
                getPackageName()
        );

        if (iconRes == 0) {
            iconRes = R.drawable.ic_dot;
        }

        imgIcon.setImageResource(iconRes);
        imgIcon.setColorFilter(Color.WHITE);
        imgIcon.setPadding(dp(9), dp(9), dp(9), dp(9));

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setShape(GradientDrawable.OVAL);

        try {
            iconBg.setColor(Color.parseColor(colorCode));
        } catch (Exception e) {
            iconBg.setColor(Color.parseColor("#ADB5BD"));
        }

        imgIcon.setBackground(iconBg);

        LinearLayout.LayoutParams iconParams =
                new LinearLayout.LayoutParams(dp(46), dp(46));
        iconParams.setMargins(0, 0, dp(12), 0);
        imgIcon.setLayoutParams(iconParams);

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
        textBox.setLayoutParams(textParams);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#111827"));

        TextView tvDate = new TextView(this);
        tvDate.setText(date);
        tvDate.setTextSize(13);
        tvDate.setTextColor(Color.parseColor("#6B7280"));

        textBox.addView(tvTitle);
        textBox.addView(tvDate);

        TextView tvAmount = new TextView(this);
        tvAmount.setText("+" + formatMoney(amount));
        tvAmount.setTextSize(15);
        tvAmount.setTypeface(null, Typeface.BOLD);
        tvAmount.setTextColor(Color.parseColor("#16A34A"));
        tvAmount.setGravity(Gravity.END);

        card.addView(imgIcon);
        card.addView(textBox);
        card.addView(tvAmount);

        layoutTransactions.addView(card);
    }

    private void exportPdf() {
        PdfDocument pdfDocument = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int y = 60;

        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setFakeBoldText(true);
        canvas.drawText("BÁO CÁO KHOẢN THU", 50, y, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(18);

        y += 50;
        canvas.drawText("Tháng: " + selectedMonth + "/" + selectedYear, 50, y, paint);

        y += 32;
        canvas.drawText("Ví: " + selectedWallet, 50, y, paint);

        y += 32;
        canvas.drawText("Tổng thu: " + formatMoney(totalIncome), 50, y, paint);

        y += 32;
        canvas.drawText("Số giao dịch: " + transactionCount, 50, y, paint);

        y += 32;
        canvas.drawText("Hoàn tiền: " + formatMoney(refund), 50, y, paint);

        y += 32;
        canvas.drawText("Trung bình: " + tvAverage.getText().toString(), 50, y, paint);

        y += 55;
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        canvas.drawText("DANH SÁCH GIAO DỊCH", 50, y, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(17);

        String monthText = String.format("%02d/%04d", selectedMonth, selectedYear);

        Cursor cursor;

        if (selectedWallet.equals("Tất cả ví")) {
            cursor = database.rawQuery(
                    "SELECT title, date, amount FROM transactions " +
                            "WHERE amount > 0 AND substr(date, 4, 7) = ? " +
                            "ORDER BY id DESC",
                    new String[]{monthText}
            );
        } else {
            cursor = database.rawQuery(
                    "SELECT title, date, amount FROM transactions " +
                            "WHERE amount > 0 AND substr(date, 4, 7) = ? AND wallet = ? " +
                            "ORDER BY id DESC",
                    new String[]{monthText, selectedWallet}
            );
        }

        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String date = cursor.getString(1);
            int amount = cursor.getInt(2);

            y += 34;

            if (y > 800) {
                break;
            }

            canvas.drawText(
                    title + " - " + date + " - +" + formatMoney(amount),
                    50,
                    y,
                    paint
            );
        }

        cursor.close();

        pdfDocument.finishPage(page);

        try {
            File file = new File(
                    getExternalFilesDir(null),
                    "BaoCaoThu_" + selectedMonth + "_" + selectedYear + ".pdf"
            );

            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.close();
            pdfDocument.close();

            Toast.makeText(this, "Đã xuất PDF", Toast.LENGTH_LONG).show();

            openFile(file, "application/pdf");

        } catch (Exception e) {
            pdfDocument.close();
            Toast.makeText(this, "Lỗi xuất PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("BaoCaoThu");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Danh mục");
        header.createCell(1).setCellValue("Ngày");
        header.createCell(2).setCellValue("Số tiền");
        header.createCell(3).setCellValue("Ví");

        String monthText = String.format("%02d/%04d", selectedMonth, selectedYear);

        Cursor cursor;

        if (selectedWallet.equals("Tất cả ví")) {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, wallet FROM transactions " +
                            "WHERE amount > 0 AND substr(date, 4, 7) = ? " +
                            "ORDER BY id DESC",
                    new String[]{monthText}
            );
        } else {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, wallet FROM transactions " +
                            "WHERE amount > 0 AND substr(date, 4, 7) = ? AND wallet = ? " +
                            "ORDER BY id DESC",
                    new String[]{monthText, selectedWallet}
            );
        }

        int rowIndex = 1;

        while (cursor.moveToNext()) {
            String title = cursor.getString(0);
            String date = cursor.getString(1);
            int amount = cursor.getInt(2);
            String wallet = cursor.getString(3);

            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(title);
            row.createCell(1).setCellValue(date);
            row.createCell(2).setCellValue(amount);
            row.createCell(3).setCellValue(wallet);
        }

        cursor.close();

        try {
            File file = new File(
                    getExternalFilesDir(null),
                    "BaoCaoThu_" + selectedMonth + "_" + selectedYear + ".xlsx"
            );

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);

            fos.close();
            workbook.close();

            Toast.makeText(this, "Đã xuất Excel .xlsx", Toast.LENGTH_LONG).show();

            openFile(
                    file,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xuất Excel .xlsx", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFile(File file, String mimeType) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Mở báo cáo bằng"));

        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng để mở file", Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
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

    private String formatMoney(int money) {
        return String.format("%,d đ", money).replace(",", ".");
    }
}