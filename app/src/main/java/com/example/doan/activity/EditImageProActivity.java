package com.example.doan.activity;
import androidx.core.content.ContextCompat;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.example.doan.NetworkChangeListener;
import com.example.doan.databinding.ActivityEditImageProBinding;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditImageProActivity extends AppCompatActivity {
    private ActivityEditImageProBinding binding;
    private Uri outputUri;
    private Button save,cancel;
    // Trình lắng nghe sự kiện mạng
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditImageProBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Lấy Url ảnh
        String imageUrl = getIntent().getStringExtra("image_url");
        //Chuyển đổi sang uri
        Uri imageUri = Uri.parse(imageUrl);
        // Intent sang Cửa sổ mới
        Intent dsPhotoEditorIntent = new Intent(this, DsPhotoEditorActivity.class);
        dsPhotoEditorIntent.setData(imageUri);
        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Edited Image");
        startActivityForResult(dsPhotoEditorIntent, 100);
        // Hủy chỉnh sửa
        cancel = (Button) findViewById(R.id.cancel);
        // Lưu ảnh chỉnh sửa
        save = (Button) findViewById(R.id.save);
        // Lưu ảnh
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFiles(outputUri, "image");
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void uploadFiles(Uri outputUri, String folderName) {
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
        String fileName = folderName +"_0_" + timestamp +"."+getFileExtension(outputUri);
        StorageReference fileRef = storageRef.child(fileName);
        UploadTask uploadTask = fileRef.putFile(outputUri);
        setUploadTaskListeners(uploadTask, progressDialog,1, () -> {
            Toast.makeText(this, "Lưu ảnh thành công!", Toast.LENGTH_SHORT).show();
        });
        Intent intent = new Intent(EditImageProActivity.this, Option.class);
        startActivity(intent);
    }
    // Hiển thị UI tác vụ upload
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                outputUri = data.getData();
                binding.editedImage.setImageURI(outputUri);
                save.setEnabled(true);

            }
        }
        else{
           finish();
        }
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