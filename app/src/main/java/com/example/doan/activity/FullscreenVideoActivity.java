package com.example.doan.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;



import androidx.appcompat.app.AppCompatActivity;

import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
public class FullscreenVideoActivity extends AppCompatActivity {
    private PlayerView playerView;
    private SimpleExoPlayer player;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);

        playerView = findViewById(R.id.playerView);

        // Get the video URL from Intent
        String videoUrl = getIntent().getStringExtra("videoUrl");

        // Create a SimpleExoPlayer instance
        player = new SimpleExoPlayer.Builder(this)
                .setTrackSelector(new DefaultTrackSelector(this))
                .build();

        // Set the ExoPlayer to the PlayerView
        playerView.setPlayer(player);

        // Create a media source from the video URL
        MediaSource mediaSource = buildMediaSource(Uri.parse(videoUrl));

        // Prepare the player with the media source
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "ExoPlayerDemo"));
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }
    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}