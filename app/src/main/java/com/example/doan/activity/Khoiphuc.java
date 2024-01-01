package com.example.doan.activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import java.util.List;
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

        khoiphuc.setOnClickListener(view -> khoiPhucOnClick());

    }
    // Khôi phục tài khoản
    private void khoiPhucOnClick() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String emailAddress = email.getText().toString().trim();
        if (TextUtils.isEmpty(emailAddress) || Dangky.isGmailAddress(emailAddress)) {
            email.setError("Email không hợp lệ");
            return;
        }

        auth.fetchSignInMethodsForEmail(emailAddress).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                List<String> providers = result.getSignInMethods();
                if (providers != null && !providers.isEmpty()) {
                    // Tài khoản tồn tại
                    resetPassword(emailAddress);
                } else {
                    Toast.makeText(Khoiphuc.this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Lỗi xảy ra khi kiểm tra tài khoản
                Toast.makeText(Khoiphuc.this, "Lỗi xảy ra khi kiểm tra tài khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword(String emailAddress) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Khoiphuc.this, "Hãy kiểm tra email để nhập mật khẩu mới!", Toast.LENGTH_SHORT).show();
                        navigateToLoginActivity();
                    }
                });
    }

    // Chuyển hướng đến màn hình đăng nhập
    private void navigateToLoginActivity() {
        Intent intent = new Intent(Khoiphuc.this, Dangnhap.class);
        startActivity(intent);
        finishAffinity();
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