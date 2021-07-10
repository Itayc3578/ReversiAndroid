package com.itayc.reversi;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

/**
 * Helper class the interact with the background music service.
 */
public class BgMusicService extends Service {

    // Attributes

    private static final int MUSIC_SRC = R.raw.background_music; // resource background music id
    private static final float VOLUME = 50f; // the volume of the music

    private static int lastPosition = -1; // to enable resuming the music

    private MediaPlayer player; // MediaPlayer object to enable playing background music


    // Methods

    /**
     * The onCreate method of the service: gets called when the service is being created (for
     * the first time) - either by binding it or by starting it with command.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        this.player = MediaPlayer.create(this, MUSIC_SRC);

        // to enable playing non stop background music
        this.player.setLooping(true);

        // setting the volume
        this.player.setVolume(VOLUME, VOLUME);

        if (lastPosition != -1) // there is a saved position to resume
            this.player.seekTo(lastPosition); // resume from last saved position
    }

    /**
     * A method that is called when the service was called using startService().
     *
     * @param intent  the intent calling the service.
     * @param flags   flags of the intent.
     * @param startId the specific request to start.
     * @return starting status.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.player.start();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * A method that is called when the service is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        lastPosition = this.player.getCurrentPosition(); // save the position for future resumes
        this.player.stop();
    }

    /**
     * A method that gets called when the service is bound using bindService().
     *
     * Note: a bound service is stopped after all the bounded clients unbind themselves from it.
     *
     * @param intent the binding intent.
     * @return IBinder to enable user-interface.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null; // not supporting binding
    }
}