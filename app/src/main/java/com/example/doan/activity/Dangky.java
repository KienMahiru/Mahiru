package com.example.doan.activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;
import android.text.TextUtils;
import android.net.ConnectivityManager;
import android.content.IntentFilter;

import android.os.Handler;

import androidx.annotation.NonNull;


import android.os.CountDownTimer;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Dangky extends AppCompatActivity {
    private boolean mPasswordVisible = false;
    private EditText mEmail;
    private EditText mPasswordEditText;
    private Button mRegisterButton;
    private ProgressDialog progressDialog;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressDialog = new ProgressDialog(this);
        mEmail = findViewById(R.id.username_edittext);
        mPasswordEditText = findViewById(R.id.password_edittext);
        mRegisterButton = findViewById(R.id.register_button);

        ImageButton showPasswordButton = findViewById(R.id.show_password_button);
        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPasswordVisible = !mPasswordVisible;
                if (mPasswordVisible) {
                    mPasswordEditText.setTransformationMethod(null);
                    showPasswordButton.setImageResource(R.drawable.ic_visibility);
                } else {
                    mPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
                    showPasswordButton.setImageResource(R.drawable.ic_visibility_off);
                }
            }
        });
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();
                if (isValidInput(email, password)) {
                    registerAndVerifyUser(email, password);
                }
            }
        });
    }

    private boolean isValidInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email không được để trống");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError("Mật khẩu không được để trống");
            return false;
        } else if (password.length() < 6) {
            mPasswordEditText.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        } else if (!containsUpperCaseLetter(password)) {
            mPasswordEditText.setError("Mật khẩu phải chứa ít nhất một ký tự viết hoa");
            return false;
        } else if (!containsLowerCaseLetter(password)) {
            mPasswordEditText.setError("Mật khẩu phải chứa ít nhất một chữ cái thường");
            return false;
        } else if (!isGmailAddress(email)) {
            mEmail.setError("Email phải là địa chỉ Gmail");
            return false;
        } else if (!containsNumber(password)) {
            mPasswordEditText.setError("Mật khẩu phải chứa ít nhất một số");
            return false;
        }
        return true;
    }

    private void registerAndVerifyUser(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressDialog.setMessage("Đang chờ xác minh..."); // Hiển thị thông báo chờ xác minh
        progressDialog.setCancelable(false);
        progressDialog.show(); // Hiển thị hộp thoại tiến trình

        // Kiểm tra xem tài khoản đã tồn tại hay chưa
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                List<String> providers = result.getSignInMethods();
                if (providers != null && providers.size() > 0) {
                    // Tài khoản đã tồn tại, hiển thị thông báo
                    Toast.makeText(Dangky.this, "Tài khoản này đã tồn tại!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
                } else {
                    // Tài khoản chưa tồn tại, tiến hành tạo mới
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Dangky.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Tạo tài khoản thành công
                                FirebaseUser user = auth.getCurrentUser();
                                // Gửi email xác minh
                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Gửi email xác minh thành công
                                            Toast.makeText(Dangky.this, "Đã gửi email xác minh. Vui lòng kiểm tra email của bạn!", Toast.LENGTH_SHORT).show();
                                            // Bắt đầu đếm ngược thời gian chờ xác minh
                                            startEmailVerificationCountdown(user);// Đếm ngược chờ xác minh
                                        } else {
                                            // Xử lý thất bại trong việc gửi email xác minh
                                            handleRegistrationFailure(user); // lỗi nên xóa tài khoản lỗi
                                        }
                                    }
                                });
                            } else {
                                // Xử lý thất bại trong việc tạo tài khoản
                                handleRegistrationFailure(null);
                            }
                        }
                    });
                }
            } else {
                // Lỗi xảy ra khi kiểm tra tài khoản
                Toast.makeText(Dangky.this, "Lỗi xảy ra khi kiểm tra tài khoản", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
            }
        });
    }

    // Hàm bắt đầu đếm ngược thời gian chờ xác minh email
    private void startEmailVerificationCountdown(FirebaseUser user) {
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsLeft = millisUntilFinished / 1000;
                String timeLeftFormatted = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60);
                progressDialog.setMessage("Đang chờ xác minh... " + timeLeftFormatted); // Cập nhật thông báo tiến trình
            }

            public void onFinish() {
                // Khi đếm ngược hoàn thành, kiểm tra lại xem tài khoản đã được xác minh hay chưa
                user.reload().addOnCompleteListener(task2 -> {
                    if (user.isEmailVerified()) {
                        // Tài khoản đã được xác minh
                        Toast.makeText(Dangky.this, "Tài khoản đã được xác minh! Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
                        Intent intent = new Intent(Dangky.this, Dangnhap.class);
                        startActivity(intent); // Chuyển đến màn hình đăng nhập
                    } else {
                        // Xử lý khi email xác minh thất bại
                        handleEmailVerificationFailure(user);//Xóa tài khoản đăng kí lỗi
                    }
                });
            }
        }.start(); // Bắt đầu đếm ngược
    }
//
    // Xử lý khi email xác minh thất bại
    private void handleEmailVerificationFailure(FirebaseUser user) {
        Toast.makeText(Dangky.this, "Tài khoản chưa được xác minh. Vui lòng kiểm tra email của bạn và thử lại sau.", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
        if (user != null) {
            // Nếu có tài khoản, xóa tài khoản đã tạo
            user.delete().addOnCompleteListener(task3 -> {
                if (task3.isSuccessful()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Dangky.this, "Đã xóa tài khoản của bạn", Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                }
            });
        }
    }

    // Xử lý khi tạo tài khoản thất bại
    private void handleRegistrationFailure(FirebaseUser user) {
        Toast.makeText(Dangky.this, "Tạo tài khoản thất bại. Vui lòng kiểm tra lại thông tin và thử lại sau.", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
        if (user != null) {
            // Nếu có tài khoản, xóa tài khoản đã tạo
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Dangky.this, "Đã xóa tài khoản của bạn.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Kiểm tra xem mật khẩu có chứa ít nhất một số hay không
    public boolean containsNumber(String password) {
        // Biểu thức chính quy để kiểm tra mật khẩu chứa ít nhất một số
        String numberPattern = ".*\\d.*";

        // Tạo một đối tượng Pattern từ biểu thức chính quy
        Pattern pattern = Pattern.compile(numberPattern);

        // So khớp mật khẩu với biểu thức chính quy
        Matcher matcher = pattern.matcher(password);

        // Trả về true nếu mật khẩu khớp với biểu thức chính quy, ngược lại trả về false
        return matcher.matches();
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
    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }



    private boolean containsUpperCaseLetter(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    // Hàm kiểm tra xem chuỗi có chứa ít nhất một chữ cái thường hay không
    private boolean containsLowerCaseLetter(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }
}
