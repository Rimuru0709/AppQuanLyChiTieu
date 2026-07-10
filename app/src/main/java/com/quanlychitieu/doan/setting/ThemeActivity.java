package com.quanlychitieu.doan.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.quanlychitieu.doan.R;

public class ThemeActivity extends AppCompatActivity {

    private static final String PREF_SETTING = "AppSetting";
    private static final String KEY_THEME = "theme";

    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private static final String THEME_SYSTEM = "system";

    private ImageView imgBackTheme;

    private LinearLayout itemLight;
    private LinearLayout itemDark;
    private LinearLayout itemSystem;

    private RadioButton radioLight;
    private RadioButton radioDark;
    private RadioButton radioSystem;

    private SharedPreferences preferences;
    private String selectedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        preferences = getSharedPreferences(
                PREF_SETTING,
                MODE_PRIVATE
        );

        initViews();
        setupHeaderInsets();
        loadCurrentTheme();
        setupEvents();
    }

    private void initViews() {
        imgBackTheme = findViewById(R.id.imgBackTheme);

        itemLight = findViewById(R.id.itemLight);
        itemDark = findViewById(R.id.itemDark);
        itemSystem = findViewById(R.id.itemSystem);

        radioLight = findViewById(R.id.radioLight);
        radioDark = findViewById(R.id.radioDark);
        radioSystem = findViewById(R.id.radioSystem);
    }

    private void setupHeaderInsets() {
        LinearLayout headerTheme = findViewById(R.id.headerTheme);

        if (headerTheme == null) {
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(
                headerTheme,
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

        ViewCompat.requestApplyInsets(headerTheme);
    }

    private void loadCurrentTheme() {
        selectedTheme = preferences.getString(
                KEY_THEME,
                THEME_LIGHT
        );

        updateRadioButtons();
    }

    private void setupEvents() {
        imgBackTheme.setOnClickListener(v -> finish());

        itemLight.setOnClickListener(v ->
                selectTheme(THEME_LIGHT)
        );

        itemDark.setOnClickListener(v ->
                selectTheme(THEME_DARK)
        );

        itemSystem.setOnClickListener(v ->
                selectTheme(THEME_SYSTEM)
        );

        radioLight.setOnClickListener(v ->
                selectTheme(THEME_LIGHT)
        );

        radioDark.setOnClickListener(v ->
                selectTheme(THEME_DARK)
        );

        radioSystem.setOnClickListener(v ->
                selectTheme(THEME_SYSTEM)
        );
    }

    private void selectTheme(String theme) {
        selectedTheme = theme;

        preferences.edit()
                .putString(KEY_THEME, theme)
                .apply();

        updateRadioButtons();
        applyTheme(theme);
    }

    private void updateRadioButtons() {
        radioLight.setChecked(
                THEME_LIGHT.equals(selectedTheme)
        );

        radioDark.setChecked(
                THEME_DARK.equals(selectedTheme)
        );

        radioSystem.setChecked(
                THEME_SYSTEM.equals(selectedTheme)
        );
    }

    private void applyTheme(String theme) {
        switch (theme) {
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                );
                break;

            case THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                );
                break;

            case THEME_LIGHT:
            default:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                );
                break;
        }
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}