package com.example.doan.fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Pair;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doan.AppSettings;
import com.example.doan.adapter.BinAdapter;
import com.example.doan.adapter.BinMusicAdapter;
import com.example.doan.adapter.BinVideoAdapter;
import com.example.doan.R;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.doan.adapter.MusicAdapter;
import com.example.doan.adapter.MyAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import android.net.Uri;
import android.view.MenuItem;

public class DeleteFragment extends Fragment {

    private View mView;
    private RecyclerView mRecyclerView;
    private BinAdapter mAdapter;
    private BottomNavigationView bottomNavigationView;
    private StorageReference mStorageRef;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private RecyclerView.LayoutManager layoutManager;
    private boolean mIsDarkMode;
    private ActionMode actionMode;
    private BinVideoAdapter adapter;
    private BinMusicAdapter binMusicAdapter;
    private List<String> imageStrings;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mIsDarkMode = AppSettings.getInstance(requireContext()).isDarkMode();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_delete, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mView.setBackgroundColor(requireContext().getColor(mIsDarkMode ? R.color.black : R.color.white));
        layoutManager = new StaggeredGridLayoutManager(2,GridLayoutManager.VERTICAL);

        mRecyclerView = mView.findViewById(R.id.recycler_view1);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        bottomNavigationView= mView.findViewById(R.id.bottom_nav);
        imageStrings = new ArrayList<>();
        mAdapter = new BinAdapter(getActivity(), imageStrings,null);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference().child("delete").child(user.getUid());
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
                        mAdapter = new BinAdapter(getActivity(), imageStrings,null);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        mStorageRef = FirebaseStorage.getInstance().getReference().child("delete").child(user.getUid());

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
                        adapter = new BinVideoAdapter(getActivity(), videoStrings );
                        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
                        mStorageRef = FirebaseStorage.getInstance().getReference().child("deletevideo").child(user1.getUid());

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
                        layoutManager= new StaggeredGridLayoutManager(2, GridLayoutManager.VERTICAL);
                        mRecyclerView.setLayoutManager(layoutManager);
                        List<String> musicStrings = new ArrayList<>();
                        binMusicAdapter = new BinMusicAdapter(getActivity(), musicStrings);
                        FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
                        mStorageRef = FirebaseStorage.getInstance().getReference().child("deletemusic").child(user2.getUid());

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
                                                                    binMusicAdapter.notifyItemInserted(musicStrings.size() - 1);
                                                                });
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
                        mRecyclerView.setAdapter(binMusicAdapter);
                        return true;
                    default:
                        return false;
                }
            }
        });

        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), getString(R.string.nav_delete));
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
        if(bottomNavigationView.getSelectedItemId() == R.id.nav_anh) {
            if (id == R.id.sapxep) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                mStorageRef = FirebaseStorage.getInstance().getReference().child("delete").child(user.getUid());
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
                                                                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss-dd/MM/yyyy", Locale.getDefault());
                                                                try {
                                                                    // Chuyển đổi chuỗi thời gian thành đối tượng Date để so sánh
                                                                    Date date1 = dateFormat.parse(o1.second);
                                                                    Date date2 = dateFormat.parse(o2.second);

                                                                    // So sánh hai đối tượng Date
                                                                    // Đảo ngược để sắp xếp từ mới đến cũ (nếu cần)
                                                                    return date2.compareTo(date1); // So sánh từ mới nhất đến cũ nhất
                                                                } catch (ParseException e) {
                                                                    e.printStackTrace();
                                                                    // Xử lý nếu không thể chuyển đổi thành Date
                                                                    return 0; // Hoặc trả về giá trị phù hợp để xử lý tình huống không mong muốn
                                                                }
                                                            }
                                                        });

                                                        List<String> imageUrls = new ArrayList<>();
                                                        List<String> imageFileNames = new ArrayList<>();

                                                        for (Pair<String, String> pair : imageList) {
                                                            imageUrls.add(pair.first);
                                                            imageFileNames.add(pair.second);
                                                        }

                                                        // Cập nhật Adapter với danh sách đã sắp xếp
                                                        mAdapter = new BinAdapter(getActivity(), imageUrls, imageFileNames);
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
            } else if(bottomNavigationView.getSelectedItemId() == R.id.nav_anh){
                // Chọn tất cả ảnh
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    mAdapter.mSelectedItems.put(i, true);
                }
                mAdapter.notifyDataSetChanged();
                actionMode = mView.startActionMode(mAdapter.getCallback());
            }
            else if(bottomNavigationView.getSelectedItemId() == R.id.nav_music){
                // Chọn tất cả ảnh
                for (int i = 0; i < binMusicAdapter.getItemCount(); i++) {
                    binMusicAdapter.mSelectedItems.put(i, true);
                }
                binMusicAdapter.notifyDataSetChanged();
                actionMode = mView.startActionMode(binMusicAdapter.getCallback());
            }
            return true;
        }
        //Sắp xếp ảnh
        if (id == R.id.grid_mode && bottomNavigationView.getSelectedItemId() == R.id.nav_anh ){
            if (layoutManager instanceof StaggeredGridLayoutManager) {
                layoutManager = new GridLayoutManager(getActivity(),2);
            } else {
                layoutManager= new StaggeredGridLayoutManager(2, GridLayoutManager.VERTICAL);
            }
            mRecyclerView.setLayoutManager(layoutManager);
        }
        return super.onOptionsItemSelected(item);
    }
}