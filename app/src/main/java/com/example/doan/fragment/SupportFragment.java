package com.example.doan.fragment;


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.doan.R;

public class SupportFragment extends Fragment {
    private View mView;
    private LinearLayout q1,q2,q3,q4;
    private LinearLayout t1,t2,t3,t4;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_support, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Lấy đối tượng ActionBar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        // Thiết lập tiêu đề mới cho ActionBar
        if (actionBar != null) {
            actionBar.setTitle("Trợ giúp");
        }
        // Câu hỏi 1 , câu trả lời 1
        q1 = mView.findViewById(R.id.cau_hoi_1);
        t1 = mView.findViewById(R.id.tra_loi_1);
        q1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gone();
                t1.setVisibility(View.VISIBLE);
            }
        });
        // Câu hỏi 2, câu trả lời 2
        q2 = mView.findViewById(R.id.cau_hoi_2);
        t2 = mView.findViewById(R.id.tra_loi_2);
        q2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gone();
                t2.setVisibility(View.VISIBLE);
            }
        });
        return mView;
    }
    private  void Gone(){
        t1.setVisibility(View.GONE);
        t2.setVisibility(View.GONE);
    }
}