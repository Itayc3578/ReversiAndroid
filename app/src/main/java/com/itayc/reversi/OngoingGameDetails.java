package com.itayc.reversi;

import java.util.Collection;

/**
 * A class whose object is used to contain details about an ongoing game (a paused/saved game),
 * including details that are relevant to the display.
 */
public class OngoingGameDetails extends LiveGameDetails { // saved game details (including display
    // related)

    // Attributes

    private final StaticGameAttributes staticAttributes; // attributes of a static game


    // Constructors

    /**
     * Constructor for the class: gets as parameters relevant attributes and calls
     * super constructor.
     *
     * @param turnsPlayed      the current amount of played turns.
     * @param firstPlayer      the first player of the game.
     * @param secondPlayer     the second player of the game.
     * @param gameWinner       the winner of the game (or empty if it has no winner).
     * @param firstPieces      the current amount of pieces of the first player.
     * @param secondPieces     the current amount of pieces of the second player.
     * @param turnAvgFirst     the current average time of the turn of the first player.
     * @param turnAvgSecond    the current average time of the turn of the second player.
     * @param difficulty       the difficulty of the game.
     * @param firstTurnsPlayed the amount of turns played by the first player.
     * @param isGameOver       true if the game is over or false otherwise.
     * @param board            the matrix board that represents the game board.
     * @param emptyToCheck     the Collection of empty pieces to check for next turn's valid moves.
     * @param currentPlayer    the current player.
     * @param boardSize        the board size of the game (matrix side's size).
     * @param startSize        the starting amount of pieces for each player.
     * @param isHumanTurn      true if the current turn is human's or false otherwise.
     * @param timePlayed       time passed since game started (in seconds).
     */
    public OngoingGameDetails(int turnsPlayed,
                              Piece firstPlayer,
                              Piece secondPlayer,
                              Piece gameWinner,
                              int firstPieces,
                              int secondPieces,
                              long turnAvgFirst,
                              long turnAvgSecond,
                              GameController.Difficulty difficulty,
                              int firstTurnsPlayed,
                              boolean isGameOver,
                              Piece[][] board,
                              Collection<String> emptyToCheck,
                              Piece currentPlayer,
                              int boardSize,
                              int startSize,
                              boolean isHumanTurn,
                              int timePlayed,
                              long id) {

        super(turnsPlayed, firstPlayer, secondPlayer, gameWinner, firstPieces, secondPieces,
                turnAvgFirst, turnAvgSecond, difficulty, firstTurnsPlayed, isGameOver,
                board, emptyToCheck, currentPlayer, boardSize, startSize, isHumanTurn);

        this.staticAttributes = new StaticGameAttributes(timePlayed, id);
    }

    /**
     * Super copy constructor of the class: receives a LiveGameDetails object and the passed
     * time since game started as parameters, and calls the super copy constructor & sets
     * unique attributes.
     *
     * @param liveGameDetails a LiveGameDetails to copy.
     * @param timePlayed time passed since game started.
     */
    public OngoingGameDetails(LiveGameDetails liveGameDetails, int timePlayed) {
        super(liveGameDetails);

        this.staticAttributes = new StaticGameAttributes(timePlayed);
    }


    // Methods

    /**
     * A getter for the played time.
     *
     * @return the time passed since game was started (in seconds).
     */
    public int getTimePlayed() {
        return this.staticAttributes.timePassed;
    }

    /**
     * A getter for the id attribute.
     *
     * @return the id of the game (or -1 if game has no id yet).
     */
    public long getId() {
        return this.staticAttributes.id;
    }

    /**
     * A setter for the id attribute.
     *
     * @param id  the id to set to.
     */
    public void setId(long id) {
        this.staticAttributes.id = id;
    }

}
