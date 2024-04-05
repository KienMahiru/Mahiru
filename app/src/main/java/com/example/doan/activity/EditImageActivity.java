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
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditImageActivity extends AppCompatActivity {
    private CropImageView edit_image;
    private Button cancel, save,cut;
    private Bitmap croppedBitmap;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        edit_image = (CropImageView) findViewById(R.id.edit_image);
        cancel = (Button) findViewById(R.id.cancel);
        save = (Button) findViewById(R.id.save);
        cut = (Button) findViewById(R.id.buttonCrop);
        String imageUrl = getIntent().getStringExtra("image_url");
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
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // Cắt ảnh
        cut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                croppedBitmap = edit_image.getCroppedImage();
                edit_image.setImageBitmap(croppedBitmap);
                save.setEnabled(true);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri croppedUri = bitmapToUriConverter(croppedBitmap);
                uploadFiles(croppedUri, "image");

            }
        });
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
        String fileName = folderName +"_" + timestamp + getFileExtension(croppedUri)+".jpg";
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
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver =this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private Uri bitmapToUriConverter(Bitmap bitmap) {
        Uri uri = null;
        try {
            File file = new File(this.getCacheDir(), "temp_image.jpg");
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