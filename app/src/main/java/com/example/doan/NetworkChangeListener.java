package com.example.doan;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

public class NetworkChangeListener extends BroadcastReceiver {
    private AlertDialog dialog; // Biến để lưu trữ tham chiếu tới hộp thoại

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Util.isNetworkAvailable(context))  // Kết nối mạng đã khả dụng
            dismissDialog();
        else showDialog(context);
    }

    private void showDialog(Context context) {
        if (dialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout_dialog = LayoutInflater.from(context).inflate(R.layout.check_internet, null);
            builder.setView(layout_dialog);
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.getWindow().setGravity(Gravity.CENTER);
        }

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}