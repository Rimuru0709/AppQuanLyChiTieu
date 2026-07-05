package com.quanlychitieu.doan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseDB.db";
    private static final int DATABASE_VERSION = 6;

    public static final String TABLE_TRANSACTION = "transactions";
    public static final String TABLE_GOAL = "goals";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TRANSACTION_TABLE = "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "date TEXT," +
                "amount INTEGER," +
                "wallet TEXT," +
                "type TEXT," +
                "icon TEXT," +
                "color TEXT" +
                ")";

        String CREATE_GOAL_TABLE = "CREATE TABLE " + TABLE_GOAL + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "targetAmount INTEGER," +
                "savedAmount INTEGER," +
                "deadline TEXT," +
                "wallet TEXT," +
                "autoSave INTEGER" +
                ")";

        db.execSQL(CREATE_TRANSACTION_TABLE);
        db.execSQL(CREATE_GOAL_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOAL);

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

    public void insertGoal(String name,
                           int targetAmount,
                           int savedAmount,
                           String deadline,
                           String wallet,
                           int autoSave) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("targetAmount", targetAmount);
        values.put("savedAmount", savedAmount);
        values.put("deadline", deadline);
        values.put("wallet", wallet);
        values.put("autoSave", autoSave);

        db.insert(TABLE_GOAL, null, values);
        db.close();
    }

    public void updateGoal(int id,
                           String name,
                           int targetAmount,
                           int savedAmount,
                           String deadline,
                           String wallet,
                           int autoSave) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("targetAmount", targetAmount);
        values.put("savedAmount", savedAmount);
        values.put("deadline", deadline);
        values.put("wallet", wallet);
        values.put("autoSave", autoSave);

        db.update(TABLE_GOAL, values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteGoal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GOAL, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }
}