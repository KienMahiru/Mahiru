package com.example.doan.activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.ImageView;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

public class FullscreenImageActivity extends AppCompatActivity {
    private PhotoView mImageView;
    private Button back,back_left,back_right;
    private int position;
    private ArrayList<String> imageUrls;
    private String imageUrl;
    private BottomNavigationView bottom_nav_image;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);
        back = (Button) findViewById(R.id.back);
        back_left = (Button) findViewById(R.id.back_left);
        back_right = (Button) findViewById(R.id.back_right);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initializeView();
        // Thanh điều hướng lựa chọn
        bottom_nav_image = (BottomNavigationView) findViewById(R.id.bottom_nav_image);
        bottom_nav_image.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.share_image:
                        return true;
                    case R.id.edit_image:
                        return true;
                    case R.id.delete_image:
                        return true;
                }
                return false;
            }
        });

    }

    private void initializeView() {
        mImageView = findViewById(R.id.image_view);
        // Lấy đường dẫn đến ảnh từ Intent
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");
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
}