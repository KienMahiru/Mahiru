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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.gcacace.signaturepad.views.SignaturePad;
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
import java.util.function.BiPredicate;

import yuku.ambilwarna.AmbilWarnaDialog;

public class EditImageActivity extends AppCompatActivity implements View.OnClickListener {
    static
    {
        System.loadLibrary("NativeImageProcessor");
    }
    private int defaultcolor;
    private CropImageView edit_image;// Ảnh có lưới cắt ảnh
    private PhotoView photoView; // Ảnh
    private ImageView boloc1,boloc2,boloc3,boloc4,boloc5; // Các bộ lọc
    private Button cancel, save; // Nút hủy, lưu
    private ImageButton edit_cut, confirm_cutter; // Các nút trong trình cắt ảnh
    private ImageButton cut,filter, buttonDraw; // Nút mở trình cắt ảnh, lọc ảnh, vẽ ảnh
    private Bitmap croppedBitmap,orginalbitmap; // Ảnh bitmap của ảnh cắt, ảnh gốc
    private LinearLayout draw_pen, tool_draw; // Layout bút vẽ, công cụ vẽ
    private SignaturePad signaturePad; // Bảng vẽ
    private ImageButton eraser, colors, confirm_draw;// Các nút trong trình vẽ ảnh
    private SeekBar seekBar; // Thanh kích cỡ bút vẽ
    private TextView txtsize_pen;// Text hiển thị kích cỡ bút

    // Trình lắng nghe sự kiện mạng
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
        cut = (ImageButton) findViewById(R.id.buttonCrop);
        edit_cut = (ImageButton) findViewById(R.id.cutter);
        // Nút xác nhận ảnh đã cắt
        confirm_cutter = (ImageButton)findViewById(R.id.confirm_edit);
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
                confirm_cutter.setVisibility(View.GONE);
                photoView.setVisibility(View.GONE);
                horizontalScrollView.setVisibility(View.GONE);
                edit_image.setVisibility(View.VISIBLE);
                edit_cut.setVisibility(View.VISIBLE);
                draw_pen.setVisibility(View.GONE);
                tool_draw.setVisibility(View.GONE);
                signaturePad.setVisibility(View.GONE);
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
        filter = (ImageButton) findViewById(R.id.buttonFilter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoView.setVisibility(View.VISIBLE);
                edit_image.setVisibility(View.GONE);
                edit_cut.setVisibility(View.GONE);
                horizontalScrollView.setVisibility(View.VISIBLE);
                confirm_cutter.setVisibility(View.GONE);
                draw_pen.setVisibility(View.GONE);
                tool_draw.setVisibility(View.GONE);
                signaturePad.setVisibility(View.GONE);
            }
        });
        // Nhấn sự kiện các bộ lọc
        boloc1.setOnClickListener(this);
        boloc2.setOnClickListener(this);
        boloc3.setOnClickListener(this);
        boloc4.setOnClickListener(this);
        boloc5.setOnClickListener(this);

        //Bảng vẽ
        signaturePad = (SignaturePad) findViewById(R.id.draw_photo);
        int width = photoView.getWidth();
        int height = photoView.getHeight();

        // Layout size pen
        draw_pen = (LinearLayout) findViewById(R.id.draw_pen);
        // Layout Công cụ
        tool_draw = (LinearLayout) findViewById(R.id.tool_draw);

        // Khởi chạy trình vẽ
        buttonDraw = (ImageButton)findViewById(R.id.buttonDraw);
        buttonDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ẩn các công cụ không liên quan
                edit_image.setVisibility(View.GONE);
                edit_cut.setVisibility(View.GONE);
                horizontalScrollView.setVisibility(View.GONE);
                confirm_cutter.setVisibility(View.GONE);
                photoView.setVisibility(View.GONE);
                // Hiển các công cụ liên quan
                draw_pen.setVisibility(View.VISIBLE);
                tool_draw.setVisibility(View.VISIBLE);
                signaturePad.setVisibility(View.VISIBLE);
                if(croppedBitmap!=null){
                    signaturePad.setSignatureBitmap(croppedBitmap);
                }else{
                    signaturePad.setSignatureBitmap(orginalbitmap);
                }
            }
        });

        // Công cụ màu
        colors = (ImageButton) findViewById(R.id.colors);
        colors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPenColorPicker();
            }
        });

        //Công cụ xóa
        eraser = (ImageButton) findViewById(R.id.eraser);
        eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signaturePad.setSignatureBitmap(orginalbitmap);
            }
        });
        //Công cụ size pen
        seekBar = (SeekBar) findViewById(R.id.size_pen);
        txtsize_pen = (TextView) findViewById(R.id.textsize_pen);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (progress<=0) progress =1;
                txtsize_pen.setText(progress+"dp");
                signaturePad.setMaxWidth(progress);
                seekBar.setMax(50);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        // Xác nhận ảnh vẽ
        confirm_draw =(ImageButton) findViewById(R.id.confirm_draw);
        confirm_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lấy bitmap sau khi vẽ
                Bitmap signaturePad_bitmap = signaturePad.getTransparentSignatureBitmap();
                photoView.setImageBitmap(signaturePad_bitmap);
                photoView.setVisibility(View.VISIBLE);
                croppedBitmap = signaturePad_bitmap;
                orginalbitmap = signaturePad_bitmap;
                // Ẩn các công cụ sau khi vẽ
                draw_pen.setVisibility(View.GONE);
                tool_draw.setVisibility(View.GONE);
                signaturePad.setVisibility(View.GONE);
                // Cho phép lưu
                save.setEnabled(true);
            }
        });
    }

    // Click các bộ lọc
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

    //Mở hộp màu
    private void onPenColorPicker(){
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultcolor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultcolor = color;
                signaturePad.setPenColor(color);
            }
        });
        ambilWarnaDialog.show();
    }

    //Vẽ bitmap dựa vào photoView
    private void DrawBitmap(){
        BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
        orginalbitmap = drawable.getBitmap();
    }

    // Upload vào database
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
        String fileName = folderName +"_0_" + timestamp +"."+getFileExtension(croppedUri);
        StorageReference fileRef = storageRef.child(fileName);
        UploadTask uploadTask = fileRef.putFile(croppedUri);
        setUploadTaskListeners(uploadTask, progressDialog,1, () -> {
            Toast.makeText(this, "Lưu ảnh thành công!", Toast.LENGTH_SHORT).show();
        });
        Intent intent = new Intent(EditImageActivity.this, Option.class);
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