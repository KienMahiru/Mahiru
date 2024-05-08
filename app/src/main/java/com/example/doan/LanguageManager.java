package com.example.doan;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.bumptech.glide.load.engine.Resource;
import com.google.rpc.context.AttributeContext;

import java.util.Locale;

public class LanguageManager {
    private Context ct;
    private SharedPreferences sharedPreferences;

    public LanguageManager(Context ctx){
        this.ct = ctx;
        sharedPreferences = ct.getSharedPreferences("LANG", Context.MODE_PRIVATE);
    }

    public void updateResource(String code){
        Locale locale = new Locale(code);
        Locale.setDefault(locale);

        Resources resources = ct.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Lưu ngôn ngữ đã chọn vào SharedPreferences
        saveLanguage(code);
    }

    private void saveLanguage(String code){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lang", code);
        editor.apply();
    }

    public String getSavedLanguage(){
        return sharedPreferences.getString("lang", Locale.getDefault().getLanguage());
    }
}
