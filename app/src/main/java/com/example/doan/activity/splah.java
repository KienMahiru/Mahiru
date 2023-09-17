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
    ImageView i1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splah);
        i1=(ImageView)findViewById(R.id.i1);
        Glide.with(this)
                .load(R.drawable.logo)
                .transform(new CircleCrop())
                .into(i1);
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextActivity();
            }
        },3000);

    }

    private void nextActivity() {
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user==null) {//chua login
            Intent intent1=new Intent(splah.this,Dangnhap.class);
            startActivity(intent1);
        }else {
            Intent intent=new Intent(splah.this,FullscreenImageActivity.class);
            startActivity(intent);
        }
    }}