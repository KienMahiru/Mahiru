package com.example.doan.fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doan.AppSettings;
import com.example.doan.adapter.MusicAdapter;
import com.example.doan.R;
import com.example.doan.adapter.MyAdapter;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.ClipData;
import java.util.ArrayList;
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
import com.google.firebase.storage.OnProgressListener;


public class HomeFragment extends Fragment {
    private static final int REQUEST_CODE_SELECT_IMAGES=3;
    private static final int REQUEST_CODE_SELECT_VIDEO=8;
    private static final int REQUEST_CODE_SELECT_MUSIC=20;
    private View mView;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private VideoAdapter adapter;
    private MusicAdapter musicAdapter;
    private StorageReference mStorageRef;
    private FirebaseStorage mStorage;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
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
        if (mIsDarkMode) {
            mView.setBackgroundColor(requireContext().getColor(R.color.black));
        } else {
            mView.setBackgroundColor(requireContext().getColor(R.color.white));
        }

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,GridLayoutManager.VERTICAL);
        mRecyclerView = mView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        bottomNavigationView= mView.findViewById(R.id.bottom_nav);
        List<String> imageStrings = new ArrayList<>();
        mAdapter = new MyAdapter(getActivity(), imageStrings);
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
                        mAdapter = new MyAdapter(getActivity(), imageStrings);
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




        // Lấy đối tượng ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        // Thiết lập tiêu đề mới cho ActionBar
        if (actionBar != null) {
            actionBar.setTitle("Home");
        }

        Button myButton = mView.findViewById(R.id.my_button);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Thêm hành động tại đây
                if(bottomNavigationView.getSelectedItemId() == R.id.nav_video){
                    selectVideos();
                } else if (bottomNavigationView.getSelectedItemId() == R.id.nav_anh){
                    // Chọn tải ảnh lên
                    selectImages();
                }
                else{
                    selectMusics();
                }
            }
        });
        button_image= mView.findViewById(R.id.my_button2);
        button_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImages();
                clicked = true;
                button_image.setVisibility(View.GONE);
                button_video.setVisibility(View.GONE);
                button_music.setVisibility(View.GONE);


            }
        });
        button_video = mView.findViewById(R.id.my_button3);
        button_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectVideos();
                clicked = true;
                button_video.setVisibility(View.GONE);
                button_image.setVisibility(View.GONE);
                button_music.setVisibility(View.GONE);

            }
        });
        button_music = mView.findViewById(R.id.my_button4);
        button_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectMusics();
                clicked = true;
                button_video.setVisibility(View.GONE);
                button_image.setVisibility(View.GONE);
                button_music.setVisibility(View.GONE);

            }
        });

        myButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                button_image.setVisibility(View.VISIBLE);
                button_image.animate()

                        .rotation(360);
                button_video.setVisibility(View.VISIBLE);
                button_video.animate()

                        .rotation(360);
                button_music.setVisibility(View.VISIBLE);
                button_music.animate()

                        .rotation(360);
                // Thiết lập timeout sau 5 giây
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (!clicked) {
                            // Ẩn các button đã animation
                            button_video.setVisibility(View.GONE);
                            button_image.setVisibility(View.GONE);
                            button_music.setVisibility(View.GONE);
                        }
                        clicked = false;
                    }
                }, 5000);  // Sau 5 giây
                return true;
            }
        });

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_actionbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
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

                // Chọn tất cả ảnh
                for (int i = 0; i < musicAdapter.getItemCount(); i++) {
                    musicAdapter.mSelectedItems.put(i, true);
                }
                musicAdapter.notifyDataSetChanged();
                actionMode = mView.startActionMode(musicAdapter.getCallback());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGES && resultCode == getActivity().RESULT_OK && data != null) {
            List<Uri> imageUris = new ArrayList<>();

            ClipData clipData = data.getClipData();
            if (clipData != null) {
                // Trường hợp chọn nhiều ảnh
                int count = clipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }
            } else {
                // Trường hợp chọn một ảnh
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
            }
            uploadImages(imageUris);
        }
        else if (requestCode == REQUEST_CODE_SELECT_VIDEO && resultCode == getActivity().RESULT_OK && data != null) {
            List<Uri> videoUris = new ArrayList<>();

            ClipData clipData = data.getClipData();
            if (clipData != null) {
                // Trường hợp chọn nhiều ảnh
                int count = clipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri videoUri = clipData.getItemAt(i).getUri();
                    videoUris.add(videoUri);
                }
            } else {
                // Trường hợp chọn một ảnh
                Uri videoUri = data.getData();
                videoUris.add(videoUri);
            }
            uploadVideos(videoUris);
        }
        else if (requestCode == REQUEST_CODE_SELECT_MUSIC && resultCode == getActivity().RESULT_OK && data != null) {
            List<Uri> musicUris = new ArrayList<>();

            ClipData clipData = data.getClipData();
            if (clipData != null) {
                // Trường hợp chọn nhiều ảnh
                int count = clipData.getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri musicUri = clipData.getItemAt(i).getUri();
                    musicUris.add(musicUri);
                }
            } else {
                // Trường hợp chọn một ảnh
                Uri musicUri = data.getData();
                musicUris.add(musicUri);
            }
            uploadMusics(musicUris);
        }
    }
    private void uploadImages(List<Uri> imageUris) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference storageRef = storage.getReference().child("image").child(user.getUid());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timestamp = dateFormat.format(calendar.getTime());

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Đang tải ảnh lên");
        progressDialog.setMessage("Vui lòng đợi...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(imageUris.size());
        progressDialog.setProgress(0);
        progressDialog.show();

        for (int i = 0; i < imageUris.size(); i++) {
            Uri imageUri = imageUris.get(i);
            String imageName = "image_" + i +"_"+timestamp+ ".jpg";
            StorageReference imageRef = storageRef.child(imageName);

            // Tải ảnh lên Firebase Storage
            UploadTask uploadTask = imageRef.putFile(imageUri);

            // Đăng ký listener để xử lý thành công hoặc thất bại
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Xử lý khi ảnh được tải lên thành công
                    // Tăng giá trị của biến đếm
                    successfulUploads++;

                    // Kiểm tra nếu số lượng ảnh đã được tải lên thành công bằng với số lượng ảnh đã chọn, thì hiển thị Toast
                    if (successfulUploads == imageUris.size()) {
                        Toast.makeText(getActivity(), "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.setProgress(progressDialog.getProgress() + 1);
                    if (progressDialog.getProgress() == progressDialog.getMax()) {
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Xử lý khi có lỗi xảy ra trong quá trình tải ảnh lên
                    progressDialog.setProgress(progressDialog.getProgress() + 1);
                    if (progressDialog.getProgress() == progressDialog.getMax()) {
                        progressDialog.dismiss();
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    // Cập nhật giá trị tiến độ vào ProgressDialog
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setProgress((int)progress);
                }
            });
        }

    }
    private void uploadMusics(List<Uri> musicUris) {
        FirebaseStorage storage2 = FirebaseStorage.getInstance();
        FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference storageRef1 = storage2.getReference().child("music").child(user2.getUid());

        Calendar calendar2 = Calendar.getInstance();
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timestamp2 = dateFormat2.format(calendar2.getTime());

        ProgressDialog progressDialog2 = new ProgressDialog(getActivity());
        progressDialog2.setTitle("Đang tải nhạc lên");
        progressDialog2.setMessage("Vui lòng đợi...");
        progressDialog2.setCancelable(false);
        progressDialog2.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog2.setMax(musicUris.size());
        progressDialog2.setProgress(0);
        progressDialog2.show();

        for (int i = 0; i < musicUris.size(); i++) {
            Uri musicUri = musicUris.get(i);
            String musicName = "music_" + i +"_"+timestamp2+ ".mp3";
            StorageReference imageRef3 = storageRef1.child(musicName);

            // Tải ảnh lên Firebase Storage
            UploadTask uploadTask2 = imageRef3.putFile(musicUri);

            // Đăng ký listener để xử lý thành công hoặc thất bại
            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Xử lý khi ảnh được tải lên thành công
                    // Tăng giá trị của biến đếm
                    successfulUploads++;

                    // Kiểm tra nếu số lượng ảnh đã được tải lên thành công bằng với số lượng ảnh đã chọn, thì hiển thị Toast
                    if (successfulUploads == musicUris.size()) {
                        Toast.makeText(getActivity(), "Tải nhạc lên thành công", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog2.setProgress(progressDialog2.getProgress() + 1);
                    if (progressDialog2.getProgress() == progressDialog2.getMax()) {
                        progressDialog2.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Xử lý khi có lỗi xảy ra trong quá trình tải ảnh lên
                    progressDialog2.setProgress(progressDialog2.getProgress() + 1);
                    if (progressDialog2.getProgress() == progressDialog2.getMax()) {
                        progressDialog2.dismiss();
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    // Cập nhật giá trị tiến độ vào ProgressDialog
                    double progress2 = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog2.setProgress((int)progress2);
                }
            });
        }

    }
    private void uploadVideos(List<Uri> videoUris) {
        FirebaseStorage storage1 = FirebaseStorage.getInstance();
        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
        StorageReference storageRef1 = storage1.getReference().child("video").child(user1.getUid());

        Calendar calendar1 = Calendar.getInstance();
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timestamp1 = dateFormat1.format(calendar1.getTime());

        ProgressDialog progressDialog1 = new ProgressDialog(getActivity());
        progressDialog1.setTitle("Đang tải video lên");
        progressDialog1.setMessage("Vui lòng đợi...");
        progressDialog1.setCancelable(false);
        progressDialog1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog1.setMax(videoUris.size());
        progressDialog1.setProgress(0);
        progressDialog1.show();

        for (int i = 0; i < videoUris.size(); i++) {
            Uri videoUri = videoUris.get(i);
            String videoName = "video_" + i +"_"+timestamp1+ ".mp4";
            StorageReference imageRef1 = storageRef1.child(videoName);

            // Tải ảnh lên Firebase Storage
            UploadTask uploadTask1 = imageRef1.putFile(videoUri);

            // Đăng ký listener để xử lý thành công hoặc thất bại
            uploadTask1.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Xử lý khi ảnh được tải lên thành công
                    // Tăng giá trị của biến đếm
                    successfulUploads++;

                    // Kiểm tra nếu số lượng ảnh đã được tải lên thành công bằng với số lượng ảnh đã chọn, thì hiển thị Toast
                    if (successfulUploads == videoUris.size()) {
                        Toast.makeText(getActivity(), "Tải video lên thành công", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog1.setProgress(progressDialog1.getProgress() + 1);
                    if (progressDialog1.getProgress() == progressDialog1.getMax()) {
                        progressDialog1.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Xử lý khi có lỗi xảy ra trong quá trình tải ảnh lên
                    progressDialog1.setProgress(progressDialog1.getProgress() + 1);
                    if (progressDialog1.getProgress() == progressDialog1.getMax()) {
                        progressDialog1.dismiss();
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    // Cập nhật giá trị tiến độ vào ProgressDialog
                    double progress1 = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog1.setProgress((int)progress1);
                }
            });
        }

    }
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGES);
    }
    private void selectVideos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
    }
    private void selectMusics() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CODE_SELECT_MUSIC);
    }
}