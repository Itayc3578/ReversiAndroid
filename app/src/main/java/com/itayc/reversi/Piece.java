package com.itayc.reversi;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * An enum used to represent a game piece/disc and a player.
 */
public enum Piece {

    // Enum values

    YELLOW(Color.YELLOW), WHITE(Color.WHITE), GREEN(Color.GREEN), RED(Color.RED), BLUE(Color.BLUE),
    GRAY(Color.GRAY), BLACK(Color.BLACK), CYAN(Color.CYAN), MAGENTA(Color.MAGENTA),
    EMPTY(-99);


    // Attributes

    private static final List<Piece> validValues; // valid player pieces
    static {
        validValues = new ArrayList<>();

        for (Piece piece: values())
            if (piece.isValid())
                validValues.add(piece);

    }

    private final int color; // color id constant of the piece


    // Constructor

    /**
     * Constructor for the piece enum, receives a color id constant as a parameter.
     *
     * @param color the color id constant of the enum.
     */
    Piece (int color) {
        this.color = color;
    }


    // Methods

    /**
     * A getter method for the color (id) of the piece.
     *
     * @return the color id constant of the piece.
     */
    public int getColor() {
        return this.color;
    }

    /**
     * A method that returns true if the piece is valid (usable) or false otherwise.
     *
     * @return true if the piece is valid or false otherwise.
     */
    public boolean isValid() {
        return this != EMPTY;
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

    /**
     * A static method that returns all the valid Piece values.
     *
     * Note that the returned List is NOT a copy, therefore it is not to be modified.
     *
     * @return A List that contains all the valid Piece values.
     */
    public static List<Piece> getValidValues() {
        return validValues;
    }
}
