package com.itayc.reversi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The activity in which the user selects the game difficulty.
 */
public class SelectDifficultyActivity extends AppCompatActivity {

    // Attributes

    public static final String DIFFICULTY = "Difficulty"; // the Intent's extra key's name


    // Methods

    /**
     * The onCreate method (called when the activity is created).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_difficulty);
    }

    /**
     * A method that goes to the Game activity with intent that indicates the desired difficulty.
     *
     * @param view the clicked view that invoked the method.
     */
    public void goGame(View view) {
        Intent gameIntent = new Intent(this, GameActivity.class);
        GameController.Difficulty difficulty;

        switch (view.getId()) {
            case R.id.btnLocalGame:
                difficulty = GameController.Difficulty.LOCAL;
                break;

            case R.id.btnEasyGame:
                difficulty = GameController.Difficulty.EASY;
                break;

            case R.id.btnMediumGame:
                difficulty = GameController.Difficulty.MEDIUM;
                break;

            case R.id.btnHardGame:
                difficulty = GameController.Difficulty.HARD;
                break;

            case R.id.btnExtremeGame:
                difficulty = GameController.Difficulty.EXTREME;
                break;

            default:
                difficulty = null;
        }

        gameIntent.putExtra(DIFFICULTY, difficulty);
        startActivity(gameIntent);
    }
}