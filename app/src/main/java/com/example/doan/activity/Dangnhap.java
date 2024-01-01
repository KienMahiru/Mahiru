package com.example.doan.activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageButton;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.content.IntentFilter;
import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
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

        ImageButton showPasswordButton = findViewById(R.id.show_password_button);
        showPasswordButton.setOnClickListener(view -> {
            mPasswordVisible = !mPasswordVisible;
            int visibility = mPasswordVisible ? View.VISIBLE : View.GONE;
            mPasswordEditText.setTransformationMethod(visibility == View.VISIBLE ? null : new PasswordTransformationMethod());
            showPasswordButton.setImageResource(visibility == View.VISIBLE ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        });

        mRegisterView.setOnClickListener(v -> {
            startActivity(new Intent(Dangnhap.this, Dangky.class));
        });
        TextView t1= findViewById(R.id.khoiphucmk);
        t1.setOnClickListener(v -> {
            startActivity(new Intent(Dangnhap.this, Khoiphuc.class));
        });

        // Kiểm tra xem người dùng đã đăng nhập tự động hay chưa
        checkAutoLogin();

        mLoginButton.setOnClickListener(v -> loginUser());
    }

    private void checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("login", MODE_PRIVATE);
        if (prefs.getBoolean("remember", false)) {
            progressDialog.setMessage("Đang đăng nhập tự động...");
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
                                    if(isValidUser(email, password))
                                        loginUserAutomatically(auth, email, password);
                                } else {
                                    handleAutoLoginFailure("Đăng nhập tự động thất bại! Tài khoản đã lưu không tồn tại");
                                }
                            } else {
                                Toast.makeText(this, "Đăng nhập tự động thất bại! Hãy kiểm tra lại tài khoản hoặc mạng của bạn", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
            } else {
                handleAutoLoginFailure("Đăng nhập tự động thất bại! Hãy kiểm tra lại tài khoản của bạn!");

            }
        } else {
            Toast.makeText(this, "Chào mừng bạn tới kho lưu trữ ảnh Mahiru!", Toast.LENGTH_SHORT).show();
        }
    }
    private void loginUserAutomatically(FirebaseAuth auth, String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(loginTask -> {
                    if (loginTask.isSuccessful()) {
                        Toast.makeText(Dangnhap.this, "Đăng nhập tự động thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Dangnhap.this, Option.class));
                        progressDialog.dismiss();
                        finish();
                    } else {
                        handleAutoLoginFailure("Đăng nhập tự động thất bại! Hãy kiểm tra lại mật khẩu hoặc mạng của bạn");
                    }
                });
    }
    private void handleAutoLoginFailure(String text) {
        Toast.makeText(Dangnhap.this, text, Toast.LENGTH_SHORT).show();
        clearSharedPreferences();
        progressDialog.dismiss();
    }

    private void loginUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        if (isValidUser(email, password)) {
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
                                handleManualLoginFailure("Đăng nhập thất bại! Tài khoản này không tồn tại.");
                            }
                        } else {
                            handleManualLoginFailure("Đăng nhập thất bại! Hãy kiểm tra lại tài khoản của bạn!");
                        }
                    });
        } else {
            handleManualLoginFailure("Tên đăng nhập hoặc mật khẩu không đúng");
        }
    }

    private void loginUserManually(FirebaseAuth auth, String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Dangnhap.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Dangnhap.this, Option.class));
                    } else {
                        mPasswordEditText.setError("Mật khẩu của bạn không đúng! Vui lòng nhập lại mật khẩu.");
                        handleManualLoginFailure("");
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
            mEmail.setError("Email không được để trống và phải là địa chỉ gmail!!!");
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6 || !Dangky.containsUpperCaseLetter(password) || !Dangky.containsLowerCaseLetter(password) ||
                !Dangky.containsNumber(password)) {
            mPasswordEditText.setError("Mật khẩu phải chứa ít nhất 1 chữ cái viết hoa, 1 chữ cái viết thường, 1 số và tối thiểu là 6 ký tự");
            return false;
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thoát ứng dụng")
                .setMessage("Bạn có chắc chắn muốn thoát ứng dụng?")
                .setPositiveButton("Có", (dialog, which) -> finishAffinity())
                .setNegativeButton("Không", null)
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