package com.quanlychitieu.doan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseDB.db";
    private static final int DATABASE_VERSION = 9;

    public static final String TABLE_TRANSACTION = "transactions";
    public static final String TABLE_GOAL = "goals";
    public static final String TABLE_WALLET = "wallets";
    public static final String TABLE_ALERT_SETTING = "alert_settings";

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

        String CREATE_WALLET_TABLE = "CREATE TABLE " + TABLE_WALLET + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "isDefault INTEGER" +
                ")";

        String CREATE_ALERT_SETTING_TABLE = "CREATE TABLE " + TABLE_ALERT_SETTING + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "category TEXT UNIQUE," +
                "warningPercent INTEGER," +
                "settingValue INTEGER," +
                "enable INTEGER" +
                ")";

        db.execSQL(CREATE_TRANSACTION_TABLE);
        db.execSQL(CREATE_GOAL_TABLE);
        db.execSQL(CREATE_WALLET_TABLE);
        db.execSQL(CREATE_ALERT_SETTING_TABLE);

        insertDefaultWallets(db);
    }

    private void insertDefaultWallets(SQLiteDatabase db) {
        db.execSQL("INSERT INTO wallets(name, isDefault) VALUES('Ví mặc định', 1)");
        db.execSQL("INSERT INTO wallets(name, isDefault) VALUES('Tiết kiệm', 1)");
        db.execSQL("INSERT INTO wallets(name, isDefault) VALUES('Ngân hàng', 1)");
        db.execSQL("INSERT INTO wallets(name, isDefault) VALUES('Momo', 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOAL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALERT_SETTING);

        onCreate(db);
    }

    public Cursor getAllWallets() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT name FROM " + TABLE_WALLET + " ORDER BY id ASC", null);
    }

    public void insertWallet(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("isDefault", 0);

        db.insert(TABLE_WALLET, null, values);
        db.close();
    }

    public boolean walletHasTransaction(String wallet) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM transactions WHERE wallet=?",
                new String[]{wallet}
        );

        boolean hasData = false;

        if (cursor.moveToFirst()) {
            hasData = cursor.getInt(0) > 0;
        }

        cursor.close();
        return hasData;
    }

    public void deleteWallet(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WALLET, "name=?", new String[]{name});
        db.close();
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

    public void updateTransaction(int id,
                                  String title,
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

        db.update(TABLE_TRANSACTION, values, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTION, "id=?", new String[]{String.valueOf(id)});
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

    public int getWarningPercent(String category) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT warningPercent FROM " + TABLE_ALERT_SETTING + " WHERE category=?",
                new String[]{category}
        );

        int percent = 80;

        if (cursor.moveToFirst()) {
            percent = cursor.getInt(0);
        }

        cursor.close();
        return percent;
    }

    public void saveWarningPercent(String category, int percent) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("category", category);
        values.put("warningPercent", percent);
        values.put("settingValue", 0);
        values.put("enable", 1);

        db.insertWithOnConflict(
                TABLE_ALERT_SETTING,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public int getAlertSettingValue(String category, int defaultValue) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT settingValue FROM " + TABLE_ALERT_SETTING + " WHERE category=?",
                new String[]{category}
        );

        int value = defaultValue;

        if (cursor.moveToFirst()) {
            value = cursor.getInt(0);
        }

        cursor.close();
        return value;
    }

    public void saveAlertSettingValue(String category, int value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("category", category);
        values.put("settingValue", value);
        values.put("warningPercent", 80);
        values.put("enable", 1);

        db.insertWithOnConflict(
                TABLE_ALERT_SETTING,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }
}