package com.quanlychitieu.doan.history;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;

public class IncomeHistoryActivity extends AppCompatActivity {

    LinearLayout layoutHistory;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_history);

        layoutHistory = findViewById(R.id.layoutHistory);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        loadIncomeHistory();
    }

    private void loadIncomeHistory() {
        Cursor cursor = database.rawQuery(
                "SELECT title, date, amount FROM transactions WHERE amount > 0 ORDER BY id DESC",
                null
        );

        while (cursor.moveToNext()) {
            addItem(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getInt(2)
            );
        }

        cursor.close();
    }

    private void addItem(String title, String date, int amount) {
        TextView tv = new TextView(this);
        tv.setText(title + "\n" + date + "\n+" + formatMoney(amount));
        tv.setTextSize(16);
        tv.setPadding(0, 18, 0, 18);
        layoutHistory.addView(tv);
    }

    private String formatMoney(int money) {
        return String.format("%,d đ", Math.abs(money)).replace(",", ".");
    }
}