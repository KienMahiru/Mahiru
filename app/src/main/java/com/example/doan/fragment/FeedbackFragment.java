package com.example.doan.fragment;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doan.R;

public class FeedbackFragment extends Fragment {
    private View mView;
    private Button sendButton,sendphone;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_feed, container, false);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setupActionBar(((AppCompatActivity) getActivity()).getSupportActionBar(), getString(R.string.Feedback));
        setupViews();
        return mView;
    }

    public void setupActionBar(ActionBar actionBar, String text) {
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    private void setupViews() {
        sendButton = mView.findViewById(R.id.send_button);
        sendphone = mView.findViewById(R.id.sendphone);

        sendButton.setOnClickListener(v -> sendEmail());
        sendphone.setOnClickListener(v -> dialPhoneNumber("012345678"));
    }

    private void sendEmail(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "meomeo123@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
    private void dialPhoneNumber(String phoneNumber) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(dialIntent);
    }
}