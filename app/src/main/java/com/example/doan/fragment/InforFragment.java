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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.doan.AppSettings;
import com.example.doan.R;

public class InforFragment extends Fragment {
    private View mView;
    private boolean mIsDarkMode;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mIsDarkMode = AppSettings.getInstance(requireContext()).isDarkMode();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_infor, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(),getString(R.string.infor_app));

        setupImageViewRotation();
        return mView;
    }
    private void setupImageViewRotation() {
        ImageView imageView = mView.findViewById(R.id.circleImageView);
        imageView.setOnTouchListener((view, motionEvent) -> {
            RotateAnimation rotateAnimation;
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(100);
                rotateAnimation.setRepeatCount(Animation.INFINITE);
                imageView.startAnimation(rotateAnimation);
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (imageView.getAnimation() != null) {
                    imageView.clearAnimation();
                }
                return true;
            }
            return false;
        });
    }

}