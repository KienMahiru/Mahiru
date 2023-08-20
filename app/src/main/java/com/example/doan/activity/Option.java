package com.example.doan.activity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.content.IntentFilter;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.example.doan.fragment.DeleteFragment;
import com.example.doan.fragment.FeedbackFragment;
import com.example.doan.fragment.InforFragment;
import com.example.doan.fragment.SettingsFragment;
import com.example.doan.fragment.SupportFragment;
import com.google.android.gms.tasks.Task;
import com.bumptech.glide.Glide;
import com.example.doan.fragment.ChangePasswordFragment;
import com.example.doan.fragment.HomeFragment;
import com.example.doan.fragment.MyProfileFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import java.io.IOException;

public class Option extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    public static final int MY_REQUEST_CODE= 10;
    private static final int FRAGMENT_MY_PROFILE= 1;
    private static final int FRAGMENT_HOME=0;
    private static final int FRAGMENT_DELETE=4;
    private static final int FRAGMENT_SETTINGS=5;
    private static final int FRAGMENT_INFOR=6;
    private static final int FRAGMENT_FEED=7;
    private static final int FRAGMENT_CHANGE_PASSWORD= 2;
    private static final int FRAGMENT_SUPPORT= 20;
    private int mCurrentFragment = FRAGMENT_HOME;
    final private MyProfileFragment myProfileFragment = new MyProfileFragment();
    final private ActivityResultLauncher<Intent>mActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()==RESULT_OK) {
                Intent intent = result.getData();
                if(intent==null){return;}
                Uri uri = intent.getData();
                myProfileFragment.setUri(uri);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    myProfileFragment.setBitmapImageView(bitmap);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    });
    private DrawerLayout mDrawerLayout;

    private ImageView imageView;
    private TextView tvname,tvemail;
    private NavigationView mNavigationView;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mNavigationView= findViewById(R.id.navigation_view);
        imageView = mNavigationView.getHeaderView(0).findViewById(R.id.avatar);
        tvname = mNavigationView.getHeaderView(0).findViewById(R.id.tv_name);
        tvemail = mNavigationView.getHeaderView(0).findViewById(R.id.tv_email);


        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        showInf();
        mNavigationView.setNavigationItemSelectedListener(this);
        replaceFragment(new HomeFragment());
        mNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
    }
    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận thoát ứng dụng")
                    .setMessage("Bạn có chắc chắn muốn thoát ứng dụng?")
                    .setNegativeButton("Không", null)
                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Thoát ứng dụng
                            finishAffinity();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        if(id== R.id.nav_home){
            if(mCurrentFragment!= FRAGMENT_HOME){
                replaceFragment(new HomeFragment());
                mCurrentFragment = FRAGMENT_HOME;
            }
        }
        else if(id==R.id.change_password){
            if(mCurrentFragment!= FRAGMENT_CHANGE_PASSWORD){
                replaceFragment(new ChangePasswordFragment());
                mCurrentFragment = FRAGMENT_CHANGE_PASSWORD;
            }
        }
        else if(id==R.id.nav_delete){
            if(mCurrentFragment!= FRAGMENT_DELETE){
                replaceFragment(new DeleteFragment());
                mCurrentFragment = FRAGMENT_DELETE;
            }
        }
        else if(id==R.id.thong_tin_app){
            if(mCurrentFragment!= FRAGMENT_INFOR){
                replaceFragment(new InforFragment());
                mCurrentFragment = FRAGMENT_INFOR;
            }
        }
        else if(id==R.id.tro_giup){
            if(mCurrentFragment!= FRAGMENT_SUPPORT){
                replaceFragment(new SupportFragment());
                mCurrentFragment = FRAGMENT_SUPPORT;
            }
        }
        else if(id==R.id.phan_hoi){
            if(mCurrentFragment!= FRAGMENT_FEED){
                replaceFragment(new FeedbackFragment());
                mCurrentFragment = FRAGMENT_FEED;
            }
        }
        else if(id==R.id.nav_settings){
            if(mCurrentFragment!= FRAGMENT_SETTINGS){
                replaceFragment(new SettingsFragment());
                mCurrentFragment = FRAGMENT_SETTINGS;
            }
        }
        else if (id==R.id.sign_out){
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Đăng xuất người dùng
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(Option.this, Dangnhap.class);
                            startActivity(intent);
                            SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                            Toast.makeText(Option.this, "Bạn đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Ở lại trang hiện tại
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else if(id==R.id.my_profile){
            if(mCurrentFragment!= FRAGMENT_MY_PROFILE){
                replaceFragment(myProfileFragment);
                mCurrentFragment = FRAGMENT_MY_PROFILE;
            }
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void showInf() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // Kiểm tra xem việc tải lại thông tin người dùng thành công hay không
                    if (task.isSuccessful()) {
                        // Thông tin người dùng đã được cập nhật, có thể hiển thị lên giao diện
                        String name = user.getDisplayName();
                        String email = user.getEmail();
                        Uri photoUrl = user.getPhotoUrl();
                        if (name == null) {
                            tvname.setVisibility(View.GONE);
                        } else {
                            tvname.setVisibility(View.VISIBLE);
                        }
                        tvemail.setText(email);
                        tvname.setText(name);
                        Glide.with(Option.this).load(photoUrl).error(R.mipmap.ic_launcher).into(imageView);
                        // ...
                    } else {
                        // Có lỗi xảy ra khi tải lại thông tin người dùng
                        // Hiển thị thông báo lỗi hoặc xử lý lỗi theo cách khác
                    }
                }
            });
        }


    }
    private void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame,fragment);
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==MY_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                openGallery();

            }else{
                Toast.makeText(this, "Vui lòng cấp quyền truy cập!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent,"Chọn Ảnh"));

    }
    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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