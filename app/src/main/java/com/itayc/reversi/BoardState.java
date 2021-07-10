package com.itayc.reversi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * That class is complementary to the BoardModel class.
 *
 * The BoardModel class was split for convenience with the Minimax Algorithm and for better
 * organized code (instead of having several static methods that get BoardState as parameter
 * and operate on it, I ultimately decided to move them here).
 */
public class BoardState {

    // Attributes

    private Piece[][] board; // The board of the game
    private final HashMap<Piece, Integer> pieceCount; // Piece counts for both players
    private final Collection<String> emptyToCheck; // empty squares to check for next
    // turn (for performance)


    // Constructors

    /**
     * Empty constructor of the class: initiates the relevant data structures.
     */
    public BoardState() {
        this.pieceCount = new HashMap<>();
        this.emptyToCheck = new HashSet<>();
    }

    /**
     * Copy constructor of the class: receives another BoardState and copies it's attributes.
     * @param boardState the BoardState object to copy it's attributes.
     */
    public BoardState(BoardState boardState) {
        copyBoard(boardState.board);
        this.pieceCount = new HashMap<>(boardState.pieceCount);
        this.emptyToCheck = cloneEmptyToCheck(boardState.emptyToCheck);
    }


    // Methods

    /**
     * A method that gets a List of Cells that represents squares in the board, the
     * current player's piece and the rival's piece, and updates the squares of the game board
     * according to the received list. Also updates the piece count of both players accordingly.
     *
     * @param squaresToUpdate a Cell List that represents the squares on the board to update.
     * @param currentPlayer the piece of the player that the squares should be updated to.
     * @param otherPlayer the piece of the rival of the current player.
     */
    public void updateBoard(List<Cell> squaresToUpdate, Piece currentPlayer, Piece otherPlayer) {
        for (Cell toUpdate: squaresToUpdate) {
            int row = toUpdate.getRow();
            int col = toUpdate.getCol();

            if (isSquareEmpty(row, col)) {
                addAllEmpties(surroundingEmptySquares(row, col));

                // no longer empty
                removeEmpty(Cell.toSquareTag(row, col));
            }

            setSquare(currentPlayer, row, col);
        }

        // Update pieces counts of the players

        addPieceAmount(currentPlayer, squaresToUpdate.size());

        subtractPieceAmount(otherPlayer, squaresToUpdate.size() - 1);
        // 1 for empty square - 1 of the new squares is empty and so isn't to be subtracted
    }

    /**
     * A method that receives as parameters a row and a column which represent
     * a square in the board, and returns a Collection that contains all the empty squares
     * that surround it (adjacent to it).
     *
     * The reason for that method is to improve performance so that the whole board won't be
     * checked when there isn't really need for it, but instead only the relevant squares will.
     *
     * @param row a row in the board.
     * @param col a column in the board.
     * @return a Collection that contains all the empty squares surrounding the received square.
     */
    public Collection<String> surroundingEmptySquares(int row, int col) {
        Collection<String> adjacentEmpty = new HashSet<>();
        int currentRow; // the current row index to check
        int currentCol; // the current column index to check

        for (int i=-1; i <= 1; i++)
            for (int j=-1; j <= 1; j++) {
                currentRow = row + i; // right/left/neither
                currentCol = col + j; // up/down/neither

                if (isInBoard(currentRow, currentCol) // in the board
                        && !(i == 0 && j == 0) // not same square
                        && isSquareEmpty(currentRow, currentCol)) // empty square

                    // add it to the set
                    adjacentEmpty.add(Cell.toSquareTag(currentRow, currentCol));
            }

        return adjacentEmpty;
    }

    /**
     * A method that gets as parameters the piece of the current player and of the rival of the
     * current player, and builds and returns a HashMap (dictionary) that contains all the possible
     * outcomes (new pieces) of available next moves of the player so that the key is the next
     * move's square and the value is a List of Cells that holds the new pieces that could be
     * obtained if the player chooses to play his next turn placing a piece on that square.
     *
     * The method that iterates over the relevant empty squares to check and for each of them,
     * gets the possible obtainable squares if the player plays his next turn placing a piece on
     * that empty square and adds them to a HashMap that contains the results if there are any
     * obtainable squares moving this way (meaning if the choice is valid).
     *
     * @param currentPlayer the piece of the current player.
     * @param nextPlayer the piece of the next player to play.
     * @return a HashMap (dictionary) that contains the possible outcomes (new pieces) of available
     * next moves of the player so that the key is the next move's square and the value is an array
     * of cells that holds the new pieces that could be obtained if the player chooses to play his
     * next turn placing a piece on that square, or if there aren't valid moves, finishes the game
     * and returns an empty HashMap.
     */
    public HashMap<String, List<Cell>> validChoices(Piece currentPlayer, Piece nextPlayer) {

        HashMap<String, List<Cell>> availableChoices = new HashMap<>();

        // for every relevant empty square to check
        for (String toCheck : this.emptyToCheck) {
            int row = Cell.getTagRow(toCheck); // convert tag of row to string
            int col = Cell.getTagCol(toCheck); // convert tag of column to string

            List<Cell> nextAvailableSquares = nextAvailableSquares(currentPlayer,
                    nextPlayer, row, col);

            if (nextAvailableSquares.size() != 0) // possible obtainable squares
                availableChoices.put(toCheck, nextAvailableSquares);
        }

        return availableChoices;
    }

    /**
     * A private method that receives as parameters the piece of the current player, the piece
     * of the rival of the current player and a location of an empty square on the board
     * represented by a row and a column, and returns a List of locations/cells in the board
     * that the current player is able to achieve by playing the next move placing his piece
     * on that square.
     *
     * The method iterates over all surrounding directions, and for each of them, if there are
     * any squares that the current player can achieve/conquer/overcome by playing his next move
     * to the received square, it adds them to a resulting ArrayList, and returns it as List.
     *
     * @param current current player's piece.
     * @param rival the piece of the current player's rival.
     * @param row a row in the board.
     * @param col a column in the board.
     * @return a List of Cells that contains all available squares by playing the next move to
     * the received square.
     */
    private List<Cell> nextAvailableSquares(Piece current, Piece rival, int row, int col) {
        ArrayList<Cell> toChange = new ArrayList<>(); // squares to change to the current's pieces
        ArrayList<Cell> currentList;

        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++) {
                currentList = goDirection(current, rival, row, col, i, j);
                if (currentList != null)
                    toChange.addAll(currentList);
            }

        if (toChange.size() != 0) // valid move
            toChange.add(new Cell(row, col)); // add the current empty cell to the ArrayList

        return toChange;
    }

    /**
     * A private method that receives as parameters the piece of the current player, the piece of
     * the rival of that player, a row and a column which represent a current location of an empty
     * square, and a direction to move to represented by horizontal and vertical indicators, and
     * returns an ArrayList of squares (Cells) that the current player can achieve by playing his
     * next turn moving to that square from the received direction, or null if that aren't such
     * squares.
     *
     * While in still in the board borders, the method moves from the empty square in the
     * received direction, and firstly checks if the next piece in that direction is indeed a
     * rival piece. If it isn't, that means that the current direction is of no benefit to the
     * current player, and returns null. If it is, it keeps moving in that direction, until
     * it gets to a piece of the current player, which means that there are squares to conquer,
     * and it returns them. If it gets to an empty square once again, or exceeds the board
     * borders without finding a "friendly" piece, that means that the player can't conquer/flip
     * that square, and returns null.
     *
     * @param currentPlayer the piece of the current player.
     * @param rival the piece of the rival player.
     * @param row the row of the empty square to check.
     * @param col the column of the empty square to check.
     * @param goRow the vertical direction to check: 1 means go down, -1 means go up, and 0
     *              means neither (stay).
     * @param goCol the horizontal direction to check: 1 means go right, -1 means go left, and 0
     *              means neither (stay).
     * @return an ArrayList of squares (Cells) that the current player can achieve by playing his
     * next turn moving to that square from the received direction, or null if that aren't
     * such squares.
     */
    private ArrayList<Cell> goDirection(Piece currentPlayer, Piece rival,
                                        int row, int col, int goRow, int goCol) {
        if (!isSquareEmpty(row, col))
            return null;

        int currentRow = row + goRow; // 1, -1, or 0 -> (respectively) down, up or neither
        int currentCol = col + goCol; // 1, -1, or 0 -> (respectively) right, left or neither

        boolean isPassRival = false; // did we pass through the rival?
        ArrayList<Cell> piecesToChange = new ArrayList<>(); // Pieces to change

        // While we are in the board
        while (isInBoard(currentRow, currentCol)) {
            Piece currentSquare = getSquare(currentRow, currentCol);

            // If the first square in the squares to check is indeed a rival's square
            if (!isPassRival && currentSquare == rival)
                isPassRival = true;

            // If we have already gone through rival's squares
            if (isPassRival) {

                // We got to current player's square, meaning occupation is completed
                if (currentSquare == currentPlayer)
                    return piecesToChange;

                // We got to an empty square, meaning occupation is failed
                if (currentSquare == Piece.EMPTY)
                    return null; // an empty square had cut the sequence

                // Rival square, adding to the list of occupied squares
                piecesToChange.add(new Cell(currentRow, currentCol));

                currentRow += goRow; // Forward the iteration
                currentCol += goCol; // Forward the iteration
            }
            else
                return null; // first move was invalid
        }

        return null; // exceeded the board
    }

    /**
     * A method that gets a row and a column as parameters, and returns true if the square
     * at these indexes is inside the board, or false otherwise.
     *
     * @param row a row (index).
     * @param col a column (index).
     * @return true if the square at the received row and column is inside the game board, or
     * false otherwise.
     */
    public boolean isInBoard(int row, int col) {
        return row < this.board.length && row >= 0 && col < this.board.length && col >=0;
    }

    /**
     * A method that receives a board size (side length of the matrix) as a parameter, and
     * initiates the board attribute to a new matrix of pieces according to that size.
     *
     * @param boardSize the length of the side of the board matrix.
     */
    public void initBoard(int boardSize) {
        this.board = new Piece[boardSize][boardSize];
    }

    /**
     * A method that gets a matrix of pieces that represent a game board as a parameter and
     * sets the board to be a clone of it.
     *
     * @param board the board to copy to the current board.
     */
    public void copyBoard(Piece[][] board) {
        this.board = cloneBoard(board);
    }

    /**
     * A method that gets as a parameter a Collection of Strings which represent the empty squares
     * to check, and adds all the empty squares to check in it to the current board state's empty
     * squares to check squares.
     *
     * @param emptiesToAdd a Collection of Strings that each represent an empty square to add to
     *                    the to the current empty squares to check.
     */
    public void addAllEmpties(Collection<String> emptiesToAdd) {
        this.emptyToCheck.addAll(emptiesToAdd);
    }

    /**
     * A method that clears(discards) all the current empty pieces to check.
     */
    public void clearEmptyToCheck() {
        this.emptyToCheck.clear();
    }

    /**
     * A method that gets as parameters a piece of a player and an amount to put, and sets that
     * player's piece count to the received amount to put.
     *
     * @param player a player of which to update the piece amount.
     * @param amountToPut the amount of pieces to change the receives player's pieces count to.
     */
    public void putPieceAmount(Piece player, int amountToPut) {
        this.pieceCount.put(player, amountToPut);
    }

    /**
     * A private method that gets as parameters a piece of a player and an amount to add, and
     * adds that amount to that player's pieces count.
     *
     * @param player a player of which to update the piece amount.
     * @param amountToAdd the amount of pieces to add to the receives player's pieces count.
     */
    @SuppressWarnings("ConstantConditions") // assuming the player is valid
    private void addPieceAmount(Piece player, int amountToAdd) {
        this.pieceCount.put(player, this.pieceCount.get(player) + amountToAdd);
    }

    /**
     * A private method that gets as parameters a piece of a player and an amount to subtract, and
     * subtracts that amount from that player's pieces count.
     *
     * @param player a player of which to update the piece amount.
     * @param amountToSubtract the amount of pieces to subtract from the receives player's
     *                        pieces count.
     */
    private void subtractPieceAmount(Piece player, int amountToSubtract) {
        addPieceAmount(player, amountToSubtract * -1);
    }

    /**
     * A method that clears the pieces counts of the players.
     */
    public void clearPieceAmounts() {
        this.pieceCount.clear();
    }

    /**
     * A method that gets as a parameter a player, and returns the piece amount of that player.
     *
     * @param player a player of which to get the piece amount.
     * @return the piece amount of the received player.
     */
    @SuppressWarnings("ConstantConditions") // assuming the player is valid
    public int getPieceAmount(Piece player) {
        return this.pieceCount.get(player);
    }

    /**
     * A clone getter for the board- returns a clone of the current board (deep copy).
     *
     * @return a clone of the current board.
     */
    public Piece[][] getBoardClone() {
        return cloneBoard(this.board);
    }

    /**
     * A "raw" getter for the board- returns a reference to the current board matrix.
     *
     * @return a reference to the current board matrix.
     */
    public Piece[][] getBoardRaw() {
        return this.board;
    }

    /**
     * A method that receives as parameters a piece, a row and a column, and sets the board cell
     * at those indexes to be the received piece.
     *
     * @param piece a piece to change the square to.
     * @param row the row of the desired square to change.
     * @param col the column of the desired square to change.
     */
    public void setSquare(Piece piece, int row, int col) {
        this.board[row][col] = piece;
    }

    /**
     * A getter for a board cell- receives as parameters a row and a column, and returns the
     * piece at those indexes (in the current board).
     *
     * @param row a row in the board.
     * @param col a column in the board.
     * @return the piece at the received indexes.
     */
    public Piece getSquare(int row, int col) {
        return this.board[row][col];
    }

    /**
     * A clone getter for the empty squares to check Collection- returns a clone of the
     * current empty to check Collection.
     *
     * @return a clone of the current empty to check Collection.
     */
    public Collection<String> getEmptyToCheckClone() {
        return cloneEmptyToCheck(this.emptyToCheck);
    }

    /**
     * A method that gets as parameters a row and a column, and returns true if the square at
     * those indexes is empty, or false otherwise.
     *
     * @param row a row of a square to check.
     * @param col a column of a square to check.
     * @return true if the square at the received indexes is empty, false otherwise.
     */
    public boolean isSquareEmpty(int row, int col) {
        return this.board[row][col] == Piece.EMPTY;
    }

    /**
     * A method that receives as a parameter a String that is a tag of a new empty square to add
     * to the current empty squares to check, and adds it.
     *
     * @param newEmptyToCheck a String that is a tag of a new empty square to add to the current
     *                        empty squares to check.
     */
    public void addEmpty(String newEmptyToCheck) {
        this.emptyToCheck.add(newEmptyToCheck);
    }

    /**
     * A private method that receives as a parameter a String that is a tag of a new empty
     * square to remove from the current empty squares to check, and removes it.
     *
     * @param emptyToRemove a String that is a tag of a new empty square to remove from the
     *                      current empty squares to check.
     */
    private void removeEmpty(String emptyToRemove) {
        this.emptyToCheck.remove(emptyToRemove);
    }

    /**
     * A static function that gets a game board (two-dimensional array of pieces), and returns a
     * clone of it (deep copy).
     *
     * @param board the two-dimensional array of pieces to clone.
     * @return a deep copy of the received array.
     */
    private static Piece[][] cloneBoard(Piece[][] board) {
        Piece[][] res = new Piece[board.length][board.length];

        for (int i = 0; i < res.length; i++)
            System.arraycopy(board[i], 0, res[i], 0, res[i].length);

        return res;
    }

    /**
     * A static function that gets an empty to check Collection, and returns a
     * clone of it (deep copy).
     *
     * @param emptyToCheck the empty to check Collection to clone.
     * @return a deep copy of the received Collection.
     */
    private static Collection<String> cloneEmptyToCheck(Collection<String> emptyToCheck) {
        return new HashSet<>(emptyToCheck);
    }
}
