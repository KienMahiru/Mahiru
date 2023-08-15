package com.example.doan.adapter;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.doan.activity.FullscreenImageActivity;
import com.example.doan.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.List;
public class BinAdapter extends RecyclerView.Adapter<BinAdapter.BinViewHolder> {
    private List<String> mImageUrls;
    private Context mContext;
    private Picasso mPicasso;
    public SparseBooleanArray mSelectedItems;
    private ActionMode actionMode;
    private RecyclerView mRecyclerView;
    public BinAdapter(Context context, List<String> imageUrls) {
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
    public BinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_item_layout, parent, false);
        return new BinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BinViewHolder holder, int position) {
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
            inflater.inflate(R.menu.contextual_action_bar_1, menu);
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

                case R.id.delete_1:
// Tạo một hộp thoại AlertDialog.Builder mới
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("Bạn có chắc muốn xóa ảnh đã chọn không?");

// Thêm nút Yes vào hộp thoại
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Xóa các ảnh được chọn khỏi Firebase Storage và danh sách mImageUrls
                            for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                                int position = mSelectedItems.keyAt(i);
                                if (mSelectedItems.get(position)) {
                                    String imageUrl = mImageUrls.get(position);
                                    // Tạo một StorageReference từ URL ảnh
                                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                    storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            if (position < mImageUrls.size()) { // Kiểm tra xem position có hợp lệ không
                                                mImageUrls.remove(position);
                                                mSelectedItems.delete(position);
                                                notifyDataSetChanged();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mContext, "Lỗi xóa ảnh", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            mode.finish(); // Kết thúc ActionMode
                        }
                    });

// Thêm nút No vào hộp thoại
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Không làm gì cả
                        }
                    });

// Hiển thị hộp thoại
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                case R.id.khoi_phuc_anh:
                    // Xóa các ảnh được chọn khỏi Firebase Storage và danh sách mImageUrls
                    ProgressDialog progressDialog = new ProgressDialog(mContext);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Đang hoàn tác ảnh...");
                    progressDialog.show();
                    for (int i = mSelectedItems.size() - 1; i >= 0; i--) {
                        int position = mSelectedItems.keyAt(i);
                        if (mSelectedItems.get(position)) {
                            String imageUrl = mImageUrls.get(position);
                            // Tạo một StorageReference từ URL ảnh
                            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Tạo một StorageReference mới tới thư mục "image"
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    StorageReference deleteRef = FirebaseStorage.getInstance().getReference().child("image/" +user.getUid()+"/"+ storageRef.getName());
                                    // Copy ảnh vào thư mục "image" trên Firebase Storage
                                    deleteRef.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            // Xóa ảnh khỏi Firebase Storage
                                            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (position < mImageUrls.size()) { // Kiểm tra xem position có hợp lệ không
                                                        // Xóa URL ảnh khỏi danh sách mImageUrls
                                                        mImageUrls.remove(position);
                                                        // Xóa phần tử tương ứng trong SparseBooleanArray
                                                        mSelectedItems.delete(position);
                                                        // Cập nhật lại giao diện người dùng
                                                        notifyDataSetChanged();
                                                        Toast.makeText(mContext, "Hoàn tác ảnh thành công!", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Xử lý lỗi nếu xóa không thành công
                                                    Toast.makeText(mContext, "Lỗi ảnh", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Xử lý lỗi nếu copy không thành công
                                            progressDialog.dismiss();
                                            Toast.makeText(mContext, "Lỗi hoàn tác ảnh", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Xử lý lỗi nếu không tải được tệp tin ảnh
                                    Toast.makeText(mContext, "Lỗi đường dẫn ảnh", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }
                    mode.finish(); // Kết thúc ActionMode
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



    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    public static class BinViewHolder extends RecyclerView.ViewHolder {
        ImageView myImageView;
        ImageView checkView;

        public BinViewHolder(View itemView) {
            super(itemView);
            myImageView = itemView.findViewById(R.id.my_image_view);
            checkView = itemView.findViewById(R.id.check_view);
        }
    }
}