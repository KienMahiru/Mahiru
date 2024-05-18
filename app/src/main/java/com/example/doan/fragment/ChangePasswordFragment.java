package com.example.doan.fragment;

import com.example.doan.activity.Dangky;
import com.example.doan.activity.Dangnhap;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.example.doan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {
    private View view;
    private EditText oldpass, newpass, confirmpass;
    private Button update;
    private boolean mPasswordVisible = false;

    private int passwordAttempts = 0;
    private long retryTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_change_password, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        oldpass = view.findViewById(R.id.oldpassword);
        newpass = view.findViewById(R.id.newpassword);
        confirmpass = view.findViewById(R.id.xacnhan_newpassword);

        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), getString(R.string.nav_change_password));

        ImageButton showPasswordButton = view.findViewById(R.id.show_password_button);

        EditText[] passwordFields = {oldpass, newpass, confirmpass};
        showPasswordButton.setOnClickListener(v -> togglePasswordVisibility(passwordFields, showPasswordButton));

        update = view.findViewById(R.id.capnhat);
        update.setOnClickListener(v -> updateUserPassword());
        return view;
    }

    private void showCustomSnackbar(String message) {
        // Find the root CoordinatorLayout to display the Snackbar
        CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinator_layout1);

        // Create a Snackbar
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_INDEFINITE);

        // Get the layout of the Snackbar to customize it
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0); // Remove default padding
        snackbarLayout.setBackgroundColor(Color.TRANSPARENT); // Set transparent background to remove black border

        // Inflate custom view for the Snackbar
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.custom_snackbar, null);

        // Set message and icon for the custom view
        TextView textView = customView.findViewById(R.id.snackbar_text);
        textView.setText(message);

        ImageView iconView = customView.findViewById(R.id.snackbar_icon);
        iconView.setImageResource(R.drawable.baseline_error_outline_24); // Set your icon here

        Button dismissButton = customView.findViewById(R.id.snackbar_dismiss_button);
        dismissButton.setOnClickListener(v -> snackbar.dismiss());

        // Ensure custom view takes full width
        customView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Remove default views and add custom view
        snackbarLayout.removeAllViews();
        snackbarLayout.addView(customView);

        // Set LayoutParams for Snackbar to position it at the top
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarLayout.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT; // Ensure Snackbar width is full screen
        params.gravity = Gravity.TOP;
        snackbarLayout.setLayoutParams(params);

        // Show Snackbar
        snackbar.show();
    }

    private void togglePasswordVisibility(EditText[] passwordFields, ImageButton showPasswordButton) {
        mPasswordVisible = !mPasswordVisible;

        for (EditText passwordField : passwordFields) {
            passwordField.setTransformationMethod(
                    mPasswordVisible ? null : new PasswordTransformationMethod()
            );
        }
        showPasswordButton.setImageResource(mPasswordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
    }

    private void updateUserPassword() {
        if (System.currentTimeMillis() < retryTime) {
            long remainingTime = (retryTime - System.currentTimeMillis()) / 1000;
            Toast.makeText(getActivity(), getString(R.string.wait_retry, remainingTime), Toast.LENGTH_SHORT).show();
            return;
        }

        String oldPassword = oldpass.getText().toString().trim();
        String newPassword = newpass.getText().toString().trim();
        String confirmPassword = confirmpass.getText().toString().trim();

        if (Kiemtra(oldPassword, newPassword, confirmPassword)) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    passwordAttempts = 0; // Reset attempts on successful reauthentication
                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.succes_changepass, Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getActivity(), Dangnhap.class);
                            startActivity(intent);

                            SharedPreferences.Editor editor = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                        } else {
                            Toast.makeText(getActivity(), R.string.error_uppass, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    passwordAttempts++;
                    if (passwordAttempts > 5) {
                        retryTime = System.currentTimeMillis() + 30000; // 30 seconds cooldown
                        passwordAttempts = 0;
                        showRetryDialog();
                    } else {
                        showCustomSnackbar(getString(R.string.incorrect_oldpass));
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), R.string.error_uppass, Toast.LENGTH_SHORT).show();
        }
    }

    private void showRetryDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.retry_title)
                .setMessage(R.string.retry_message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok_button, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private boolean Kiemtra(String oldpassword, String newpassword, String confirmpassword) {
        // Kiểm tra thông tin nhập vào có hợp lệ hay không
        if (oldpassword.isEmpty()) {
            showCustomSnackbar(getString(R.string.input_oldpass));
            return false;
        }
        if (newpassword.isEmpty()) {
            newpass.setError(getString(R.string.input_newpass));
            return false;
        }
        if (confirmpassword.isEmpty()) {
            confirmpass.setError(getString(R.string.input_oldpass));
            return false;
        }
        if (!newpassword.equals(confirmpassword)) {
            confirmpass.setError(getString(R.string.re_inputpass));
            return false;
        }
        if (newpassword.length() < 6) {
            newpass.setError(getString(R.string.least_6char));
            return false;
        } else if (!Dangky.containsUpperCaseLetter(newpassword)) {
            newpass.setError(getString(R.string.one_upper));
            return false;
        } else if (!Dangky.containsLowerCaseLetter(newpassword)) {
            newpass.setError(getString(R.string.one_lower));
            return false;
        } else if (!Dangky.containsNumber(newpassword)) {
            newpass.setError(getString(R.string.one_number));
            return false;
        }
        return true;
    }
}
