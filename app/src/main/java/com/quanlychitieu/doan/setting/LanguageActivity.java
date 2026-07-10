package com.quanlychitieu.doan.setting;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.quanlychitieu.doan.R;

public class LanguageActivity extends AppCompatActivity {

    private static final String PREF_SETTING = "AppSetting";
    private static final String KEY_LANGUAGE = "language";

    private static final String LANGUAGE_VI = "vi";
    private static final String LANGUAGE_EN = "en";

    private ImageView imgBackLanguage;

    private LinearLayout itemVietnamese;
    private LinearLayout itemEnglish;

    private RadioButton radioVietnamese;
    private RadioButton radioEnglish;

    private TextView btnSaveLanguage;

    private SharedPreferences preferences;
    private String selectedLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences(
                PREF_SETTING,
                MODE_PRIVATE
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        initViews();
        loadCurrentLanguage();
        setupEvents();
    }

    private void initViews() {
        imgBackLanguage = findViewById(R.id.imgBackLanguage);

        itemVietnamese = findViewById(R.id.itemVietnamese);
        itemEnglish = findViewById(R.id.itemEnglish);

        radioVietnamese = findViewById(R.id.radioVietnamese);
        radioEnglish = findViewById(R.id.radioEnglish);

        btnSaveLanguage = findViewById(R.id.btnSaveLanguage);
    }

    private void loadCurrentLanguage() {
        selectedLanguage = preferences.getString(
                KEY_LANGUAGE,
                LANGUAGE_VI
        );

        updateRadioButtons();
    }

    private void setupEvents() {
        imgBackLanguage.setOnClickListener(v -> finish());

        itemVietnamese.setOnClickListener(v -> {
            selectedLanguage = LANGUAGE_VI;
            updateRadioButtons();
        });

        itemEnglish.setOnClickListener(v -> {
            selectedLanguage = LANGUAGE_EN;
            updateRadioButtons();
        });

        radioVietnamese.setOnClickListener(v -> {
            selectedLanguage = LANGUAGE_VI;
            updateRadioButtons();
        });

        radioEnglish.setOnClickListener(v -> {
            selectedLanguage = LANGUAGE_EN;
            updateRadioButtons();
        });

        btnSaveLanguage.setOnClickListener(v ->
                saveLanguage()
        );
    }

    private void updateRadioButtons() {
        radioVietnamese.setChecked(
                LANGUAGE_VI.equals(selectedLanguage)
        );

        radioEnglish.setChecked(
                LANGUAGE_EN.equals(selectedLanguage)
        );
    }

    private void saveLanguage() {
        preferences.edit()
                .putString(
                        KEY_LANGUAGE,
                        selectedLanguage
                )
                .apply();

        LocaleListCompat appLocale =
                LocaleListCompat.forLanguageTags(
                        selectedLanguage
                );

        AppCompatDelegate.setApplicationLocales(
                appLocale
        );

        Toast.makeText(
                this,
                "Đã cập nhật ngôn ngữ",
                Toast.LENGTH_SHORT
        ).show();
    }
}