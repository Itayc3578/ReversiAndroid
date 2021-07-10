package com.itayc.reversi;

/**
 * A class used to represent a square (cell) in the game board matrix.
 */
public class Cell { // Square locations in the board.

    // Attributes

    // delimiter for String tag representation of the cell
    private static final String DELIMITER = SettingsManager.getAvailableDelimiter();

    private final int row; // row in the board
    private final int col; // column in the board


    // Constructor

    /**
     * Constructor of the class: gets a row and a column as parameters.
     *
     * @param row the row of the Cell in the matrix.
     * @param col the column of the Cell in the matrix.
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }


    // Methods

    /**
     * A getter for the row attribute.
     *
     * @return the row of the current Cell.
     */
    public int getRow() {
        return this.row;
    }

    /**
     * A getter for the column attribute.
     *
     * @return the column of the current Cell.
     */
    public int getCol() {
        return this.col;
    }

    /**
     * A static function that gets a row and a column as parameters,
     * and returns them in String tag representation.
     *
     * @param row a row.
     * @param col a column.
     * @return the received row and column as string tag.
     */
    public static String toSquareTag(int row, int col) {
        return row + DELIMITER + col;
    }

    /**
     * A static function that gets a row, a column and a remainder as parameters,
     * and returns them in String tag representation (with remainder).
     *
     * @param row a row.
     * @param col a column.
     * @param remainder a remainder to attach to the tag.
     * @return the received row, column and remainder in String tag representation.
     */
    public static String toSquareTag(int row, int col, String remainder) {
        return toSquareTag(row, col) + DELIMITER + remainder;
    }

    /**
     * A static function that gets a String tag representation of a cell as a parameter,
     * and returns the row of that cell.
     *
     * @param tag String tag representation of the cell.
     * @return the row of the received cell.
     */
    public static int getTagRow(String tag) {
        return Integer.parseInt(tag.split(DELIMITER)[0]);
    }

    /**
     * A static function that gets a String tag representation of a cell as a parameter,
     * and returns the column of that cell.
     *
     * @param tag String tag representation of the cell.
     * @return the column of the received cell.
     */
    public static int getTagCol(String tag) {
        return Integer.parseInt(tag.split(DELIMITER)[1]);
    }

    /**
     * A static function that gets a String tag representation of a cell that has a remainder
     * as a parameter, and returns the remainder of that tag.
     *
     * @param tag String tag representation of a cell with a remainder.
     * @return the remainder of the received tag.
     */
    public static String getRemainder(String tag) {
        return tag.split(DELIMITER)[2];
    }

}
