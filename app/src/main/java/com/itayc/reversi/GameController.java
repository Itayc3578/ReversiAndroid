package com.itayc.reversi;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * The Controller class according to the MVC model implemented in the project.
 */
public class GameController { // controller of the game

    // Attributes

    public static final long INIT_AVG = BoardModel.getInitAvg(); // initiative average before
    // updated
    public static final int INIT_TURNS = BoardModel.getInitTurns(); // initiative turn count
    public static final Piece INIT_WINNER = BoardModel.getInitWinner(); // initiative winner
    public static final boolean INIT_GAME_OVER = BoardModel.isInitGameOver(); // initiative value
    // for isGameOver
    public static final Piece[][] INIT_BOARD = BoardModel.getInitBoard(); // initiative board state
    // is null
    public static final Collection<String> INIT_EMPTY_TO_CHECK = BoardModel.getInitEmptyToCheck();
    // initiative emptyToCheck is null
    public static final int INIT_PIECE_COUNT = BoardModel.getInitPieceCount(); // initiative
    // piece count

    private final BoardModel model; // the model of for the game

    private final LiveGameDetails defaultGameState; // default game state for new games
    private final Stack<LiveGameDetails> lastStates; // stack to hold last game's state and
    // enable undo
    private final Stack<LiveGameDetails> undoneStates; // stack to hold states that were undone
    private LiveGameDetails currentGameState; // current state of the game

    private Piece currentPlayer; // current turn's player
    private Piece nextPlayer; // next turn's player
    private Piece firstPlayer; // the first player color
    private Piece secondPlayer; // the second player color

    // Minimax

    /**
     * Enum for game difficulties.
     */
    public enum Difficulty {

        // Enum values

        LOCAL(-1), // local offline game: both players are playing from the host phone
        EASY(1), // easy difficulty vs computer
        MEDIUM(2), // medium difficulty vs computer
        HARD(4), // hard difficulty vs computer
        EXTREME(5); // extreme difficulty vs computer


        // Enum attributes

        private final int depth; // minimax depth of the difficulty


        // Enum Constructor

        /**
         * A (private) constructor for the enum: receives the minimax depth of the difficulty.
         *
         * @param depth the minimax depth of the difficulty.
         */
        Difficulty(int depth) {
            this.depth = depth;
        }


        // Enum methods

        /**
         * A method that returns true if the the difficulty means that the computer is the
         * opponent, or false otherwise.
         *
         * @return true if the difficulty means that the computer is the opponent, or false
         * otherwise.
         */
        public boolean isVsComputer() {
            return this != LOCAL;
        }

        /**
         * A comparing method to compare two difficulties: the "higher" difficulty is the harder.
         * I didn't override compareTo because it's an enum and it can't be done.
         *
         * @param other the other Difficulty enum to compare to.
         * @return A positive number if the current difficulty is higher than then received
         * difficulty, a negative number if the current difficulty is lesser than it, or 0
         * if the difficulty is the same.
         */
        public int compareByLevel(Difficulty other) {
            return this.depth - other.depth;
        }

        /**
         * An override for the toString method: returns the enum converted from capitalized
         * snake case to standard english readable words.
         *
         * @return the enum converted from capitalized snake case to standard english
         * readable words.
         */
        @Override
        public String toString() {
            return SettingsManager.capsSnakeToStandard(this.name());
        }
    }

    private static final int MAX_VAL = Integer.MAX_VALUE; // maximum integer value
    private static final int MIN_VAL = Integer.MIN_VALUE; // minimum integer value

    private static final long MAX_TURN_CALC = 2000; // maximum time for computer to choose a move

    private static final int CORNER_BONUS = 10; // corner bonus
    private static final int ADJACENT_SIDE = 2; // adjacent side penalty
    private static final int ADJACENT_DIAGONAL = 3; // adjacent diagonal penalty
    private final int[][] cornersAdjacent; // kind of dictionary to support iterating over corners

    private Difficulty difficulty; // the difficulty of the game
    private boolean isVsComputer; // true if game vs the computer, or false otherwise
    private boolean isHumanTurn; // true if the current turn is human's turn, or false otherwise

    private Piece maximizer; // the maximizer for the minimax calculations
    private Piece minimizer; // the minimizer for the minimax calculations
    private String computerMove; // the move of the computer (determined by the minimax algorithm)


    // Constructor

    /**
     * The constructor of the class. receives as parameters the default piece of the first player,
     * the default piece of the second player, the default starting number of pieces for
     * each player, the default board size, the default starting player, the default difficulty,
     * the default isHumanTurn and the state of last game (or null if a new game is to be
     * initiated), and initiates the controller.
     *
     * @param firstPlayer the default first player of the game.
     * @param secondPlayer the default second player of the game.
     * @param startSize the default starting number of pieces for each player.
     * @param boardSize the default matrix board size.
     * @param startPlayer the default player to kick-off the game.
     * @param difficulty the default difficulty of the game.
     * @param isHumanTurn true if the default game starter is a human, or false otherwise (pc).
     * @param lastGameState state of the last game to load, or null if a new game is to be
     *                     initiated.
     */
    public GameController(Piece firstPlayer, Piece secondPlayer, int startSize, int boardSize,
                          Piece startPlayer, Difficulty difficulty, boolean isHumanTurn,
                          LiveGameDetails lastGameState) {

        this.model = new BoardModel(firstPlayer, secondPlayer);

        this.cornersAdjacent = new int[2][2];
        this.cornersAdjacent[0] = new int[]{0, 1}; // if corner index is 0, then adjacent is plus 1
        this.cornersAdjacent[1][1] = -1; // if corner index is board edge, then adjacent is minus 1

        this.lastStates = new Stack<>();
        this.undoneStates = new Stack<>();

        this.defaultGameState =
                new LiveGameDetails(firstPlayer, secondPlayer, boardSize, startSize, startPlayer,
                        difficulty, isHumanTurn || !difficulty.isVsComputer());

        // if possible then load game, otherwise initiate new game with default state
        loadGame(lastGameState != null ? lastGameState: this.defaultGameState);
    }


    // Methods

    /**
     * A method that starts a new game (after a game was ended): clears relevant stacks
     * and loads the default game (kick-off) state.
     */
    public void newGame() {
        this.lastStates.clear(); // new game -> undo isn't possible
        this.undoneStates.clear(); // new game -> redo isn't possible

        loadGame(this.defaultGameState); // setting the current state to default
    }


    /**
     * A method that receives an object that contains details about a live game and
     * loads it as the current game state.
     *
     * @param gameDetails an object that contains details about a live game.
     */
    public void loadGame(LiveGameDetails gameDetails) {
        this.currentGameState = gameDetails;

        this.firstPlayer = gameDetails.getFirstPlayer();
        this.secondPlayer = gameDetails.getSecondPlayer();

        this.difficulty = gameDetails.getDifficulty();
        this.isVsComputer = this.difficulty.isVsComputer();

        this.cornersAdjacent[1][0] = gameDetails.getBoardSize() - 1;

        loadGame();
    }

    /**
     * A method that undoes a move: pushes the current game state to the undone games stack, and
     * pops and loads the last game state from the last game states stack.
     */
    public void undo() {
        this.undoneStates.push(this.currentGameState);
        this.currentGameState = this.lastStates.pop();
        loadGame();
    }

    /**
     * A method that redoes a move: pushes the current game state to the stack of the last game
     * states, and pops and loads the undone game state from the proper stack.
     */
    public void redo() {
        this.lastStates.push(this.currentGameState);
        this.currentGameState = this.undoneStates.pop();
        loadGame();
    }

    /**
     * A method that loads a game: extracts the relevant attributes from the current game state
     * and calls the proper method in the model class with the current game state.
     */
    public void loadGame() {
        this.currentPlayer = this.currentGameState.getCurrentPlayer();
        this.nextPlayer = this.currentGameState.getNextPlayer();

        this.isHumanTurn = this.currentGameState.isHumanTurn();

        this.model.loadGame(this.currentGameState);
    }

    /**
     * A method that finishes the game: invokes the finish method of the model and updates the
     * current game state attribute to the final game state.
     */
    public void forceFinish() {
        this.model.finishGame(this.currentPlayer, this.nextPlayer);
        this.currentGameState = createCurrentState();
    }

    /**
     * A method that receives a List of squares that represents obtainable squares of a choice
     * of the next move and plays the next turn according to the choice.
     *
     * It clears the redo stack (because redo isn't available after playing a new move),
     * saves the current state so that undo is possible (if that's not computer's turn),
     * invokes the method in the model that plays the next turn with the proper players,
     * updates the turn status, and updates the current game state (after the turn was played).
     *
     * @param toChange a List of squares that represents obtainable squares of a choice of
     *                the next move (the selected move).
     */
    public void nextTurn(List<Cell> toChange) {
        // Undo/Redo related

        // clearing redo stack, new move was made
        this.undoneStates.clear();

        if (this.isHumanTurn)
            this.lastStates.push(this.currentGameState); // saving current state to enable undo


        // Turns Related

        if (this.model.nextTurn(this.currentPlayer, this.nextPlayer, toChange)) { // turn changed
            // Switch Player
            this.currentPlayer = this.nextPlayer;
            this.nextPlayer = this.nextPlayer == this.firstPlayer ?
                    this.secondPlayer : this.firstPlayer;

            if (this.isVsComputer)
                this.isHumanTurn = !this.isHumanTurn;
        }

        this.currentGameState = createCurrentState();
    }


    /**
     * a variable to take the time stamp before a minimax calculation begins so that it will
     * have a time limit for the calculations.
     */
    private long minimaxStart;

    /**
     * A method that receives as a parameter a dictionary (HashMap) of the available choices to
     * play and returns the computer's choice of the determined next move (based on the Minimax
     * algorithm).
     *
     * @param availableChoices a dictionary (HashMap) of the available choices to play -
     *                        the keys are the squares available on the board to move to
     *                        and the values are the squares that could be obtained if the next
     *                        move will be to the matching square key.
     * @return the computer's choice of the next turn's move (based on the Minimax algorithm).
     */
    public String computerTurn(HashMap<String, List<Cell>> availableChoices) {
        this.maximizer = this.currentPlayer;
        this.minimizer = this.nextPlayer;

        this.minimaxStart = System.currentTimeMillis(); // timestamp before calculations begin

        minimax(difficulty.depth, true, this.model.getBoardState(), availableChoices,
                MIN_VAL, MAX_VAL, false);

        return this.computerMove;
    }

    /**
     * A private method that receives as parameters an object that represents a board state,
     * a dictionary (HashMap) that represents the available choices and their possible profits
     * and a boolean that indicates whether the current player is the maximizer or the minimizer,
     * and returns a static evaluation of the board according to the received parameters.
     *
     * It adds a bonus to the final evaluation (int) if the state includes a corner for the
     * maximizer, (accordingly) a penalty if it includes a corner for the minimizer; a penalty
     * (lesser) if the state includes a square that is diagonally adjacent to a corner and is
     * owned by the maximizer (he should strive to avoid it), and (accordingly) a bonus if it's
     * owned by the minimizer; a (lesser) penalty if the square is either horizontally or
     * vertically adjacent to a corner and is owned by the maximizer, and (accordingly) a bonus
     * if it's owned by the minimizer; and finally a bonus for the amount of available moves of
     * the maximizer, or (accordingly) a penalty for the amount of available moves of the
     * minimizer (both should strive to have as many available moves as possible) - corners and
     * adjacent squares are last-stage factors so that another factor was necessary to be
     * considered beside them while in the early stages of the game.
     *
     * @param boardState an object that represents a board state.
     * @param availableChoices a dictionary (HashMap) of the available choices to play -
     *                         the keys are the squares available on the board to move to
     *                         and the values are the squares that could be obtained if the next
     *                         move will be to the matching square key.
     * @param isMaximizer true if that current player is the maximizer or false otherwise.
     * @return a static evaluation of the board according to the received parameters - the higher
     * it is, the better it is for the maximizer; and the lower it is, the better it is for
     * the minimizer (and worse for the maximizer).
     */
    private int eval(BoardState boardState, HashMap<String, List<Cell>> availableChoices,
                     boolean isMaximizer) {
        Piece[][] board = boardState.getBoardRaw(); // for easier and better readable code

        int eval = 0;

        for (int[] cornerRow: this.cornersAdjacent) {
            int row = cornerRow[0];

            for (int[] cornerCol : this.cornersAdjacent) {
                int col = cornerCol[0];


                Piece currentCorner = board[row][col]; // a corner
                if (currentCorner != Piece.EMPTY)
                    eval += CORNER_BONUS * (currentCorner == this.maximizer ? 1 : -1);
                else { // not empty

                    // horizontal adjacent
                    eval -= ADJACENT_SIDE * getSquareValue(board[row][col + cornerCol[1]]);

                    // vertical adjacent
                    eval -= ADJACENT_SIDE * getSquareValue(board[row + cornerRow[1]][col]);

                    // diagonal adjacent
                    eval -= ADJACENT_DIAGONAL
                            *  getSquareValue(board[row + cornerRow[1]][col + cornerCol[1]]);
                }
            }
        }

        // consider available moves
        eval += isMaximizer ? availableChoices.size(): availableChoices.size() * -1;

        return eval;
    }

    /**
     * A method that receives a square as a parameter and returns the appropriate multiplier
     * based on the square owner - if it's the maximizer, then 1 (as is); if it's the minimizer,
     * then by -1 (minus); and if neither own the square (empty), then 0.
     *
     * Note that the weights of the bonus/penalty aren't considered here, but rather are
     * multiplied by the returned value in the evaluation function.
     *
     * @param square a square on the board.
     * @return the appropriate multiplier according to the owner of the received square.
     */
    private int getSquareValue(Piece square) {
        return square == Piece.EMPTY ? 0: square == this.maximizer ? 1: -1;
    }

    /**
     * A method that receives an object that represents a board state and a boolean that indicates
     * whether the current player is the maximizer as parameters, and returns the available
     * choices of the next player to play.
     *
     * @param boardState an object that represents a board state.
     * @param isMaximizer true if the current player is the maximizer, or false otherwise.
     * @return the available choices of the next player to play.
     */
    private HashMap<String, List<Cell>> otherChoices(BoardState boardState, boolean isMaximizer) {
        Piece current, other;

        if (isMaximizer) {
            current = this.maximizer;
            other = this.minimizer;
        }
        else {
            current = this.minimizer;
            other = this.maximizer;
        }

        return boardState.validChoices(other, current);
    }

    /**
     * A recursive method that receives as parameters the depth of the minimax run (decremented
     * every deeper layer in the tree until 0); a boolean that indicates whether the current
     * minimax run is of the maximizer or the minimizer; an object that represents a board state;
     * a dictionary (HashMap) that holds the available choices of the current minimax player;
     * the alpha (current highest guaranteed value); the beta (current lowest guaranteed value);
     * and a boolean that indicates whether the method was called again after a player had no
     * available choices so that the other player be checked as well to determine if the
     * game is indeed finished (or a player had a second turn). It considers all the possible
     * game outcomes of all the possible moves in the given depth or until run-time limit is
     * reached, and based on the current player determines what move will he make (based on
     * the assumption that every player strives to play the best possible move available). Then,
     * it updates the computer move (static) to be the best possible choice of a move of the
     * maximizer.
     *
     * @param depth the depth of the minimax run (decremented every deeper layer in the tree
     *              until 0, and then stops the minimax from going deeper).
     * @param isMaximizer true if the current checked player is the maximizer, or false otherwise.
     * @param boardState an object that represents a board state.
     * @param availableChoices a dictionary (HashMap) that represents the available choices
     *                         of the current player (keys) and their outcomes (values).
     * @param alpha the current highest guaranteed value of the board (best for maximizer).
     * @param beta the current lowest guaranteed value of the board (best for minimizer).
     * @param isSecondCheck true if the method was called again after a player had no available
     *                      choices (to check if the game is really over).
     * @return the value selected by the current player (highest if maximizer or lowest if
     * minimizer).
     */
    private int minimax(int depth, boolean isMaximizer,
                        BoardState boardState, HashMap<String, List<Cell>> availableChoices,
                        int alpha, int beta, boolean isSecondCheck) {

        if (availableChoices.size() == 0) //
            if (isSecondCheck)
                return boardState.getPieceAmount(this.maximizer)
                        - boardState.getPieceAmount(this.minimizer);
            else
                return minimax(depth, !isMaximizer, boardState,
                        otherChoices(boardState, isMaximizer), alpha, beta, true);

        if (depth == 0 || MAX_TURN_CALC - (System.currentTimeMillis() - this.minimaxStart) <= 0)
            return eval(boardState, availableChoices, isMaximizer);

        int bestValue;

        if (isMaximizer) {
            bestValue = MIN_VAL;

            for (String choice : availableChoices.keySet()) {
                BoardState currentState = new BoardState(boardState);
                currentState.updateBoard(Objects.requireNonNull(availableChoices.get(choice)),
                        this.maximizer, this.minimizer);

                int currentValue = minimax(depth - 1, false,
                        currentState, currentState.validChoices(
                                this.minimizer, this.maximizer), alpha, beta, false);

                if (currentValue > bestValue) {
                    bestValue = currentValue;

                    if (depth == this.difficulty.depth)
                        this.computerMove = choice;
                }

                alpha = Math.max(alpha, bestValue);
                if (beta <= alpha)
                    break;
            }
        } else {
            bestValue = MAX_VAL;

            for (String choice : availableChoices.keySet()) {
                BoardState currentState = new BoardState(boardState);
                currentState.updateBoard(Objects.requireNonNull(availableChoices.get(choice)),
                        this.minimizer, this.maximizer);

                int currentValue = minimax(depth - 1, true,
                        currentState, currentState.validChoices(
                                this.maximizer, this.minimizer), alpha, beta, false);

                if (currentValue < bestValue) {
                    bestValue = currentValue;

                    if (depth == this.difficulty.depth) // not really needed - safe check
                        this.computerMove = choice;
                }

                beta = Math.min(beta, bestValue);
                if (beta <= alpha)
                    break;
            }
        }

        return bestValue;
    }


    /**
     * A private method that builds and returns an object that represents the current game state.
     *
     * @return an object that represents the current game state.
     */
    private LiveGameDetails createCurrentState() {
        return new LiveGameDetails(
                getTurnsCount(),
                this.firstPlayer,
                this.secondPlayer,
                getWinner(),
                getPieceAmount(this.firstPlayer),
                getPieceAmount(this.secondPlayer),
                getTurnAvg(this.firstPlayer),
                getTurnAvg(this.secondPlayer),
                this.difficulty,
                getTurnsCount(this.firstPlayer),
                isGameOver(),
                getBoardClone(),
                getEmptyToCheckClone(),
                this.currentPlayer,
                this.currentGameState.getBoardSize(), // current game board's size shouldn't
                // change throughout the game
                this.currentGameState.getStartSize(),
                isHumanTurn()
        );
    }

    /**
     * A getter for the isHumanTurn attribute.
     * @return true if that current turn is of the human player, or false otherwise (if it's
     * the computer's turn).
     */
    public boolean isHumanTurn() {
        return this.isHumanTurn;
    }

    /**
     * A method that returns if undoing a move is possible: if the stack of saved last moves
     * is not empty, that means that there is a possible state to return to and thus the method
     * will return true (if it's human turn - computer is not allowed to undo). If it is empty
     * (or it is the computer's turn), then the method will return false.
     *
     * @return true if undoing a move is possible, or false otherwise.
     */
    public boolean isUndoPossible() {
        return !this.lastStates.isEmpty() && isHumanTurn();
    }

    /**
     * A method that returns if redoing an undone move is possible: if the stack of undone moves
     * is not empty, that means that there is a possible state to return to and thus the method
     * will return true (if it's human turn - computer is not allowed to redo). If it is empty,
     * (or it is the computer's turn), then the method will return false.
     *
     * @return true if redoing a move is possible, or false otherwise.
     */
    public boolean isRedoPossible() {
        return !this.undoneStates.isEmpty() && isHumanTurn();
    }

    /**
     * A method that returns true if game state was changed since it was loaded or false otherwise.
     *
     * If undoing a move is possible that means the state was changed, and if it's empty that
     * means that no changes were made (if redo is possible that doesn't matter since it isn't
     * saved so the resulting saving state wont be changed as well).
     *
     * @return true if the game state was changed since it was loaded or false otherwise.
     */
    public boolean isStateChanged() {
        return !this.lastStates.isEmpty(); // if the undo stack isn't empty, game state was changed
    }

    /**
     * A method that receives a location on the board (row and column) and returns the piece of
     * the player at that location (calls the corresponding method in the model).
     *
     * @param row location row.
     * @param col location column.
     * @return the piece of the player at the received location.
     */
    public Piece getPieceByLocation(int row, int col) {
        return model.getPieceByLocation(row, col);
    }

    /**
     * A getter for the valid moves for the current turn (calls the corresponding method in
     * the model).
     *
     * @return the valid move choices for the current turn.
     */
    public HashMap<String, List<Cell>> getValidChoices() {
        return this.model.getValidChoices();
    }

    /**
     * A getter for the winner attribute (calls the corresponding method in the model).
     *
     * @return the winner of the game, or empty if there is no winner (including draw).
     */
    public Piece getWinner() {
        return this.model.getWinner();
    }

    /**
     * A getter for the turns count of a player: gets as parameter a player and returns the
     * number of turns he has played (calls the corresponding method in the model).
     *
     * @param player a player of which to return the turns count.
     * @return the number of turns the received player has played.
     */
    private int getTurnsCount(Piece player) {
        return this.model.getTurnsCount(player);
    }

    /**
     * A getter for the number of turns passed until current turn (calls the corresponding
     * method in the model).
     *
     * @return the number of turns passed until current turn.
     */
    public int getTurnsCount() {
        return this.model.getTurnsCount();
    }

    /**
     * A getter that returns true if the game is over, or false otherwise (calls the
     * corresponding method in the model).
     *
     * @return true if the game is over, or false otherwise.
     */
    public boolean isGameOver() {
        return this.model.isGameOver();
    }

    /**
     * A getter for the pieces counters of the players. gets as a parameter a player of which
     * the pieces counter will be returned (calls the corresponding method in the model).
     *
     * @param player a player of which the pieces counter will be returned.
     * @return the pieces counter of the received player.
     */
    public int getPieceAmount(Piece player) {
        return this.model.getPieceAmount(player);
    }

    /**
     * A getter for the average time that it takes a player to play his turn. Gets as a parameter
     * a player of which the average will be returned (calls the corresponding method in
     * the model).
     *
     * @param player the desired player of which the average will be returned.
     * @return the average time that it takes to the first player to play his turn.
     */
    public long getTurnAvg(Piece player) {
        return this.model.getTurnAvg(player);
    }

    /**
     * A clone getter for the game board (calls the corresponding method in the model).
     *
     * @return a clone of the board matrix that represents the game status (deep copy).
     */
    public Piece[][] getBoardClone() {
        return this.model.getBoardClone();
    }

    /**
     * A clone getter for the empty squares to check Collection- returns a clone of the
     * current empty to check Collection.
     *
     * @return a clone of the current empty to check Collection.
     */
    public Collection<String> getEmptyToCheckClone() {
        return this.model.getEmptyToCheckClone();
    }

    /**
     * A getter for the current player.
     *
     * @return the current player of the game.
     */
    public Piece getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * A getter for the first player of the current game.
     *
     * @return the first player of the current game
     */
    public Piece getFirstPlayer() {
        return this.firstPlayer;
    }

    /**
     * A getter for the second player of the current game.
     *
     * @return the second player of the current game.
     */
    public Piece getSecondPlayer() {
        return this.secondPlayer;
    }

    /**
     * A getter for the current game state attribute.
     *
     * @return an object that contains details about the current game state.
     */
    public LiveGameDetails getCurrentGameState() {
        return this.currentGameState;
    }

}
