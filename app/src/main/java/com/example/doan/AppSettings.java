package com.example.doan;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {
    private static final String SWITCH_STATE = "switch_state";

    private static AppSettings sInstance;

    private SharedPreferences mSharedPreferences;

    private AppSettings(Context context) {
        mSharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    public static AppSettings getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppSettings(context.getApplicationContext());
        }
        return sInstance;
    }

    public boolean isDarkMode() {
        return mSharedPreferences.getBoolean(SWITCH_STATE, false);
    }

    public void setDarkMode(boolean darkMode) {
        mSharedPreferences.edit().putBoolean(SWITCH_STATE, darkMode).apply();
    }
}