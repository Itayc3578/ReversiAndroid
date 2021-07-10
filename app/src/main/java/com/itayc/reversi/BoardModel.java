package com.itayc.reversi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * The Model class (the first part of the model class) of the project (according to the
 * MVC model).
 */
public class BoardModel { // game model

    // Attributes

    private static final long INIT_AVG = -1L; // initiative average before updated
    private static final int INIT_TURNS = 0; // initiative turn count
    private static final Piece INIT_WINNER = Piece.EMPTY; // initiative winner
    private static final boolean INIT_GAME_OVER = false; // initiative value for isGameOver
    private static final Piece[][] INIT_BOARD = null; // initiative board state is null
    private static final Collection<String> INIT_EMPTY_TO_CHECK = null; // initiative
    // emptyToCheck is null
    private static final int INIT_PIECE_COUNT = 2; // initiative piece count

    private int boardSize; // The size of the matrix board's side

    private final BoardState boardState; // an object that hold the state of the board

    private HashMap<String, List<Cell>> validChoices; // the valid choices for current player

    private Piece winner; // the current winner of the game
    private boolean isGameOver; // true if the game has ended or false otherwise

    private final HashMap<Piece, Integer> turnsCount; // dictionary for each player's played turns

    private final HashMap<Piece, Double> avgTurns; // average turn reaction time of the players
    private long lastTimeStamp; // the last time stamp to compare for average calculating


    // Constructor

    /**
     * Constructor of the class. gets as parameters the first and second players and
     * initiates relevant attributes.
     *
     * @param firstPlayer the first player.
     * @param secondPlayer the second player.
     */
    public BoardModel(Piece firstPlayer, Piece secondPlayer) {
        this.boardState = new BoardState();

        this.avgTurns = new HashMap<>();

        this.avgTurns.put(firstPlayer, (double) INIT_AVG);
        this.avgTurns.put(secondPlayer, (double) INIT_AVG);

        this.turnsCount = new HashMap<>();
    }


    // Methods

    /**
     * A method that gets as a parameter an object that contains details about a live game
     * and resumes it as the current game state.
     *
     * @param gameState an object that contains details about a game state so that the model
     *                  can load it.
     */
    public void loadGame(LiveGameDetails gameState) {
        Piece firstPlayer = gameState.getFirstPlayer();
        Piece secondPlayer = gameState.getSecondPlayer();

        this.boardSize = gameState.getBoardSize();
        this.isGameOver = gameState.isGameOver();
        this.winner = gameState.getGameWinner();

        this.boardState.clearPieceAmounts();
        this.boardState.putPieceAmount(firstPlayer, gameState.getFirstPieces());
        this.boardState.putPieceAmount(secondPlayer, gameState.getSecondPieces());

        this.turnsCount.clear();
        this.turnsCount.put(firstPlayer, gameState.getFirstTurnsPlayed());
        this.turnsCount.put(secondPlayer, gameState.getSecondTurnsPlayer());

        this.avgTurns.put(firstPlayer, (double) gameState.getTurnAvgFirst());
        this.avgTurns.put(secondPlayer, (double) gameState.getTurnAvgSecond());

        this.boardState.clearEmptyToCheck();

        Piece[][] boardToLoad = gameState.getBoard();

        if (boardToLoad != INIT_BOARD) { // loading ready game state
            this.boardState.copyBoard(boardToLoad);
            this.boardState.addAllEmpties(gameState.getEmptyToCheck());
        }
        else // need to initiate game state
        {
            initModelBoard(firstPlayer, secondPlayer, gameState.getStartSize());
            gameState.setBoard(getBoardClone());
            gameState.setEmptyToCheck(getEmptyToCheckClone());
        }

        if (!this.isGameOver) // to avoid unnecessary run
            this.validChoices = this.boardState.validChoices(gameState.getCurrentPlayer(),
                    gameState.getNextPlayer());

        this.lastTimeStamp = System.currentTimeMillis(); // take time stamp
    }

    /**
     * A private method that gets as parameters the first player, the second player, and a starting
     * amount of pieces for each player, and initiates the game board accordingly.
     *
     * @param firstPlayer the first player's piece.
     * @param secondPlayer the second player's piece.
     * @param startSize the starting amount of pieces for each player.
     */
    private void initModelBoard(Piece firstPlayer, Piece secondPlayer, int startSize) {
        this.boardState.initBoard(this.boardSize);

        // size of starting sub-matrix
        int startSide = (int) (Math.sqrt(startSize * 2));

        // Index of first middle square (lowest index to start placing players' squares)
        int firstMiddleIndex = (this.boardSize - startSide) / 2;

        // Initiate board
        for (int i = 0; i< this.boardSize; i++)
            for (int j = 0; j< this.boardSize; j++)
                if (i >= firstMiddleIndex && i < firstMiddleIndex + startSide
                        && j >= firstMiddleIndex && j < firstMiddleIndex + startSide) // middle
                    this.boardState.setSquare((i + j) % 2 == 0 ? firstPlayer : secondPlayer, i, j);
                else
                    this.boardState.setSquare(INIT_WINNER, i, j);

        // Initiate empty-squares-to-check Collection
        for (int i = firstMiddleIndex - 1; i <= firstMiddleIndex + startSide; i++)
            for (int j = firstMiddleIndex - 1; j <= firstMiddleIndex + startSide; j++)
                if (this.boardState.isInBoard(i, j) && this.boardState.isSquareEmpty(i, j))
                    this.boardState.addEmpty(Cell.toSquareTag(i, j));
    }

    /**
     * A method that receives a location on the board (row and column) and returns the piece of
     * the player at that location.
     *
     * @param row location row.
     * @param col location column.
     * @return the piece of the player at the received location.
     */
    public Piece getPieceByLocation(int row, int col) {
        return this.boardState.getSquare(row, col);
    }

    /**
     * A method that receives the current player's piece, the piece of the other player
     * (the rival), and a List of Cells that represents squares that need to be changed according
     * to the current move, and plays the next turn.
     *
     * @param current current player's piece.
     * @param rival the piece of rival of the current player (the other/next player).
     * @param toChangeArray a List of Cells that represents squares that need to be changed
     *                      according to the current move of the player.
     * @return true if the next turn's player is the rival (next) player, or false otherwise
     * (that is, the current player has another turn because the rival had no available
     * choices to play after the current turn was played- so the turn passes back to the current
     * player).
     */
    public boolean nextTurn(Piece current, Piece rival, List<Cell> toChangeArray) {
        incrementTurnsCount(current); // increment the turns count of the player
        updateAverage(current); // update the average time for the current player to make decision

        this.boardState.updateBoard(toChangeArray, current, rival); // update the board
        this.validChoices = this.boardState.validChoices(rival, current); // turn changed

        if (this.validChoices.size() == 0) { // no available choice for the next turn
            this.validChoices = this.boardState.validChoices(current, rival);

            if (this.validChoices.size() != 0) // other player can play
                return false; // turn has not changed

            // no available choices for either players
            finishGame(current, rival);
        }

        return true;
    }

    /**
     * A method that finishes the game. It receives as parameters the current player and the
     * rival of that player, and updates relevant attributes according to game's final state.
     *
     * @param currentPlayer the piece of the current player.
     * @param otherPlayer the piece of the rival of the current player.
     */
    public void finishGame(Piece currentPlayer, Piece otherPlayer) {
        this.isGameOver = true;

        int currentPieces = getPieceAmount(currentPlayer);
        int otherPieces = getPieceAmount(otherPlayer);

        // update the winner

        if (currentPieces > otherPieces)
            this.winner = currentPlayer;
        else if (otherPieces > currentPieces)
            this.winner = otherPlayer;
        // else winner stays Empty
    }

    /**
     * A private method that gets as a parameter a player and increments his turn count.
     *
     * @param player a player of which the turns count will be incremented.
     */
    @SuppressWarnings("ConstantConditions") // asserting the player is valid
    private void incrementTurnsCount(Piece player) {
        this.turnsCount.put(player, this.turnsCount.get(player) + 1);
    }

    /**
     * A private method that gets the current player as a parameter and updates his average time
     * per turn.
     *
     * (If it's the first turn of the received player the resulting calculation will still be
     * his average, so there is no need to check it separately).
     *
     * @param currentPlayer the current player.
     */
    @SuppressWarnings("ConstantConditions") // asserting the player is valid
    private void updateAverage(Piece currentPlayer) {
        long currentTimeStamp = System.currentTimeMillis(); // take current time stamp
        double diff = currentTimeStamp - this.lastTimeStamp; // calculate the difference between
        // the last time stamp and the current one.
        double lastAvg = this.avgTurns.get(currentPlayer); // get the last average recorded before
        // the current turn

        int currentTurn = getTurnsCount(currentPlayer); // get the amount of turns player has
        // played in order to calculate and update the average precisely

        // calculate the new average and update the dictionary
        this.avgTurns.put(currentPlayer, (lastAvg * (currentTurn - 1) + diff) / currentTurn);

        this.lastTimeStamp = currentTimeStamp; // take time stamp for next turn's calculation
    }

    /**
     * A getter for the initiative value of the average time per turn.
     *
     * @return the initiative value of the average time per turn.
     */
    public static long getInitAvg() {
        return INIT_AVG;
    }

    /**
     * A getter for the initiative value of the turn count.
     *
     * @return the initiative value of the turn count.
     */
    public static int getInitTurns() {
        return INIT_TURNS;
    }

    /**
     * A getter for the initiative value of the game winner.
     *
     * @return the initiative value of the game winner.
     */
    public static Piece getInitWinner() {
        return INIT_WINNER;
    }

    /**
     * A getter for the initiative value of the isGameOver boolean.
     *
     * @return the initiative value of the isGameOver boolean.
     */
    public static boolean isInitGameOver() {
        return INIT_GAME_OVER;
    }

    /**
     * A getter for the initiative value of the board matrix.
     *
     * @return the initiative value of the board matrix.
     */
    public static Piece[][] getInitBoard() {
        return INIT_BOARD;
    }

    /**
     * A getter for the initiative value of the empty squares to check Collection.
     *
     * @return the initiative value of the empty squares to check Collection.
     */
    public static Collection<String> getInitEmptyToCheck() {
        return INIT_EMPTY_TO_CHECK;
    }

    /**
     * A getter for the initiative value of the pieces count.
     *
     * @return the initiative value of the pieces count.
     */
    public static int getInitPieceCount() {
        return INIT_PIECE_COUNT;
    }

    /**
     * A getter for the winner attribute.
     *
     * @return the winner of the game, or empty if there is no winner (including draw).
     */
    public Piece getWinner() {
        return this.winner;
    }

    /**
     * A getter for the turns count of a player: gets as parameter a player and returns the
     * number of turns he has played.
     *
     * @param player a player of which to return the turns count.
     * @return the number of turns the received player has played.
     */
    @SuppressWarnings("ConstantConditions") // asserting the player is valid
    public int getTurnsCount(Piece player) {
        return this.turnsCount.get(player);
    }

    /**
     * An overloading method to the turns count getter: receives no parameters, and calculates
     * and returns the summary of the turns that have passed since game was started.
     *
     * @return the amount of turns that have passed since game's start.
     */
    public int getTurnsCount() {
        int sum = 0;
        for (int turnCount: this.turnsCount.values())
            sum += turnCount;

        return sum;
    }

    /**
     * A getter method for the isGameOver attribute.
     *
     * @return true if the game is over or false otherwise.
     */
    public boolean isGameOver() {
        return this.isGameOver;
    }

    /**
     * A getter for the pieces counters of the players. gets as a parameter a player of which
     * the pieces counter will be returned.
     *
     * @param player a player of which the pieces counter will be returned.
     * @return the pieces counter of the received player.
     */
    public int getPieceAmount(Piece player) {
        return this.boardState.getPieceAmount(player);
    }

    /**
     * A getter for the average time that it takes a player to play his turn. Gets as a parameter
     * a player of which the average will be returned.
     *
     * @param player the desired player of which the average will be returned.
     * @return the average time that it takes to the first player to play his turn.
     */
    @SuppressWarnings("ConstantConditions") // asserting the player is valid
    public long getTurnAvg(Piece player) {
        return this.avgTurns.get(player).longValue();
    }

    /**
     * A getter for the valid moves for the current turn.
     *
     * @return the valid move choices for the current turn.
     */
    public HashMap<String, List<Cell>> getValidChoices() {
        return this.validChoices;
    }

    /**
     * A clone getter for the game board (deep copy).
     *
     * @return a clone of the board matrix that represents the game status.
     */
    public Piece[][] getBoardClone() {
        return this.boardState.getBoardClone();
    }

    /**
     * A clone getter for the empty squares to check Collection- returns a clone of the
     * current empty to check Collection.
     *
     * @return a clone of the current empty to check Collection.
     */
    public Collection<String> getEmptyToCheckClone() {
        return this.boardState.getEmptyToCheckClone();
    }

    /**
     * A getter for the boardState attribute.
     *
     * @return the object that contains the state of the board.
     */
    public BoardState getBoardState() {
        return this.boardState;
    }


    // Sizes related:

    public static final int MAX_BOARD_SIZE = 20; // maximum matrix board size
    public static final int MIN_BOARD_SIZE = 4; // minimum matrix board size

    // valid board sizes array, maximum size will be the aforementioned maximum board size
    private static final List<Integer> VALID_BOARD_SIZES = new ArrayList<>();

    // valid starting numbers of pieces for each player
    private static final List<Integer> VALID_START_SIZES = new ArrayList<>();

    // initiating the arrays so that validation check functions will perform O(1)
    // complexity at run-time
    static {
        for (int i = MIN_BOARD_SIZE; i <= MAX_BOARD_SIZE; i += 2) {
            VALID_BOARD_SIZES.add(i);
            VALID_START_SIZES.add((i - 2) * (i - 2) / 2);
        }
    }
    
    /**
     * A function that gets a board size as a parameter and returns a List that contains
     * valid start sizes (starting numbers of pieces for each player) for that board size,
     * or an empty List if there aren't such sizes.
     *
     * @param boardSize a board size.
     * @return a List that contains valid start sizes (starting numbers of pieces for each
     * player) for that board size, or an empty List if there aren't such sizes.
     */
    public static List<Integer> getMatchingStartSizes(int boardSize) {
        double temp = Math.sqrt(boardSize * 2);

        int index = (int) temp;

        // getting rid of board size that equals start size
        index = (index - index % 2) / 2 - (int)(index / temp);

        // checking if received board size is in range
        if (boardSize > MAX_BOARD_SIZE)
            index = 0;

        // multiplying by 0 if boardSize is odd (thus returning empty list), or by 1 if even
        return VALID_START_SIZES.subList(0, (boardSize + 1) % 2 * index);
    }

    /**
     * A function that gets a start size (starting number of pieces for each player) as a parameter
     * and returns a List that contains valid board sizes for that start size, or an empty
     * List if there aren't such sizes.
     *
     * @param startSize start size (starting number of pieces for each player).
     * @return a List that contains valid board sizes for that start size, or an empty
     * List if there aren't such sizes.
     */
    public static List<Integer> getMatchingBoardSizes(int startSize) {
        // getting the size/length of the array
        int length = VALID_BOARD_SIZES.size();

        // Example: startSize = 8

        // multiplying startSize by 2 will result in sub-matrix total size. square root of it
        // will be it's side. now, we will divide it by 2.
        // In the example: total matrix size: 8 * 2 -> 16, square root of 16 is 4. 4 is the
        // sub-matrix size. now, divide it by 2 (all the valid matrix sizes are even). First
        // element in the valid board sizes array is 4, it's index is 0. Because the board sizes
        // are ordered in mathematical series (incrementing by 2), we will first divide it by 2.
        // In the example: 4 / 2 -> 2.
        double index = Math.sqrt(startSize * 2) / 2;

        // Mathematical calculation for the index in the board sizes array: square of the
        // sub-matrix size minus 1. We want to skip sizes that are lower than or equal to
        // the starting sub-matrix.
        // In the example: square of 2 is 4, minus one is 3, and that is the index of the first
        // valid board size for the received startSize.
        index = index * index - 1;

        // invalid: index is not a whole number or not in the array -> returning an empty array.
        // The starting sub-matrix size (startSize multiplied by 2) has to have a whole square
        // root for it to be valid, so the resulting index has to be a whole number.
        // In addition, we have a pre-defined maximum size, so we have to make sure that the
        // resulting index is in the array (we also have a minimum, we can't accept 0
        // as starting size that will result in starting board size of 2x2. The index of it
        // would be -1). Else, index equals length which will result in returning an empty array
        // In the example: 2 is whole, and in the pre-defined range, so it is the index of
        // the first valid board size in the board sizes array, all the following sizes are
        // greater than it so are valid too.
        if (index % 1 != 0 || index < 0 || index > length)
            index = length;

        return VALID_BOARD_SIZES.subList((int) index, length);
    }

}
