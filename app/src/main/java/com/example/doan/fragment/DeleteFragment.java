package com.example.doan.fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
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
    private FirebaseStorage mStorage;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private boolean mIsDarkMode;
    private ActionMode actionMode;
    private BinVideoAdapter adapter;
    private BinMusicAdapter binMusicAdapter;
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
        if (mIsDarkMode) {
            mView.setBackgroundColor(requireContext().getColor(R.color.black));
        } else {
            mView.setBackgroundColor(requireContext().getColor(R.color.white));
        }
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,GridLayoutManager.VERTICAL);

        mRecyclerView = mView.findViewById(R.id.recycler_view1);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        bottomNavigationView= mView.findViewById(R.id.bottom_nav);
        List<String> imageStrings = new ArrayList<>();
        mAdapter = new BinAdapter(getActivity(), imageStrings);
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
                        mAdapter = new BinAdapter(getActivity(), imageStrings);
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
                        List<String> musicStrings = new ArrayList<>();
                        binMusicAdapter = new BinMusicAdapter(getActivity(), musicStrings );
                        FirebaseUser user2 = FirebaseAuth.getInstance().getCurrentUser();
                        mStorageRef = FirebaseStorage.getInstance().getReference().child("deletemusic").child(user2.getUid());

                        mStorageRef.listAll()
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        musicStrings.clear();
                                        for (StorageReference itemRef : listResult.getItems()) {
                                            itemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    String uriString1 = uri.toString();
                                                    musicStrings.add(uriString1);
                                                    binMusicAdapter.notifyItemInserted(musicStrings.size() - 1);
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

                        mRecyclerView.setAdapter(binMusicAdapter);
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
            actionBar.setTitle("Thùng rác");
        }
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
        return super.onOptionsItemSelected(item);
    }
}