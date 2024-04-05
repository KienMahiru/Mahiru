package com.example.doan.activity;
import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class EditImageActivity extends AppCompatActivity {
    private PhotoView edit_image;
    private Button cancel, save;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        edit_image = findViewById(R.id.edit_image);
        cancel = (Button) findViewById(R.id.cancel);
        save = findViewById(R.id.save);
        String imageUrl = getIntent().getStringExtra("image_url");
        Picasso.get().load(imageUrl).into(edit_image);
        cancel.setOnClickListener(new View.OnClickListener() {
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