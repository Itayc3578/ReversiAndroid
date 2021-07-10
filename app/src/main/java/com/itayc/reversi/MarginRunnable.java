package com.itayc.reversi;

import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * A Runnable class to enable measuring correctly the margins after the game board is painted.
 */
public class MarginRunnable implements Runnable {

    // Attributes

    private static int margin = -1; // the final margin of the piece (to ensure consistency)

    private final int SCALE; // the scale according to which to set the margin
    private final Button btn; // the button of which to set the margin
    private final FrameLayout parent; // the parent layout of the button


    // Constructor

    /**
     * The constructor of the class.
     *
     * @param parent      the button's parent (the margin measurement will be according to it).
     * @param btn         the button of which to set the margin.
     * @param marginScale the margin scale: 10 means tenth of the parent will be the margin...
     */
    public MarginRunnable(FrameLayout parent, Button btn, int marginScale) {
        this.btn = btn;
        this.SCALE = marginScale;
        this.parent = parent;

        this.btn.setVisibility(View.INVISIBLE); // it will be altered after drawn - until
        // the margin is correctly measured the button will be invisible
    }


    // Methods

    /**
     * The run method: it get's called after the measures are ready and it is possible to
     * measure the desired margin.
     *
     * It measures the margin according to the instance's attributes and sets it.
     */
    @Override
    public void run() {
        // calculate the margin
        if (margin == -1)
            margin = this.parent.getWidth() / this.SCALE;

        FrameLayout.MarginLayoutParams marginLayoutParams =
                (FrameLayout.MarginLayoutParams) this.btn.getLayoutParams();

        marginLayoutParams.setMargins(margin, margin, margin, margin);

        this.btn.requestLayout();

        this.btn.setVisibility(View.VISIBLE); // after measured, the button can be displayed
    }

}
