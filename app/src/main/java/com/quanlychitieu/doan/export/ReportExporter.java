package com.quanlychitieu.doan.export;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

public class ReportExporter {

    public static void exportPdf(
            Context context,
            SQLiteDatabase database,
            int selectedMonth,
            int selectedYear,
            String selectedWallet,
            int totalIncome,
            int transactionCount,
            int refund,
            String average
    ) {
        exportReportPdf(
                context,
                database,
                selectedMonth,
                selectedYear,
                selectedWallet,
                totalIncome,
                transactionCount,
                "BÁO CÁO KHOẢN THU",
                "Tổng thu",
                "DANH SÁCH KHOẢN THU",
                "BaoCaoThu_",
                true
        );
    }

    public static void exportExcel(
            Context context,
            SQLiteDatabase database,
            int selectedMonth,
            int selectedYear,
            String selectedWallet
    ) {
        exportReportExcel(
                context,
                database,
                selectedMonth,
                selectedYear,
                selectedWallet,
                "BaoCaoThu",
                "BaoCaoThu_",
                true
        );
    }

    public static void exportExpensePdf(
            Context context,
            SQLiteDatabase database,
            int selectedMonth,
            int selectedYear,
            String selectedWallet,
            int totalExpense,
            int transactionCount
    ) {
        exportReportPdf(
                context,
                database,
                selectedMonth,
                selectedYear,
                selectedWallet,
                totalExpense,
                transactionCount,
                "BÁO CÁO KHOẢN CHI",
                "Tổng chi",
                "DANH SÁCH KHOẢN CHI",
                "BaoCaoChi_",
                false
        );
    }

    public static void exportExpenseExcel(
            Context context,
            SQLiteDatabase database,
            int selectedMonth,
            int selectedYear,
            String selectedWallet
    ) {
        exportReportExcel(
                context,
                database,
                selectedMonth,
                selectedYear,
                selectedWallet,
                "BaoCaoChi",
                "BaoCaoChi_",
                false
        );
    }

    private static void exportReportPdf(
            Context context,
            SQLiteDatabase database,
            int selectedMonth,
            int selectedYear,
            String selectedWallet,
            int totalAmount,
            int transactionCount,
            String titleReport,
            String totalLabel,
            String listTitle,
            String filePrefix,
            boolean isIncome
    ) {
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
        canvas.drawText(titleReport, 50, y, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(18);

        y += 50;
        canvas.drawText("Tháng: " + selectedMonth + "/" + selectedYear, 50, y, paint);

        y += 32;
        canvas.drawText("Ví: " + selectedWallet, 50, y, paint);

        y += 32;
        canvas.drawText(totalLabel + ": " + formatMoney(totalAmount), 50, y, paint);

        y += 32;
        canvas.drawText("Số giao dịch: " + transactionCount, 50, y, paint);

        y += 55;
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        canvas.drawText(listTitle, 50, y, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(17);

        String monthText = String.format("%02d/%04d", selectedMonth, selectedYear);

        Cursor cursor;

        String amountCondition = isIncome ? "amount > 0" : "amount < 0";

        if (selectedWallet.equals("Tất cả ví")) {
            cursor = database.rawQuery(
                    "SELECT title, date, amount FROM transactions " +
                            "WHERE " + amountCondition + " AND substr(date, 4, 7) = ? " +
                            "ORDER BY id DESC",
                    new String[]{monthText}
            );
        } else {
            cursor = database.rawQuery(
                    "SELECT title, date, amount FROM transactions " +
                            "WHERE " + amountCondition + " AND substr(date, 4, 7) = ? AND wallet = ? " +
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

            String moneyText;

            if (amount > 0) {
                moneyText = "+" + formatMoney(amount);
            } else {
                moneyText = formatMoney(amount);
            }

            canvas.drawText(
                    title + " - " + date + " - " + moneyText,
                    50,
                    y,
                    paint
            );
        }

        cursor.close();
        pdfDocument.finishPage(page);

        try {
            File file = new File(
                    context.getExternalFilesDir(null),
                    filePrefix + selectedMonth + "_" + selectedYear + ".pdf"
            );

            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.close();
            pdfDocument.close();

            Toast.makeText(context, "Đã xuất PDF", Toast.LENGTH_LONG).show();
            openFile(context, file, "application/pdf");

        } catch (Exception e) {
            pdfDocument.close();
            Toast.makeText(context, "Lỗi xuất PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private static void exportReportExcel(
            Context context,
            SQLiteDatabase database,
            int selectedMonth,
            int selectedYear,
            String selectedWallet,
            String sheetName,
            String filePrefix,
            boolean isIncome
    ) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Danh mục");
        header.createCell(1).setCellValue("Ngày");
        header.createCell(2).setCellValue("Số tiền");
        header.createCell(3).setCellValue("Ví");

        String monthText = String.format("%02d/%04d", selectedMonth, selectedYear);

        Cursor cursor;

        String amountCondition = isIncome ? "amount > 0" : "amount < 0";

        if (selectedWallet.equals("Tất cả ví")) {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, wallet FROM transactions " +
                            "WHERE " + amountCondition + " AND substr(date, 4, 7) = ? " +
                            "ORDER BY id DESC",
                    new String[]{monthText}
            );
        } else {
            cursor = database.rawQuery(
                    "SELECT title, date, amount, wallet FROM transactions " +
                            "WHERE " + amountCondition + " AND substr(date, 4, 7) = ? AND wallet = ? " +
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
                    context.getExternalFilesDir(null),
                    filePrefix + selectedMonth + "_" + selectedYear + ".xlsx"
            );

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            Toast.makeText(context, "Đã xuất Excel .xlsx", Toast.LENGTH_LONG).show();

            openFile(
                    context,
                    file,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

        } catch (Exception e) {
            Toast.makeText(context, "Lỗi xuất Excel .xlsx", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openFile(Context context, File file, String mimeType) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, "Mở báo cáo bằng"));

        } catch (Exception e) {
            Toast.makeText(context, "Không tìm thấy ứng dụng để mở file", Toast.LENGTH_SHORT).show();
        }
    }

    private static String formatMoney(int money) {
        return String.format("%,d đ", money).replace(",", ".");
    }
}