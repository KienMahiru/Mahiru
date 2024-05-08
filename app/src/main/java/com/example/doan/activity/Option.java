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
    public static final int FRAGMENT_DELETE=4;
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
                if(intent==null) return;
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
    private int Image_bin_screen;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupDrawer();
        replaceFragment(new HomeFragment());
        mNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        // Kiểm tra các activity khác intent tới các fragament
        UI_Fragament();
    }
    private void setupDrawer(){
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
    }
    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }else {
            showExitConfirmationDialog();
        }
    }
    private void showExitConfirmationDialog(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_exit)
                .setMessage(R.string.quest_exit)
                .setNegativeButton(R.string.no1, null)
                .setPositiveButton(R.string.yes1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Thoát ứng dụng
                        finishAffinity();
                    }
                })
                .setIcon(R.drawable.warning_icon)
                .show();
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
            showSignOutConfirmationDialog();
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
    private void UI_Fragament(){
        Image_bin_screen = getIntent().getIntExtra("Image_DeleteFragment",0);
        if (Image_bin_screen ==FRAGMENT_DELETE){
            mCurrentFragment = FRAGMENT_DELETE;
            Image_bin_screen=0;
            replaceFragment(new DeleteFragment());
        }
    }
    private void showSignOutConfirmationDialog(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_signout)
                .setMessage(R.string.quest_signout)
                .setPositiveButton(R.string.yes1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        signOutUser();
                    }
                })
                .setNegativeButton(R.string.no1, null)
                .setIcon(R.drawable.warning_icon)
                .show();
    }
    private void signOutUser(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Option.this, Dangnhap.class);
        startActivity(intent);
        SharedPreferences.Editor editor = getSharedPreferences("login", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        Toast.makeText(Option.this, R.string.confim_exit, Toast.LENGTH_SHORT).show();
        finish();
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
                            handleUserInfo(user);
                    } else {
                        // Xử lý lỗi khi tải lại thông tin người dùng
                        Exception exception = task.getException();
                        if (exception != null) {
                            // Hiển thị thông báo lỗi
                            Toast.makeText(Option.this, R.string.error_infor, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
    private void handleUserInfo(FirebaseUser user){
        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();
        if (name == null) {
            tvname.setVisibility(View.GONE);
        } else {
            tvname.setVisibility(View.VISIBLE);
            tvname.setText(name);
        }
        tvemail.setText(email);

        if(photoUrl != null){
            Glide.with(Option.this).load(photoUrl).error(R.mipmap.ic_launcher).into(imageView);
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
                Toast.makeText(this, R.string.confim_asset, Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent,getString(R.string.select_img)));

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