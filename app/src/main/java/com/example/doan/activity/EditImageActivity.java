package com.example.doan.activity;
import com.canhub.cropper.CropImageView;
import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.zomato.photofilters.SampleFilters;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubFilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditImageActivity extends AppCompatActivity implements View.OnClickListener {
    static
    {
        System.loadLibrary("NativeImageProcessor");
    }
    private CropImageView edit_image;
    private PhotoView photoView;
    private ImageView boloc1,boloc2,boloc3,boloc4,boloc5;
    private Button cancel, save,cut,edit_cut, confirm_cutter;
    private Bitmap croppedBitmap,orginalbitmap;
    private Button filter;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        photoView = (PhotoView) findViewById(R.id.photo_edit);
        // Giăng cắt ảnh
        edit_image = (CropImageView) findViewById(R.id.edit_image);
        // Hủy chỉnh sửa
        cancel = (Button) findViewById(R.id.cancel);
        // Lưu ảnh chỉnh sửa
        save = (Button) findViewById(R.id.save);
        cut = (Button) findViewById(R.id.buttonCrop);
        edit_cut = (Button) findViewById(R.id.cutter);
        // Nút xác nhận ảnh đã cắt
        confirm_cutter = (Button) findViewById(R.id.confirm_edit);
        // Lấy URL ảnh chỉnh sửa
        String imageUrl = getIntent().getStringExtra("image_url");
        // Hiển thị ảnh
        Picasso.get().load(imageUrl).into(photoView);
        // Vẽ bitmap ảnh gốc
        DrawBitmap();

        // Bộ lọc
        HorizontalScrollView horizontalScrollView = (HorizontalScrollView) findViewById(R.id.thanhboloc);
        boloc1 = (ImageView) findViewById(R.id.boloc1);
        boloc2 = (ImageView) findViewById(R.id.boloc2);
        boloc3 = (ImageView) findViewById(R.id.boloc3);
        boloc4 = (ImageView) findViewById(R.id.boloc4);
        boloc5 = (ImageView) findViewById(R.id.boloc5);
        // Lưu ảnh
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri croppedUri = bitmapToUriConverter(croppedBitmap);
                uploadFiles(croppedUri, "image");
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Mở trình cắt ảnh
        cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoView.setVisibility(View.GONE);
                horizontalScrollView.setVisibility(View.GONE);
                edit_image.setVisibility(View.VISIBLE);
                edit_cut.setVisibility(View.VISIBLE);
                if(croppedBitmap != null) {
                    DrawBitmap();
                    Uri uri = bitmapToUriConverter(orginalbitmap);
                    Picasso.get().load(uri).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    edit_image.setImageBitmap(bitmap);
                                }
                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    finish();
                                    Toast.makeText(EditImageActivity.this, "Lỗi ảnh!", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    // Chuẩn bị tải ảnh (nếu cần)
                                }
                            });
                } else {
                    Picasso.get().load(Uri.parse(imageUrl)).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            edit_image.setImageBitmap(bitmap);
                        }
                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            finish();
                            Toast.makeText(EditImageActivity.this, "Lỗi ảnh!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            // Chuẩn bị tải ảnh (nếu cần)
                        }
                    });
                }

            }
        });

        //Cắt ảnh
        edit_cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                croppedBitmap = edit_image.getCroppedImage();
                edit_image.setImageBitmap(croppedBitmap);
                save.setEnabled(true);
                confirm_cutter.setVisibility(View.VISIBLE);
            }
        });
        confirm_cutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoView.setImageBitmap(croppedBitmap);
                DrawBitmap();
                edit_cut.setVisibility(View.GONE);
                edit_image.setVisibility(View.GONE);
                photoView.setVisibility(View.VISIBLE);
                confirm_cutter.setVisibility(View.GONE);
            }
        });

        // Khởi chạy sự kiện bộ lọc
        filter = (Button) findViewById(R.id.buttonFilter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoView.setVisibility(View.VISIBLE);
                edit_image.setVisibility(View.GONE);
                edit_cut.setVisibility(View.GONE);
                horizontalScrollView.setVisibility(View.VISIBLE);
                confirm_cutter.setVisibility(View.GONE);
            }
        });
        // Nhấn sự kiện các bộ lọc
        boloc1.setOnClickListener(this);
        boloc2.setOnClickListener(this);
        boloc3.setOnClickListener(this);
        boloc4.setOnClickListener(this);
        boloc5.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.boloc1:
                com.zomato.photofilters.imageprocessors.Filter myFilter = SampleFilters.getBlueMessFilter();
                Bitmap image = orginalbitmap.copy(Bitmap.Config.ARGB_8888,true);
                Bitmap outputImage = myFilter.processFilter(image);
                croppedBitmap = outputImage;
                photoView.setImageBitmap(outputImage);
                edit_image.setImageBitmap(outputImage);
                save.setEnabled(true);
                break;
            case R.id.boloc2:
                Filter filter1 = SampleFilters.getStarLitFilter();
                Bitmap image1 = orginalbitmap.copy(Bitmap.Config.ARGB_8888,true);
                Bitmap outputImage1 = filter1.processFilter(image1);
                croppedBitmap = outputImage1;
                photoView.setImageBitmap(outputImage1);
                edit_image.setImageBitmap(outputImage1);
                save.setEnabled(true);
                break;
            case R.id.boloc3:
                Filter filter2 = SampleFilters.getNightWhisperFilter();
                Bitmap image2 = orginalbitmap.copy(Bitmap.Config.ARGB_8888,true);
                Bitmap outputImage2 = filter2.processFilter(image2);
                croppedBitmap = outputImage2;
                photoView.setImageBitmap(outputImage2);
                edit_image.setImageBitmap(outputImage2);
                save.setEnabled(true);
                break;
            case R.id.boloc4:
                Filter filter3 = SampleFilters.getLimeStutterFilter();
                Bitmap image3 = orginalbitmap.copy(Bitmap.Config.ARGB_8888,true);
                Bitmap outputImage3 = filter3.processFilter(image3);
                croppedBitmap = outputImage3;
                photoView.setImageBitmap(outputImage3);
                edit_image.setImageBitmap(outputImage3);
                save.setEnabled(true);
                break;
            case R.id.boloc5:
                Filter filter4 = SampleFilters.getAweStruckVibeFilter();
                Bitmap image4 = orginalbitmap.copy(Bitmap.Config.ARGB_8888,true);
                Bitmap outputImage4 = filter4.processFilter(image4);
                croppedBitmap = outputImage4;
                photoView.setImageBitmap(outputImage4);
                edit_image.setImageBitmap(outputImage4);
                save.setEnabled(true);
                break;
        }
    }
    private void DrawBitmap(){
        BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
        orginalbitmap = drawable.getBitmap();
    }
    private void uploadFiles(Uri croppedUri, String folderName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference storageRef = storage.getReference().child(folderName).child(user.getUid());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timestamp = dateFormat.format(calendar.getTime());

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Đang tải lên");
        progressDialog.setMessage("Vui lòng đợi...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.show();
        String fileName = folderName +"_" + timestamp +"."+getFileExtension(croppedUri);
        StorageReference fileRef = storageRef.child(fileName);
        UploadTask uploadTask = fileRef.putFile(croppedUri);
        setUploadTaskListeners(uploadTask, progressDialog,1, () -> {
            Toast.makeText(this, "Lưu ảnh thành công!", Toast.LENGTH_SHORT).show();
        });
        Intent intent = new Intent(EditImageActivity.this, Option.class);
        startActivity(intent);
    }
    private void setUploadTaskListeners(UploadTask uploadTask, ProgressDialog progressDialog, int totalFiles, Runnable onSuccessAction) {
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.setProgress(progressDialog.getProgress() + 1);
            if (progressDialog.getProgress() == totalFiles) {
                progressDialog.dismiss();
            }
            onSuccessAction.run();
        }).addOnFailureListener(e -> {
            progressDialog.setProgress(progressDialog.getProgress() + 1);
            if (progressDialog.getProgress() == totalFiles) {
                progressDialog.dismiss();
            }
        }).addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
            progressDialog.setProgress((int) progress);
        });
    }
    // Lấy đuôi tệp "JPG"
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver =this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    // Chuyển đổi Bitmap sang URI
    private Uri bitmapToUriConverter(Bitmap bitmap) {
        Uri uri = null;
        try {
            Calendar calendar1 = Calendar.getInstance();
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
            String timestamp1 = dateFormat1.format(calendar1.getTime());

            File file = new File(this.getCacheDir(), "temp_image"+timestamp1+".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            uri = FileProvider.getUriForFile(EditImageActivity.this, "com.example.doan.provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
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