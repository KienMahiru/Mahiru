package com.example.doan.adapter;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doan.activity.FullscreenMusicActivity;
import com.example.doan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.util.List;

public class BinMusicAdapter extends RecyclerView.Adapter<BinMusicAdapter.BinMusicViewHolder> {
    private List<String> mMusicUrls;
    private Context mContext;
    private Picasso mPicasso;
    public SparseBooleanArray mSelectedItems;
    private ActionMode actionMode;
    private String musicTitle;
    public BinMusicAdapter(Context context, List<String> musicUrls) {
        mMusicUrls = musicUrls;
        mContext = context;
        mPicasso = Picasso.get();
        mSelectedItems = new SparseBooleanArray();
    }
    public ActionMode.Callback getCallback() {
        return callback;
    }
    @NonNull
    @Override
    public BinMusicAdapter.BinMusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_item_layout2, parent, false);
        return new BinMusicAdapter.BinMusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BinMusicAdapter.BinMusicViewHolder holder, int position) {
        String musicUrl = mMusicUrls.get(position);

        // Phần xử lý lấy title
        handleMusicTitle(holder, musicUrl);

        // Phần xử lý Picasso
        mPicasso.load(musicUrl)
                .placeholder(R.drawable.placeholder_music)
                .into(holder.myMusicView);

        // Phần xử lý trạng thái ảnh và sự kiện click
        handleMusicStateAndClick(holder, musicUrl, position);
    }

    private void handleMusicTitle(BinMusicAdapter.BinMusicViewHolder holder, String musicUrl) {
        getMusicTitleFromFirebaseStorage(musicUrl)
                .addOnSuccessListener(musicTitle -> {
                    holder.musicName.setText(musicTitle);
                })
                .addOnFailureListener(exception -> {
                    // Xử lý khi không thể lấy tên nhạc
                });
    }

    private void handleMusicStateAndClick(BinMusicAdapter.BinMusicViewHolder holder, String musicUrl, int position) {
        boolean isSelected = mSelectedItems.get(position);

        // Xử lý trạng thái của music
        handleMusicState(holder, isSelected);

        // Xử lý sự kiện click
        handleMusicClick(holder, musicUrl);
        // Xử lý sự kiện long click
        handleMusicLongClick(holder, position);
    }

    private void handleMusicState(BinMusicAdapter.BinMusicViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.checkView.setVisibility(View.VISIBLE);
            holder.myMusicView.setTag("selected");
        } else {
            holder.checkView.setVisibility(View.GONE);
            holder.myMusicView.setTag(null);
        }
    }

    private void handleMusicClick(BinMusicAdapter.BinMusicViewHolder holder, String musicUrl) {
        holder.myMusicView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, FullscreenMusicActivity.class);
            intent.putExtra("musicUrl", musicUrl);
            mContext.startActivity(intent);
        });
    }

    private void handleMusicLongClick(BinMusicAdapter.BinMusicViewHolder holder, int position) {
        holder.myMusicView.setOnLongClickListener(view -> {
            boolean isSelected = mSelectedItems.get(position);
            if (isSelected) {
                holder.checkView.setVisibility(View.GONE);
                mSelectedItems.delete(position);
            } else {
                isSelected = true;
                mSelectedItems.put(position, isSelected);
                holder.checkView.setVisibility(View.VISIBLE);
                holder.myMusicView.setTag("selected");
                if (actionMode == null && mSelectedItems.size() == 1) {
                    actionMode = view.startActionMode(callback);
                }
            }

            if (mSelectedItems.size() == 0) {
                actionMode.finish();
                actionMode = null;
            }
            return true;
        });
    }
    private ActionMode.Callback callback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_action_bar_1, menu);
            // Check if at least one item is selected
            boolean hasSelection = hasSelectedItems();

            // If no items are selected, hide the Contextual action bar
            if (!hasSelection) {
                mode.finish();
                return false;
            }

            return true;
        }
        private boolean hasSelectedItems() {
            for (int i = 0; i < mSelectedItems.size(); i++) {
                if (mSelectedItems.valueAt(i)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete_1:
                    showDeleteConfirmationDialog();
                    return true;

                case R.id.khoi_phuc_anh:
                    showRestoreConfirmationDialog();
                    return true;

                default:
                    return false;
            }
        }

        private void showDeleteConfirmationDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("Bạn có chắc muốn xóa vĩnh viễn nhạc đã chọn không?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteSelectedMusic();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Không làm gì cả
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void deleteSelectedMusic() {
            for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                int position = mSelectedItems.keyAt(i);
                if (mSelectedItems.get(position)) {
                    deleteMusicTask(position);
                }
            }
            actionMode.finish();
        }

        private void deleteMusicTask(final int position) {
            // Logic xóa nhạc
            final String musicUrl = mMusicUrls.get(position);
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);

            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (position < mMusicUrls.size()) {
                        mMusicUrls.remove(position);
                        mSelectedItems.delete(position);
                        notifyDataSetChanged();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    handleDeleteFailure(position);
                }
            });
        }

        private void handleDeleteFailure(int position) {
            Toast.makeText(mContext, "Lỗi xóa nhạc", Toast.LENGTH_SHORT).show();
        }

        private void showRestoreConfirmationDialog() {
            ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Đang hoàn tác nhạc");
            progressDialog.show();

            for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                int position = mSelectedItems.keyAt(i);
                if (mSelectedItems.get(position)) {
                    restoreMusicTask(position, progressDialog);
                }
            }
        }

        private void restoreMusicTask(final int position, final ProgressDialog progressDialog) {
            final String musicUrl = mMusicUrls.get(position);
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    uploadRestoredMusic(bytes, storageRef, position, progressDialog);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    handleRestoreFailure(position, progressDialog);
                }
            });
        }

        private void uploadRestoredMusic(byte[] bytes, StorageReference storageRef, final int position, final ProgressDialog progressDialog) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            StorageReference restoreRef = FirebaseStorage.getInstance().getReference().child("music/" + user.getUid() + "/" + storageRef.getName());

            restoreRef.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    handleRestoreSuccess(storageRef, position, progressDialog);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    handleRestoreFailure(position, progressDialog);
                }
            });
        }

        private void handleRestoreSuccess(StorageReference storageRef, final int position, ProgressDialog progressDialog) {
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (position < mMusicUrls.size()) {
                        mMusicUrls.remove(position);
                        mSelectedItems.delete(position);
                        notifyDataSetChanged();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    handleRestoreFailure(position, progressDialog);
                }
            });
        }

        private void handleRestoreFailure(int position, ProgressDialog progressDialog) {
            Toast.makeText(mContext, "Lỗi hoàn tác nhạc", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Clear all selected items
            mSelectedItems.clear();
            // Update the UI
            notifyDataSetChanged();
        }
    };


    private Task<String> getMusicTitleFromFirebaseStorage(String musicUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(musicUrl);

        return storageRef.getMetadata().continueWith(task -> {
            if (task.isSuccessful()) {
                StorageMetadata storageMetadata = task.getResult();
                if (storageMetadata != null) {
                    return storageMetadata.getName();
                } else {
                    // Xử lý khi không có thông tin metadata
                    return null;
                }
            } else {
                // Xử lý khi không thể lấy thông tin metadata
                Exception exception = task.getException();
                // ...
                return null;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMusicUrls.size();
    }
    public static class BinMusicViewHolder extends RecyclerView.ViewHolder {
        ImageView myMusicView;
        ImageView checkView;
        TextView musicName;
        public BinMusicViewHolder(View itemView) {
            super(itemView);
            myMusicView = itemView.findViewById(R.id.my_music_view);
            checkView = itemView.findViewById(R.id.check_view);
            musicName = itemView.findViewById(R.id.music_name);
        }
    }
}