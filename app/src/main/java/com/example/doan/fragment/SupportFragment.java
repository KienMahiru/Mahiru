package com.example.doan.fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.doan.AppSettings;
import com.example.doan.R;

public class SupportFragment extends Fragment {
    private View mView;
    private LinearLayout[] questions;
    private LinearLayout[] answers;
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
        mView = inflater.inflate(R.layout.fragment_support, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Thêm kiểm tra màu nền tối/ sáng
        mView.setBackgroundColor(requireContext().getColor(mIsDarkMode ? R.color.black : R.color.white));

        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), getString(R.string.support));

        setupQuestionAnswerPairs();
        return mView;
    }

    private void setupQuestionAnswerPairs() {
        questions = new LinearLayout[2];
        answers = new LinearLayout[2];

        // Câu hỏi 1 , câu trả lời 1
        questions[0] = mView.findViewById(R.id.cau_hoi_1);
        answers[0] = mView.findViewById(R.id.tra_loi_1);
        setOnClickForPair(questions[0], answers[0]);

        // Câu hỏi 2, câu trả lời 2
        questions[1] = mView.findViewById(R.id.cau_hoi_2);
        answers[1] = mView.findViewById(R.id.tra_loi_2);
        setOnClickForPair(questions[1], answers[1]);
    }

    private void setOnClickForPair(LinearLayout question, LinearLayout answer) {
        question.setOnClickListener(view -> {
            hideAllAnswers();
            answer.setVisibility(View.VISIBLE);
        });
    }

    private void hideAllAnswers() {
        for (LinearLayout answer : answers) {
            answer.setVisibility(View.GONE);
        }
    }
}