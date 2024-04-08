package com.example.doan.activity;
import static com.example.doan.activity.Option.FRAGMENT_DELETE;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.example.doan.adapter.BinAdapter;
import com.example.doan.fragment.DeleteFragment;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FullscreenImage_bin extends AppCompatActivity {
    private PhotoView mImageView;
    private Button back,back_left,back_right;
    private int position;
    private ArrayList<String> imageUrls;
    private String imageUrl;
    private BottomNavigationView bottom_nav_image;
    private Option option;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image_bin);
        option = new Option();
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        back = (Button) findViewById(R.id.back);
        back_left = (Button) findViewById(R.id.back_left);
        back_right = (Button) findViewById(R.id.back_right);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FullscreenImage_bin.this, Option.class);
                intent.putExtra("Image_DeleteFragment",FRAGMENT_DELETE);
                startActivity(intent);
            }
        });
        initializeView(imageUrls);
        // Thanh điều hướng lựa chọn
        bottom_nav_image = (BottomNavigationView) findViewById(R.id.bottom_nav_bin);
        bottom_nav_image.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete_image_bin:
                        AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenImage_bin.this);
                        builder.setMessage("Bạn có chắc muốn xóa ảnh vĩnh viễn không?")
                                .setPositiveButton("Yes", (dialog, which) -> delete_image_bin(imageUrl, imageUrls))
                                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                .show();
                        return true;
                    case R.id.recovery_image:
                        return true;
                }
                return false;
            }
        });

    }

    private void initializeView(ArrayList<String> imageUrls) {
        mImageView = findViewById(R.id.image_view);
        // Lấy đường dẫn đến ảnh từ Intent

        position = getIntent().getIntExtra("position",0);
        // Load ảnh vào ImageView sử dụng Picasso
        imageUrl =  imageUrls.get(position);
        Picasso.get()
                .load(imageUrl)
                .into(mImageView);
        // Thiết lập khả năng zoom cho ImageView sử dụng PhotoView
        mImageView.setMaximumScale(10);
        mImageView.setMediumScale(5);
        mImageView.setMinimumScale(1);
        mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImageView.setZoomable(true);

        back_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position > 0) {
                    position -= 1;
                    imageUrl = imageUrls.get(position);
                    Picasso.get().load(imageUrl).into(mImageView);
                }
            }
        });

        back_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position < imageUrls.size() - 1) {
                    position += 1;
                    imageUrl = imageUrls.get(position);
                    Picasso.get().load(imageUrl).into(mImageView);
                }
            }
        });
    }
    private void delete_image_bin(String imageUrl, ArrayList<String> imageUrls){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Đang xóa ảnh...");
        progressDialog.show();
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageRef.delete()
                .addOnSuccessListener(aVoid -> {
                    //Xóa ảnh đang hiển thị
                    imageUrls.remove(imageUrl);
                    //Hiển thị ảnh mới
                    if(position>=0 && position< imageUrls.size()){
                        position--;
                        Picasso.get().load(imageUrls.get(position)).into(mImageView);
                    }
                    else{
                        finish();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FullscreenImage_bin.this, "Lỗi xóa ảnh", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> progressDialog.dismiss());
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
    public void onBackPressed() {
        Intent intent = new Intent(FullscreenImage_bin.this, Option.class);
        intent.putExtra("Image_DeleteFragment",FRAGMENT_DELETE);
        startActivity(intent);
    }
}