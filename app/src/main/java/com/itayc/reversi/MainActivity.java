package com.itayc.reversi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * The main activity of the application.
 */
public class MainActivity extends AppCompatActivity {

    // Attributes

    /**
     * BroadcastReceiver for low battery.
     */
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Battery is low", Toast.LENGTH_SHORT).show();
        }
    };

    private Intent bgMusicIntent; // Intent for the background music service.


    // Methods

    /**
     * The onCreate method (called when the activity is created).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        // register the broadcast receiver
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));

        SettingsManager settings = SettingsManager.getInstance(this);

        bgMusicIntent = new Intent(this, BgMusicService.class);

        if (settings.getBooleanValue(SettingsManager.Keys.BACKGROUND_MUSIC))
            startService(bgMusicIntent);
    }

    /**
     * A method that goes to the Settings activity.
     *
     * @param view the view that invoked the method.
     */
    public void goSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent); // go
    }

    /**
     * A method that goes to the Contact activity.
     *
     * @param view the view that invoked the method.
     */
    public void goContact(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent); // go
    }

    /**
     * A method that goes to the Game activity.
     *
     * @param view the view that invoked the method.
     */
    public void goQuickGame(View view) {
        Intent gameIntent = new Intent(this, GameActivity.class);
        startActivity(gameIntent);
    }

    /**
     * A method that goes to the SelectDifficulty activity.
     *
     * @param view the view that invoked the method.
     */
    public void goSelectDifficulty(View view) {
        Intent difficultyIntent = new Intent(this, SelectDifficultyActivity.class);
        startActivity(difficultyIntent);
    }

    /**
     * A method that goes to the Records activity.
     *
     * @param view the view that invoked the method.
     */
    public void goRecords(View view) {
        Intent recordsIntent = new Intent(this, RecordsActivity.class);
        startActivity(recordsIntent);
    }

    /**
     * The String array of the required permissions of the application.
     */
    private final String[] required_permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    /**
     * A private method that checks if the required permission are granted: if they aren't, it
     * asks the user to permit them.
     */
    private void checkAndRequestPermissions() {
        List<String> requiredPermissions = new ArrayList<>();

        // add all not permitted permissions to the list
        for (String permission : required_permissions)
            if (ContextCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED)
                requiredPermissions.add(permission);

        // ask for permissions
        if (!requiredPermissions.isEmpty()) { // not all necessary permissions are granted
            ActivityCompat.requestPermissions(this,
                    requiredPermissions.toArray(new String[0]), 1);
        }
    }

    /**
     * The onDestroy method that is invoked when the activity is destroyed.
     *
     * Since that is the main activity when it's destroyed the broadcast receivers have to be
     * unregistered.
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(batteryReceiver);

        stopService(bgMusicIntent);
        super.onDestroy();
    }

}