package com.quanlychitieu.doan.app;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class MyApplication extends Application {

    private static final String PREF_SETTING = "AppSetting";
    private static final String KEY_THEME = "theme";

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences preferences =
                getSharedPreferences(PREF_SETTING, MODE_PRIVATE);

        String savedTheme = preferences.getString(
                KEY_THEME,
                "light"
        );

        switch (savedTheme) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES
                );
                break;

            case "system":
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                );
                break;

            case "light":
            default:
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                );
                break;
        }
    }
}