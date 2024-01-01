package com.example.doan.activity;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.doan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splah extends AppCompatActivity {
    private static final int SPLASH_DELAY = 3000;
    ImageView i1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splah);
        i1 = (ImageView) findViewById(R.id.i1);
        Glide.with(this)
                .load(R.drawable.logo)
                .transform(new CircleCrop())
                .into(i1);

        new Handler().postDelayed(this::nextActivity, SPLASH_DELAY);
    }

    private void nextActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        startActivity(new Intent(splah.this, user == null ? Dangnhap.class : FullscreenImageActivity.class));
    }
}