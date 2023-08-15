package com.example.doan.fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import static com.example.doan.activity.Option.MY_REQUEST_CODE;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.doan.activity.Option;
import com.example.doan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.text.TextUtils;
import java.util.UUID;
public class MyProfileFragment extends Fragment {
    private View view;
    private EditText fullname,email;
    private ImageView imageAva;
    private Button update;
    private Uri uri;
    private Option mOption;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_my_profile,container,false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        progressDialog = new ProgressDialog(getActivity());
        fullname = view.findViewById(R.id.fullname);
        imageAva = view.findViewById(R.id.image_ava);
        mOption= (Option)getActivity();
        email = view.findViewById(R.id.email);
        update = view.findViewById(R.id.update);
        setUser();
        imageAva.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRequestPermission();
            }
        });
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        // Thiết lập tiêu đề mới cho ActionBar
        if (actionBar != null) {
            actionBar.setTitle("Thông tin cá nhân");
        }
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickUpdateProfile();
            }
        });
        return view;
    }
    private void setUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user ==null){
            return;
        }

        fullname.setText(user.getDisplayName());
        email.setText(user.getEmail());
        Glide.with(getActivity()).load(user.getPhotoUrl()).error(R.drawable.logo).into(imageAva);
    }
    private void onClickRequestPermission(){

        if(mOption==null){
            return;
        }
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            mOption.openGallery();
            return;
        }
        if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE )== PackageManager.PERMISSION_GRANTED){
            mOption.openGallery();
        }else{
            String[] permisstions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            getActivity().requestPermissions(permisstions,MY_REQUEST_CODE);

        }
    }
    public void setBitmapImageView(Bitmap bitmapImageView){
        imageAva.setImageBitmap(bitmapImageView);
    }
    public void setUri(Uri uri){
        this.uri=uri;
    }
    private void onClickUpdateProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String name = fullname.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getActivity(), "Vui lòng nhập tên người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Đang cập nhật...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        updateProfileName(name);
        updateProfilePhoto();
    }

    private void updateProfileName(String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Cập nhật tên người dùng thành công!", Toast.LENGTH_SHORT).show();
                            mOption.showInf();
                        } else {
                            Toast.makeText(getActivity(), "Cập nhật tên người dùng thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateProfilePhoto() {
        if (uri == null) {
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("avatar/" + fileName);

        imageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUrl)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    updateProfileData(fileName, downloadUrl.toString());
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Cập nhật ảnh đại diện thất bại!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Tải ảnh lên thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileData(String fileName, String downloadUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.child("avatar").setValue(downloadUrl);
            userRef.child("avatarName").setValue(fileName)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                        mOption.showInf();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Lưu thông tin ảnh đại diện thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

}
