package com.example.doan.activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.squareup.picasso.Picasso;
import com.github.chrisbanes.photoview.PhotoView;

public class FullscreenImageActivity extends AppCompatActivity {
    private PhotoView mImageView;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        mImageView = findViewById(R.id.image_view);

        // Lấy đường dẫn đến ảnh từ Intent
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Load ảnh vào ImageView sử dụng Picasso
        Picasso.get()
                .load(imageUrl)
                .into(mImageView);

        // Thiết lập khả năng zoom cho ImageView sử dụng PhotoView
        mImageView.setMaximumScale(10);
        mImageView.setMediumScale(5);
        mImageView.setMinimumScale(1);
        mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImageView.setZoomable(true);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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