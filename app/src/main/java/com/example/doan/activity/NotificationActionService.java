package com.example.doan.activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class NotificationActionService extends Service {
    public static final String ACTION_NEXT = "com.example.doan.action.NEXT";
    public static final String ACTION_PLAY_PAUSE = "com.example.doan.action.PLAY_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.doan.action.PREVIOUS";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                FullscreenMusicActivity fullscreenMusicActivity = FullscreenMusicActivity.getInstance();
                if (fullscreenMusicActivity != null) {
                    switch (action) {
                        case ACTION_PREVIOUS:
                            fullscreenMusicActivity.skipToNextOrPreviousSong(fullscreenMusicActivity.musicUrls, false);
                            break;
                        case ACTION_PLAY_PAUSE:
                            fullscreenMusicActivity.playOrPausePlayer();
                            break;
                        case ACTION_NEXT:
                            fullscreenMusicActivity.skipToNextOrPreviousSong(fullscreenMusicActivity.musicUrls, true);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + action);
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
