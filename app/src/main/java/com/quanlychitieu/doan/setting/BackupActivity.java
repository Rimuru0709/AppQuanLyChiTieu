package com.quanlychitieu.doan.setting;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class BackupActivity extends AppCompatActivity {

    private static final String PREF_BACKUP =
            "BackupPreferences";

    private static final String KEY_LAST_BACKUP =
            "last_backup";

    private ImageView imgBack;

    private LinearLayout itemBackupData;
    private LinearLayout itemRestoreData;

    private TextView tvLastBackup;

    private DatabaseHelper dbHelper;

    private String pendingBackupContent;

    private ActivityResultLauncher<String>
            createBackupFileLauncher;

    private ActivityResultLauncher<String[]>
            openBackupFileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        dbHelper = new DatabaseHelper(this);

        registerFileLaunchers();
        initViews();
        setupSafeArea();
        setupEvents();
        loadLastBackupTime();
    }

    // =========================================================
    // ĐĂNG KÝ TRÌNH CHỌN TỆP
    // =========================================================

    private void registerFileLaunchers() {
        createBackupFileLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.CreateDocument(
                                "application/json"
                        ),
                        uri -> {
                            if (uri == null) {
                                Toast.makeText(
                                        this,
                                        "Đã hủy sao lưu",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            writeBackupFile(uri);
                        }
                );

        openBackupFileLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.OpenDocument(),
                        uri -> {
                            if (uri == null) {
                                Toast.makeText(
                                        this,
                                        "Đã hủy chọn tệp",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            confirmRestore(uri);
                        }
                );
    }

    // =========================================================
    // KHỞI TẠO VIEW
    // =========================================================

    private void initViews() {
        imgBack =
                findViewById(R.id.imgBack);

        itemBackupData =
                findViewById(R.id.itemBackupData);

        itemRestoreData =
                findViewById(R.id.itemRestoreData);

        tvLastBackup =
                findViewById(R.id.tvLastBackup);
    }

    // =========================================================
    // SAFE AREA
    // =========================================================

    private void setupSafeArea() {
        View content =
                findViewById(R.id.contentLayout);

        if (content == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                content,
                (view, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
                            );

                    view.setPadding(
                            dp(22),
                            systemBars.top + dp(8),
                            dp(22),
                            systemBars.bottom + dp(28)
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(content);
    }

    // =========================================================
    // SỰ KIỆN
    // =========================================================

    private void setupEvents() {
        if (imgBack != null) {
            imgBack.setOnClickListener(
                    view -> finish()
            );
        }

        if (itemBackupData != null) {
            itemBackupData.setOnClickListener(
                    view -> startBackup()
            );
        }

        if (itemRestoreData != null) {
            itemRestoreData.setOnClickListener(
                    view -> startRestore()
            );
        }
    }

    // =========================================================
    // SAO LƯU DỮ LIỆU
    // =========================================================

    private void startBackup() {
        try {
            pendingBackupContent =
                    createBackupJson().toString(4);

            String fileName =
                    "QuanLyChiTieu_Backup_"
                            + getCurrentDateForFile()
                            + ".json";

            createBackupFileLauncher.launch(
                    fileName
            );

        } catch (Exception exception) {
            exception.printStackTrace();

            Toast.makeText(
                    this,
                    "Không thể tạo dữ liệu sao lưu",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private JSONObject createBackupJson()
            throws Exception {

        SQLiteDatabase database =
                dbHelper.getReadableDatabase();

        JSONObject root =
                new JSONObject();

        root.put(
                "app_name",
                "QuanLyChiTieu"
        );

        root.put(
                "backup_version",
                1
        );

        root.put(
                "created_at",
                getCurrentDateTime()
        );

        root.put(
                "transactions",
                readTable(
                        database,
                        DatabaseHelper.TABLE_TRANSACTION
                )
        );

        root.put(
                "goals",
                readTable(
                        database,
                        DatabaseHelper.TABLE_GOAL
                )
        );

        root.put(
                "wallets",
                readTable(
                        database,
                        DatabaseHelper.TABLE_WALLET
                )
        );

        root.put(
                "alert_settings",
                readTable(
                        database,
                        DatabaseHelper.TABLE_ALERT_SETTING
                )
        );

        root.put(
                "notifications",
                readTable(
                        database,
                        DatabaseHelper.TABLE_NOTIFICATION
                )
        );

        return root;
    }

    private JSONArray readTable(
            SQLiteDatabase database,
            String tableName
    ) throws Exception {

        JSONArray array =
                new JSONArray();

        Cursor cursor = null;

        try {
            cursor = database.query(
                    tableName,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            String[] columnNames =
                    cursor.getColumnNames();

            while (cursor.moveToNext()) {
                JSONObject item =
                        new JSONObject();

                for (int index = 0;
                     index < columnNames.length;
                     index++) {

                    String columnName =
                            columnNames[index];

                    if (cursor.isNull(index)) {
                        item.put(
                                columnName,
                                JSONObject.NULL
                        );

                    } else {
                        int fieldType =
                                cursor.getType(index);

                        switch (fieldType) {
                            case Cursor.FIELD_TYPE_INTEGER:
                                item.put(
                                        columnName,
                                        cursor.getLong(index)
                                );
                                break;

                            case Cursor.FIELD_TYPE_FLOAT:
                                item.put(
                                        columnName,
                                        cursor.getDouble(index)
                                );
                                break;

                            case Cursor.FIELD_TYPE_BLOB:
                                item.put(
                                        columnName,
                                        android.util.Base64
                                                .encodeToString(
                                                        cursor.getBlob(
                                                                index
                                                        ),
                                                        android.util.Base64
                                                                .NO_WRAP
                                                )
                                );
                                break;

                            case Cursor.FIELD_TYPE_STRING:
                            default:
                                item.put(
                                        columnName,
                                        cursor.getString(index)
                                );
                                break;
                        }
                    }
                }

                array.put(item);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return array;
    }

    private void writeBackupFile(
            Uri uri
    ) {
        if (pendingBackupContent == null
                || pendingBackupContent
                .trim()
                .isEmpty()) {

            Toast.makeText(
                    this,
                    "Không có dữ liệu để sao lưu",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try (
                OutputStream outputStream =
                        getContentResolver()
                                .openOutputStream(uri)
        ) {
            if (outputStream == null) {
                throw new Exception(
                        "Không thể mở tệp sao lưu"
                );
            }

            outputStream.write(
                    pendingBackupContent.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            outputStream.flush();

            String backupTime =
                    getCurrentDateTime();

            saveLastBackupTime(
                    backupTime
            );

            showLastBackupTime(
                    backupTime
            );

            Toast.makeText(
                    this,
                    "Sao lưu dữ liệu thành công",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception exception) {
            exception.printStackTrace();

            Toast.makeText(
                    this,
                    "Không thể lưu tệp sao lưu",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // KHÔI PHỤC DỮ LIỆU
    // =========================================================

    private void startRestore() {
        openBackupFileLauncher.launch(
                new String[]{
                        "application/json",
                        "text/plain"
                }
        );
    }

    private void confirmRestore(
            Uri uri
    ) {
        new AlertDialog.Builder(this)
                .setTitle("Khôi phục dữ liệu")
                .setMessage(
                        "Dữ liệu hiện tại sẽ bị thay thế bằng dữ liệu trong tệp sao lưu. Bạn có chắc muốn tiếp tục?"
                )
                .setPositiveButton(
                        "Khôi phục",
                        (dialog, which) ->
                                restoreFromFile(uri)
                )
                .setNegativeButton(
                        "Hủy",
                        null
                )
                .show();
    }

    private void restoreFromFile(
            Uri uri
    ) {
        try {
            String jsonText =
                    readTextFromUri(uri);

            if (jsonText == null
                    || jsonText.trim().isEmpty()) {

                Toast.makeText(
                        this,
                        "Tệp sao lưu không có dữ liệu",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            JSONObject root =
                    new JSONObject(jsonText);

            if (!isValidBackup(root)) {
                Toast.makeText(
                        this,
                        "Tệp sao lưu không hợp lệ",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            restoreDatabase(root);

            Toast.makeText(
                    this,
                    "Khôi phục dữ liệu thành công",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception exception) {
            exception.printStackTrace();

            Toast.makeText(
                    this,
                    "Không thể khôi phục dữ liệu",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String readTextFromUri(
            Uri uri
    ) throws Exception {

        StringBuilder builder =
                new StringBuilder();

        try (
                InputStream inputStream =
                        getContentResolver()
                                .openInputStream(uri);

                BufferedReader reader =
                        inputStream == null
                                ? null
                                : new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            if (reader == null) {
                throw new Exception(
                        "Không thể mở tệp sao lưu"
                );
            }

            String line;

            while ((line = reader.readLine())
                    != null) {

                builder.append(line);
            }
        }

        return builder.toString();
    }

    private boolean isValidBackup(
            JSONObject root
    ) {
        String appName =
                root.optString(
                        "app_name",
                        ""
                );

        int backupVersion =
                root.optInt(
                        "backup_version",
                        -1
                );

        return "QuanLyChiTieu".equals(appName)
                && backupVersion == 1
                && root.has("transactions")
                && root.has("goals")
                && root.has("wallets")
                && root.has("alert_settings")
                && root.has("notifications");
    }

    private void restoreDatabase(
            JSONObject root
    ) throws Exception {

        SQLiteDatabase database =
                dbHelper.getWritableDatabase();

        database.beginTransaction();

        try {
            clearCurrentData(database);

            restoreTable(
                    database,
                    DatabaseHelper.TABLE_WALLET,
                    root.getJSONArray("wallets")
            );

            restoreTable(
                    database,
                    DatabaseHelper.TABLE_TRANSACTION,
                    root.getJSONArray("transactions")
            );

            restoreTable(
                    database,
                    DatabaseHelper.TABLE_GOAL,
                    root.getJSONArray("goals")
            );

            restoreTable(
                    database,
                    DatabaseHelper.TABLE_ALERT_SETTING,
                    root.getJSONArray("alert_settings")
            );

            restoreTable(
                    database,
                    DatabaseHelper.TABLE_NOTIFICATION,
                    root.getJSONArray("notifications")
            );

            resetAutoIncrement(
                    database,
                    DatabaseHelper.TABLE_WALLET
            );

            resetAutoIncrement(
                    database,
                    DatabaseHelper.TABLE_TRANSACTION
            );

            resetAutoIncrement(
                    database,
                    DatabaseHelper.TABLE_GOAL
            );

            resetAutoIncrement(
                    database,
                    DatabaseHelper.TABLE_ALERT_SETTING
            );

            resetAutoIncrement(
                    database,
                    DatabaseHelper.TABLE_NOTIFICATION
            );

            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
    }

    private void clearCurrentData(
            SQLiteDatabase database
    ) {
        database.delete(
                DatabaseHelper.TABLE_NOTIFICATION,
                null,
                null
        );

        database.delete(
                DatabaseHelper.TABLE_ALERT_SETTING,
                null,
                null
        );

        database.delete(
                DatabaseHelper.TABLE_GOAL,
                null,
                null
        );

        database.delete(
                DatabaseHelper.TABLE_TRANSACTION,
                null,
                null
        );

        database.delete(
                DatabaseHelper.TABLE_WALLET,
                null,
                null
        );
    }

    private void restoreTable(
            SQLiteDatabase database,
            String tableName,
            JSONArray data
    ) throws Exception {

        for (int index = 0;
             index < data.length();
             index++) {

            JSONObject item =
                    data.getJSONObject(index);

            ContentValues values =
                    jsonToContentValues(item);

            long result =
                    database.insertOrThrow(
                            tableName,
                            null,
                            values
                    );

            if (result == -1) {
                throw new Exception(
                        "Không thể khôi phục bảng "
                                + tableName
                );
            }
        }
    }

    private ContentValues jsonToContentValues(
            JSONObject item
    ) throws Exception {

        ContentValues values =
                new ContentValues();

        Iterator<String> keys =
                item.keys();

        while (keys.hasNext()) {
            String key =
                    keys.next();

            Object value =
                    item.get(key);

            if (value == JSONObject.NULL) {
                values.putNull(key);

            } else if (value instanceof Integer) {
                values.put(
                        key,
                        (Integer) value
                );

            } else if (value instanceof Long) {
                values.put(
                        key,
                        (Long) value
                );

            } else if (value instanceof Double) {
                values.put(
                        key,
                        (Double) value
                );

            } else if (value instanceof Boolean) {
                values.put(
                        key,
                        (Boolean) value
                                ? 1
                                : 0
                );

            } else {
                values.put(
                        key,
                        String.valueOf(value)
                );
            }
        }

        return values;
    }

    private void resetAutoIncrement(
            SQLiteDatabase database,
            String tableName
    ) {
        database.execSQL(
                "DELETE FROM sqlite_sequence "
                        + "WHERE name = ?",
                new Object[]{
                        tableName
                }
        );

        database.execSQL(
                "INSERT INTO sqlite_sequence(name, seq) "
                        + "SELECT ?, IFNULL(MAX(id), 0) "
                        + "FROM "
                        + tableName,
                new Object[]{
                        tableName
                }
        );
    }

    // =========================================================
    // THỜI GIAN SAO LƯU GẦN NHẤT
    // =========================================================

    private void saveLastBackupTime(
            String backupTime
    ) {
        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_BACKUP,
                        MODE_PRIVATE
                );

        preferences.edit()
                .putString(
                        KEY_LAST_BACKUP,
                        backupTime
                )
                .apply();
    }

    private void loadLastBackupTime() {
        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_BACKUP,
                        MODE_PRIVATE
                );

        String lastBackup =
                preferences.getString(
                        KEY_LAST_BACKUP,
                        "Chưa có"
                );

        showLastBackupTime(
                lastBackup
        );
    }

    private void showLastBackupTime(
            String backupTime
    ) {
        if (tvLastBackup == null) {
            return;
        }

        if (backupTime == null
                || backupTime.trim().isEmpty()) {

            tvLastBackup.setText(
                    "Chưa có"
            );

            return;
        }

        tvLastBackup.setText(
                backupTime
        );
    }

    // =========================================================
    // HÀM HỖ TRỢ
    // =========================================================

    private String getCurrentDateForFile() {
        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()
                );

        return formatter.format(
                new Date()
        );
    }

    private String getCurrentDateTime() {
        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm:ss",
                        Locale.getDefault()
                );

        return formatter.format(
                new Date()
        );
    }

    private int dp(
            int value
    ) {
        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}