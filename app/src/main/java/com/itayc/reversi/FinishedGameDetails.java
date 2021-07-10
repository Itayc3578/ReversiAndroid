package com.itayc.reversi;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import java.util.Date;

/**
 * A class used to represent a finished game: the class object contains details about a
 * finished game.
 */
public class FinishedGameDetails extends GameDetails {

    // Attributes

    private final StaticGameAttributes staticAttributes; // attributes of a static game

    private Piece gameLoser; // loser of the game (Calculates automatically)
    private final Date dateFinished; // the date when the game was finished


    // Constructors

    /**
     * Constructor of the class. Gets as parameters relevant attributes.
     *
     * @param turnsPlayed turns played until game ended.
     * @param firstPlayer first player's color at the game.
     * @param secondPlayer second player's color at the game.
     * @param gameWinner winner of the game (Empty if it was a tie).
     * @param firstPieces first player's amount of pieces when the game finished.
     * @param secondPieces  second player's amount of pieces when the game finished.
     * @param turnAvgFirst  the average turn time of the first player.
     * @param turnAvgSecond the average turn time of the second player.
     * @param difficulty the difficulty of the game.
     * @param timePlayed Time passed until game ended.
     */
    public FinishedGameDetails(int turnsPlayed, Piece firstPlayer,
                               Piece secondPlayer, Piece gameWinner, int firstPieces,
                               int secondPieces, long turnAvgFirst, long turnAvgSecond,
                               GameController.Difficulty difficulty, int timePlayed) {

        super(turnsPlayed, firstPlayer, secondPlayer, gameWinner, firstPieces, secondPieces,
                turnAvgFirst, turnAvgSecond, difficulty);

        init();

        this.staticAttributes = new StaticGameAttributes(timePlayed);
        this.dateFinished = new Date(); // saving current date
    }

    /**
     * Second constructor of the class (with id). Gets as parameters relevant attributes.
     *
     * @param turnsPlayed turns played until game ended.
     * @param firstPlayer first player's color at the game.
     * @param secondPlayer second player's color at the game.
     * @param gameWinner winner of the game.
     * @param firstPieces first player's amount of pieces when the game finished.
     * @param secondPieces  second player's amount of pieces when the game finished.
     * @param turnAvgFirst  the average turn time of the first player.
     * @param turnAvgSecond the average turn time of the second player.
     * @param difficulty the difficulty of the game.
     * @param timePlayed Time passed until game ended.
     * @param id id of the game.
     * @param dateFinished the date when the game was finished.
     */
    public FinishedGameDetails(int turnsPlayed, Piece firstPlayer,
                               Piece secondPlayer, Piece gameWinner, int firstPieces,
                               int secondPieces, long turnAvgFirst, long turnAvgSecond,
                               GameController.Difficulty difficulty, int timePlayed, long id,
                               Date dateFinished) {

        super(turnsPlayed, firstPlayer, secondPlayer, gameWinner, firstPieces,
                secondPieces, turnAvgFirst, turnAvgSecond, difficulty);

        init();

        this.staticAttributes = new StaticGameAttributes(timePlayed, id);
        this.dateFinished = dateFinished;

    }

    /**
     * Third constructor of the class: gets as parameters a GameDetails and the time played,
     * and copies the details from the received object as well as setting the timePlayed attribute
     * to the received time played.
     *
     * @param gameDetails an object that contains basic details about the game.
     * @param timePlayed the time that took the game to finish.
     */
    public FinishedGameDetails(GameDetails gameDetails, int timePlayed) {
        this(
                gameDetails.getTurnsPlayed(),
                gameDetails.getFirstPlayer(),
                gameDetails.getSecondPlayer(),
                gameDetails.getGameWinner(),
                gameDetails.getFirstPieces(),
                gameDetails.getSecondPieces(),
                gameDetails.getTurnAvgFirst(),
                gameDetails.getTurnAvgSecond(),
                gameDetails.getDifficulty(),
                timePlayed
        );
    }


    // Methods

    /**
     * A private sub-method for the constructors to execute common initiative code.
     */
    private void init() {
        if (this.gameWinner.isValid()) // there is a loser
            this.gameLoser = this.gameWinner == this.firstPlayer ?
                    this.secondPlayer : this.firstPlayer;
    }

    /**
     * A getter for the id attribute.
     *
     * @return the id of the game.
     */
    public long getId() {
        return this.staticAttributes.id;
    }

    /**
     * A setter for the id attribute.
     *
     * @param id the id to set the id attribute to.
     */
    public void setId(long id) {
        this.staticAttributes.id = id;
    }

    /**
     * A method that returns the time played of the game as a formatted string.
     *
     * @return the time played of the game as a formatted string.
     */
    @SuppressLint("DefaultLocale") // suppressing it because it returns time and not date.
    public String getTimePlayedFormatted() {
        int timePlayed = getTimePlayedRaw();

        return String.format("%02d:%02d", timePlayed / 60, timePlayed % 60);
    }

    /**
     * An override to the toString method. returns a String that describes (briefly)
     * the finished game.
     *
     * @return a String that describes (briefly) the finished game.
     */
    @NonNull
    @Override
    public String toString() {
        final String winnerIntro = this.gameWinner.isValid() ?
                ("Winner: " + this.gameWinner + " with " + getPieces(this.gameWinner)
                        + " Pieces; " + "Loser: " + this.gameLoser + " with "
                        + getPieces(this.gameLoser) + " Pieces. ") :
                ("It's a Draw: both players have " + getPieces(this.gameWinner) + " Pieces.");

        return winnerIntro + "Time played: " + getTimePlayedFormatted() + ", Turns played: "
                + this.turnsPlayed + ".";
    }

    /**
     * A "raw" getter for the played time: returns the played time as is- the time passed
     * since the game started in seconds.
     *
     * @return the played time as is- the time passed since the game started in seconds.
     */
    public int getTimePlayedRaw() {
        return this.staticAttributes.timePassed;
    }

    /**
     * A getter for the dateFinished attribute.
     *
     * @return the Date object that represents the game's ending date.
     */
    public Date getDate() {
        return this.dateFinished;
    }

    /**
     * A getter for a player's pieces: gets a player's piece as a parameter and returns the
     * amount of pieces of that player (if the received player doesn't exist it returns by
     * default the amount of pieces of the second player - shouldn't happen since this method
     * is private and used only within the class for the toString method).
     *
     * @param player the player of which to get the number of pieces.
     * @return the number of pieces of the received player.
     */
    private int getPieces(Piece player) {
        return player == this.firstPlayer ? this.firstPieces : this.secondPieces;
    }
}
