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
import android.os.CountDownTimer;
import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dangky extends AppCompatActivity {
    private static final int COUNTDOWN_INTERVAL = 1000;
    private static final int COUNTDOWN_DURATION = 30000;
    private boolean mPasswordVisible = false;
    private EditText mEmail;
    private EditText mPasswordEditText;
    private Button mRegisterButton;
    private ProgressDialog progressDialog;
    // Class Xử lý khi mất kết nối Internet
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressDialog = new ProgressDialog(this);
        mEmail = findViewById(R.id.username_edittext);//Trường Email
        mPasswordEditText = findViewById(R.id.password_edittext);// Trường Password
        mRegisterButton = findViewById(R.id.register_button);// Nút đăng ký
        //Nút ẩn/hiển password
        ImageButton showPasswordButton = findViewById(R.id.show_password_button);
        //Lắng nghe sự kiện nút ẩn/hiện password
        showPasswordButton.setOnClickListener(view -> {
            mPasswordVisible = !mPasswordVisible;
            int visibility = mPasswordVisible ? View.VISIBLE : View.GONE;
            mPasswordEditText.setTransformationMethod(visibility == View.VISIBLE ? null : new PasswordTransformationMethod());
            showPasswordButton.setImageResource(visibility == View.VISIBLE ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        });
        //Lắng nghe sự kiện nút đăng ký
        mRegisterButton.setOnClickListener(v -> {
            String email = mEmail.getText().toString().trim();
            String password = mPasswordEditText.getText().toString().trim();
            if (isInputValid(email, password)) {
                registerAndVerifyUser(email, password);
            }
        });
    }
    // Kiểm tra đầu vào các trường dữ liệu
    public boolean isInputValid(String email, String password){
        if (TextUtils.isEmpty(email) || !isGmailAddress(email)) {
            mEmail.setError("Email không được để trống và phải là địa chỉ gmail!!!");
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6 || !containsUpperCaseLetter(password) || !containsLowerCaseLetter(password) ||
                !containsNumber(password)) {
            mPasswordEditText.setError("Mật khẩu phải chứa ít nhất 1 chữ cái viết hoa, 1 chữ cái viết thường, 1 số và tối thiểu là 6 ký tự");
            return false;
        }
        return true;
    }
    //Quá trình xác minh tài khoản
    private void registerAndVerifyUser(String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        progressDialog.setMessage("Đang chờ xác minh");
        progressDialog.setCancelable(false);
        progressDialog.show();
        //API Firebase Auth
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                List<String> providers = result.getSignInMethods();
                if (providers != null && providers.size() > 0) {
                    Toast.makeText(Dangky.this, "Tài khoản đã tồn tại!!!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Dangky.this, task1 -> {
                        if (task1.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    Toast.makeText(Dangky.this, "Đã gửi email xác minh", Toast.LENGTH_SHORT).show();
                                    startEmailVerificationCountdown(user);
                                } else {
                                    handleRegistrationFailure(user);
                                }
                            });
                        } else {
                            handleRegistrationFailure(null);
                        }
                    });
                }
            } else {
                Toast.makeText(Dangky.this, "Lỗi xảy ra khi kiểm tra tài khoản", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    // Bắt đầu đếm ngược thời gian lắng nghe xác minh email
    private void startEmailVerificationCountdown(FirebaseUser user) {
        // COUNTDOWN_DURATION = 30000- Thời gian đếm ngược 30s.
        // COUNTDOWN_INTERVAL = 1000- Khoảng cách đếm ngược 1s.
        new CountDownTimer(COUNTDOWN_DURATION, COUNTDOWN_INTERVAL) {
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

    // Xử lý khi email xác minh thất bại
    private void handleEmailVerificationFailure(FirebaseUser user) {
        Toast.makeText(Dangky.this, "Tài khoản chưa được xác minh. Vui lòng kiểm tra email của bạn và thử lại sau.", Toast.LENGTH_SHORT).show();
        progressDialog.dismiss(); // Ẩn hộp thoại tiến trình
        if (user != null) {
            // Nếu có tài khoản, xóa tài khoản đã tạo
            user.delete().addOnCompleteListener(task3 -> {
                if (task3.isSuccessful()) {
                    new Handler().postDelayed(() -> Toast.makeText(Dangky.this, "Đã xóa tài khoản của bạn!!!", Toast.LENGTH_SHORT).show(), 2000);
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
    public static boolean containsNumber(String password) {
        // Kiểm tra mật khẩu chứa ít nhất một số
        return password.matches(".*\\d.*");
    }
    //Tiếp tực nếu có internet
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }
    // Hiển thị Thông báo khi mất mạng
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

    public static boolean isGmailAddress(String text) {
        // Biểu thức chính quy để kiểm tra địa chỉ gmail
        String gmailPattern = "[a-zA-Z0-9._%+-]+@gmail\\.com";

        // Tạo một đối tượng Pattern từ biểu thức chính quy
        Pattern pattern = Pattern.compile(gmailPattern);

        // So khớp đoạn văn bản với biểu thức chính quy
        Matcher matcher = pattern.matcher(text);

        // Trả về true nếu đoạn văn bản khớp với biểu thức chính quy, ngược lại trả về false
        return matcher.matches();
    }

    public static boolean containsUpperCaseLetter(String password) {
        return password.matches(".*[A-Z].*");
    }

    public static boolean containsLowerCaseLetter(String password) {
        return password.matches(".*[a-z].*");
    }
}
