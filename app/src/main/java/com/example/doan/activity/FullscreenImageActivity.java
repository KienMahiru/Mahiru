package com.example.doan.activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.example.doan.adapter.MyAdapter;
import com.example.doan.fragment.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FullscreenImageActivity extends AppCompatActivity {
    private PhotoView mImageView;
    private Button back_left,back_right; // Nút chuyển ảnh trái, phải
    private ImageButton back;
    private int position;//Vị trí ảnh trong List ảnh
    private Context context;
    private MyAdapter mAdapter; // Adapter
    private String imageUrl;// Đường link URL của ảnh
    private ArrayList<String> imageUrls;// List URL ảnh
    private BottomNavigationView bottom_nav_image;// Thanh điều hướng dưới
    private HomeFragment homeFragment; // Giao diện kho

    // Lắng nghe sự kiện mạng
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);
        // Lấy list ảnh
        imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        // Nút quay lại
        back = (ImageButton) findViewById(R.id.back);
        // Nút chuyển ảnh bên trái
        back_left = (Button) findViewById(R.id.back_left);
        // Nút chuyển ảnh bên phải
        back_right = (Button) findViewById(R.id.back_right);
        // Lắng nghe sự kiện nút back
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FullscreenImageActivity.this, Option.class);
                startActivity(intent);
            }
        });
        initializeView(imageUrls);
        // Thanh điều hướng lựa chọn
        bottom_nav_image = (BottomNavigationView) findViewById(R.id.bottom_nav_image);
        bottom_nav_image.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.share_image:
                        shareImage(imageUrl);
                        return true;
                    case R.id.edit_image:
                        // Tạo hộp thoại lựa chọn
                        AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenImageActivity.this);
                        builder.setTitle("Lựa chọn chỉnh sửa ảnh");
                        builder.setItems(new CharSequence[]{"Đơn giản", "Chuyên nghiệp"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Xử lý sự kiện khi người dùng chọn một mục
                                switch (which) {
                                    case 0:
                                        // Xử lý khi chọn Đơn giản
                                        edit_image(imageUrl);
                                        break;
                                    case 1:
                                        // Xử lý khi chọn Chuyên nghiệp
                                        edit_image_pro(imageUrl);
                                        break;
                                }
                            }
                        });

                        // Hiển thị hộp thoại lựa chọn
                        builder.show();

                        return true;
                    case R.id.delete_image:
                        deleteImage(imageUrl, imageUrls);
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

        // Lắng nghe sự kiện nhấn nút chuyển ảnh trái
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

    // Trình chia sẻ ảnh
    private void shareImage(String imageUrl) {
        // Thêm dữ liệu ảnh vào intent
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        try {
            final File localFile = File.createTempFile("image", ".jpg");
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Uri imageUri = FileProvider.getUriForFile(FullscreenImageActivity.this, getPackageName() + ".provider", localFile);
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    startActivity(Intent.createChooser(shareIntent, "Chia sẻ hình ảnh"));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(FullscreenImageActivity.this, "Chia sẻ ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Trình chỉnh sửa ảnh đơn giản
    private void edit_image(String imageUrl) {
        Intent intent = new Intent(FullscreenImageActivity.this, EditImageActivity.class);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }

    // Trình chỉnh sửa ảnh chuyên nghiệp
    private void edit_image_pro(String imageUrl) {
        Intent intent = new Intent(FullscreenImageActivity.this, EditImageProActivity.class);
        intent.putExtra("image_url", imageUrl);
        startActivity(intent);
    }

    // Trình xóa ảnh
    private void deleteImage(String imageUrl, ArrayList<String> imageUrls) {
        // Tạo progressDialog
        ProgressDialog progressDialog = new ProgressDialog(FullscreenImageActivity.this);
        progressDialog.setMessage("Đang xóa ảnh...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Xóa ảnh từ Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        FirebaseUser user  = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference deleteRef = FirebaseStorage.getInstance().getReference().child("delete/" + user.getUid() + "/" + storageRef.getName());

        // Copy file to deleteRef
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                deleteRef.putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            // Delete the original file
                            storageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Xóa URL của ảnh khỏi danh sách imageUrls
                                        imageUrls.remove(imageUrl);

                                        // Hiển thị ảnh mới
                                        if (position >= 0 && position < imageUrls.size()) {
                                            position--;
                                            Picasso.get().load(imageUrls.get(position)).into(mImageView);
                                        } else {
                                            // Không còn ảnh trong danh sách, kết thúc FullscreenImageActivity
                                            finish();
                                        }
                                        // Ẩn progressDialog
                                        progressDialog.dismiss();

                                        // Hiển thị thông báo xóa thành công
                                        Toast.makeText(FullscreenImageActivity.this, "Xóa ảnh thành công", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Xóa ảnh không thành công, hiển thị thông báo lỗi
                                        progressDialog.dismiss();
                                        Toast.makeText(FullscreenImageActivity.this, "Xóa ảnh thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // Copy file không thành công, hiển thị thông báo lỗi
                            progressDialog.dismiss();
                            Toast.makeText(FullscreenImageActivity.this, "Xóa ảnh thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(FullscreenImageActivity.this, Option.class);
        startActivity(intent);
    }
}