package com.quanlychitieu.doan.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseDB.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE transactions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "date TEXT," +
                        "amount INTEGER)"
        );

        db.execSQL(
                "INSERT INTO transactions(title,date,amount) " +
                        "VALUES('Ăn uống','11/06/2026',-120000)"
        );

        db.execSQL(
                "INSERT INTO transactions(title,date,amount) " +
                        "VALUES('Lương tháng 6','10/06/2026',15000000)"
        );

        db.execSQL(
                "INSERT INTO transactions(title,date,amount) " +
                        "VALUES('Đi lại','09/06/2026',-30000)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {

    }
}