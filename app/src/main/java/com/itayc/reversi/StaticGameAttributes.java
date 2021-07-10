package com.itayc.reversi;

/**
 * This class contains attributes that are common to a saved ongoing game (OnGoingGameDetails)
 * and a finished game (FinishedGameDetails), but not to a live game (LiveGameDetails).
 */
public class StaticGameAttributes {

    // Attributes

    public final int timePassed; // time passed since game started
    public long id; // the id of the game (or -1 if game has no id yet)


    // Constructors

    /**
     * Constructor of the class: receives relevant attributes.
     *
     * @param timePassed time passed since game started.
     * @param id         the id of the game.
     */
    public StaticGameAttributes(int timePassed, long id) {
        this.timePassed = timePassed;
        this.id = id;
    }

    /**
     * Secondary constructor of the class: receives relevant attributes except for the game id.
     *
     * This constructor is used for games that are yet to have an id.
     *
     * @param timePassed time passed since game started.
     */
    public StaticGameAttributes(int timePassed) {
        this.timePassed = timePassed;
        this.id = -1;
    }
}
