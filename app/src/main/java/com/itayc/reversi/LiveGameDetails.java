package com.itayc.reversi;

import java.util.Collection;

/**
 * The class whose object used to represent details about a live game.
 */
public class LiveGameDetails extends GameDetails {

    // Attributes

    protected final int firstTurnsPlayed; // amount of turns first player played
    protected final boolean isGameOver; // true if the game has ended or false otherwise
    protected Piece[][] board; // the board matrix of the game
    protected Collection<String> emptyToCheck; // empty squares to check Collection
    protected final Piece currentPlayer; // current turn's player
    protected final int boardSize; // board size
    protected final int startSize; // starting amount of pieces for each player
    protected final boolean isHumanTurn; // true if it is currently a human's turn, false otherwise


    // Constructors

    /**
     * Main constructor of the class: receives relevant attributes.
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
     */
    public LiveGameDetails(
            int turnsPlayed,
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
            boolean isHumanTurn) {

        super(turnsPlayed, firstPlayer, secondPlayer, gameWinner, firstPieces, secondPieces,
                turnAvgFirst, turnAvgSecond, difficulty);

        this.firstTurnsPlayed = firstTurnsPlayed;
        this.isGameOver = isGameOver;
        this.board = board;
        this.emptyToCheck = emptyToCheck;
        this.currentPlayer = currentPlayer;
        this.boardSize = boardSize;
        this.startSize = startSize;
        this.isHumanTurn = isHumanTurn;
    }

    /**
     * The secondary constructor of the class: receives relevant attributes for new game and
     * initiates the instance as a new default game.
     *
     * @param firstPlayer  the first player of the game.
     * @param secondPlayer the second player of the game.
     * @param boardSize    the board matrix side's size.
     * @param startSize    the initiative amount of pieces for each player.
     * @param startPlayer  the player that kick offs the game.
     * @param difficulty   the difficulty of the game.
     * @param isHumanStarts  true if a human starts the game, false otherwise.
     */
    public LiveGameDetails(Piece firstPlayer,
                           Piece secondPlayer,
                           int boardSize,
                           int startSize,
                           Piece startPlayer,
                           GameController.Difficulty difficulty,
                           boolean isHumanStarts) {

        this(
                GameController.INIT_TURNS, // default turn initiative number
                firstPlayer, // first player
                secondPlayer, // second player
                GameController.INIT_WINNER, // default winner
                startSize, // amount of pieces for first player
                startSize, // amount of pieces for second player
                GameController.INIT_AVG, // default average value before first update
                GameController.INIT_AVG, // default average value before first update
                difficulty, // default difficulty
                GameController.INIT_TURNS, // first player's initiative turn count
                GameController.INIT_GAME_OVER, // initiative value for isGameOver attribute
                GameController.INIT_BOARD, // board array -> initiative board array
                GameController.INIT_EMPTY_TO_CHECK, // empty squares to check
                startPlayer, // current player -> first player starts
                boardSize, // size for the matrix board
                startSize, // amount of pieces for each player to start with
                isHumanStarts // is human the starting player?
        );
    }

    /**
     * Copy constructor- shallow copy!
     *
     * @param liveGameDetails an instance to copy elements from.
     */
    public LiveGameDetails(LiveGameDetails liveGameDetails) {
        this(
                liveGameDetails.turnsPlayed,
                liveGameDetails.firstPlayer,
                liveGameDetails.secondPlayer,
                liveGameDetails.gameWinner,
                liveGameDetails.firstPieces,
                liveGameDetails.secondPieces,
                liveGameDetails.turnAvgFirst,
                liveGameDetails.turnAvgSecond,
                liveGameDetails.difficulty,
                liveGameDetails.firstTurnsPlayed,
                liveGameDetails.isGameOver,
                liveGameDetails.board,
                liveGameDetails.emptyToCheck,
                liveGameDetails.currentPlayer,
                liveGameDetails.boardSize,
                liveGameDetails.startSize,
                liveGameDetails.isHumanTurn
        );
    }


    // Methods

    /**
     * A getter for the firstTurnsPlayed attribute.
     *
     * @return the amount of turns played by the first player.
     */
    public int getFirstTurnsPlayed() {
        return this.firstTurnsPlayed;
    }

    /**
     * A getter for the second player's played turns.
     *
     * @return the amount of turns played by the second player.
     */
    public int getSecondTurnsPlayer() {
        return getTurnsPlayed() - getFirstTurnsPlayed();
    }

    /**
     * A getter for the isGameOver attribute.
     *
     * @return true if the game is over or false otherwise.
     */
    public boolean isGameOver() {
        return this.isGameOver;
    }

    /**
     * A getter for the game board.
     *
     * @return a reference to the board matrix.
     */
    public Piece[][] getBoard() {
        return this.board; // do i need clone?
    }

    /**
     * A getter for the emptyToCheck attribute.
     *
     * @return a reference to the Collection that contains empty squares to check
     * (while calculating a turn's valid moves).
     */
    public Collection<String> getEmptyToCheck() {
        return this.emptyToCheck;
    }

    /**
     * A getter for the current player.
     *
     * @return the current player's piece.
     */
    public Piece getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * A getter for the next player to play.
     *
     * @return the next player to play's piece.
     */
    public Piece getNextPlayer() {
        return this.firstPlayer == this.currentPlayer ?
                this.secondPlayer: this.firstPlayer;
    }

    /**
     * A getter for the board size.
     *
     * @return the board matrix side's size.
     */
    public int getBoardSize() {
        return this.boardSize;
    }

    /**
     * A getter for the startSize attribute.
     *
     * @return the initiative amount of pieces for each player.
     */
    public int getStartSize() {
        return this.startSize;
    }

    /**
     * A getter for the isHumanTurn attribute.
     *
     * @return true if the current turn is a human's turn or false otherwise.
     */
    public boolean isHumanTurn() {
        return this.isHumanTurn;
    }

    /**
     * A setter for the board attribute.
     *
     * @param board a reference to the new board to set to.
     */
    public void setBoard(Piece[][] board) {
        this.board = board;
    }

    /**
     * A setter for the emptyToCheck attribute.
     *
     * @param emptyToCheck a reference to the new emptyToCheck Collection to set to.
     */
    public void setEmptyToCheck(Collection<String> emptyToCheck) {
        this.emptyToCheck = emptyToCheck;
    }
}
