package com.example.doan.fragment;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.example.doan.AppSettings;
import com.example.doan.LanguageManager;
import com.example.doan.R;
import com.example.doan.activity.FullscreenImageActivity;

import java.io.File;

public class SettingsFragment extends Fragment {
    private ProgressBar progressBar;
    private View mView;
    public Switch aSwitch;
    private Button xoa_cache, langSwitch;
    private ImageView btn_vn, btn_eng;
    private LanguageManager languageManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), getString(R.string.nav_settings));

        aSwitch = mView.findViewById(R.id.darkmode);
        xoa_cache = mView.findViewById(R.id.xoa_cache);
        progressBar = mView.findViewById(R.id.progressBar);
        languageManager = new LanguageManager(requireContext());
        String currentLanguage = languageManager.getSavedLanguage();

        // Nút thay đổi ngôn ngữ
        langSwitch = mView.findViewById(R.id.lang_switch);
        langSwitch.setOnClickListener(view -> lang_Switch());
        initSwitch();
        xoa_cache.setOnClickListener(view -> clearCache());
        return mView;
    }

    private void lang_Switch(){
        // Tạo hộp thoại lựa chọn
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.quest_language);
        builder.setItems(new CharSequence[]{getString(R.string.lang1), getString(R.string.lang2)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Xử lý sự kiện khi người dùng chọn một mục
                switch (which) {
                    case 0:
                        // Xử lý khi chọn Tiếng Việt
                        languageManager.updateResource("vi");
                        getActivity().recreate();
                        break;
                    case 1:
                        // Xử lý khi chọn Tiếng Anh
                        languageManager.updateResource("en");
                        getActivity().recreate();
                        break;
                }
            }
        });
        builder.show();
    }
    private void initSwitch() {
        boolean isDarkMode = AppSettings.getInstance(requireContext()).isDarkMode();
        aSwitch.setChecked(isDarkMode);

        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppSettings.getInstance(requireContext()).setDarkMode(isChecked);
            changeBackgroundColor(isChecked ? requireContext().getColor(R.color.black) : requireContext().getColor(R.color.white));
            updateFragmentBackgroundColors();
        });
    }

    private void clearCache() {
        progressBar.setVisibility(View.VISIBLE);
        Context context = requireContext();
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.isDirectory()) {
            deleteDir(cacheDir);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(context, R.string.succes_delset, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else {
            return dir != null && dir.isFile() && dir.delete();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        boolean isDarkMode = AppSettings.getInstance(requireContext()).isDarkMode();
        aSwitch.setChecked(isDarkMode);
        if (isDarkMode) {
            changeBackgroundColor(requireContext().getColor(R.color.black));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        boolean darkMode = aSwitch.isChecked();
        AppSettings.getInstance(requireContext()).setDarkMode(darkMode);
        updateFragmentBackgroundColors();
    }

    public void updateFragmentBackgroundColors() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        for (Fragment fragment : fragmentManager.getFragments()) {
            if (fragment instanceof SettingsFragment) {
                continue;
            }
            View view = fragment.getView();
            if (view != null) {
                view.setBackgroundColor(requireContext().getColor(AppSettings.getInstance(requireContext()).isDarkMode() ? R.color.black : R.color.white));
            }
        }
    }

    public void changeBackgroundColor(int color) {
        Window window = requireActivity().getWindow();
        if (window != null) {
            window.getDecorView().setBackgroundColor(color);
        }
    }
}