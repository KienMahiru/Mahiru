package com.example.doan.fragment;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doan.R;


public class InforFragment extends Fragment {
    private View mView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_infor, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Lấy đối tượng ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        // Thiết lập tiêu đề mới cho ActionBar
        if (actionBar != null) {
            actionBar.setTitle("Thông tin app");
        }
        ImageView imageView = mView.findViewById(R.id.circleImageView);

        imageView.setOnTouchListener(new View.OnTouchListener() {
            RotateAnimation rotateAnimation;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(100);
                    rotateAnimation.setRepeatCount(Animation.INFINITE);
                    imageView.startAnimation(rotateAnimation);
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (rotateAnimation != null) {
                        imageView.clearAnimation();
                        rotateAnimation.cancel();
                        rotateAnimation = null;
                    }
                    return true;
                }
                return false;
            }
        });

        return mView;
    }
}