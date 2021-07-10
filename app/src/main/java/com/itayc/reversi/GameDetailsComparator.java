package com.itayc.reversi;

import androidx.annotation.NonNull;

import java.util.Comparator;

/**
 * Comparator used to enable comparing/sorting finished game details based on various parameters.
 */
public class GameDetailsComparator implements Comparator<FinishedGameDetails> { // comparator
    // to enable sorting game details

    /**
     * An enum of various sorting options.
     */
    public enum CompareGDType {

        // Enum values

        ID, TIME, TURNS, FIRST_PLAYER, SECOND_PLAYER, WINNER, FIRST_PIECES, SECOND_PIECES,
        DIFFICULTY, DATE, FIRST_AVERAGE_TIME, SECOND_AVERAGE_TIME;


        // Enum methods

        /**
         * An override for the toString method: returns the enum converted from capitalized
         * snake case to standard english readable words.
         *
         * @return the enum converted from capitalized snake case to standard english
         * readable words.
         */
        @NonNull
        @Override
        public String toString() {
            return SettingsManager.capsSnakeToStandard(this.name());
        }
    }


    // Attributes

    private CompareGDType compareAttr; // sorting option to sort by
    private boolean isAscending; // true if sort is ascending, or false otherwise (descending)


    // Constructor

    /**
     * Constructor of the class: gets as parameters the compare attribute and a boolean that
     * indicates if the sorting is ascending or descending.
     *
     * @param compareAttr the sorting option to sort by.
     * @param isAscending true if the sorting is ascending, or false otherwise (descending).
     */
    public GameDetailsComparator(CompareGDType compareAttr, boolean isAscending) {
        this.compareAttr = compareAttr;
        this.isAscending = isAscending;
    }


    // Methods

    /**
     * A method that gets as parameters two objects that represent a finished game, and returns,
     * according to the comparing options, a positive number if the first finished game is
     * "greater" than the second, 0 if there are "even" and a negative number if it's "lesser" than
     * the second ("greater" means it will be before the second in the sorting order and vice
     * versa).
     *
     * @param gd1 the first object that represents a finished game.
     * @param gd2  the second object that represents a finished game.
     * @return a positive number if the first received object is before the second in the
     * sorting order, 0 if it doesn't matter, or negative if it's after the second.
     */
    @Override
    public int compare(FinishedGameDetails gd1, FinishedGameDetails gd2) {
        int res;

        switch (this.compareAttr) {
            case ID:
                res = (int) (gd1.getId() - gd2.getId());
                break;

            case TIME:
                res = gd1.getTimePlayedRaw() - gd2.getTimePlayedRaw();
                break;

            case TURNS:
                res = gd1.getTurnsPlayed() - gd2.getTurnsPlayed();
                break;

            case FIRST_PLAYER:
                res = gd1.getFirstPlayer().compareTo(gd2.getFirstPlayer());
                break;

            case SECOND_PLAYER:
                res = gd1.getSecondPlayer().compareTo(gd2.getSecondPlayer());
                break;

            case DIFFICULTY:
                res = gd1.getDifficulty().compareByLevel(gd2.getDifficulty());
                break;

            case WINNER:
                res = gd1.getGameWinner().compareTo(gd2.getGameWinner());
                break;

            case FIRST_PIECES:
                res =  gd1.getFirstPieces() - gd2.getFirstPieces();
                break;

            case SECOND_PIECES:
                res = gd1.getSecondPieces() - gd2.getSecondPieces();
                break;

            case DATE:
                res = gd1.getDate().compareTo(gd2.getDate());
                break;

            case FIRST_AVERAGE_TIME:
                res = (int) (gd1.getTurnAvgFirst() - gd2.getTurnAvgFirst());
                break;

            case SECOND_AVERAGE_TIME:
                res = (int) (gd1.getTurnAvgSecond() - gd2.getTurnAvgSecond());
                break;

            default:
                res = 0;
        }

        return this.isAscending ? res : res * -1;
    }

    /**
     * A setter for the comparing option attribute.
     *
     * @param compareAttr the comparing option to sort by.
     */
    public void setCompareAttr(CompareGDType compareAttr) {
        this.compareAttr = compareAttr;
    }

    /**
     * A setter for the order of the sorting.
     *
     * @param ascending true if the new order to set is ascending, or false otherwise (descending).
     */
    public void setAscending(boolean ascending) {
        this.isAscending = ascending;
    }
}
