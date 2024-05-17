package com.example.doan.activity;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import com.google.android.exoplayer2.Player;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import java.util.ArrayList;

public class FullscreenVideoActivity extends AppCompatActivity {
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private String videoUrl;
    private ArrayList<String> videoUrls;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private int position;//Vị trí ảnh trong List ảnh
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_video);

        playerView = findViewById(R.id.playerView);

        // Get the video URL from Intent
         videoUrls = getIntent().getStringArrayListExtra("videoUrls");

        position = getIntent().getIntExtra("position_video",0);
        // Create and set up ExoPlayer
        player = new SimpleExoPlayer.Builder(this)
                .setTrackSelector(new DefaultTrackSelector(this))
                .build();
        playerView.setPlayer(player);
        concatenatingMediaSource = new ConcatenatingMediaSource();
        videoUrl = videoUrls.get(position);
        // Create and set up media source

        for (String url : videoUrls) {
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
            MediaSource mediaSource = buildMediaSource(this, mediaItem.playbackProperties.uri);
            concatenatingMediaSource.addMediaSource(mediaSource);
        }

        player.setMediaSource(concatenatingMediaSource);
        player.prepare();
        player.setPlayWhenReady(true);


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
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

    public static MediaSource buildMediaSource(Context context, Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoPlayerDemo"));
        MediaItem mediaItem = MediaItem.fromUri(uri);
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
    }
}