package com.example.doan.fragment;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.example.doan.AppSettings;
import com.example.doan.R;
import java.io.File;

public class SettingsFragment extends Fragment {
    private ProgressBar progressBar;
    private View mView;
    private Switch aSwitch;
    private Button xoa_cache;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_settings, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        FeedbackFragment feedbackFragment = new FeedbackFragment();
        feedbackFragment.setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), "Cài đặt");

        aSwitch = mView.findViewById(R.id.darkmode);
        xoa_cache = mView.findViewById(R.id.xoa_cache);
        progressBar = mView.findViewById(R.id.progressBar);

        initSwitch();

        xoa_cache.setOnClickListener(view -> clearCache());

        return mView;
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
            Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_SHORT).show();
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

    private void updateFragmentBackgroundColors() {
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

    private void changeBackgroundColor(int color) {
        Window window = requireActivity().getWindow();
        if (window != null) {
            window.getDecorView().setBackgroundColor(color);
        }
    }
}