package com.example.doan.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class Dangnhap extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private EditText mEmail;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mRegisterView;
    private CheckBox mRememberCheckBox;
    private boolean mPasswordVisible = false;
    private ProgressDialog progressDialog;

    private ImageButton showPasswordButton;
    private int loginAttempts = 0;
    private long retryTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressDialog = new ProgressDialog(this);
        mEmail = findViewById(R.id.tv_email);
        mPasswordEditText = findViewById(R.id.password_edittext);
        mRememberCheckBox = findViewById(R.id.remember_checkbox);
        mLoginButton = findViewById(R.id.login_button);
        mRegisterView = findViewById(R.id.register_button);

        showPasswordButton = findViewById(R.id.show_password_button);
        showPasswordButton.setOnClickListener(view -> {
            mPasswordVisible = !mPasswordVisible;
            int visibility = mPasswordVisible ? View.VISIBLE : View.GONE;
            mPasswordEditText.setTransformationMethod(visibility == View.VISIBLE ? null : new PasswordTransformationMethod());
            showPasswordButton.setImageResource(visibility == View.VISIBLE ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        });

        mRegisterView.setOnClickListener(v -> {
            startActivity(new Intent(Dangnhap.this, Dangky.class));
        });
        TextView t1 = findViewById(R.id.khoiphucmk);
        t1.setOnClickListener(v -> {
            startActivity(new Intent(Dangnhap.this, Khoiphuc.class));
        });

        // Kiểm tra xem người dùng đã đăng nhập tự động hay chưa
        checkAutoLogin();

        mLoginButton.setOnClickListener(v -> {
            if (System.currentTimeMillis() < retryTime) {
                long remainingTime = (retryTime - System.currentTimeMillis()) / 1000;
                Toast.makeText(Dangnhap.this, getString(R.string.wait_retry, remainingTime), Toast.LENGTH_SHORT).show();
            } else {
                loginUser();
            }
        });
    }

    private void checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
        if (prefs.getBoolean("remember", false)) {
            progressDialog.setMessage(getString(R.string.auto_login));
            progressDialog.setCancelable(false);
            progressDialog.show();
            String email = prefs.getString("email", "");
            String password = prefs.getString("password", "");
            if (isValidUser(email, password)) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.fetchSignInMethodsForEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                SignInMethodQueryResult result = task.getResult();
                                List<String> providers = result.getSignInMethods();
                                if (providers != null && providers.size() > 0) {
                                    if (isValidUser(email, password))
                                        loginUserAutomatically(auth, email, password);
                                } else {
                                    handleAutoLoginFailure(getString(R.string.failed_login1));
                                }
                            } else {
                                Toast.makeText(this, R.string.failed_login2, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
            } else {
                handleAutoLoginFailure(getString(R.string.failed_login3));
            }
        } else {
            Toast.makeText(this, R.string.welcome_login, Toast.LENGTH_SHORT).show();
        }
    }

    private void loginUserAutomatically(FirebaseAuth auth, String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(loginTask -> {
                    if (loginTask.isSuccessful()) {
                        Toast.makeText(Dangnhap.this, R.string.succes_login1, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Dangnhap.this, Option.class));
                        progressDialog.dismiss();
                        finish();
                    } else {
                        handleAutoLoginFailure(getString(R.string.failed_login4));
                    }
                });
    }

    private void handleAutoLoginFailure(String text) {
        Toast.makeText(Dangnhap.this, text, Toast.LENGTH_SHORT).show();
        clearSharedPreferences();
        progressDialog.dismiss();
    }

    private void loginUser() {
        String email = mEmail.getText().toString();
        String password = mPasswordEditText.getText().toString();
        if (isValidUser(email, password)) {
            loginAttempts++;
            if (loginAttempts > 5) {
                retryTime = System.currentTimeMillis() + 30000; // 30 seconds
                loginAttempts = 0;
                showRetryDialog();
                return;
            }

            SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
            if (mRememberCheckBox.isChecked()) {
                editor.putString("email", email);
                editor.putString("password", password);
                editor.putBoolean("remember", true);
            } else {
                editor.clear();
            }
            editor.apply();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> providers = result.getSignInMethods();
                            if (providers != null && providers.size() > 0) {
                                loginUserManually(auth, email, password);
                            } else {
                                handleManualLoginFailure(getString(R.string.failed_login5));
                            }
                        } else {
                            handleManualLoginFailure(getString(R.string.failed_login6));
                        }
                    });
        } else {
            handleManualLoginFailure(getString(R.string.incorrect_data));
        }
    }

    private void loginUserManually(FirebaseAuth auth, String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Dangnhap.this, R.string.succes_login2, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Dangnhap.this, Option.class));
                    } else {
                        showCustomSnackbar(getString(R.string.incorrect_pass));
                        handleManualLoginFailure(getString(R.string.login_failed));
                    }
                });
    }

    private void handleManualLoginFailure(String text) {
        Toast.makeText(Dangnhap.this, text, Toast.LENGTH_SHORT).show();
        mRememberCheckBox.setChecked(false);
        clearSharedPreferences();
    }

    private void clearSharedPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }

    private boolean isValidUser(String email, String password) {
        if (TextUtils.isEmpty(email) || !Dangky.isGmailAddress(email)) {
            showCustomSnackbar(getString(R.string.email_blank));
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6 || !Dangky.containsUpperCaseLetter(password) || !Dangky.containsLowerCaseLetter(password) ||
                !Dangky.containsNumber(password)) {
            showCustomSnackbar(getString(R.string.change_pass));
            return false;
        }
        return true;
    }

    private void showCustomSnackbar(String message) {
        // Tìm CoordinatorLayout gốc để hiển thị Snackbar
        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinator_layout);

        // Tạo một Snackbar
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);

        // Lấy layout của Snackbar để tùy chỉnh
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0); // Loại bỏ padding mặc định
        snackbarLayout.setBackgroundColor(Color.TRANSPARENT); // Đặt nền trong suốt để loại bỏ viền đen

        // Tạo view tùy chỉnh cho Snackbar
        View customView = LayoutInflater.from(this).inflate(R.layout.custom_snackbar, null);

        // Đặt message và icon cho view tùy chỉnh
        TextView textView = customView.findViewById(R.id.snackbar_text);
        textView.setText(message);

        ImageView iconView = customView.findViewById(R.id.snackbar_icon);
        iconView.setImageResource(R.drawable.baseline_error_outline_24); // Đặt icon của bạn tại đây

        Button dismissButton = customView.findViewById(R.id.snackbar_dismiss_button);
        dismissButton.setOnClickListener(v -> snackbar.dismiss());

        // Xóa các view mặc định của Snackbar và thêm view tùy chỉnh
        snackbarLayout.removeAllViews();
        snackbarLayout.addView(customView);

        // Tạo LayoutParams cho Snackbar để định vị nó ở phía trên
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarLayout.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackbarLayout.setLayoutParams(params);

        // Hiển thị Snackbar
        snackbar.show();
    }

    private void showRetryDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.retry_title)
                .setMessage(R.string.retry_message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_exit)
                .setMessage(R.string.quest_exit)
                .setPositiveButton(R.string.yes1, (dialog, which) -> finishAffinity())
                .setNegativeButton(R.string.no1, null)
                .setIcon(R.drawable.warning_icon)
                .show();
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
}
