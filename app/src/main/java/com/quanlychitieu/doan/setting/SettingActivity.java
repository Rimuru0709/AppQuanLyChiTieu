package com.quanlychitieu.doan.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;
import com.quanlychitieu.doan.activity.LoginActivity;
import com.quanlychitieu.doan.bottomnav.BottomNavHelper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingActivity extends AppCompatActivity {

    private static final String PREF_SETTING = "AppSetting";
    private static final String KEY_THEME = "theme";

    private ImageView imgBack;

    private LinearLayout itemAccount;
    private LinearLayout itemBudget;
    private LinearLayout itemReminder;
    private LinearLayout itemBackup;
    private LinearLayout itemTheme;
    private LinearLayout itemLogout;

    private TextView tvThemeValue;

    private SharedPreferences preferences;

    private FirebaseAuth auth;

    private TextView tvLoginLogout;
    private ImageView imgLoginLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        preferences = getSharedPreferences(
                PREF_SETTING,
                MODE_PRIVATE
        );

        auth = FirebaseAuth.getInstance();
        initViews();
        setupHeaderInsets();
        BottomNavHelper.setup(this);
        setupEvents();
        updateSettingValues();
        updateLoginLogoutUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (preferences != null) {
            updateSettingValues();
        }
        updateLoginLogoutUI();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);

        itemAccount = findViewById(R.id.itemAccount);
        itemBudget = findViewById(R.id.itemBudget);
        itemReminder = findViewById(R.id.itemReminder);
        itemBackup = findViewById(R.id.itemBackup);
        itemTheme = findViewById(R.id.itemTheme);
        itemLogout = findViewById(R.id.itemLogout);
        tvThemeValue = findViewById(R.id.tvThemeValue);
        tvLoginLogout = findViewById(R.id.tvLoginLogout);
        imgLoginLogout = findViewById(R.id.imgLoginLogout);
        tvLoginLogout = findViewById(R.id.tvLoginLogout);
        imgLoginLogout = findViewById(R.id.imgLoginLogout);
    }

    private void setupHeaderInsets() {
        View header = findViewById(R.id.headerSetting);

        if (header == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                header,
                (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            dpToPx(16),
                            systemBars.top + dpToPx(8),
                            dpToPx(16),
                            dpToPx(8)
                    );

                    view.getLayoutParams().height =
                            dpToPx(64) + systemBars.top;

                    view.requestLayout();

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(header);
    }

    private void setupEvents() {
        if (imgBack != null) {
            imgBack.setOnClickListener(v -> finish());
        }

        if (itemAccount != null) {
            itemAccount.setOnClickListener(v -> {
                Intent intent = new Intent(
                        SettingActivity.this,
                        AccountActivity.class
                );

                startActivity(intent);
            });
        }

        if (itemBudget != null) {
            itemBudget.setOnClickListener(v -> {
                Intent intent = new Intent(
                        SettingActivity.this,
                        BudgetActivity.class
                );

                startActivity(intent);
            });
        }

        if (itemReminder != null) {
            itemReminder.setOnClickListener(v -> {
                Intent intent = new Intent(
                        SettingActivity.this,
                        ReminderActivity.class
                );

                startActivity(intent);
            });
        }

        if (itemBackup != null) {
            itemBackup.setOnClickListener(v -> {
                Intent intent = new Intent(
                        SettingActivity.this,
                        BackupActivity.class
                );

                startActivity(intent);
            });
        }

        if (itemTheme != null) {
            itemTheme.setOnClickListener(v -> {
                Intent intent = new Intent(
                        SettingActivity.this,
                        ThemeActivity.class
                );

                startActivity(intent);
            });
        }

        if (itemLogout != null) {
            itemLogout.setOnClickListener(v -> {

                FirebaseUser user =
                        auth.getCurrentUser();

                if (user == null) {

                    Intent intent =
                            new Intent(
                                    SettingActivity.this,
                                    LoginActivity.class
                            );

                    startActivity(intent);

                } else {

                    logout();
                }

            });
        }
    }

    private void updateSettingValues() {
        updateThemeValue();
    }

    private void updateThemeValue() {
        if (tvThemeValue == null) {
            return;
        }

        String currentTheme = preferences.getString(
                KEY_THEME,
                "light"
        );

        if ("dark".equals(currentTheme)) {
            tvThemeValue.setText("Tối");

        } else if ("system".equals(currentTheme)) {
            tvThemeValue.setText("Theo hệ thống");

        } else {
            tvThemeValue.setText("Sáng");
        }
    }

    private void logout() {

        auth.signOut();

        Toast.makeText(
                this,
                "Đăng xuất thành công",
                Toast.LENGTH_SHORT
        ).show();

        updateLoginLogoutUI();

        Intent intent =
                new Intent(
                        SettingActivity.this,
                        LoginActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void updateLoginLogoutUI() {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {

            if (tvLoginLogout != null) {
                tvLoginLogout.setText("Đăng nhập");
            }

            if (imgLoginLogout != null) {
                imgLoginLogout.setImageResource(
                        R.drawable.ic_logout
                );
            }

        } else {

            if (tvLoginLogout != null) {
                tvLoginLogout.setText("Đăng xuất");
            }

            if (imgLoginLogout != null) {
                imgLoginLogout.setImageResource(
                        R.drawable.ic_logout
                );
            }

        }
    }
}