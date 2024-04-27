package com.example.doan.fragment;
import static com.example.doan.activity.Option.MY_REQUEST_CODE;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.example.doan.adapter.VideoAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private List<String> imageStrings = new ArrayList<>();
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
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        musicStrings.clear();
                                        for (StorageReference itemRef : listResult.getItems()) {
                                            itemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String uriString2 = uri.toString();
                                                    musicStrings.add(uriString2);
                                                    musicAdapter.notifyItemInserted(musicStrings.size() - 1);
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

                        mRecyclerView.setAdapter(musicAdapter);
                        return true;
                    default:
                        return false;
                }
            }
        });

        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), "Home");

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
    private List<String> originalImageStrings = new ArrayList<>();
    private void saveOriginalImageData() {
        originalImageStrings.clear();
        originalImageStrings.addAll(imageStrings);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.sapxep) {
            if (bottomNavigationView.getSelectedItemId() == R.id.nav_anh) {
                saveOriginalImageData();
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

                                                    imageList.add(new Pair<>(uriString, dateTimeString));

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

            } else if (bottomNavigationView.getSelectedItemId() == R.id.nav_video || bottomNavigationView.getSelectedItemId() == R.id.nav_music) {
                imageStrings.clear();
                imageStrings.addAll(originalImageStrings);
                mAdapter.notifyDataSetChanged();// Gọi phương thức để khôi phục lại danh sách ảnh ban đầu
                mRecyclerView.setAdapter(mAdapter);

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
                Toast.makeText(getActivity(), "Vui lòng cấp quyền truy cập!", Toast.LENGTH_SHORT).show();
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
        progressDialog.setTitle("Đang tải lên");
        progressDialog.setMessage("Vui lòng đợi...");
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
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.child("date").setValue(timestamp);
            userRef.child("url").setValue(fileName);
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
        uploadFiles(imageUris, "image", "Tải ảnh lên thành công");


    }

    private void uploadMusics(List<Uri> musicUris) {
        uploadFiles(musicUris, "music", "Tải nhạc lên thành công");

    }

    private void uploadVideos(List<Uri> videoUris) {
        uploadFiles(videoUris, "video", "Tải video lên thành công");
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
