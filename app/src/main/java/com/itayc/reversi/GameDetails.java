package com.itayc.reversi;

/**
 * An abstract class used to contain basic details about a game.
 */
public abstract class GameDetails { // Abstract class for storing basic game details

    // Attributes

    protected final int turnsPlayed; // turns played
    protected final Piece firstPlayer; // first player's color at the game
    protected final Piece secondPlayer; // second player's color at the game
    protected final Piece gameWinner; // winner of the game
    protected final int firstPieces; // first player's amount of pieces
    protected final int secondPieces; // second player's amount of pieces
    protected final long turnAvgFirst; // first player's average time per turn (in milliseconds)
    protected final long turnAvgSecond; // second player's average time per turn (in milliseconds)

    protected final GameController.Difficulty difficulty; // the difficulty of the game


    // Constructor

    /**
     * Constructor for the class. Gets as parameters relevant attributes.
     *
     * @param turnsPlayed Turns played until game ended.
     * @param firstPlayer First player's color at the game.
     * @param secondPlayer Second player's color at the game.
     * @param gameWinner Winner of the game.
     * @param firstPieces First player's amount of pieces when the game finished.
     * @param secondPieces  Second player's amount of pieces when the game finished.
     * @param turnAvgFirst  the average turn time of the first player.
     * @param turnAvgSecond the average turn time of the second player.
     * @param difficulty the difficulty of the game.
     */
    public GameDetails(int turnsPlayed, Piece firstPlayer, Piece secondPlayer,
                       Piece gameWinner, int firstPieces, int secondPieces,
                       long turnAvgFirst, long turnAvgSecond,
                       GameController.Difficulty difficulty) {

        this.turnsPlayed = turnsPlayed;
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        this.gameWinner = gameWinner;
        this.firstPieces = firstPieces;
        this.secondPieces = secondPieces;
        this.turnAvgFirst = turnAvgFirst;
        this.turnAvgSecond = turnAvgSecond;

        this.difficulty = difficulty;
    }


    // Methods

    /**
     * A getter for the amount of played turns.
     *
     * @return the amount of the played turns.
     */
    public int getTurnsPlayed() {
        return this.turnsPlayed;
    }

    /**
     * A getter for the first player's piece in the game.
     *
     * @return the first player's piece in the game.
     */
    public Piece getFirstPlayer() {
        return this.firstPlayer;
    }

    /**
     * A getter for the second player's piece in the game.
     *
     * @return the second player's piece in the game.
     */
    public Piece getSecondPlayer() {
        return this.secondPlayer;
    }

    /**
     * A getter for the winner of the game.
     *
     * @return the winner of the game (or Empty if game is not yet ended/ended as a tie).
     */
    public Piece getGameWinner() {
        return this.gameWinner;
    }

    /**
     * A getter for the first player's current amount of pieces.
     *
     * @return the first player's current amount of pieces.
     */
    public int getFirstPieces() {
        return this.firstPieces;
    }

    /**
     * A getter for the second player's current amount of pieces.
     *
     * @return the second player's current amount of pieces.
     */
    public int getSecondPieces() {
        return this.secondPieces;
    }

    /**
     * A getter for the first player's current average time to play a turn.
     *
     * @return the first player's current average time to play a turn.
     */
    public long getTurnAvgFirst() {
        return this.turnAvgFirst;
    }

    /**
     * A getter for the second player's current average time to play a turn.
     *
     * @return the second player's current average time to play a turn.
     */
    public long getTurnAvgSecond() {
        return this.turnAvgSecond;
    }

    /**
     * A getter for the difficulty of the game.
     *
     * @return the difficulty of the game.
     */
    public GameController.Difficulty getDifficulty() {
        return this.difficulty;
    }
}
