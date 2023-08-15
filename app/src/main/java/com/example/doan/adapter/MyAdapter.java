package com.example.doan.adapter;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doan.activity.FullscreenImageActivity;
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
import java.util.ArrayList;
import java.util.List;
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<String> mImageUrls;
    private Context mContext;
    private Picasso mPicasso;
    public SparseBooleanArray mSelectedItems;
    public ActionMode actionMode;
    private RecyclerView mRecyclerView;
    public MyAdapter(Context context, List<String> imageUrls) {
        mImageUrls = imageUrls;
        mContext = context;
        mPicasso = Picasso.get();
        mSelectedItems = new SparseBooleanArray();
    }
    public ActionMode.Callback getCallback() {
        return callback;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String imageUrl = mImageUrls.get(position);
        mPicasso.load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(holder.myImageView);

        // Xác định trạng thái của ảnh
        boolean isSelected = mSelectedItems.get(position);
        if (isSelected) {
            holder.checkView.setVisibility(View.VISIBLE);
            holder.myImageView.setTag("selected");
        } else {
            holder.checkView.setVisibility(View.GONE);
            holder.myImageView.setTag(null);
        }

        // Bắt sự kiện click vào ImageView
        holder.myImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chuyển sang một Activity khác và truyền đường dẫn của ảnh được click qua Intent
                Intent intent = new Intent(mContext, FullscreenImageActivity.class);
                intent.putExtra("imageUrl", imageUrl);
                mContext.startActivity(intent);
            }
        });

        // Bắt sự kiện long click vào ImageView
        holder.myImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Kiểm tra xem ảnh được nhấn có được chọn hay không
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
                    holder.myImageView.setTag("selected");
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
                            selectedUrls.add(mImageUrls.get(position));
                        }
                    }
                    Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.setType("image/*");
                    ArrayList<Uri> imageUris = new ArrayList<>();
                    for (String url : selectedUrls) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                        try {
                            final File localFile = File.createTempFile("image", "jpg");
                            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Uri imageUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", localFile);
                                    imageUris.add(imageUri);
                                    if (imageUris.size() == selectedUrls.size()) {
                                        shareImages(imageUris);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(mContext, "Tải ảnh về thất bại", Toast.LENGTH_SHORT).show();
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
                    List<String> tempImageUrls = new ArrayList<>(mImageUrls);
                    ProgressDialog progressDialog1 = new ProgressDialog(mContext);
                    progressDialog1.setMessage("Đang xóa ảnh...");
                    progressDialog1.setCancelable(false);
                    progressDialog1.show();
                    // Xóa các ảnh được chọn khỏi Firebase Storage và danh sách mImageUrls
                    for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                        int position = mSelectedItems.keyAt(i);
                        if (mSelectedItems.get(position)) {
                            String imageUrl = tempImageUrls.get(position);
                            // Tạo một StorageReference từ URL ảnh
                            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

                            // Tải tệp tin ảnh dưới dạng một mảng byte
                            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Tạo một StorageReference mới tới thư mục "delete"
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    StorageReference deleteRef = FirebaseStorage.getInstance().getReference().child("delete/" +user.getUid()+"/"+ storageRef.getName());
                                    // Copy ảnh vào thư mục "delete" trên Firebase Storage
                                    deleteRef.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // Xóa ảnh khỏi Firebase Storage
                                            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Xóa URL ảnh khỏi danh sách mImageUrls
                                                    mImageUrls.remove(imageUrl);
                                                    // Xóa phần tử tương ứng trong SparseBooleanArray
                                                    mSelectedItems.delete(position);
                                                    // Cập nhật lại giao diện người dùng
                                                    notifyDataSetChanged();
                                                    progressDialog1.dismiss();
                                                    Toast.makeText(mContext, "Xóa ảnh thành công!", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Xử lý lỗi nếu xóa không thành công
                                                    progressDialog1.dismiss();
                                                    Toast.makeText(mContext, "Xóa ảnh thất bại!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Xử lý lỗi nếu copy không thành công
                                            progressDialog1.dismiss();
                                            Toast.makeText(mContext, "Error moving image to delete folder", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý lỗi nếu không tải được tệp tin ảnh
                                    progressDialog1.dismiss();
                                    Toast.makeText(mContext, "Lỗi tải tệp ảnh! ", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                    // Xóa danh sách tạm thời
                    tempImageUrls.clear();
                    mode.finish(); // Kết thúc ActionMode
                    return true;
                case R.id.save_image:
                    // Download and save the selected images
                    int numSelected = mSelectedItems.size();
                    ProgressDialog progressDialog = new ProgressDialog(mContext); // Tạo một ProgressDialog mới
                    progressDialog.setMessage("Đang tải ảnh về..."); // Thiết lập thông báo cho ProgressDialog
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Thiết lập kiểu hiển thị của ProgressDialog
                    progressDialog.setCancelable(false);
                    progressDialog.setMax(numSelected); // Thiết lập giá trị tối đa của ProgressDialog
                    progressDialog.show(); // Hiển thị ProgressDialog
                    for (int i = 0; i < numSelected; i++) {
                        int position = mSelectedItems.keyAt(i);
                        String imageUrl = mImageUrls.get(position);
                        String fileName = "image_" + System.currentTimeMillis() + ".jpg";

                        // Create a new File object to save the image
                        File downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File file = new File(downloadDirectory, fileName);
                        // Create a StorageReference object from the image URL
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);

                        // Download the image to the device and save it to the file
                        final int finalI = i;
                        storageRef.getFile(file)
                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Check if this is the last image to download
                                        if (finalI == numSelected - 1) {
                                            // Display a message to the user that all images have been saved
                                            Toast.makeText(mContext, "Tải ảnh về thành công!", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(mContext, "Tải ảnh về thất bại", Toast.LENGTH_SHORT).show();
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
    private void shareImages(ArrayList<Uri> imageUris) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("image/*");
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        mContext.startActivity(Intent.createChooser(shareIntent, "Chia sẻ ảnh"));
    }


    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView myImageView;
        ImageView checkView;

        public MyViewHolder(View itemView) {
            super(itemView);
            myImageView = itemView.findViewById(R.id.my_image_view);
            checkView = itemView.findViewById(R.id.check_view);
        }
    }
}