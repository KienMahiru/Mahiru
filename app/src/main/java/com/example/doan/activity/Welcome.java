package com.example.doan.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.content.pm.ActivityInfo;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;

public class Welcome extends AppCompatActivity {
    private ProgressBar mProgressBar;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private boolean isConnected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mProgressBar = findViewById(R.id.progress_bar);
        new Thread(() -> {
            while (!isConnected) {
                // Kiểm tra trạng thái kết nối mạng
                if (isNetworkConnected()) {
                    isConnected = true;
                    runOnUiThread(() -> navigateToDangnhap());
                    break;
                }
                try {
                    Thread.sleep(1000); // Chờ 1 giây trước khi kiểm tra lại
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void navigateToDangnhap() {
        Intent intent = new Intent(Welcome.this, Dangnhap.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}