package com.quanlychitieu.doan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseDB.db";
    private static final int DATABASE_VERSION = 5;

    public static final String TABLE_TRANSACTION = "transactions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE = "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "date TEXT," +
                "amount INTEGER," +
                "wallet TEXT," +
                "type TEXT," +
                "icon TEXT," +
                "color TEXT" +
                ")";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        onCreate(db);
    }

    public void insertTransaction(String title,
                                  String date,
                                  int amount,
                                  String wallet,
                                  String type,
                                  String icon,
                                  String color) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("title", title);
        values.put("date", date);
        values.put("amount", amount);
        values.put("wallet", wallet);
        values.put("type", type);
        values.put("icon", icon);
        values.put("color", color);

        db.insert(TABLE_TRANSACTION, null, values);

        db.close();
    }
}