package com.quanlychitieu.doan.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.activity.LoginActivity;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;

public class SettingActivity extends AppCompatActivity {

    private ImageView imgBack;

    private LinearLayout itemBudget, itemReminder, itemBackup, itemSecurity;
    private LinearLayout itemTheme, itemLanguage, itemLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setupHeaderInsets();
        BottomNavHelper.setup(this);
        initViews();
        setupEvents();
    }

    private void setupHeaderInsets() {
        View header = findViewById(R.id.headerSetting);

        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top + dpToPx(8),
                    v.getPaddingRight(),
                    dpToPx(10)
            );

            v.getLayoutParams().height = dpToPx(72) + systemBars.top;
            v.requestLayout();

            return insets;
        });
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);

        itemBudget = findViewById(R.id.itemBudget);
        itemReminder = findViewById(R.id.itemReminder);
        itemBackup = findViewById(R.id.itemBackup);
        itemSecurity = findViewById(R.id.itemSecurity);
        itemTheme = findViewById(R.id.itemTheme);
        itemLanguage = findViewById(R.id.itemLanguage);
        itemLogout = findViewById(R.id.itemLogout);
    }

    private void setupEvents() {
        imgBack.setOnClickListener(v -> finish());

        itemBudget.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, BudgetActivity.class);
            startActivity(intent);
        });

        itemReminder.setOnClickListener(v ->
                Toast.makeText(this, "Mở Nhắc nhở", Toast.LENGTH_SHORT).show()
        );

        itemBackup.setOnClickListener(v ->
                Toast.makeText(this, "Mở Sao lưu & khôi phục", Toast.LENGTH_SHORT).show()
        );

        itemSecurity.setOnClickListener(v ->
                Toast.makeText(this, "Mở Thông tin tài khoản", Toast.LENGTH_SHORT).show()
        );

        itemTheme.setOnClickListener(v ->
                Toast.makeText(this, "Đổi giao diện", Toast.LENGTH_SHORT).show()
        );

        itemLanguage.setOnClickListener(v ->
                Toast.makeText(this, "Chọn ngôn ngữ", Toast.LENGTH_SHORT).show()
        );

        itemLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}