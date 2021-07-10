package com.itayc.reversi;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

/**
 * A class to enable convenient interaction with the MusicControl object to enable sounds
 * throughout the application.
 */
public class MusicControl {

    // Attributes

    private static MusicControl musicControl = null; // singleton instance of the class

    private final SoundPool soundPool; // SoundPool to enable playing sounds

    /**
     * An enum used to represent the various sounds.
     */
    public enum Sound {
        PIECE_MOVED(R.raw.piece_moved), // moved piece sound
        GAME_START(R.raw.game_start), // game stated sound
        GAME_OVER(R.raw.game_over), // game over sound
        SUPER_MOVE(R.raw.super_move), // super-move sound
        DIALOG_POPUP(R.raw.dialog_popup), // popup dialog sound
        INVALID_SELECTION(R.raw.invalid_choice); // invalid selection sound


        // Attributes of the enum

        private final int rawId; // id of resource
        private final int priority; // priority (default - 1)
        private int loadedId; // id of loaded

        /**
         * Constructor of the enum: receives it's raw id.
         *
         * @param rawId the raw id of the sound.
         */
        Sound(int rawId) {
            this.rawId = rawId;
            this.priority = 1;
        }
    }


    // Constructor

    /**
     * Constructor of the class: interacts with SoundPool and loads all sounds.
     *
     * @param context context for resources permissions.
     */
    private MusicControl(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) // Newer version >= 21
            soundPool = new SoundPool.Builder().setMaxStreams(20).build();
        else // Older version
            soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 1);

        // Loading sound files
        for (Sound sound: Sound.values())
            sound.loadedId = soundPool.load(context, sound.rawId, sound.priority);
    }


    // Methods

    /**
     * The singleton instance getter of the class.
     *
     * @param context context for resources permissions.
     * @return the singleton instance of the class.
     */
    public static MusicControl getInstance(Context context) {
        if (musicControl == null)
            musicControl = new MusicControl(context);

        return musicControl;
    }


    /**
     * A method that gets a Sound enum value as a parameter and plays it.
     *
     * @param sound a Sound enum to play.
     */
    public void playSound(Sound sound) {
        soundPool.play(sound.loadedId, 1,
                1, sound.priority, 0, 1);
    }

}
