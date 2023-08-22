package com.example.doan.fragment;

import com.example.doan.activity.Dangnhap;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.doan.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordFragment extends Fragment {
    private View view;
    private EditText oldpass,newpass,confirmpass;
    private Button update;
    private boolean mPasswordVisible = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_change_password,container,false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        oldpass= view.findViewById(R.id.oldpassword);
        newpass = view.findViewById(R.id.newpassword);
        confirmpass= view.findViewById(R.id.xacnhan_newpassword);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        // Thiết lập tiêu đề mới cho ActionBar
        if (actionBar != null) {
            actionBar.setTitle("Đổi mật khẩu");
        }
        ImageButton showPasswordButton = view.findViewById(R.id.show_password_button);
        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPasswordVisible = !mPasswordVisible;
                if (mPasswordVisible) {
                    oldpass.setTransformationMethod(null);
                    newpass.setTransformationMethod(null);
                    confirmpass.setTransformationMethod(null);
                    showPasswordButton.setImageResource(R.drawable.ic_visibility);
                } else {
                    oldpass.setTransformationMethod(new PasswordTransformationMethod());
                    newpass.setTransformationMethod(new PasswordTransformationMethod());
                    confirmpass.setTransformationMethod(new PasswordTransformationMethod());
                    showPasswordButton.setImageResource(R.drawable.ic_visibility_off);
                }
            }
        });
        update = view.findViewById(R.id.capnhat);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldpassword= oldpass.getText().toString().trim();
                String newpassword= newpass.getText().toString().trim();
                String confirmpassword= confirmpass.getText().toString().trim();
                if(Kiemtra(oldpassword,newpassword,confirmpassword)){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldpassword);
                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Xác thực thành công, cho phép thay đổi mật khẩu mới
                                user.updatePassword(newpassword)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "Cập nhật mật khẩu thành công!Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                                                    FirebaseAuth.getInstance().signOut();
                                                    Intent intent = new Intent(getActivity(), Dangnhap.class);
                                                    startActivity(intent);
                                                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE).edit();
                                                    editor.clear();
                                                    editor.apply();
                                                } else {
                                                    Toast.makeText(getActivity(), "Cập nhật mật khẩu thất bại!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // Xác thực thất bại, hiển thị thông báo lỗi
                                oldpass.setError("Mật khẩu cũ không đúng! Vui lòng nhập lại mật khẩu!");
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "Cập nhật mật khẩu thất bại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private boolean Kiemtra(String oldpassword,String newpassword, String confirmpassword){
        // Kiểm tra thông tin nhập vào có hợp lệ hay không
        if (oldpassword.isEmpty()) {
            oldpass.setError("Hãy nhập mật khẩu cũ");
            return false;
        }
        if (newpassword.isEmpty()) {
            newpass.setError("Hãy nhập mật khẩu mới");
            return false;
        }
        if (confirmpassword.isEmpty()) {
            confirmpass.setError("Hãy nhập mật khẩu cũ");
            return false;
        }
        if (!newpassword.equals(confirmpassword)) {
            confirmpass.setError("Hãy nhập lại mật khẩu mới hoặc xác nhận mật khẩu mới");
            return false;
        }
        if(newpassword.length()<6){
            newpass.setError( "Hãy nhập ít nhất 6 ký tự!");
            return false;
        }
        else if (!containsUpperCaseLetter(newpassword)) {
            newpass.setError("Mật khẩu phải chứa ít nhất một ký tự viết hoa");
            return false;
        } else if (!containsLowerCaseLetter(newpassword)) {
            newpass.setError("Mật khẩu phải chứa ít nhất một chữ cái thường");
            return false;
        }
        else if (!containsNumber(newpassword)) {
            newpass.setError("Mật khẩu phải chứa ít nhất một số");
            return false;
        }
        return true;
    }

    private boolean containsUpperCaseLetter(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }
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
    private boolean containsLowerCaseLetter(String password) {
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }
}