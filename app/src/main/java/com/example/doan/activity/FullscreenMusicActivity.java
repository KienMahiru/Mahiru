package com.example.doan.activity;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class FullscreenMusicActivity extends AppCompatActivity {
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private PlayerView playerView;
    private SimpleExoPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_music);
        // Get the video URL from Intent
        String musicUrl = getIntent().getStringExtra("musicUrl");
        String musicName = getIntent().getStringExtra("musicName");

        // Initialize views
        playerView = findViewById(R.id.musicView);
        TextView musicNameTextView = findViewById(R.id.musicNameTextView);
        musicNameTextView.setText(musicName);

        // Create and set up ExoPlayer
        player = new SimpleExoPlayer.Builder(this)
                .setTrackSelector(new DefaultTrackSelector(this))
                .build();
        playerView.setPlayer(player);

        // Create and set up media source
        MediaSource mediaSource = buildMediaSource(FullscreenMusicActivity.this,Uri.parse(musicUrl));
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    public static MediaSource buildMediaSource(Context context, Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoPlayerDemo"));
        MediaItem mediaItem = MediaItem.fromUri(uri);
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
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