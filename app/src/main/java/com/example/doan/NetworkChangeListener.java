package com.example.doan;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.doan.R;

public class NetworkChangeListener extends BroadcastReceiver {
    private AlertDialog dialog; // Biến để lưu trữ tham chiếu tới hộp thoại

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Util.isNetworkAvailable(context)) { // Kết nối mạng đã khả dụng
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss(); // Đóng hộp thoại nếu đang hiển thị
            }
        } else { // Không có kết nối mạng
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout_dialog = LayoutInflater.from(context).inflate(R.layout.check_internet, null);
            builder.setView(layout_dialog);

            dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
    }
}