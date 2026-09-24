package com.quanlychitieu.doan.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME =
            "ExpenseDB.db";
    private static final int DATABASE_VERSION = 14;

    // =========================================================
    // TÊN BẢNG
    // =========================================================

    public static final String TABLE_TRANSACTION =
            "transactions";

    public static final String TABLE_GOAL =
            "goals";

    public static final String TABLE_WALLET =
            "wallets";

    public static final String TABLE_ALERT_SETTING =
            "alert_settings";

    public static final String TABLE_NOTIFICATION =
            "notifications";

    public static final String TABLE_BUDGET =
            "budgets";

    public static final String TABLE_CATEGORY =
            "categories";

    // =========================================================
    // CỘT BẢNG AI CHAT
    // =========================================================

    public static final String TABLE_AI_CHAT =
            "ai_chat_messages";

    public static final String AI_CHAT_ID =
            "id";

    public static final String AI_CHAT_MESSAGE =
            "message";

    public static final String AI_CHAT_TYPE =
            "type";

    public static final String AI_CHAT_CREATED_AT =
            "created_at";

    // =========================================================
    // CỘT BẢNG TRANSACTIONS
    // =========================================================

    public static final String TRANSACTION_ID =
            "id";

    public static final String TRANSACTION_TITLE =
            "title";

    public static final String TRANSACTION_CATEGORY =
            "category";

    public static final String TRANSACTION_DATE =
            "date";

    public static final String TRANSACTION_AMOUNT =
            "amount";

    public static final String TRANSACTION_WALLET =
            "wallet";

    public static final String TRANSACTION_TYPE =
            "type";

    public static final String TRANSACTION_ICON =
            "icon";

    public static final String TRANSACTION_COLOR =
            "color";

    // =========================================================
    // CỘT BẢNG GOALS
    // =========================================================

    public static final String GOAL_ID =
            "id";

    public static final String GOAL_NAME =
            "name";

    public static final String GOAL_TARGET_AMOUNT =
            "targetAmount";

    public static final String GOAL_SAVED_AMOUNT =
            "savedAmount";

    public static final String GOAL_DEADLINE =
            "deadline";

    public static final String GOAL_WALLET =
            "wallet";

    public static final String GOAL_AUTO_SAVE =
            "autoSave";

    // =========================================================
    // CỘT BẢNG WALLETS
    // =========================================================

    public static final String WALLET_ID =
            "id";

    public static final String WALLET_NAME =
            "name";

    public static final String WALLET_IS_DEFAULT =
            "isDefault";

    // =========================================================
    // CỘT BẢNG ALERT SETTINGS
    // =========================================================

    public static final String ALERT_SETTING_ID =
            "id";

    public static final String ALERT_SETTING_CATEGORY =
            "category";

    public static final String ALERT_WARNING_PERCENT =
            "warningPercent";

    public static final String ALERT_SETTING_VALUE =
            "settingValue";

    public static final String ALERT_ENABLE =
            "enable";

    // =========================================================
    // CỘT BẢNG NOTIFICATIONS
    // =========================================================

    public static final String NOTIFICATION_ID =
            "id";

    public static final String NOTIFICATION_TITLE =
            "title";

    public static final String NOTIFICATION_MESSAGE =
            "message";

    public static final String NOTIFICATION_TYPE =
            "type";

    public static final String NOTIFICATION_IS_READ =
            "is_read";

    public static final String NOTIFICATION_CREATED_AT =
            "created_at";

    // =========================================================
    // CỘT BẢNG BUDGETS
    // =========================================================

    public static final String BUDGET_ID =
            "id";

    public static final String BUDGET_CATEGORY =
            "category";

    public static final String BUDGET_AMOUNT =
            "amount";

    public static final String BUDGET_MONTH =
            "month";

    // =========================================================
    // CỘT BẢNG CATEGORIES
    // =========================================================

    public static final String CATEGORY_ID =
            "id";

    public static final String CATEGORY_NAME =
            "name";

    public static final String CATEGORY_TYPE =
            "type";

    public static final String CATEGORY_ICON =
            "icon";

    public static final String CATEGORY_COLOR =
            "color";

    public static final String CATEGORY_IS_DEFAULT =
            "is_default";

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public DatabaseHelper(Context context) {
        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    // =========================================================
    // TẠO DATABASE LẦN ĐẦU
    // =========================================================

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTransactionTable(db);
        createGoalTable(db);
        createWalletTable(db);
        createAlertSettingTable(db);
        createNotificationTable(db);
        createBudgetTable(db);
        createCategoryTable(db);
        createAiChatTable(db);

        insertDefaultWallets(db);
        insertDefaultCategories(db);
    }

    private void createTransactionTable(
            SQLiteDatabase db
    ) {
        String sql =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_TRANSACTION
                        + " ("

                        + TRANSACTION_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                        + TRANSACTION_TITLE
                        + " TEXT NOT NULL,"

                        + TRANSACTION_CATEGORY
                        + " TEXT NOT NULL DEFAULT 'Khác',"

                        + TRANSACTION_DATE
                        + " TEXT,"

                        + TRANSACTION_AMOUNT
                        + " INTEGER NOT NULL DEFAULT 0,"

                        + TRANSACTION_WALLET
                        + " TEXT,"

                        + TRANSACTION_TYPE
                        + " TEXT,"

                        + TRANSACTION_ICON
                        + " TEXT,"

                        + TRANSACTION_COLOR
                        + " TEXT"

                        + ")";

        db.execSQL(sql);
    }

    private void createGoalTable(
            SQLiteDatabase db
    ) {
        String sql =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_GOAL
                        + " ("

                        + GOAL_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                        + GOAL_NAME
                        + " TEXT NOT NULL,"

                        + GOAL_TARGET_AMOUNT
                        + " INTEGER NOT NULL DEFAULT 0,"

                        + GOAL_SAVED_AMOUNT
                        + " INTEGER NOT NULL DEFAULT 0,"

                        + GOAL_DEADLINE
                        + " TEXT,"

                        + GOAL_WALLET
                        + " TEXT,"

                        + GOAL_AUTO_SAVE
                        + " INTEGER NOT NULL DEFAULT 0"

                        + ")";

        db.execSQL(sql);
    }

    private void createWalletTable(
            SQLiteDatabase db
    ) {
        String sql =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_WALLET
                        + " ("

                        + WALLET_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                        + WALLET_NAME
                        + " TEXT NOT NULL UNIQUE,"

                        + WALLET_IS_DEFAULT
                        + " INTEGER NOT NULL DEFAULT 0"

                        + ")";

        db.execSQL(sql);
    }

    private void createAlertSettingTable(
            SQLiteDatabase db
    ) {
        String sql =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_ALERT_SETTING
                        + " ("

                        + ALERT_SETTING_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                        + ALERT_SETTING_CATEGORY
                        + " TEXT NOT NULL UNIQUE,"

                        + ALERT_WARNING_PERCENT
                        + " INTEGER NOT NULL DEFAULT 80,"

                        + ALERT_SETTING_VALUE
                        + " INTEGER NOT NULL DEFAULT 0,"

                        + ALERT_ENABLE
                        + " INTEGER NOT NULL DEFAULT 1"

                        + ")";

        db.execSQL(sql);
    }

    private void createNotificationTable(
            SQLiteDatabase db
    ) {
        String sql =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_NOTIFICATION
                        + " ("

                        + NOTIFICATION_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                        + NOTIFICATION_TITLE
                        + " TEXT NOT NULL,"

                        + NOTIFICATION_MESSAGE
                        + " TEXT NOT NULL,"

                        + NOTIFICATION_TYPE
                        + " TEXT NOT NULL,"

                        + NOTIFICATION_IS_READ
                        + " INTEGER NOT NULL DEFAULT 0,"

                        + NOTIFICATION_CREATED_AT
                        + " TEXT NOT NULL"

                        + ")";

        db.execSQL(sql);
    }

    private void createBudgetTable(
            SQLiteDatabase db
    ) {
        String sql =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_BUDGET
                        + " ("

                        + BUDGET_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                        + BUDGET_CATEGORY
                        + " TEXT NOT NULL,"

                        + BUDGET_AMOUNT
                        + " INTEGER NOT NULL DEFAULT 0,"

                        + BUDGET_MONTH
                        + " TEXT NOT NULL,"

                        + "UNIQUE("
                        + BUDGET_CATEGORY
                        + ", "
                        + BUDGET_MONTH
                        + ")"

                        + ")";

        db.execSQL(sql);
    }

    private void createCategoryTable(
            SQLiteDatabase db
    ) {
        String sql =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_CATEGORY
                        + " ("

                        + CATEGORY_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT,"

                        + CATEGORY_NAME
                        + " TEXT NOT NULL,"

                        + CATEGORY_TYPE
                        + " TEXT NOT NULL,"

                        + CATEGORY_ICON
                        + " TEXT NOT NULL DEFAULT 'ic_dot',"

                        + CATEGORY_COLOR
                        + " TEXT NOT NULL DEFAULT '#ADB5BD',"

                        + CATEGORY_IS_DEFAULT
                        + " INTEGER NOT NULL DEFAULT 0,"

                        + "UNIQUE("
                        + CATEGORY_NAME
                        + ", "
                        + CATEGORY_TYPE
                        + ")"

                        + ")";

        db.execSQL(sql);
    }

    private void createAiChatTable(
            SQLiteDatabase db
    ) {
        String sql =
                "CREATE TABLE IF NOT EXISTS "
                        + TABLE_AI_CHAT
                        + " ("
                        + AI_CHAT_ID
                        + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + AI_CHAT_MESSAGE
                        + " TEXT NOT NULL,"
                        + AI_CHAT_TYPE
                        + " INTEGER NOT NULL,"
                        + AI_CHAT_CREATED_AT
                        + " INTEGER NOT NULL"
                        + ")";

        db.execSQL(sql);
    }

    private void insertDefaultCategories(
            SQLiteDatabase db
    ) {
        // Danh mục chi tiêu
        insertDefaultCategory(
                db, "Ăn uống", "EXPENSE",
                "ic_food", "#FF3131"
        );

        insertDefaultCategory(
                db, "Đi lại", "EXPENSE",
                "ic_bus", "#2196F3"
        );

        insertDefaultCategory(
                db, "Mua sắm", "EXPENSE",
                "ic_shopping", "#FF9800"
        );

        insertDefaultCategory(
                db, "Giải trí", "EXPENSE",
                "ic_default", "#9C27B0"
        );

        insertDefaultCategory(
                db, "Hóa đơn", "EXPENSE",
                "ic_bill", "#FF9800"
        );

        insertDefaultCategory(
                db, "Sức khỏe", "EXPENSE",
                "ic_heart", "#FFB3C6"
        );

        insertDefaultCategory(
                db, "Nhà cửa", "EXPENSE",
                "ic_default", "#F9844A"
        );

        insertDefaultCategory(
                db, "Cafe", "EXPENSE",
                "ic_food", "#9D4EDD"
        );

        insertDefaultCategory(
                db, "Giáo dục", "EXPENSE",
                "ic_work", "#5B8FD7"
        );

        insertDefaultCategory(
                db, "Quà tặng", "EXPENSE",
                "ic_donate", "#D14D8B"
        );

        insertDefaultCategory(
                db, "Thực phẩm", "EXPENSE",
                "ic_shopping", "#7CB342"
        );

        insertDefaultCategory(
                db, "Gia đình", "EXPENSE",
                "ic_heart", "#F9C74F"
        );

        insertDefaultCategory(
                db, "Thể dục", "EXPENSE",
                "ic_default", "#F3722C"
        );

        insertDefaultCategory(
                db, "Khác", "EXPENSE",
                "ic_dot", "#ADB5BD"
        );

        // Danh mục thu nhập
        insertDefaultCategory(
                db, "Lương", "INCOME",
                "ic_salary", "#2ECC71"
        );

        insertDefaultCategory(
                db, "Thưởng", "INCOME",
                "ic_reward", "#FB8500"
        );

        insertDefaultCategory(
                db, "Làm thêm", "INCOME",
                "ic_work", "#A2D2FF"
        );

        insertDefaultCategory(
                db, "Đầu tư", "INCOME",
                "ic_invest", "#2A9D8F"
        );

        insertDefaultCategory(
                db, "Bán hàng", "INCOME",
                "ic_sell", "#9D4EDD"
        );

        insertDefaultCategory(
                db, "Được tặng", "INCOME",
                "ic_donate", "#9D6B53"
        );

        insertDefaultCategory(
                db, "Hoàn tiền", "INCOME",
                "ic_reward", "#43AA8B"
        );

        insertDefaultCategory(
                db, "Tiền lãi", "INCOME",
                "ic_invest", "#277DA1"
        );

        insertDefaultCategory(
                db, "Cho thuê", "INCOME",
                "ic_sell", "#577590"
        );

        insertDefaultCategory(
                db, "Khác", "INCOME",
                "ic_dot", "#ADB5BD"
        );
    }

    private void insertDefaultCategory(
            SQLiteDatabase db,
            String name,
            String type,
            String icon,
            String color
    ) {
        ContentValues values =
                new ContentValues();

        values.put(
                CATEGORY_NAME,
                name
        );

        values.put(
                CATEGORY_TYPE,
                type
        );

        values.put(
                CATEGORY_ICON,
                icon
        );

        values.put(
                CATEGORY_COLOR,
                color
        );

        values.put(
                CATEGORY_IS_DEFAULT,
                1
        );

        db.insertWithOnConflict(
                TABLE_CATEGORY,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    // =========================================================
    // NÂNG CẤP DATABASE
    // =========================================================

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        /*
         * Không DROP TABLE để tránh mất dữ liệu cũ.
         */

        if (oldVersion < 10) {
            createNotificationTable(db);
        }

        if (oldVersion < 11) {
            addCategoryColumnIfNeeded(db);
            migrateOldTransactionCategories(db);
        }

        if (oldVersion < 12) {
            createBudgetTable(db);
        }

        if (oldVersion < 13) {
            createCategoryTable(db);
            insertDefaultCategories(db);
        }

        if (oldVersion < 14) {
            createAiChatTable(db);
        }
    }

    private void addCategoryColumnIfNeeded(
            SQLiteDatabase db
    ) {
        if (columnExists(
                db,
                TABLE_TRANSACTION,
                TRANSACTION_CATEGORY
        )) {
            return;
        }

        db.execSQL(
                "ALTER TABLE "
                        + TABLE_TRANSACTION
                        + " ADD COLUMN "
                        + TRANSACTION_CATEGORY
                        + " TEXT NOT NULL DEFAULT 'Khác'"
        );
    }

    private boolean columnExists(
            SQLiteDatabase db,
            String tableName,
            String columnName
    ) {
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "PRAGMA table_info("
                            + tableName
                            + ")",
                    null
            );

            int columnNameIndex =
                    cursor.getColumnIndex(
                            "name"
                    );

            if (columnNameIndex < 0) {
                return false;
            }

            while (cursor.moveToNext()) {
                String currentColumnName =
                        cursor.getString(
                                columnNameIndex
                        );

                if (columnName.equals(
                        currentColumnName
                )) {
                    return true;
                }
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    /**
     * Chuyển dữ liệu cũ:
     * Nếu title trước đây chính là tên danh mục,
     * chuyển title sang category.
     */
    private void migrateOldTransactionCategories(
            SQLiteDatabase db
    ) {
        db.execSQL(
                "UPDATE "
                        + TABLE_TRANSACTION
                        + " SET "
                        + TRANSACTION_CATEGORY
                        + " = "
                        + TRANSACTION_TITLE
                        + " WHERE "
                        + TRANSACTION_TITLE
                        + " IN ("

                        + "'Ăn uống',"
                        + "'Đi lại',"
                        + "'Mua sắm',"
                        + "'Giải trí',"
                        + "'Hóa đơn',"
                        + "'Sức khỏe',"
                        + "'Lương',"
                        + "'Thưởng',"
                        + "'Làm thêm',"
                        + "'Đầu tư',"
                        + "'Bán hàng',"
                        + "'Được tặng',"
                        + "'Khác'"

                        + ")"
        );
    }

    // =========================================================
    // VÍ MẶC ĐỊNH
    // =========================================================

    private void insertDefaultWallets(
            SQLiteDatabase db
    ) {
        insertDefaultWalletIfNotExists(
                db,
                "Ví mặc định"
        );

        insertDefaultWalletIfNotExists(
                db,
                "Tiết kiệm"
        );

        insertDefaultWalletIfNotExists(
                db,
                "Ngân hàng"
        );

        insertDefaultWalletIfNotExists(
                db,
                "Momo"
        );
    }

    private void insertDefaultWalletIfNotExists(
            SQLiteDatabase db,
            String walletName
    ) {
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM "
                            + TABLE_WALLET
                            + " WHERE "
                            + WALLET_NAME
                            + " = ?",
                    new String[]{
                            walletName
                    }
            );

            boolean exists =
                    cursor.moveToFirst()
                            && cursor.getInt(0) > 0;

            if (!exists) {
                ContentValues values =
                        new ContentValues();

                values.put(
                        WALLET_NAME,
                        walletName
                );

                values.put(
                        WALLET_IS_DEFAULT,
                        1
                );

                db.insert(
                        TABLE_WALLET,
                        null,
                        values
                );
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // =========================================================
    // WALLET
    // =========================================================

    public Cursor getAllWallets() {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_WALLET,
                new String[]{
                        WALLET_NAME
                },
                null,
                null,
                null,
                null,
                WALLET_ID + " ASC"
        );
    }

    public long insertWallet(
            String name
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                WALLET_NAME,
                normalizeText(
                        name,
                        "Ví mới"
                )
        );

        values.put(
                WALLET_IS_DEFAULT,
                0
        );

        return db.insert(
                TABLE_WALLET,
                null,
                values
        );
    }

    public boolean walletHasTransaction(
            String wallet
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM "
                            + TABLE_TRANSACTION
                            + " WHERE "
                            + TRANSACTION_WALLET
                            + " = ?",
                    new String[]{
                            wallet
                    }
            );

            return cursor.moveToFirst()
                    && cursor.getInt(0) > 0;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public int deleteWallet(
            String name
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_WALLET,
                WALLET_NAME + " = ?",
                new String[]{
                        name
                }
        );
    }

    // =========================================================
    // CATEGORIES (Danh mục giao dịch)
    // =========================================================

    public Cursor getCategoriesByType(
            String type
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        String normalizedType =
                "INCOME".equals(type)
                        ? "INCOME"
                        : "EXPENSE";

        return db.query(
                TABLE_CATEGORY,
                new String[]{
                        CATEGORY_ID,
                        CATEGORY_NAME,
                        CATEGORY_TYPE,
                        CATEGORY_ICON,
                        CATEGORY_COLOR,
                        CATEGORY_IS_DEFAULT
                },
                CATEGORY_TYPE + " = ?",
                new String[]{
                        normalizedType
                },
                null,
                null,
                CATEGORY_IS_DEFAULT + " DESC, "
                        + CATEGORY_ID + " ASC"
        );
    }

    public long insertCategory(
            String name,
            String type,
            String icon,
            String color
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        String normalizedName =
                normalizeText(name, "");

        if (normalizedName.isEmpty()) {
            return -1;
        }

        String normalizedType =
                "INCOME".equals(type)
                        ? "INCOME"
                        : "EXPENSE";

        ContentValues values =
                new ContentValues();

        values.put(
                CATEGORY_NAME,
                normalizedName
        );

        values.put(
                CATEGORY_TYPE,
                normalizedType
        );

        values.put(
                CATEGORY_ICON,
                normalizeText(icon, "ic_dot")
        );

        values.put(
                CATEGORY_COLOR,
                normalizeText(color, "#ADB5BD")
        );

        values.put(
                CATEGORY_IS_DEFAULT,
                0
        );

        return db.insertWithOnConflict(
                TABLE_CATEGORY,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public int updateCategory(
            long id,
            String name,
            String type,
            String icon,
            String color
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        String normalizedName =
                normalizeText(name, "");

        if (normalizedName.isEmpty()) {
            return 0;
        }

        String normalizedType =
                "INCOME".equals(type)
                        ? "INCOME"
                        : "EXPENSE";

        ContentValues values =
                new ContentValues();

        values.put(
                CATEGORY_NAME,
                normalizedName
        );

        values.put(
                CATEGORY_TYPE,
                normalizedType
        );

        values.put(
                CATEGORY_ICON,
                normalizeText(icon, "ic_dot")
        );

        values.put(
                CATEGORY_COLOR,
                normalizeText(color, "#ADB5BD")
        );

        return db.update(
                TABLE_CATEGORY,
                values,
                CATEGORY_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    // =========================================================
    // TRANSACTIONS (Thêm giao dịch)
    // =========================================================

    public long insertTransaction(
            String title,
            String category,
            String date,
            int amount,
            String wallet,
            String type,
            String icon,
            String color
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                TRANSACTION_TITLE,
                normalizeText(
                        title,
                        "Giao dịch"
                )
        );

        values.put(
                TRANSACTION_CATEGORY,
                normalizeText(
                        category,
                        "Khác"
                )
        );

        values.put(
                TRANSACTION_DATE,
                date
        );

        values.put(
                TRANSACTION_AMOUNT,
                amount
        );

        values.put(
                TRANSACTION_WALLET,
                wallet
        );

        values.put(
                TRANSACTION_TYPE,
                type
        );

        values.put(
                TRANSACTION_ICON,
                icon
        );

        values.put(
                TRANSACTION_COLOR,
                color
        );

        return db.insert(
                TABLE_TRANSACTION,
                null,
                values
        );
    }

    public int updateTransaction(
            int id,
            String title,
            String category,
            String date,
            int amount,
            String wallet,
            String type,
            String icon,
            String color
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                TRANSACTION_TITLE,
                normalizeText(
                        title,
                        "Giao dịch"
                )
        );

        values.put(
                TRANSACTION_CATEGORY,
                normalizeText(
                        category,
                        "Khác"
                )
        );

        values.put(
                TRANSACTION_DATE,
                date
        );

        values.put(
                TRANSACTION_AMOUNT,
                amount
        );

        values.put(
                TRANSACTION_WALLET,
                wallet
        );

        values.put(
                TRANSACTION_TYPE,
                type
        );

        values.put(
                TRANSACTION_ICON,
                icon
        );

        values.put(
                TRANSACTION_COLOR,
                color
        );

        return db.update(
                TABLE_TRANSACTION,
                values,
                TRANSACTION_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public int deleteTransaction(
            int id
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_TRANSACTION,
                TRANSACTION_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public Cursor getAllTransactions() {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_TRANSACTION,
                null,
                null,
                null,
                null,
                null,
                TRANSACTION_ID + " DESC"
        );
    }

    public Cursor getTransactionsByType(
            String type
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_TRANSACTION,
                null,
                TRANSACTION_TYPE + " = ?",
                new String[]{
                        type
                },
                null,
                null,
                TRANSACTION_ID + " DESC"
        );
    }

    public Cursor getTransactionsByCategory(
            String category
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_TRANSACTION,
                null,
                TRANSACTION_CATEGORY + " = ?",
                new String[]{
                        category
                },
                null,
                null,
                TRANSACTION_ID + " DESC"
        );
    }

    // =========================================================
    // GOALS
    // =========================================================

    public long insertGoal(
            String name,
            int targetAmount,
            int savedAmount,
            String deadline,
            String wallet,
            int autoSave
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                GOAL_NAME,
                name
        );

        values.put(
                GOAL_TARGET_AMOUNT,
                targetAmount
        );

        values.put(
                GOAL_SAVED_AMOUNT,
                savedAmount
        );

        values.put(
                GOAL_DEADLINE,
                deadline
        );

        values.put(
                GOAL_WALLET,
                wallet
        );

        values.put(
                GOAL_AUTO_SAVE,
                autoSave
        );

        return db.insert(
                TABLE_GOAL,
                null,
                values
        );
    }

    public int updateGoal(
            int id,
            String name,
            int targetAmount,
            int savedAmount,
            String deadline,
            String wallet,
            int autoSave
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                GOAL_NAME,
                name
        );

        values.put(
                GOAL_TARGET_AMOUNT,
                targetAmount
        );

        values.put(
                GOAL_SAVED_AMOUNT,
                savedAmount
        );

        values.put(
                GOAL_DEADLINE,
                deadline
        );

        values.put(
                GOAL_WALLET,
                wallet
        );

        values.put(
                GOAL_AUTO_SAVE,
                autoSave
        );

        return db.update(
                TABLE_GOAL,
                values,
                GOAL_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    public int deleteGoal(
            int id
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_GOAL,
                GOAL_ID + " = ?",
                new String[]{
                        String.valueOf(id)
                }
        );
    }

    // =========================================================
    // BUDGETS
    // =========================================================

    /**
     * Thêm mới hoặc cập nhật ngân sách
     * của một danh mục trong một tháng.
     */
    public long saveBudget(
            String category,
            int amount,
            String month
    ) {
        String normalizedCategory =
                normalizeText(
                        category,
                        ""
                );

        String normalizedMonth =
                normalizeText(
                        month,
                        ""
                );

        if (normalizedCategory.isEmpty()
                || normalizedMonth.isEmpty()
                || amount <= 0) {

            return -1;
        }

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                BUDGET_CATEGORY,
                normalizedCategory
        );

        values.put(
                BUDGET_AMOUNT,
                amount
        );

        values.put(
                BUDGET_MONTH,
                normalizedMonth
        );

        return db.insertWithOnConflict(
                TABLE_BUDGET,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Lấy số tiền ngân sách theo danh mục và tháng.
     * Nếu chưa thiết lập thì trả về 0.
     */
    public int getBudgetAmount(
            String category,
            String month
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;

        try {
            cursor = db.query(
                    TABLE_BUDGET,
                    new String[]{
                            BUDGET_AMOUNT
                    },
                    BUDGET_CATEGORY
                            + " = ? AND "
                            + BUDGET_MONTH
                            + " = ?",
                    new String[]{
                            category,
                            month
                    },
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    /**
     * Lấy toàn bộ ngân sách của tháng.
     */
    public Cursor getBudgetsByMonth(
            String month
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_BUDGET,
                null,
                BUDGET_MONTH + " = ?",
                new String[]{
                        month
                },
                null,
                null,
                BUDGET_CATEGORY + " ASC"
        );
    }

    /**
     * Lấy một ngân sách theo danh mục và tháng.
     */
    public Cursor getBudget(
            String category,
            String month
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_BUDGET,
                null,
                BUDGET_CATEGORY
                        + " = ? AND "
                        + BUDGET_MONTH
                        + " = ?",
                new String[]{
                        category,
                        month
                },
                null,
                null,
                null
        );
    }

    /**
     * Kiểm tra ngân sách đã tồn tại chưa.
     */
    public boolean budgetExists(
            String category,
            String month
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM "
                            + TABLE_BUDGET
                            + " WHERE "
                            + BUDGET_CATEGORY
                            + " = ? AND "
                            + BUDGET_MONTH
                            + " = ?",
                    new String[]{
                            category,
                            month
                    }
            );

            return cursor.moveToFirst()
                    && cursor.getInt(0) > 0;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Xóa ngân sách theo ID.
     */
    public int deleteBudget(
            int budgetId
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_BUDGET,
                BUDGET_ID + " = ?",
                new String[]{
                        String.valueOf(
                                budgetId
                        )
                }
        );
    }

    /**
     * Xóa ngân sách theo danh mục và tháng.
     */
    public int deleteBudget(
            String category,
            String month
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_BUDGET,
                BUDGET_CATEGORY
                        + " = ? AND "
                        + BUDGET_MONTH
                        + " = ?",
                new String[]{
                        category,
                        month
                }
        );
    }

    // =========================================================
    // ALERT SETTINGS
    // =========================================================

    public int getWarningPercent(
            String category
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;
        int warningPercent = 80;

        try {
            cursor = db.query(
                    TABLE_ALERT_SETTING,
                    new String[]{
                            ALERT_WARNING_PERCENT
                    },
                    ALERT_SETTING_CATEGORY
                            + " = ?",
                    new String[]{
                            category
                    },
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                warningPercent =
                        cursor.getInt(0);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return warningPercent;
    }

    public void saveWarningPercent(
            String category,
            int percent
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        boolean currentEnabled =
                isCategoryAlertEnabled(
                        category
                );

        int currentSettingValue =
                getAlertSettingValue(
                        category,
                        0
                );

        ContentValues values =
                new ContentValues();

        values.put(
                ALERT_SETTING_CATEGORY,
                category
        );

        values.put(
                ALERT_WARNING_PERCENT,
                percent
        );

        values.put(
                ALERT_SETTING_VALUE,
                currentSettingValue
        );

        values.put(
                ALERT_ENABLE,
                currentEnabled ? 1 : 0
        );

        db.insertWithOnConflict(
                TABLE_ALERT_SETTING,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public int getAlertSettingValue(
            String category,
            int defaultValue
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;
        int value = defaultValue;

        try {
            cursor = db.query(
                    TABLE_ALERT_SETTING,
                    new String[]{
                            ALERT_SETTING_VALUE
                    },
                    ALERT_SETTING_CATEGORY
                            + " = ?",
                    new String[]{
                            category
                    },
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                value =
                        cursor.getInt(0);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return value;
    }

    public void saveAlertSettingValue(
            String category,
            int value
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        int currentWarningPercent =
                getWarningPercent(
                        category
                );

        boolean currentEnabled =
                isCategoryAlertEnabled(
                        category
                );

        ContentValues values =
                new ContentValues();

        values.put(
                ALERT_SETTING_CATEGORY,
                category
        );

        values.put(
                ALERT_SETTING_VALUE,
                value
        );

        values.put(
                ALERT_WARNING_PERCENT,
                currentWarningPercent
        );

        values.put(
                ALERT_ENABLE,
                currentEnabled ? 1 : 0
        );

        db.insertWithOnConflict(
                TABLE_ALERT_SETTING,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public boolean isCategoryAlertEnabled(
            String category
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;

        try {
            cursor = db.query(
                    TABLE_ALERT_SETTING,
                    new String[]{
                            ALERT_ENABLE
                    },
                    ALERT_SETTING_CATEGORY
                            + " = ?",
                    new String[]{
                            category
                    },
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                return cursor.getInt(0) == 1;
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        /*
         * Chưa có cài đặt thì mặc định bật.
         */
        return true;
    }

    public void saveCategoryAlertEnabled(
            String category,
            boolean enabled
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        int currentWarningPercent =
                getWarningPercent(
                        category
                );

        int currentSettingValue =
                getAlertSettingValue(
                        category,
                        0
                );

        ContentValues values =
                new ContentValues();

        values.put(
                ALERT_SETTING_CATEGORY,
                category
        );

        values.put(
                ALERT_WARNING_PERCENT,
                currentWarningPercent
        );

        values.put(
                ALERT_SETTING_VALUE,
                currentSettingValue
        );

        values.put(
                ALERT_ENABLE,
                enabled ? 1 : 0
        );

        db.insertWithOnConflict(
                TABLE_ALERT_SETTING,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    public long insertNotification(
            String title,
            String message,
            String type,
            String createdAt
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                NOTIFICATION_TITLE,
                normalizeText(
                        title,
                        "Thông báo"
                )
        );

        values.put(
                NOTIFICATION_MESSAGE,
                normalizeText(
                        message,
                        "Không có nội dung."
                )
        );

        values.put(
                NOTIFICATION_TYPE,
                normalizeText(
                        type,
                        "REMINDER"
                )
        );

        values.put(
                NOTIFICATION_IS_READ,
                0
        );

        values.put(
                NOTIFICATION_CREATED_AT,
                normalizeText(
                        createdAt,
                        "Không xác định"
                )
        );

        return db.insert(
                TABLE_NOTIFICATION,
                null,
                values
        );
    }

    public Cursor getAllNotifications() {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_NOTIFICATION,
                null,
                null,
                null,
                null,
                null,
                NOTIFICATION_ID + " DESC"
        );
    }

    public Cursor getNotificationsByType(
            String type
    ) {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_NOTIFICATION,
                null,
                NOTIFICATION_TYPE + " = ?",
                new String[]{
                        type
                },
                null,
                null,
                NOTIFICATION_ID + " DESC"
        );
    }

    public int markNotificationAsRead(
            int notificationId
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                NOTIFICATION_IS_READ,
                1
        );

        return db.update(
                TABLE_NOTIFICATION,
                values,
                NOTIFICATION_ID + " = ?",
                new String[]{
                        String.valueOf(
                                notificationId
                        )
                }
        );
    }

    public int markAllNotificationsAsRead() {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                NOTIFICATION_IS_READ,
                1
        );

        return db.update(
                TABLE_NOTIFICATION,
                values,
                NOTIFICATION_IS_READ + " = ?",
                new String[]{
                        "0"
                }
        );
    }

    public int getUnreadNotificationCount() {
        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;
        int unreadCount = 0;

        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM "
                            + TABLE_NOTIFICATION
                            + " WHERE "
                            + NOTIFICATION_IS_READ
                            + " = 0",
                    null
            );

            if (cursor.moveToFirst()) {
                unreadCount =
                        cursor.getInt(0);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return unreadCount;
    }

    public int deleteNotification(
            int notificationId
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_NOTIFICATION,
                NOTIFICATION_ID + " = ?",
                new String[]{
                        String.valueOf(
                                notificationId
                        )
                }
        );
    }

    public int deleteAllNotifications() {
        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_NOTIFICATION,
                null,
                null
        );
    }

    // =========================================================
    // AI CHAT
    // =========================================================

    /**
     * type = 0: tin nhắn người dùng
     * type = 1: câu trả lời của AI
     */
    public long insertAiChatMessage(
            String message,
            int type
    ) {
        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                AI_CHAT_MESSAGE,
                normalizeText(message, "")
        );

        values.put(
                AI_CHAT_TYPE,
                type
        );

        values.put(
                AI_CHAT_CREATED_AT,
                System.currentTimeMillis()
        );

        return db.insert(
                TABLE_AI_CHAT,
                null,
                values
        );
    }

    public Cursor getAiChatMessages() {
        SQLiteDatabase db =
                getReadableDatabase();

        return db.query(
                TABLE_AI_CHAT,
                null,
                null,
                null,
                null,
                null,
                AI_CHAT_ID + " ASC"
        );
    }

    public int getAiChatMessageCount() {
        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM "
                            + TABLE_AI_CHAT,
                    null
            );

            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    public int deleteAiChatMessages() {
        SQLiteDatabase db =
                getWritableDatabase();

        return db.delete(
                TABLE_AI_CHAT,
                null,
                null
        );
    }

    // =========================================================
    // HÀM HỖ TRỢ
    // =========================================================

    private String normalizeText(
            String value,
            String defaultValue
    ) {
        if (value == null
                || value.trim().isEmpty()) {

            return defaultValue;
        }

        return value.trim();
    }
}
