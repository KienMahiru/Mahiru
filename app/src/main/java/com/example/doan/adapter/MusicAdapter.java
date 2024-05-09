package com.example.doan.adapter;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
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
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doan.activity.FullscreenMusicActivity;
import com.example.doan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {
    private List<String> mMusicUrls;
    private Context mContext;
    public SparseBooleanArray mSelectedItems;
    public ActionMode actionMode;

    public MusicAdapter(Context context, List<String> musicUrls) {
        mMusicUrls = musicUrls;
        mContext = context;
        mSelectedItems = new SparseBooleanArray();
    }
    public ActionMode.Callback getCallback() {
        return callback;
    }
    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_item_layout2, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
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
        holder.musicName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.rename1);

                // Create an EditText view to get the new music name
                final EditText input = new EditText(mContext);
                builder.setView(input);

                // Set positive button for OK action
                builder.setPositiveButton(R.string.yes1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newMusicName = input.getText().toString();
                        if (!newMusicName.isEmpty()) {
                            // Update the music name in the RecyclerView
                            holder.musicName.setText(newMusicName + ".mp3");

                            // Get the current file path or URL

                            // Get the FirebaseStorage reference to the current file
                            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);

                            // Lấy tên tệp mới với phần mở rộng
                            String newFileName = newMusicName + ".mp3";

                            // Tạo một tham chiếu mới với tên tệp mới
                            final StorageReference newRef = storageRef.getParent().child(newFileName);

                            // Copy nội dung của tệp hiện tại vào tệp mới
                            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    newRef.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // Xóa tệp hiện tại
                                            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Cập nhật dữ liệu trong danh sách và cập nhật giao diện người dùng

                                                    notifyDataSetChanged();
                                                    Toast.makeText(mContext, R.string.succes_rename1, Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Xử lý khi xóa tệp hiện tại thất bại
                                                    Toast.makeText(mContext, R.string.error_delfile, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Xử lý khi tạo tệp mới thất bại
                                            Toast.makeText(mContext, R.string.error_crefile, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý khi sao chép nội dung tệp thất bại
                                    Toast.makeText(mContext, R.string.error_copyfile, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                // Thiết lập nút Không cho hành động từ chối
                builder.setNegativeButton(R.string.no1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Không làm gì, đóng hộp thoại
                        dialog.dismiss();
                    }
                });

                // Hiển thị AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
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

    // Phương thức để lấy URL của file metadata ảnh trong cùng thư mục trên Firebase Storage
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
            inflater.inflate(R.menu.contextual_action_bar, menu);
            // Check if at least one item is selected
            boolean hasSelection = false;
            for (int i = 0; i < mSelectedItems.size(); i++) {
                if (mSelectedItems.valueAt(i)) {
                    hasSelection = true;
                    break;
                }
            }

            // If no items are selected, hide the Contextual action bar
            if (!hasSelection) {
                mode.finish();
                return false;
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.share:
                    ArrayList<String> selectedUrls = new ArrayList<>();
                    for (int i = 0; i < mSelectedItems.size(); i++) {
                        int position = mSelectedItems.keyAt(i);
                        if (mSelectedItems.get(position)) {
                            selectedUrls.add(mMusicUrls.get(position));
                        }
                    }
                    Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.setType("music/*");
                    ArrayList<Uri> musicUris = new ArrayList<>();
                    for (String url : selectedUrls) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                        try {
                            final File localFile = File.createTempFile("music", ".mp3");
                            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Uri musicUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", localFile);
                                    musicUris.add(musicUri);
                                    if (musicUris.size() == selectedUrls.size()) {
                                        shareMusics(musicUris);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(mContext, R.string.error_shamu, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mode.finish();
                    return true;
                case R.id.delete:
                    // Tạo một danh sách tạm thời để lưu lại các giá trị của mMusicUrls và thumbnailUrls
                    List<String> tempMusicUrls = new ArrayList<>(mMusicUrls);
                    List<String> tempThumbnailUrls = new ArrayList<>();

                    ProgressDialog progressDialog3 = new ProgressDialog(mContext);
                    progressDialog3.setCancelable(false);
                    progressDialog3.setMessage(mContext.getString(R.string.loading_del1));
                    progressDialog3.show();

                    // Xóa các music được chọn khỏi Firebase Storage và danh sách mMusicUrls
                    for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                        int position = mSelectedItems.keyAt(i);
                        if (mSelectedItems.get(position)) {
                            String musicUrl = tempMusicUrls.get(position);
                            String thumbnailUrl = getThumbnailUrl(musicUrl);
                            tempThumbnailUrls.add(thumbnailUrl);

                            // Tạo một StorageReference từ URL music và thumbnail
                            StorageReference musicStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);
                            StorageReference thumbnailStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(thumbnailUrl);

                            // Tạo một StorageReference mới tới thư mục "deletemusic" trên Firebase Storage
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String folderName = "music_" + System.currentTimeMillis();
                            StorageReference deleteFolderRef = FirebaseStorage.getInstance().getReference().child("deletemusic/" + user.getUid() + "/" + folderName);

                            // Upload tệp nhạc và thumbnail vào thư mục mới
                            uploadFileToStorage(musicStorageRef, deleteFolderRef, musicStorageRef.getName(), progressDialog3);
                            uploadFileToStorage(thumbnailStorageRef, deleteFolderRef, "thumbnail.jpg", progressDialog3);

                            // Xóa URL video khỏi danh sách mMusicUrls
                            mMusicUrls.remove(musicUrl);

                            // Xóa phần tử tương ứng trong SparseBooleanArray
                            mSelectedItems.delete(position);
                        }
                    }
                            // Cập nhật lại giao diện người dùng
                            notifyDataSetChanged();
                            progressDialog3.dismiss();
                            Toast.makeText(mContext, R.string.succes_del1, Toast.LENGTH_SHORT).show();
                            mode.finish(); // Kết thúc ActionMode
                            return true;
                case R.id.save_image:
                    // Download and save the selected images
                    int numSelected = mSelectedItems.size();
                    ProgressDialog progressDialog = new ProgressDialog(mContext); // Tạo một ProgressDialog mới
                    progressDialog.setMessage(mContext.getString(R.string.loading_downmu)); // Thiết lập thông báo cho ProgressDialog
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Thiết lập kiểu hiển thị của ProgressDialog
                    progressDialog.setMax(numSelected); // Thiết lập giá trị tối đa của ProgressDialog
                    progressDialog.show(); // Hiển thị ProgressDialog
                    for (int i = 0; i < numSelected; i++) {
                        int position = mSelectedItems.keyAt(i);
                        String musicUrl = mMusicUrls.get(position);
                        String fileName = "music_" + System.currentTimeMillis() + ".mp3";

                        // Create a new File object to save the image
                        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File file = new File(downloadDirectory, fileName);
                        // Create a StorageReference object from the image URL
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);

                        // Download the image to the device and save it to the file
                        final int finalI = i;
                        storageRef.getFile(file)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Check if this is the last image to download
                                        if (finalI == numSelected - 1) {
                                            // Display a message to the user that all images have been saved
                                            Toast.makeText(mContext, R.string.succes_downmu, Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            actionMode.finish();
                                            mSelectedItems.clear(); // Clear all selected items
                                            notifyDataSetChanged(); // Update the UI
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Display an error message to the user if the download fails
                                        progressDialog.dismiss();
                                        Toast.makeText(mContext, R.string.error_downmu2, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Clear all selected items
            mSelectedItems.clear();
            // Update the UI
            notifyDataSetChanged();
        }
    };

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
                Toast.makeText(mContext, R.string.error_movemu, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
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
    private void shareMusics(ArrayList<Uri> musicUris) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("music/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, musicUris);
        mContext.startActivity(Intent.createChooser(shareIntent, mContext.getString(R.string.share_mu)));
    }

    @Override
    public int getItemCount() {
        return mMusicUrls.size();
    }
    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView myMusicView;
        ImageView checkView;
        TextView musicName;

        public MusicViewHolder(View itemView) {
            super(itemView);
            myMusicView = itemView.findViewById(R.id.my_music_view);
            checkView = itemView.findViewById(R.id.check_view);
            musicName = itemView.findViewById(R.id.music_name);
        }
    }
}