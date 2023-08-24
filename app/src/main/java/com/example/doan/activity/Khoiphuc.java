package com.example.doan.activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import androidx.annotation.NonNull;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Khoiphuc extends AppCompatActivity {
    private EditText email;
    private Button khoiphuc;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_khoiphuc);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        email= findViewById(R.id.email);
        khoiphuc = findViewById(R.id.khoiphuc);
        khoiphuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();

                String emailAddress = email.getText().toString().trim();
                if (TextUtils.isEmpty(emailAddress)) {
                    email.setError("Email không được để trống");
                    return;
                }
                else if (!isGmailAddress(emailAddress)) {
                    email.setError("Email phải là địa chỉ Gmail");
                    return;
                }

                auth.fetchSignInMethodsForEmail(emailAddress)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                SignInMethodQueryResult result = task.getResult();
                                List<String> providers = result.getSignInMethods();
                                if (providers != null && providers.size() > 0) {
                                    // Tài khoản tồn tại
                                    auth.sendPasswordResetEmail(emailAddress)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Khoiphuc.this, "Hãy kiểm tra email để nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(Khoiphuc.this, Dangnhap.class);
                                                        startActivity(intent);
                                                        finishAffinity();
                                                    }
                                                }
                                            });

                                } else {
                                    Toast.makeText(Khoiphuc.this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();

                                }
                            } else {
                                // Lỗi xảy ra khi kiểm tra tài khoản
                                Toast.makeText(Khoiphuc.this, "Lỗi xảy ra khi kiểm tra tài khoản", Toast.LENGTH_SHORT).show();
                            }
                        });


            }
        });


    }
    public boolean isGmailAddress(String text) {
        // Biểu thức chính quy để kiểm tra địa chỉ email Gmail
        String gmailPattern = "[a-zA-Z0-9._%+-]+@gmail\\.com";

        // Tạo một đối tượng Pattern từ biểu thức chính quy
        Pattern pattern = Pattern.compile(gmailPattern);

        // So khớp đoạn văn bản với biểu thức chính quy
        Matcher matcher = pattern.matcher(text);

        // Trả về true nếu đoạn văn bản khớp với biểu thức chính quy, ngược lại trả về false
        return matcher.matches();
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