package com.example.doan.adapter;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doan.activity.FullscreenMusicActivity;
import com.example.doan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class BinMusicAdapter extends RecyclerView.Adapter<BinMusicAdapter.BinMusicViewHolder> {
    private List<String> mMusicUrls;
    private Context mContext;
    public SparseBooleanArray mSelectedItems;
    private ActionMode actionMode;
    public BinMusicAdapter(Context context, List<String> musicUrls) {
        mMusicUrls = musicUrls;
        mContext = context;
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
        String thumbnailUrl = getThumbnailUrl(musicUrl);

        holder.musicName.setText(getNameSong(musicUrl));

        // Tải ảnh thumbnail
        loadThumbnail(getThumbnailUrl(musicUrl), holder.myMusicView);

        // Xác định trạng thái của ảnh
        boolean isSelected = mSelectedItems.get(position);
        if (isSelected) {
            holder.checkView.setVisibility(View.VISIBLE);
            holder.myMusicView.setTag("selected");
        } else {
            holder.checkView.setVisibility(View.GONE);
            holder.myMusicView.setTag(null);
        }

        // Bắt sự kiện click vào VideoView
        holder.myMusicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển sang một Activity khác và truyền đường dẫn của video được click qua Intent
                Intent intent = new Intent(mContext, FullscreenMusicActivity.class);
                intent.putExtra("musicUrl", musicUrl);
                intent.putExtra("thumbnailUrl", thumbnailUrl);
                intent.putExtra("musicName", getNameSong(musicUrl)); // Sử dụng biến thành viên
                intent.putStringArrayListExtra("musicUrlList", (ArrayList<String>) mMusicUrls);
                mContext.startActivity(intent);
            }
        });

        // Bắt sự kiện long click vào VideoView
        holder.myMusicView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Kiểm tra xem video được nhấn có được chọn hay không
                boolean isSelected = mSelectedItems.get(position);
                if (isSelected) {
                    // Nếu đã được chọn lần trước đó, ẩn checkView và xóa khỏi danh sách các item đã chọn
                    holder.checkView.setVisibility(View.GONE);
                    mSelectedItems.delete(position);
                } else {
                    // Nếu chưa được chọn, đánh dấu là đã chọn và hiển thị checkView
                    isSelected = true;
                    mSelectedItems.put(position, isSelected);
                    holder.checkView.setVisibility(View.VISIBLE);
                    holder.myMusicView.setTag("selected");
                    // Kích hoạt Contextual action bar nếu chưa có và chưa có item nào được chọn
                    if (actionMode == null && mSelectedItems.size() == 1) {
                        actionMode = view.startActionMode(callback);
                    }
                }

                // Kiểm tra xem có còn item nào được chọn hay không
                if (mSelectedItems.size() == 0) {
                    // Nếu không còn, kết thúc Contextual action bar
                    actionMode.finish();
                    actionMode = null;
                }

                return true;
            }
        });
    }

    private void loadThumbnail(String musicUrl, ImageView imageView) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Load ảnh từ URL có thể truy cập từ Internet bằng Glide
                Glide.with(mContext)
                        .load(uri)
                        .placeholder(R.drawable.placeholder_music)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Xử lý khi không thể tải URL
                Log.e("Glide", "Load failed", e);
            }
        });
    }

    private String getNameSong (String newMusicUrl){
        // Lấy tên file từ URL
        String fileName = newMusicUrl.substring(newMusicUrl.lastIndexOf("%2F") + 3, newMusicUrl.lastIndexOf(".mp3"));
        String songName = "";
        // Giải mã tên file
        try {
            String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
            songName = decodedFileName;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return songName;
    }

    private String getThumbnailUrl(String musicUrl) {
        // Tạo một StorageReference từ URL của file mp3
        StorageReference musicRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);

        // Tạo một tham chiếu đến thư mục chứa file mp3 và file ảnh thumbnail
        StorageReference folderRef = musicRef.getParent();

        // Tạo URL cho file metadata ảnh
        return folderRef.child("thumbnail.jpg").toString();
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
                    showDeleteConfirmationDialog(mode);
                    return true;

                case R.id.khoi_phuc_anh:
                    showRestoreConfirmationDialog(mode);
                    return true;

                default:
                    return false;
            }
        }

        private void showDeleteConfirmationDialog(ActionMode mode) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.quest_delmusic);
            builder.setPositiveButton(R.string.yes1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteSelectedMusic();
                    mode.finish();
                }
            });
            builder.setNegativeButton(R.string.no1, new DialogInterface.OnClickListener() {
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
            // Lấy URL của file mp3 và thumbnail
            final String musicUrl = mMusicUrls.get(position);
            final String thumbnailUrl = getThumbnailUrl(musicUrl);

            // Tạo tham chiếu đến file mp3
            StorageReference musicRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);

            // Lấy tham chiếu của thư mục chứa file mp3
            final StorageReference folderRef = musicRef.getParent();

            // Xóa file mp3
            musicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Xóa file thumbnail
                    StorageReference thumbnailRef = FirebaseStorage.getInstance().getReferenceFromUrl(thumbnailUrl);
                    thumbnailRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Xóa thư mục chứa file mp3 và thumbnail
                            folderRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Cập nhật UI
                                    if (position < mMusicUrls.size()) {
                                        mMusicUrls.remove(position);
                                        mSelectedItems.delete(position);
                                        notifyDataSetChanged();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý khi xóa thư mục thất bại
                                }
                            });
                        }
                        
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Xử lý khi xóa file thumbnail thất bại
                        }
                    });
                    Toast.makeText(mContext, R.string.succes_del1, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext, R.string.error_delmu, Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void showRestoreConfirmationDialog(ActionMode mode) {
            ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(mContext.getString(R.string.loading_undo1));
            progressDialog.show();

            for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                int position = mSelectedItems.keyAt(i);
                if (mSelectedItems.get(position)) {
                    restoreMusicTask(position, progressDialog);
                    mode.finish();
                }
            }
        }

        private void restoreMusicTask(final int position, final ProgressDialog progressDialog) {
            final String musicUrl = mMusicUrls.get(position);
            final String thumbnailUrl = getThumbnailUrl(musicUrl);

            // Tạo một StorageReference từ URL music và thumbnail
            StorageReference musicStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);
            StorageReference thumbnailStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(thumbnailUrl);

            // Tạo một StorageReference mới tới thư mục "music" trên Firebase Storage
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String folderName = "music_" + System.currentTimeMillis();
            StorageReference FolderRef = FirebaseStorage.getInstance().getReference().child("music/" + user.getUid() + "/" + folderName);

            // Upload tệp nhạc và thumbnail vào thư mục mới
            uploadFileToStorage(musicStorageRef,FolderRef, musicStorageRef.getName(), progressDialog);
            uploadFileToStorage(thumbnailStorageRef, FolderRef, "thumbnail.jpg", progressDialog);
            // Cập nhật lại giao diện người dùng
            notifyDataSetChanged();
            progressDialog.dismiss();
            Toast.makeText(mContext, R.string.restore_music_success, Toast.LENGTH_SHORT).show();
        }

        private void uploadFileToStorage(StorageReference sourceRef, StorageReference destinationRef, String fileName, ProgressDialog progressDialog) {
            sourceRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Upload tệp lên thư mục mới
                    destinationRef.child(fileName).putBytes(bytes);
                    // Xóa tệp gốc
                    sourceRef.delete();
                    // Cập nhật progressbar
                    progressDialog.incrementProgressBy(1);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Xử lý lỗi nếu không tải được tệp
                    Toast.makeText(mContext, R.string.error_undomu, Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Clear all selected items
            mSelectedItems.clear();
            // Update the UI
            notifyDataSetChanged();
        }
    };
    // Hủy Contextual Action Mode khi sử dụng Adapter khác
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (actionMode != null) {
            actionMode.finish();
        }
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