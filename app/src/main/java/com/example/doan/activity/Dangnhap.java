package com.example.doan.activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.text.method.PasswordTransformationMethod;
import android.widget.ImageButton;
import android.content.DialogInterface;
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
import android.text.TextUtils;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import androidx.annotation.NonNull;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Dangnhap extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private EditText mEmail;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private TextView mRegisterView;
    private CheckBox mRememberCheckBox;
    private boolean mPasswordVisible = false;
    private ProgressDialog progressDialog;
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private SignInButton button_google;
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


        // Kiểm tra xem người dùng đã đăng nhập tự động hay chưa
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
                                    // Tài khoản tồn tại
                                    if (isValidUser(email, password)) {


                                        auth.signInWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(loginTask -> {
                                                    if (loginTask.isSuccessful()) {
                                                        // Đăng nhập thành công, chuyển sang màn hình chính
                                                        Toast.makeText(Dangnhap.this, "Đăng nhập tự động thành công!", Toast.LENGTH_SHORT).show();

                                                        Intent intent = new Intent(Dangnhap.this, Option.class);
                                                        startActivity(intent);
                                                        progressDialog.dismiss();
                                                        finish();
                                                    } else {
                                                        // Đăng nhập thất bại
                                                        Toast.makeText(Dangnhap.this, "Đăng nhập tự động thất bại! Hãy kiểm tra lại mật khẩu hoặc mạng của bạn", Toast.LENGTH_SHORT).show();
                                                        SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                                                        editor.clear();
                                                        editor.apply();
                                                        progressDialog.dismiss();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(Dangnhap.this, "Đăng nhập tự động thất bại! Tài khoản đã lưu không tồn tại", Toast.LENGTH_SHORT).show();
                                    SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                                    editor.clear();
                                    editor.apply();
                                    progressDialog.dismiss();
                                }
                            } else {
                                // Lỗi xảy ra khi kiểm tra tài khoản
                                Toast.makeText(this, "Đăng nhập tự động thất bại! Hãy kiểm tra lại tài khoản hoặc mạng của bạn", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
            }
            else{
                Toast.makeText(this, "Đăng nhập tự động thất bại! Hãy kiểm tra lại tài khoản của bạn!", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();
                progressDialog.dismiss();
            }
        }
        else{
            Toast.makeText(this, "Chào mừng bạn tới kho lưu trữ ảnh Mahiru!", Toast.LENGTH_SHORT).show();
        }
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
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();
                if (isValidUser(email, password)) {
                    // Lưu thông tin đăng nhập của người dùng nếu checkbox được đánh dấu
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
                                        // Tài khoản tồn tại
                                        auth.signInWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(Dangnhap.this, new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        if (task.isSuccessful()) {
                                                            // Sign in success, update UI with the signed-in user's information
                                                            // Đăng nhập thành công, chuyển sang màn hình chính
                                                            Toast.makeText(Dangnhap.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(Dangnhap.this, Option.class);
                                                            startActivity(intent);
                                                        } else {
                                                            // If sign in fails, display a message to the user.
                                                            mPasswordEditText.setError("Mật khẩu của bạn không đúng! Vui lòng nhập lại mật khẩu.");
                                                            mRememberCheckBox.setChecked(false);
                                                            SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                                                            editor.clear();
                                                            editor.apply();
                                                        }
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(Dangnhap.this, "Đăng nhập thất bại! Tài khoản này không tồn tại.", Toast.LENGTH_SHORT).show();
                                        mRememberCheckBox.setChecked(false);
                                        editor.clear();
                                        editor.apply();
                                    }
                                } else {
                                    // Lỗi xảy ra khi kiểm tra tài khoản
                                    Toast.makeText(Dangnhap.this, "Đăng nhập thất bại! Hãy kiểm tra lại tài khoản của bạn!", Toast.LENGTH_SHORT).show();
                                    mRememberCheckBox.setChecked(false);
                                    editor.clear();
                                    editor.apply();
                                }
                            });

                } else {
                    // Đăng nhập thất bại, hiển thị thông báo lỗi
                    Toast.makeText(Dangnhap.this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                    mRememberCheckBox.setChecked(false);
                    SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();
                }
            }
        });

        mRegisterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dangnhap.this, Dangky.class);
                startActivity(intent);
            }
        });
        TextView t1= findViewById(R.id.khoiphucmk);
        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Dangnhap.this, Khoiphuc.class);
                startActivity(intent);
            }
        });
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
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thoát ứng dụng")
                .setMessage("Bạn có chắc chắn muốn thoát ứng dụng?")
                .setNegativeButton("Không", null)
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Thoát ứng dụng
                        finishAffinity();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private boolean isValidUser(String email, String password) {
        // Kiểm tra thông tin đăng nhập có hợp lệ hay không
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email không được để trống");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError("Mật khẩu không được để trống");
            mRememberCheckBox.setChecked(false);
            return false;
        }
        else if (password.length() < 6) {
            mPasswordEditText.setError("Mật khẩu phải có ít nhất 6 ký tự");
            mRememberCheckBox.setChecked(false);
            return false;
        }
        else if (!isGmailAddress(email)) {
            mEmail.setError("Email phải là địa chỉ Gmail");
            return false;
        }
        else if (!containsNumber(password)) {
            mPasswordEditText.setError("Mật khẩu phải chứa ít nhất một số");
            return false;
        }
        else if (!containsUpperCaseLetter(password)) {
            mPasswordEditText.setError("Mật khẩu phải chứa ít nhất một ký tự viết hoa");
            mRememberCheckBox.setChecked(false);
            return false;
        } else if (!containsLowerCaseLetter(password)) {
            mPasswordEditText.setError("Mật khẩu phải chứa ít nhất một chữ cái thường");
            mRememberCheckBox.setChecked(false);
            return false;
        }

        return true;
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
    private void Xacthuc(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = mEmail.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, password);

// Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }
}