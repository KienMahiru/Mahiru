package com.example.doan.fragment;
import com.example.doan.activity.Dangky;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.doan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), "Đổi mật khẩu");

        ImageButton showPasswordButton = view.findViewById(R.id.show_password_button);

        EditText[] passwordFields = {oldpass, newpass, confirmpass};
        showPasswordButton.setOnClickListener(v -> togglePasswordVisibility(passwordFields, showPasswordButton));

        update = view.findViewById(R.id.capnhat);
        update.setOnClickListener(v -> updateUserPassword());
        return view;
    }

    private void togglePasswordVisibility(EditText[] passwordFields,ImageButton showPasswordButton) {
        mPasswordVisible = !mPasswordVisible;

        for (EditText passwordField : passwordFields) {
            passwordField.setTransformationMethod(
                    mPasswordVisible ? null : new PasswordTransformationMethod()
            );
        }
        showPasswordButton.setImageResource(mPasswordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
    }

    private void updateUserPassword() {
        String oldPassword = oldpass.getText().toString().trim();
        String newPassword = newpass.getText().toString().trim();
        String confirmPassword = confirmpass.getText().toString().trim();

        if (Kiemtra(oldPassword, newPassword, confirmPassword)) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(getActivity(), "Cập nhật mật khẩu thành công! Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getActivity(), Dangnhap.class);
                            startActivity(intent);

                            SharedPreferences.Editor editor = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                        } else {
                            Toast.makeText(getActivity(), "Cập nhật mật khẩu thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    oldpass.setError("Mật khẩu cũ không đúng! Vui lòng nhập lại mật khẩu!");
                }
            });
        } else {
            Toast.makeText(getActivity(), "Cập nhật mật khẩu thất bại!", Toast.LENGTH_SHORT).show();
        }
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
            confirmpass.setError("Hãy nhập lại xác nhận mật khẩu mới");
            return false;
        }
        if(newpassword.length()<6){
            newpass.setError( "Hãy nhập ít nhất 6 ký tự!");
            return false;
        }
        else if (!Dangky.containsUpperCaseLetter(newpassword)) {
            newpass.setError("Mật khẩu phải chứa ít nhất một ký tự viết hoa");
            return false;
        } else if (!Dangky.containsLowerCaseLetter(newpassword)) {
            newpass.setError("Mật khẩu phải chứa ít nhất một chữ cái thường");
            return false;
        }
        else if (!Dangky.containsNumber(newpassword)) {
            newpass.setError("Mật khẩu phải chứa ít nhất một số");
            return false;
        }
        return true;
    }
}