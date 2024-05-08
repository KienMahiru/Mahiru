package com.example.doan.fragment;
import static com.example.doan.activity.Option.MY_REQUEST_CODE;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doan.AppSettings;
import com.example.doan.activity.Option;
import com.example.doan.adapter.MusicAdapter;
import com.example.doan.R;
import com.example.doan.adapter.MyAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.ClipData;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import com.example.doan.adapter.VideoAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import android.net.Uri;
import android.view.MenuItem;
import android.content.Intent;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.app.ProgressDialog;

public class HomeFragment extends Fragment {
    private static final int REQUEST_CODE_SELECT_IMAGES=3;
    private static final int REQUEST_CODE_SELECT_VIDEO=8;
    private static final int REQUEST_CODE_SELECT_MUSIC=20;
    private static final int REQUEST_STORAGE_PERMISSION = 30;
    private View mView;
    public RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private VideoAdapter adapter;
    private MusicAdapter musicAdapter;
    private StorageReference mStorageRef;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private RecyclerView.LayoutManager layoutManager;
    private GridLayoutManager gridLayoutManager;
    private int successfulUploads = 0;
    private boolean mIsDarkMode;
    private boolean clicked = false;
    private ActionMode actionMode;
    private Button button_video;
    private Button button_image;
    private Button button_music;
    private BottomNavigationView bottomNavigationView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mIsDarkMode = AppSettings.getInstance(requireContext()).isDarkMode();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_home, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Thêm kiểm tra màu nền tối/ sáng
        mView.setBackgroundColor(requireContext().getColor(mIsDarkMode ? R.color.black : R.color.white));

        layoutManager = new StaggeredGridLayoutManager(2,GridLayoutManager.VERTICAL);
        mRecyclerView = mView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        bottomNavigationView= mView.findViewById(R.id.bottom_nav);
        List<String> imageStrings = new ArrayList<>();
        mAdapter = new MyAdapter(getActivity(), imageStrings,null);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("image").child(user.getUid());

        mStorageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        imageStrings.clear();
                        for (StorageReference itemRef : listResult.getItems()) {
                            itemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String uriString = uri.toString();
                                    imageStrings.add(uriString);
                                    mAdapter.notifyItemInserted(imageStrings.size() - 1);
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        mRecyclerView.setAdapter(mAdapter);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.nav_anh:
                        List<String> imageStrings = new ArrayList<>();
                        mAdapter = new MyAdapter(getActivity(), imageStrings,null);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        mStorageRef = FirebaseStorage.getInstance().getReference().child("image").child(user.getUid());

                        mStorageRef.listAll()
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        imageStrings.clear();
                                        for (StorageReference itemRef : listResult.getItems()) {
                                            itemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String uriString = uri.toString();
                                                    imageStrings.add(uriString);
                                                    mAdapter.notifyItemInserted(imageStrings.size() - 1);
                                                }
                                            });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        mRecyclerView.setAdapter(mAdapter);

                        return true;
                    case R.id.nav_video:
                        List<String> videoStrings = new ArrayList<>();
                        adapter = new VideoAdapter(getActivity(), videoStrings);
                        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
                        mStorageRef = FirebaseStorage.getInstance().getReference().child("video").child(user1.getUid());

                        mStorageRef.listAll()
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        videoStrings.clear();
                                        for (StorageReference itemRef : listResult.getItems()) {
                                            itemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String uriString1 = uri.toString();
                                                    videoStrings.add(uriString1);
                                                    adapter.notifyItemInserted(videoStrings.size() - 1);
                                                }
                                            });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        mRecyclerView.setAdapter(adapter);

                        return true;
                    case R.id.nav_music:
                        List<String> musicStrings = new ArrayList<>();
                        musicAdapter = new MusicAdapter(getActivity(), musicStrings);
                        FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
                        mStorageRef = FirebaseStorage.getInstance().getReference().child("music").child(user2.getUid());

                        mStorageRef.listAll()
                                .addOnSuccessListener(listResult -> {
                                            musicStrings.clear();
                                            for (StorageReference prefixRef : listResult.getPrefixes()) {
                                                // Lấy tên folder chứa bài hát
                                                String folderName = prefixRef.getName();
                                                StorageReference folderRef = mStorageRef.child(folderName);

                                                // Lấy danh sách file trong folder
                                                folderRef.listAll()
                                                        .addOnSuccessListener(folderListResult -> {
                                                            for (StorageReference itemRef : folderListResult.getItems()) {
                                                                // Kiểm tra phần mở rộng của tệp và chỉ thêm vào danh sách nếu là file mp3
                                                                itemRef.getMetadata().addOnSuccessListener(metadata -> {
                                                                    String fileName = metadata.getName();
                                                                    if (fileName.toLowerCase().endsWith(".mp3")) {
                                                                        itemRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                                            String uriString = uri.toString();
                                                                            musicStrings.add(uriString);
                                                                            musicAdapter.notifyItemInserted(musicStrings.size() - 1);
                                                                        });
                                                                    }
                                                                });
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
                                            }
                                        })
                                .addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
                        mRecyclerView.setAdapter(musicAdapter);
                        return true;
                    default:
                        return false;
                }
            }
        });

        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), getString(R.string.nav_home));

        Button myButton = mView.findViewById(R.id.my_button);
        button_image = mView.findViewById(R.id.my_button2);
        button_video = mView.findViewById(R.id.my_button3);
        button_music = mView.findViewById(R.id.my_button4);


        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

        button_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectImages();
                handleButtonClickActions();
            }
        });

        button_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVideos();
                handleButtonClickActions();
            }
        });

        button_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectMusics();
                handleButtonClickActions();
            }
        });

        myButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                animateButtons();
                // Thiết lập timeout sau 5 giây
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (!clicked) {
                            // Ẩn các button đã animation
                            hideButtons();
                        }
                        clicked = false;
                    }
                }, 5000);  // Sau 5 giây
                return true;
            }
        });

        return mView;
    }

    private void handleButtonClick() {
        if (bottomNavigationView.getSelectedItemId() == R.id.nav_video) {
            selectVideos();
        } else if (bottomNavigationView.getSelectedItemId() == R.id.nav_anh) {
            // Chọn tải ảnh lên
            selectImages();
        } else {
            selectMusics();
        }
    }

    private void handleButtonClickActions() {
        clicked = true;
        hideButtons();
    }
    private void animateButtons() {
        button_image.setVisibility(View.VISIBLE);
        button_image.animate().rotation(360);
        button_video.setVisibility(View.VISIBLE);
        button_video.animate().rotation(360);
        button_music.setVisibility(View.VISIBLE);
        button_music.animate().rotation(360);
    }
    private void hideButtons() {
        button_video.setVisibility(View.GONE);
        button_image.setVisibility(View.GONE);
        button_music.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actionbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(bottomNavigationView.getSelectedItemId() == R.id.nav_anh) {
            if (id == R.id.sapxep) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                mStorageRef = FirebaseStorage.getInstance().getReference().child("image").child(user.getUid());
                // Lưu trữ danh sách ảnh ban đầu
                mStorageRef.listAll()
                        .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                            @Override
                            public void onSuccess(ListResult listResult) {
                                List<Pair<String, String>> imageList = new ArrayList<>(); // Danh sách tạm thời để lưu trữ cặp giá trị (URL ảnh, thời gian)

                                // Duyệt qua danh sách các tệp ảnh
                                for (StorageReference itemRef : listResult.getItems()) {
                                    String imageName = itemRef.getName();
                                    // Tách chuỗi tên tệp ảnh để lấy ra phần thời gian
                                    String[] parts = imageName.split("_");
                                    if (parts.length >= 3) { // Kiểm tra xem có đủ phần tử sau khi tách không
                                        // Lấy phần thời gian từ chuỗi tên tệp ảnh
                                        String dateString = parts[2].substring(0, 8); // Lấy phần ngày (8 ký tự từ vị trí 0)
                                        String timeString = parts[3].substring(0, 6); // Lấy phần giờ (6 ký tự từ vị trí 8)
                                        String dateTimeString = dateString + "_" + timeString;// Kết hợp phần ngày và phần giờ
                                        try {
                                            // Lấy URL của ảnh và thêm vào danh sách chính
                                            itemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String uriString = uri.toString();
                                                    String dateTimeString = dateString + "_" + timeString;
                                                    String dateString1 = dateTimeString.substring(6, 8) + "/" + dateTimeString.substring(4, 6) + "/" + dateTimeString.substring(0, 4);
                                                    String timeString1 = dateTimeString.substring(9, 11) + ":" + dateTimeString.substring(11, 13) + ":" + dateTimeString.substring(13, 15);

                                                    // Định dạng lại thành định dạng mới
                                                    String formattedDateTime = timeString1 + "-" + dateString1;

                                                    imageList.add(new Pair<>(uriString, formattedDateTime));

                                                    // Nếu đã duyệt qua tất cả các ảnh
                                                    if (imageList.size() == listResult.getItems().size()) {
                                                        // Sắp xếp danh sách theo thời gian từ mới đến cũ
                                                        Collections.sort(imageList, new Comparator<Pair<String, String>>() {
                                                            @Override
                                                            public int compare(Pair<String, String> o1, Pair<String, String> o2) {
                                                                return o2.second.compareTo(o1.second);
                                                            }
                                                        });

                                                        List<String> imageUrls = new ArrayList<>();
                                                        List<String> imageFileNames = new ArrayList<>();

                                                        for (Pair<String, String> pair : imageList) {
                                                            imageUrls.add(pair.first);
                                                            imageFileNames.add(pair.second);
                                                        }

                                                        // Cập nhật Adapter với danh sách đã sắp xếp
                                                        mAdapter = new MyAdapter(getActivity(), imageUrls, imageFileNames);
                                                        mRecyclerView.setAdapter(mAdapter);
                                                    }
                                                }
                                            });
                                        } catch (NumberFormatException e) {
                                            // Xử lý nếu không thể chuyển đổi thời gian thành số
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
        if(id == R.id.chontatca){
            if(bottomNavigationView.getSelectedItemId() == R.id.nav_video){
                // Chọn tất cả video
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    adapter.mSelectedItems.put(i, true);
                }
                adapter.notifyDataSetChanged();
                actionMode = mView.startActionMode(adapter.getCallback());
            } else if (bottomNavigationView.getSelectedItemId() == R.id.nav_anh){
                // Chọn tất cả ảnh
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    mAdapter.mSelectedItems.put(i, true);
                }
                mAdapter.notifyDataSetChanged();
                actionMode = mView.startActionMode(mAdapter.getCallback());
            }
            else{
                // Chọn tất cả nhạc
                for (int i = 0; i < musicAdapter.getItemCount(); i++) {
                    musicAdapter.mSelectedItems.put(i, true);
                }
                musicAdapter.notifyDataSetChanged();
                actionMode = mView.startActionMode(musicAdapter.getCallback());
            }
            return true;
        }
        //Sắp xếp ảnh
        if (id == R.id.grid_mode){
            if (layoutManager instanceof StaggeredGridLayoutManager) {
                 layoutManager = new GridLayoutManager(getActivity(),2);
            } else {
                 layoutManager= new StaggeredGridLayoutManager(2, GridLayoutManager.VERTICAL);
            }
            mRecyclerView.setLayoutManager(layoutManager);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != getActivity().RESULT_OK || data == null) {
            return;
        }

        List<Uri> selectedUris = getSelectedUris(data);

        switch (requestCode) {
            case REQUEST_CODE_SELECT_IMAGES:
                uploadImages(selectedUris);
                break;

            case REQUEST_CODE_SELECT_VIDEO:
                uploadVideos(selectedUris);
                break;

            case REQUEST_CODE_SELECT_MUSIC:
                uploadMusics(selectedUris);
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp
                handleButtonClick();
            } else {
                // Quyền bị từ chối
                Toast.makeText(getActivity(), R.string.confim_asset, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<Uri> getSelectedUris(Intent data) {
        List<Uri> selectedUris = new ArrayList<>();
        ClipData clipData = data.getClipData();

        if (clipData != null) {
            // Trường hợp chọn nhiều ảnh
            int count = clipData.getItemCount();
            for (int i = 0; i < count; i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                selectedUris.add(uri);
            }
        } else {
            // Trường hợp chọn một ảnh
            Uri uri = data.getData();
            selectedUris.add(uri);
        }

        return selectedUris;
    }

    private void uploadFiles(List<Uri> fileUris, String folderName, String successMessage) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference storageRef = storage.getReference().child(folderName).child(user.getUid());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timestamp = dateFormat.format(calendar.getTime());

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(R.string.upload1);
        progressDialog.setMessage(getString(R.string.loading1));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(fileUris.size());
        progressDialog.setProgress(0);
        progressDialog.show();

        int totalFiles = fileUris.size();
        int[] successfulUploads = {0};  // Sử dụng một mảng để thay thế biến final

        for (int i = 0; i < totalFiles; i++) {
            Uri fileUri = fileUris.get(i);
            String fileName = folderName + "_" + i + "_" + timestamp + getFileExtension(fileUri);
            StorageReference fileRef = storageRef.child(fileName);

            UploadTask uploadTask = fileRef.putFile(fileUri);

            setUploadTaskListeners(uploadTask, progressDialog, totalFiles, () -> {
                successfulUploads[0]++;
                if (successfulUploads[0] == totalFiles) {
                    showToast(successMessage);
                }
            });
        }
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
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void showToast(String message) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());
    }

    // Sử dụng các phương thức uploadFiles cho từng loại tệp tin
    private void uploadImages(List<Uri> imageUris) {
        uploadFiles(imageUris, "image", getString(R.string.succes_upimg1));
    }

    private void uploadMusics(List<Uri> musicUris) {
        for (Uri musicUri : musicUris) {
            String musicFileName = getFileNameFromUri(musicUri);
            // Lấy hình ảnh thumbnail từ file mp3
            Bitmap thumbnail = getThumbnailFromMp3(musicUri);
            if (thumbnail != null) {
                // Tạo một tên cho thư mục chứa file mp3 và file hình ảnh
                String folderName = "music_" + System.currentTimeMillis();
                // Upload file mp3 và hình ảnh thumbnail vào thư mục đó
                uploadFilesWithThumbnail(musicUri, thumbnail, folderName);
                Toast.makeText(getActivity(), R.string.succes_upimg2, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFilesWithThumbnail(Uri musicUri, Bitmap thumbnail, String folderName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference storageRef = storage.getReference().child("music").child(user.getUid()).child(folderName);

        // Upload file mp3
        String musicFileName = getFileNameFromUri(musicUri);
        StorageReference musicFileRef = storageRef.child(musicFileName);
        UploadTask musicUploadTask = musicFileRef.putFile(musicUri);
        musicUploadTask.addOnFailureListener(exception -> {
            // Xử lý khi upload file mp3 thất bại
        }).addOnSuccessListener(taskSnapshot -> {
            // Xử lý khi upload file mp3 thành công

            // Upload hình ảnh thumbnail
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumbnailData = baos.toByteArray();
            StorageReference thumbnailRef = storageRef.child("thumbnail.jpg");
            UploadTask thumbnailUploadTask = thumbnailRef.putBytes(thumbnailData);
            thumbnailUploadTask.addOnFailureListener(thumbnailException -> {
                // Xử lý khi upload hình ảnh thumbnail thất bại
            }).addOnSuccessListener(thumbnailTaskSnapshot -> {
                // Xử lý khi upload hình ảnh thumbnail thành công
            });
        });
    }

    private Bitmap getThumbnailFromMp3(Uri musicUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getActivity(), musicUri);
        byte[] data = retriever.getEmbeddedPicture();
        if (data != null) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return null;
    }

    private String getFileNameFromUri(Uri musicUri) {
        Cursor cursor = getActivity().getContentResolver().query(musicUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            String fileName = cursor.getString(index);
            cursor.close();
            return fileName;
        }
        return null;
    }

    private void uploadVideos(List<Uri> videoUris) {
        uploadFiles(videoUris, "video", getString(R.string.succes_upvid));
    }

    private void selectImages() {
        selectMedia("image/*", REQUEST_CODE_SELECT_IMAGES);
    }

    private void selectVideos() {
        selectMedia("video/*", REQUEST_CODE_SELECT_VIDEO);
    }

    private void selectMusics() {
        selectMedia("audio/*", REQUEST_CODE_SELECT_MUSIC);
    }

    private void selectMedia(String mimeType, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, requestCode);
    }
    private void requestPermission() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);

        } else {
            // Quyền đã được chấp thuận, tiến hành xử lý
            handleButtonClick();
        }

    }
}
