package com.example.doan.activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.media.session.MediaSessionCompat;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.doan.MyNotification;
import com.example.doan.NetworkChangeListener;
import com.example.doan.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jgabrielfreitas.core.BlurImageView;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import de.hdodenhof.circleimageview.CircleImageView;

public class FullscreenMusicActivity extends AppCompatActivity {
    private static FullscreenMusicActivity instance;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    TextView playerCloseBtn;
    //controls
    TextView songNameView, skipPreviousBtn, skipNextBtn, playPauseBtn, repeatModeBtn, playlistBtn;
    CircleImageView artworkView;
    SeekBar seekbar;
    TextView progressView, durationView;

    //blur image view
    BlurImageView blurImageView;
    //status bar & navigation color;
    int defaultStatusColor;
    //repeat mode
    int repeatMode = 1; //repeat all = 1, repeat one = 2, shuffle all = 3
    ExoPlayer player;

    private int currentIndex = 0;
    private long maxDuration;

    SharedPreferences sharedPreferences;

    ArrayList<String> musicUrls;

    int notificationId = 1;

    String thumbnailURL = "";

    String musicURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_music);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        instance = this;
        // Get the video URL from Intent
        String musicUrl = getIntent().getStringExtra("musicUrl");
        String musicName = getIntent().getStringExtra("musicName");
        String thumbnailUrl = getIntent().getStringExtra("thumbnailUrl");
        thumbnailURL = thumbnailUrl;
        musicUrls = getIntent().getStringArrayListExtra("musicUrlList");

        //save the status color
        defaultStatusColor = getWindow().getStatusBarColor();
        //set the navigation color
        getWindow().setNavigationBarColor (ColorUtils.setAlphaComponent(defaultStatusColor, 199)); // 0 & 255


        //views
        playerCloseBtn = findViewById(R.id.playerCloseBtn);
        songNameView = findViewById(R.id.songNameView);
        skipPreviousBtn = findViewById(R.id.skipPreviousBtn);
        skipNextBtn = findViewById(R.id.skipNextBtn);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        repeatModeBtn = findViewById(R.id.repeatModeBtn);
        playlistBtn = findViewById(R.id.playlistBtn);

        //artwork
        artworkView = findViewById(R.id.artworkView);
        //seek bar
        seekbar = findViewById(R.id.seekbar);
        progressView = findViewById(R.id.progressView);
        durationView = findViewById(R.id.durationView);

        blurImageView = findViewById(R.id.blurImageView);

        songNameView.setText(musicName);


        sharedPreferences = getSharedPreferences("dataMusic", MODE_PRIVATE);
        // Tạo một SimpleExoPlayer
        player = new SimpleExoPlayer.Builder(this).build();


        // Tạo một MediaItem từ musicUrl
        MediaItem mediaItem = MediaItem.fromUri(musicUrl);

        // Tạo một MediaSource từ MediaItem
        MediaSource mediaSource = buildMediaSource(this, mediaItem);

        // Đặt MediaSource cho player
        player.setMediaSource(mediaSource);

        player.prepare();

        if(musicUrl.equals(sharedPreferences.getString("musicUrlSaved", ""))) {
            Long savedDuration = sharedPreferences.getLong("durationSaved", 0);
            player.seekTo(savedDuration);
            seekbar.setProgress(Math.toIntExact(savedDuration));
        }else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("musicUrlSaved");
            editor.remove("durationSaved");
            editor.apply();
            // Chuẩn bị player
        }

        // Load thumbnail using Glide
        loadThumbnail(thumbnailUrl, artworkView);

        player.play();
        artworkView.setAnimation(loadRotation());

        // Convert Firebase Storage URL to HTTP URL
        convertURLNSend(musicName, thumbnailUrl);

        //player controls method
        playerControls(musicUrls, musicUrl);
    }
    private void updateMaxDuration() {
        if (player.getMediaItemCount() > 0) {
            maxDuration = player.getDuration();
            seekbar.setMax((int) maxDuration);
            durationView.setText(getReadableTime((int) maxDuration));
        }
    }

    private void convertURLNSend(String musicName, String thumbnailUrl){
        // Convert Firebase Storage URL to HTTP URL
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(thumbnailUrl);
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            sendNotification(musicName, uri.toString());
        }).addOnFailureListener(e -> {
            // Handle failure
            Toast.makeText(instance, "không gửi được", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadThumbnail(String thumbnailUrl, ImageView imageView) {
        if (thumbnailUrl != null) {
            if (thumbnailUrl.startsWith("gs://")) {
                // Nếu URL bắt đầu với "gs://", đây là URL của Firebase Storage
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(thumbnailUrl);
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Tải ảnh từ URL HTTP bằng Glide
                    Glide.with(this)
                            .load(uri)
                            .into(imageView);
                }).addOnFailureListener(e -> {
                    // Xử lý khi không thể tải ảnh từ Firebase Storage
                    imageView.setImageResource(R.drawable.default_artwork);
                });
            } else {
                // Xử lý trường hợp khác nếu cần
                imageView.setImageResource(R.drawable.default_artwork);
            }
        } else {
            // Xử lý khi thumbnailUrl là null
            imageView.setImageResource(R.drawable.default_artwork);
        }
    }


    public static FullscreenMusicActivity getInstance() {
        return instance;
    }

    private void playerControls(ArrayList<String> musicUrls, String musicUrl) {
        //song name marquee
        songNameView.setSelected(true);

        musicURL = musicUrl;

        //exit the player view
        playerCloseBtn.setOnClickListener(view -> exitPlayerView(musicURL));
        playlistBtn.setOnClickListener(view -> exitPlayerView(musicURL));

        //player listener
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                // Cập nhật trạng thái phát
                //show the playing song title
                assert mediaItem != null;

                progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                updateMaxDuration();
                seekbar.setProgress((int) player.getCurrentPosition());
                playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline, 0, 0,0 );

                //show the current art work
                showCurrentArtwork();

                //update the progress position of a current playing song
                updatePlayerPositionProgress();

                //load the artwork animation
                artworkView.setAnimation(loadRotation());

                //update player view colors
                updatePlayerColors();
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if (playbackState == ExoPlayer.STATE_READY) {
                    // Cập nhật trạng thái phát
                    // Cập nhật thời gian tối đa của thanh seekbar
                    updateMaxDuration();
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekbar.setProgress((int) player.getCurrentPosition());
                    playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline, 0, 0, 0);

                    //show the current art work
                    showCurrentArtwork();

                    //update the progress position of a current playing song
                    updatePlayerPositionProgress();

                    //load the artwork animation
                    artworkView.setAnimation(loadRotation());

                    //update player view colors
                    updatePlayerColors();
                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    if (repeatMode == 3) {
                        // Chọn ngẫu nhiên một bài hát từ danh sách
                        String randomMusicUrl = getRandomMusicUrl(musicUrls, musicURL);

                        musicURL = randomMusicUrl;

                        thumbnailURL = getThumbnailUrl(randomMusicUrl);

                        loadThumbnail(thumbnailURL, artworkView);

                        // Lấy tên file từ URL
                        String fileName = randomMusicUrl.substring(randomMusicUrl.lastIndexOf("%2F") + 3, randomMusicUrl.lastIndexOf(".mp3"));

                        // Giải mã tên file
                        try {
                            String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
                            // Cập nhật tên bài hát
                            songNameView.setText(decodedFileName);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        // Tạo một MediaItem từ randomMusicUrl
                        MediaItem mediaItem = MediaItem.fromUri(randomMusicUrl);

                        // Tạo một MediaSource từ MediaItem
                        MediaSource mediaSource = buildMediaSource(getApplicationContext(), mediaItem);

                        // Đặt MediaSource cho player
                        player.setMediaSource(mediaSource);

                        // Chuẩn bị player
                        player.prepare();

                        // Bắt đầu phát nhạc
                        player.play();
                        artworkView.setAnimation(loadRotation());

                    } else {
                        playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_outline, 0, 0, 0);
                        artworkView.clearAnimation();
                    }
                    convertURLNSend(songNameView.getText().toString(), thumbnailURL);
                }
            }
        });

        //skip to next track
        skipNextBtn.setOnClickListener(view -> skipToNextOrPreviousSong(musicUrls, true));

        //skip to previous track
        skipPreviousBtn.setOnClickListener(view -> skipToNextOrPreviousSong(musicUrls, false));

        //play or pause the player
        playPauseBtn.setOnClickListener(view -> playOrPausePlayer());

        //seek bar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(player.getPlaybackState() == ExoPlayer.STATE_READY){
                    seekBar.setProgress(progressValue);
                    progressView.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                } else if (player.getPlaybackState() == ExoPlayer.STATE_ENDED) {
                    player.seekTo(progressValue);
                    player.play();
                    artworkView.startAnimation(loadRotation());
                }
                convertURLNSend(songNameView.getText().toString(), thumbnailURL);
            }
        });

        //repeat mode
        repeatModeBtn.setOnClickListener(view -> {
            if (repeatMode == 1) {
                //repeat one
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode = 2;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_one, 0, 0, 0);
            }else if (repeatMode == 2) {
                //shuffle all
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_OFF);
                repeatMode = 3;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shuffle, 0, 0 ,0 );

            } else if (repeatMode == 3) {
                //repeat off
                repeatMode = 1;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_all, 0, 0, 0);
            }

            //update colors
            updatePlayerColors();
        });

    }

    private String getRandomMusicUrl(ArrayList<String> musicUrls, String currentMusicUrl) {
        Random random = new Random();
        int randomIndex = 0;
        if(musicUrls.size() > 1){
            do {
                randomIndex = random.nextInt(musicUrls.size());
            } while (musicUrls.get(randomIndex).equals(currentMusicUrl));
        }
        return musicUrls.get(randomIndex);
    }

    public void playOrPausePlayer() {
        if(player.isPlaying()){
            player.pause();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_outline, 0, 0,0);
            artworkView.clearAnimation();
        } else if (player.getPlaybackState() == ExoPlayer.STATE_ENDED) {
            player.seekTo(0);
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline, 0, 0,0);
            artworkView.startAnimation(loadRotation());
        } else {
            player.play();
            playPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pause_outline, 0, 0,0);
            artworkView.startAnimation(loadRotation());
        }

        //update player colors
        updatePlayerColors();

        // Cập nhật notification
        convertURLNSend(songNameView.getText().toString(), thumbnailURL);
    }

    public void skipToNextOrPreviousSong(ArrayList<String> musicUrls, boolean isNext) {
        if (musicUrls.isEmpty()) return;

        if (isNext) {
            currentIndex++;
            if (currentIndex >= musicUrls.size()) {
                currentIndex = 0;
            }
        } else {
            currentIndex--;
            if (currentIndex < 0) {
                currentIndex = musicUrls.size() - 1;
            }
        }

        // Kiểm tra nếu bài hát hiện tại là bài hát đã được lưu
        if (musicUrls.get(currentIndex).equals(musicURL)) {
            // Tăng chỉ số currentIndex thêm một lần nữa để chuyển sang bài hát khác
            if(musicUrls.size() >= 2){
                skipToNextOrPreviousSong(musicUrls, true);
            }
            return;
        }

        String newMusicUrl = musicUrls.get(currentIndex);
        musicURL = newMusicUrl;

        // Lấy tên file từ URL
        String fileName = newMusicUrl.substring(newMusicUrl.lastIndexOf("%2F") + 3, newMusicUrl.lastIndexOf(".mp3"));

        // Giải mã tên file
        try {
            String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
            // Cập nhật tên bài hát
            songNameView.setText(decodedFileName);
            thumbnailURL = getThumbnailUrl(newMusicUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        loadThumbnail(getThumbnailUrl(newMusicUrl), artworkView);

        // Tạo một MediaItem từ newMusicUrl
        MediaItem mediaItem = MediaItem.fromUri(newMusicUrl);

        // Tạo một MediaSource từ MediaItem
        MediaSource mediaSource = buildMediaSource(this, mediaItem);

        // Đặt MediaSource cho player
        player.setMediaSource(mediaSource);

        // Chuẩn bị player
        player.prepare();

        // Bắt đầu phát nhạc
        player.play();
        artworkView.startAnimation(loadRotation());
        convertURLNSend(songNameView.getText().toString(), thumbnailURL);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        convertURLNSend(songNameView.getText().toString(), thumbnailURL);
    }

    private String getThumbnailUrl(String musicUrl) {
        // Tạo một StorageReference từ URL của file mp3
        StorageReference musicRef = FirebaseStorage.getInstance().getReferenceFromUrl(musicUrl);

        // Tạo một tham chiếu đến thư mục chứa file mp3 và file ảnh thumbnail
        StorageReference folderRef = musicRef.getParent();

        // Tạo URL cho file metadata ảnh
        return folderRef.child("thumbnail.jpg").toString();
    }

    private void updatePlayerColors() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) artworkView.getDrawable();
        if (bitmapDrawable == null) {
            bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.default_artwork);
        }

        assert bitmapDrawable != null;
        Bitmap bmp = bitmapDrawable.getBitmap();

        // Đặt bitmap cho blur image view
        blurImageView.setImageBitmap(bmp);
        blurImageView.setBlur(4);

        // Xử lý màu sắc của player
        Palette.from(bmp).generate(palette -> {
            if (palette != null) {
                Palette.Swatch swatch = palette.getDarkVibrantSwatch();
                if (swatch == null) {
                    swatch = palette.getMutedSwatch();
                    if (swatch == null) {
                        swatch = palette.getDominantSwatch();
                    }
                }
                if (swatch != null) {
                    // Lấy màu từ swatch
                    int titleTextColor = swatch.getTitleTextColor();
                    int bodyTextColor = swatch.getBodyTextColor();
                    int rgbColor = swatch.getRgb();

                    // Đặt màu cho player views
                    getWindow().setStatusBarColor(rgbColor);
                    getWindow().setNavigationBarColor(rgbColor);

                    songNameView.setTextColor(titleTextColor);
                    playerCloseBtn.getCompoundDrawables()[0].setTint(titleTextColor);
                    progressView.setTextColor(bodyTextColor);
                    durationView.setTextColor(bodyTextColor);

                    repeatModeBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                    skipPreviousBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                    skipNextBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                    playPauseBtn.getCompoundDrawables()[0].setTint(titleTextColor);
                    playlistBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
                } else {
                    // Xử lý khi không có swatch nào được tạo ra từ Palette
                }
            } else {
                // Xử lý khi Palette là null
            }
        });
    }


    private Animation loadRotation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(10000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        return rotateAnimation;
    }

    private void exitPlayerView(String musicUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("musicUrlSaved", musicUrl);
        if(player != null) {
            editor.putLong("durationSaved", player.getCurrentPosition());
            editor.apply();
        }
        finish();
    }
    private void updatePlayerPositionProgress() {
        new Handler().postDelayed(() -> {
            if(player.isPlaying()){
                long currentPosition = player.getCurrentPosition();

                // Đảm bảo rằng thời gian hiện tại không vượt quá thời gian tối đa
                if (currentPosition > maxDuration) {
                    currentPosition = maxDuration;
                }

                progressView.setText(getReadableTime((int) currentPosition));
                seekbar.setProgress((int) currentPosition);
            }

            // Lặp lại việc gọi phương thức để cập nhật tiến trình của bài hát
            updatePlayerPositionProgress();
        }, 1000);
    }


    private void showCurrentArtwork() {
        artworkView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

        if(artworkView.getDrawable() == null){
            artworkView.setImageResource(R.drawable.default_artwork);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
        //release the player
        if(player.isPlaying()){
            player.stop();
        }
        player.release();
        cancelNotification(getApplicationContext(), notificationId);
    }

    public String getReadableTime(int duration) {
        int hrs = duration / (1000 * 60 * 60);
        int min = (duration % (1000 * 60 * 60)) / (1000 * 60);
        int secs = ((duration % (1000 * 60 * 60)) % (1000 * 60)) / 1000;

        // Format giờ, phút và giây thành chuỗi
        String formattedTime = String.format("%02d:%02d", min, secs);

        // Nếu có giờ, thêm vào chuỗi formattedTime
        if (hrs > 0) {
            formattedTime = String.format("%02d:%s", hrs, formattedTime);
        }

        return formattedTime;
    }

    public static MediaSource buildMediaSource(Context context, MediaItem mediaItem) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "ExoPlayerDemo"));
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
        cancelNotification(getApplicationContext(), notificationId);

    }

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }

    private void sendNotification(String musicName, String thumbnailUrl) {
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");

        Intent previousIntent = new Intent(this, NotificationActionService.class)
                .setAction(NotificationActionService.ACTION_PREVIOUS);

        int intentFlagType = PendingIntent.FLAG_UPDATE_CURRENT;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            intentFlagType = PendingIntent.FLAG_IMMUTABLE;  // or only use FLAG_MUTABLE >> if it needs to be used with inline replies or bubbles.
        }

        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0, previousIntent, intentFlagType);

        Intent playPauseIntent = new Intent(this, NotificationActionService.class)
                .setAction(NotificationActionService.ACTION_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 0, playPauseIntent, intentFlagType);

        Intent nextIntent = new Intent(this, NotificationActionService.class)
                .setAction(NotificationActionService.ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, intentFlagType);

        int playbackState = player.getPlaybackState();
        int playPauseIcon;
        if ((playbackState == ExoPlayer.STATE_READY || playbackState == ExoPlayer.STATE_BUFFERING) && player.getPlayWhenReady()) {
            playPauseIcon = R.drawable.ic_pause;
        } else {
            playPauseIcon = R.drawable.ic_play;
        }

        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.silence);

        // Load thumbnail using Glide and convert it to Bitmap
        Glide.with(this)
                .asBitmap()
                .load(thumbnailUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Create a large icon for notification
                        Bitmap largeIcon = Bitmap.createScaledBitmap(resource, 512, 512, false);

                        // Build notification with large icon
                        Notification builder = new NotificationCompat.Builder(FullscreenMusicActivity.this, MyNotification.CHANNEL_ID)
                                // Show controls on lock screen even when user hides sensitive content.
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setSmallIcon(R.drawable.ic_stat_player)
                                .setLargeIcon(largeIcon) // Set large icon here
                                .setSound(sound)
                                // Add media control buttons that invoke intents in your media service
                                .addAction(R.drawable.ic_skip_previous, "Previous", previousPendingIntent) // #0
                                .addAction(playPauseIcon, playbackState == ExoPlayer.STATE_READY || playbackState == ExoPlayer.STATE_BUFFERING ? "Pause" : "Play", playPausePendingIntent) // #1
                                .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)     // #2
                                // Apply the media style template.
                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                        .setShowActionsInCompactView(0, 1, 2 /* #1: pause button */)
                                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                                .setContentTitle(musicName)
                                .setOngoing(true)
                                .setContentText("Music")
                                .build();

                        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getApplicationContext());
                        if (ActivityCompat.checkSelfPermission(FullscreenMusicActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        managerCompat.notify(notificationId, builder);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }
}