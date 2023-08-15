package com.example.doan.adapter;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
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

import com.example.doan.activity.FullscreenVideoActivity;
import com.example.doan.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<String> mVideoUrls;
    private Context mContext;
    public SparseBooleanArray mSelectedItems;
    public ActionMode actionMode;
    private RecyclerView mRecyclerView;
    private Picasso mPicasso;
    private String videoTitle;
    public VideoAdapter(Context context, List<String> videoUrls) {
        mVideoUrls = videoUrls;
        mContext = context;
        mPicasso = Picasso.get();
        mSelectedItems = new SparseBooleanArray();
    }
    public ActionMode.Callback getCallback() {
        return callback;
    }
    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_item_layout1, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        String videoUrl = mVideoUrls.get(position);
        getVideoTitleFromFirebaseStorage(videoUrl).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String videoTitle) {
                VideoAdapter.this.videoTitle = videoTitle; // Gán giá trị cho biến thành viên
                holder.videoName.setText(videoTitle);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Xử lý khi không thể lấy tên nhạc
            }
        });
        // Tải thumbnail của video sử dụng Picasso
        // Tải video từ Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        File localFile;
        try {
            localFile = File.createTempFile("video", "mp4");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Lấy thumbnail của video từ video đã tải xuống
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(localFile.getAbsolutePath());
                Bitmap thumbnail = retriever.getFrameAtTime();

                // Hiển thị thumbnail trong ImageView
                if (thumbnail != null) {
                    holder.myVideoView.setImageBitmap(thumbnail);
                } else {
                    // Nếu không có thumbnail, hiển thị placeholder
                    holder.myVideoView.setImageResource(R.drawable.placeholder_video);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Xử lý khi không thể tải video từ Firebase Storage
            }
        });
        mPicasso.load(videoUrl)
                .placeholder(R.drawable.placeholder_video)
                .into(holder.myVideoView);

        // Xác định trạng thái của ảnh
        boolean isSelected = mSelectedItems.get(position);
        if (isSelected) {
            holder.checkView.setVisibility(View.VISIBLE);
            holder.myVideoView.setTag("selected");
        } else {
            holder.checkView.setVisibility(View.GONE);
            holder.myVideoView.setTag(null);
        }

        // Bắt sự kiện click vào VideoView
        holder.myVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển sang một Activity khác và truyền đường dẫn của video được click qua Intent
                Intent intent = new Intent(mContext, FullscreenVideoActivity.class);
                intent.putExtra("videoUrl", videoUrl);
                mContext.startActivity(intent);
            }
        });

        // Bắt sự kiện long click vào VideoView
        holder.myVideoView.setOnLongClickListener(new View.OnLongClickListener() {
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
                    holder.myVideoView.setTag("selected");
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
        holder.videoName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Đổi tên tệp");

                // Create an EditText view to get the new music name
                final EditText input = new EditText(mContext);
                builder.setView(input);

                // Set positive button for OK action
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newVideoName = input.getText().toString();
                        if (!newVideoName.isEmpty()) {
                            // Update the music name in the RecyclerView
                            holder.videoName.setText(newVideoName + ".mp4");

                            // Get the current file path or URL

                            // Get the FirebaseStorage reference to the current file
                            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);

                            // Lấy tên tệp mới với phần mở rộng
                            String newFileName = newVideoName + ".mp4";

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
                                                    Toast.makeText(mContext, "Đổi tên tệp thành công", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Xử lý khi xóa tệp hiện tại thất bại
                                                    Toast.makeText(mContext, "Lỗi khi xóa tệp hiện tại", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Xử lý khi tạo tệp mới thất bại
                                            Toast.makeText(mContext, "Lỗi khi tạo tệp mới", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý khi sao chép nội dung tệp thất bại
                                    Toast.makeText(mContext, "Lỗi khi sao chép nội dung tệp", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                // Thiết lập nút Không cho hành động từ chối
                builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
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
                            selectedUrls.add(mVideoUrls.get(position));
                        }
                    }
                    Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.setType("video/*");
                    ArrayList<Uri> videoUris = new ArrayList<>();
                    for (String url : selectedUrls) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                        try {
                            final File localFile = File.createTempFile("video", "mp4");
                            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Uri videoUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", localFile);
                                    videoUris.add(videoUri);
                                    if (videoUris.size() == selectedUrls.size()) {
                                        shareVideos(videoUris);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(mContext, "Tải video về cache thất bại", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mode.finish();
                    return true;
                case R.id.delete:
                    // Tạo một danh sách tạm thời để lưu lại các giá trị của mImageUrls
                    List<String> tempVideoUrls = new ArrayList<>(mVideoUrls);
                    ProgressDialog progressDialog1 =new ProgressDialog(mContext);
                    progressDialog1.setCancelable(false);
                    progressDialog1.setMessage("Đang xóa video...");
                    progressDialog1.show();
                    // Xóa các ảnh được chọn khỏi Firebase Storage và danh sách mImageUrls
                    for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                        int position = mSelectedItems.keyAt(i);
                        if (mSelectedItems.get(position)) {
                            String videoUrl = tempVideoUrls.get(position);
                            // Tạo một StorageReference từ URL ảnh
                            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);

                            // Tải tệp tin ảnh dưới dạng một mảng byte
                            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Tạo một StorageReference mới tới thư mục "delete"
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    StorageReference deleteRef = FirebaseStorage.getInstance().getReference().child("deletevideo/" +user.getUid()+"/"+ storageRef.getName());
                                    // Copy video vào thư mục "delete" trên Firebase Storage
                                    deleteRef.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // Xóa video khỏi Firebase Storage
                                            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Xóa URL video khỏi danh sách mImageUrls
                                                    mVideoUrls.remove(videoUrl);
                                                    // Xóa phần tử tương ứng trong SparseBooleanArray
                                                    mSelectedItems.delete(position);
                                                    // Cập nhật lại giao diện người dùng
                                                    notifyDataSetChanged();
                                                    progressDialog1.dismiss();
                                                    Toast.makeText(mContext, "Xóa video thành công!", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Xử lý lỗi nếu xóa không thành công
                                                    Toast.makeText(mContext, "Lỗi xóa video", Toast.LENGTH_SHORT).show();
                                                    progressDialog1.dismiss();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Xử lý lỗi nếu copy không thành công
                                            Toast.makeText(mContext, "Lỗi xóa video vào thùng rác", Toast.LENGTH_SHORT).show();
                                            progressDialog1.dismiss();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý lỗi nếu không tải được tệp tin ảnh
                                    Toast.makeText(mContext, "Lỗi tệp tin video", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }
                    // Xóa danh sách tạm thời
                    tempVideoUrls.clear();
                    mode.finish(); // Kết thúc ActionMode
                    return true;
                case R.id.save_image:
                    // Download and save the selected images
                    int numSelected = mSelectedItems.size();
                    ProgressDialog progressDialog = new ProgressDialog(mContext); // Tạo một ProgressDialog mới
                    progressDialog.setMessage("Đang tải video về..."); // Thiết lập thông báo cho ProgressDialog
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Thiết lập kiểu hiển thị của ProgressDialog
                    progressDialog.setMax(numSelected); // Thiết lập giá trị tối đa của ProgressDialog
                    progressDialog.show(); // Hiển thị ProgressDialog
                    for (int i = 0; i < numSelected; i++) {
                        int position = mSelectedItems.keyAt(i);
                        String videoUrl = mVideoUrls.get(position);
                        String fileName = "video_" + System.currentTimeMillis() + ".mp4";

                        // Create a new File object to save the image
                        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File file = new File(downloadDirectory, fileName);
                        // Create a StorageReference object from the image URL
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);

                        // Download the image to the device and save it to the file
                        final int finalI = i;
                        storageRef.getFile(file)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Check if this is the last image to download
                                        if (finalI == numSelected - 1) {
                                            // Display a message to the user that all images have been saved
                                            Toast.makeText(mContext, "Tải video về thành công!", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(mContext, "Tải video về thất bại", Toast.LENGTH_SHORT).show();
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
    private void shareVideos(ArrayList<Uri> videoUris) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("video/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, videoUris);
        mContext.startActivity(Intent.createChooser(shareIntent, "Chia sẻ video"));
    }

    private Task<String> getVideoTitleFromFirebaseStorage(String videoUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(videoUrl);

        return storageRef.getMetadata().continueWith(new Continuation<StorageMetadata, String>() {
            @Override
            public String then(@NonNull Task<StorageMetadata> task) throws Exception {
                if (task.isSuccessful()) {
                    StorageMetadata storageMetadata = task.getResult();
                    return storageMetadata.getName();
                } else {
                    // Xử lý khi không thể lấy thông tin metadata
                    Exception exception = task.getException();
                    // ...
                    return null;
                }
            }
        });
    }
    @Override
    public int getItemCount() {

        return mVideoUrls.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView myVideoView;
        ImageView checkView;
        TextView videoName;

        public VideoViewHolder(View itemView) {
            super(itemView);
            myVideoView = itemView.findViewById(R.id.my_video_view);
            checkView = itemView.findViewById(R.id.check_view);
            videoName = itemView.findViewById(R.id.video_name);
        }
    }
}