package com.itayc.reversi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The "contact us" activity.
 */
public class ContactActivity extends AppCompatActivity {

    // Methods

    /**
     * The onCreate method (called when the activity is created).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
    }

    /**
     * A method that is called when a player wishes to mail to the development team.
     * It creates an implicit Intent that allows the user to send an email to the development
     * team.
     *
     * @param view the clicked view that invoked the method.
     */
    public void mailUs(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        // Note that i just came up with that email and by no means is it related to me.
        emailIntent.setData(Uri.parse("mailto:reversi6841356241@gmail.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact Reversi development team");
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Hello Reversi development team, i wish to ask you: ");

        try {
            startActivity(emailIntent);
        }
        catch (android.content.ActivityNotFoundException e)
        {
            Toast.makeText(this,
                    "Some unexpected error occurred while trying to mail",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A method that is called when a player wishes to call to the development team.
     * It creates an implicit Intent that allows the user to call the development team.
     *
     * @param view the clicked view that invoked the method.
     */
    public void callUs(View view) {
        // I prefer not to change manifest and use ACTION_CALL,
        // this way it's up to the user to decide weather or not he wishes to make the call
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:0521234567"));

        try {
            startActivity(callIntent);
        }
        catch (android.content.ActivityNotFoundException e)
        {
            Toast.makeText(this,
                    "Some unexpected error occurred while trying to call",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A method that finishes the activity and returns to the referring activity (beneath it
     * in the activity stack).
     *
     * @param view the clicked view that invoked the method.
     */
    public void goBack(View view) {
        finish();
    }
}