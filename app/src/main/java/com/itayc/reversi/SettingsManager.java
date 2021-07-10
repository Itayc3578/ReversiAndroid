package com.itayc.reversi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A helper class to make the interaction with the shared preferences more convenient.
 */
public class SettingsManager {

    // Attributes

    private static SettingsManager settingsInstance = null; // to enable singleton implementation

    private static final int SETTINGS_VERSION = 6; // The version of the settings (to avoid errors)
    private static final String SHARED_PREFERENCES_NAME = "data"; // the SP file to interact with

    // for storing collections
    private static final String DELIMITERS = ";-.?|/$`~"; // available delimiters for use
    private static int nextAvailableDelimiterIndex = 0; // index of next available delimiter
    private static final String DELIMITER = getAvailableDelimiter(); // Setting's
    // delimiters have to be consistent over re-launches of application.

    private final SharedPreferences sp; // Shared Preferences pointer
    private final SharedPreferences.Editor editor; // pointer to enable editing settings

    private final HashMap<Keys, Object> settings; // settings dictionary to improve performance


    /**
     * Keys of the settings.
     */
    public enum Keys {

        // Enum values
        
        // Settings related

        SETTINGS_VERSION(-1), // version of the settings (to avoid upgrading errors)


        // Game related

        VIBRATE(true), // true if vibrate is enabled, false otherwise
        BACKGROUND_MUSIC(true), // true if background music is enabled, false otherwise
        SOUND(true), // true if sound is enabled, false otherwise
        DIALOG_ENDGAME(true), // true if endgame dialog is enabled, false otherwise
        SHOW_HINTS(true), // true if showing number hints is enabled, false otherwise
        FIRST_PLAYER_STARTS(true), // true if first player plays first, false otherwise
        COUNT_TIME_UP(true), // true if timer is counting up, false otherwise (down)
        IS_HUMAN_STARTS(true), // true if human starts the game, false otherwise
        DIFFICULTY(GameController.Difficulty.EASY.name()), // difficulty of the game
        FIRST_PLAYER_PIECE(Piece.BLACK.name()), // color of first player's piece
        SECOND_PLAYER_PIECE(Piece.WHITE.name()), // color of second player's piece
        BOARD_SIZE(8), // game board matrix's side size
        START_SIZE(2), // starting number of pieces for each player
        DURATION(3600), // maximum time to count to (in seconds)


        // Records related

        RECORDS_SORT(GameDetailsComparator.CompareGDType.TURNS.name()), // sorting method for
        // records
        RECORDS_SORT_ASCENDING(true), // true if records displaying is ordered
        // ascending, false otherwise


        // Settings activity related

        EXPAND_SETTINGS(false), // true if settings are expanded when user enters
        // Settings activity, false otherwise


        // Last game state related

        ALWAYS_LOAD_SAVED_GAME(false), // true if saved gave will always be loaded,
        // or false if dialog will pop up before loading
        IS_ASK_BEFORE_SAVE(true), // true if dialog pops up  before saving game state,
        // false if auto-saving is activated.

        LAST_GAME_ID(-1L), // last game's id
        LAST_TURNS_PLAYED(GameController.INIT_TURNS), // last game's amount of played turns
        LAST_FIRST_PLAYER(Piece.EMPTY.name()), // last game's first player's piece
        LAST_SECOND_PLAYER(Piece.EMPTY.name()), // last game's second player's piece
        LAST_WINNER(GameController.INIT_WINNER.name()), // last game's winner
        LAST_FIRST_PIECES(GameController.INIT_PIECE_COUNT), // last game's first player's
        // amount of pieces
        LAST_SECOND_PIECES(GameController.INIT_PIECE_COUNT), // last game's second player's
        // amount of pieces
        LAST_FIRST_AVG(GameController.INIT_AVG), // last game's first player's average time
        // per turn
        LAST_SECOND_AVG(GameController.INIT_AVG), // last game's second player's average time
        // per turn
        LAST_DIFFICULTY(DIFFICULTY.defaultValue), // last game's difficulty
        LAST_FIRST_TURNS(GameController.INIT_TURNS), // last game's first player's turn count
        LAST_GAME_OVER(GameController.INIT_GAME_OVER), // last game's finished state
        LAST_BOARD(""), // last game's board matrix state
        LAST_EMPTY_TO_CHECK(null), // last game's empty squares to check
        LAST_CURRENT_PLAYER(Piece.EMPTY.name()), // last game's current player
        LAST_BOARD_SIZE(BOARD_SIZE.defaultValue), // last game's board size
        LAST_START_SIZE(START_SIZE.defaultValue), // last game's start size
        LAST_HUMAN_TURN(IS_HUMAN_STARTS.defaultValue), // last game's computer turn state
        LAST_TIME_PLAYED(0); // last game's time played

        // Enum attributes

        private final Object defaultValue; // default value for the setting


        // Enum Constructor

        /**
         * Constructor of the enum, gets a default value for it as a parameter.
         *
         * @param defaultValue default value for the key.
         */
        Keys(Object defaultValue) {
            this.defaultValue = defaultValue;
        }


        // Enum methods

        /**
         * An override for the toString method: returns the enum converted from capitalized
         * snake case to standard english readable words.
         *
         * @return the enum converted from capitalized snake case to standard english
         * readable words.
         */
        @Override
        public String toString() {
            return capsSnakeToStandard(name());
        }
    }


    // Enum related Methods

    /**
     * A static method that receives as a parameter a String that is capitalized snake case,
     * and returns it converted to standard english.
     *
     * @param capsCamel a String that is capitalized snake case
     * @return the received String converted to standard english.
     */
    public static String capsSnakeToStandard(String capsCamel) {
        StringBuilder readable = new StringBuilder();

        for (String word: capsCamel.toLowerCase().split("_"))
            readable
                    .append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");

        return readable.substring(0, readable.length() - 1);
    }


    // Constructor

    /**
     * Private constructor for the class to enable singleton implementation.
     *
     * @param context context to enable essential permissions.
     */
    @SuppressLint("CommitPrefEdits") // commit will be called while editing
    private SettingsManager(Context context) {
        sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sp.edit(); // to enable editing the settings file

        checkForUpgrade(); // if version has increased, drop the settings file

        // setting the attributes (synchronizing)
        settings = new HashMap<>();
        syncSettings();
    }


    // Methods

    /**
     * A getter for singleton instance of the class.
     * NOT multi-thread safe.
     *
     * @param context context to enable essential permissions.
     * @return the instance of the singleton.
     */
    public static SettingsManager getInstance(Context context) {
        if (settingsInstance == null)
            settingsInstance = new SettingsManager(context);
        return settingsInstance;
    }

    /**
     * A function that synchronizes the settings with the shared preferences file.
     */
    private void syncSettings() {

        // Can't automate all, but automate as much as possible
        for(Keys key: Keys.values())
            syncSingleSetting(key);
    }

    /**
     * A method that checks if the settings version was changed. If it was, then the shared
     * preferences file will be flushed, and the version will be updated. Otherwise there is
     * no need to change anything.
     *
     * I created that check because of several reasons. First, the delimiters might change
     * between two version of the game, and lead to errors reading settings with old delimiters.
     * Moreover, changes such as storing preferences with different key naming methods (for
     * example changing from camel case to snake case) might cause errors as well.
     *
     * In the future I might add to this method a way of also converting and updating the old
     * settings to newer versions.
     */
    private void checkForUpgrade() {
        Keys version = Keys.SETTINGS_VERSION;
        String versionName = version.name();

        if (sp.getInt(versionName, (int) version.defaultValue) != SETTINGS_VERSION)
            editor.clear().putInt(versionName, SETTINGS_VERSION).commit();
    }

    /**
     * A function that synchronizes a single setting with the shared preferences file.
     *
     * @param key the key of the setting to synchronize.
     */
    private void syncSingleSetting(Keys key) {
        Object defaultVal = key.defaultValue;
        String keyStr = key.name();

        if (defaultVal == null) // String Set
            settings.put(key, sp.getStringSet(keyStr, null));
        else if (defaultVal instanceof Boolean) // boolean
            settings.put(key, sp.getBoolean(keyStr, (Boolean) defaultVal));
        else if (defaultVal instanceof Integer) // int
            settings.put(key, sp.getInt(keyStr, (Integer) defaultVal));
        else if (defaultVal instanceof  Long) // long
            settings.put(key, sp.getLong(keyStr, (Long) defaultVal));
        else // String
        {
            String value = sp.getString(keyStr, (String) defaultVal);
            switch (key) {
                // Pieces
                case FIRST_PLAYER_PIECE:
                case SECOND_PLAYER_PIECE:
                case LAST_FIRST_PLAYER:
                case LAST_SECOND_PLAYER:
                case LAST_WINNER:
                case LAST_CURRENT_PLAYER:
                    settings.put(key, Piece.valueOf(value));
                    break;

                // CompareGTType (Compare GameDetails)
                case RECORDS_SORT:
                    settings.put(key, GameDetailsComparator.CompareGDType.valueOf(value));
                    break;

                // Piece matrix
                case LAST_BOARD:
                    settings.put(key, invertFormattedBoard(value));
                    break;

                // Difficulty
                case DIFFICULTY:
                case LAST_DIFFICULTY:
                    settings.put(key, GameController.Difficulty.valueOf(value));
                    break;

                // Pure String
                default:
                    settings.put(key, value);
            }
        }
    }

    /**
     * A method that receives a key and an int value and updates the setting accordingly.
     *
     * @param key a key.
     * @param value an int value.
     */
    public void putValue(Keys key, int value) {
        settings.put(key, value);
        editor.putInt(key.name(), value);
        editor.commit();
    }

    /**
     * A method that receives a key and a long value and updates the setting accordingly.
     *
     * @param key a key.
     * @param value a long value.
     */
    public void putValue(Keys key, long value) {
        settings.put(key, value);
        editor.putLong(key.name(), value);
        editor.commit();
    }

//    /**
//     * A method that receives a key and a String value and updates the setting accordingly.
//     *
//     * @param key a key.
//     * @param value a String value.
//     */
//    public void putValue(Keys key, String value) {
//        settings.put(key, value);
//        editor.putString(key.name(), value);
//        editor.commit();
//    }

    /**
     * A method that receives a key and a boolean value and updates the setting accordingly.
     *
     * @param key a key.
     * @param value a boolean value.
     */
    public void putValue(Keys key, boolean value) {
        settings.put(key, value);
        editor.putBoolean(key.name(), value);
        editor.commit();
    }

    /**
     * A method that receives a key and a Set of Strings value and updates the setting accordingly.
     *
     * @param key a key.
     * @param value a Set of Strings value.
     */
    public void putValue(Keys key, Set<String> value) {
        settings.put(key, value);
        editor.putStringSet(key.name(), value);
        editor.commit();
    }

    /**
     * A method that receives a key and a Piece value and updates the setting accordingly.
     *
     * @param key a key.
     * @param value a Piece value.
     */
    public void putValue(Keys key, Piece value) {
        settings.put(key, value);
        editor.putString(key.name(), value.name());
        editor.commit();
    }

    /**
     * A method that receives a key and a two-dimensional piece array.
     * value and updates the setting accordingly.
     *
     * @param key a key.
     * @param value a two-dimensional piece array value.
     */
    public void putValue(Keys key, Piece[][] value) {
        settings.put(key, value);
        editor.putString(key.name(), formatBoardToStr(value));
        editor.commit();
    }

    /**
     * A method that receives a key and a Difficulty value and updates the setting accordingly.
     * @param key a key.
     * @param value a Difficulty value.
     */
    public void putValue(Keys key, GameController.Difficulty value) {
        settings.put(key, value);
        editor.putString(key.name(), value.name());
        editor.commit();
    }

    /**
     * A method that receives a key and a CompareGDType value and updates the setting accordingly.
     *
     * @param key a key.
     * @param value a CompareGDType value.
     */
    public void putValue(Keys key, GameDetailsComparator.CompareGDType value) {
        settings.put(key, value);
        editor.putString(key.name(), value.name());
        editor.commit();
    }


    /**
     * A method that receives a key for an int value and returns that value.
     *
     * @param key a key for an int value.
     * @return the value associated with the key.
     */
    @SuppressWarnings("ConstantConditions") // asserting that the key is valid
    public int getIntValue(Keys key) {
        return (Integer) settings.get(key);
    }

    /**
     * A method that receives a key for a long value and returns that value.
     *
     * @param key a key for a long value.
     * @return the value associated with the key.
     */
    @SuppressWarnings("ConstantConditions") // asserting that the key is valid
    public long getLongValue(Keys key) {
        return (Long) settings.get(key);
    }

//    /**
//     * A method that receives a key for a String value and returns that value.
//     *
//     * @param key a key for a String value.
//     * @return the value associated with the key.
//     */
//    public String getStringValue(Keys key) {
//        return (String) settings.get(key);
//    }

    /**
     * A method that receives a key for a boolean value and returns that value.
     *
     * @param key a key for a boolean value.
     * @return the value associated with the key.
     */
    @SuppressWarnings("ConstantConditions") // asserting that the key is valid
    public boolean getBooleanValue(Keys key) {
        return (Boolean) settings.get(key);
    }

    /**
     * A method that receives a key for a String set value and returns that value.
     *
     * @param key a key for a String set value.
     * @return the value associated with the key.
     */
    @SuppressWarnings("unchecked") // asserting that the key is valid
    public Set<String> getStringSetValue(Keys key) {
        return (Set<String>) settings.get(key);
    }

    /**
     * A method that receives a key for a Piece value and returns that value.
     *
     * @param key a key for a Piece value.
     * @return the value associated with the key.
     */
    public Piece getPieceValue(Keys key) {
        return (Piece) settings.get(key);
    }

    /**
     * A method that receives a key for a two-dimensional piece array value and returns that value.
     *
     * @param key a key for a two-dimensional piece array value.
     * @return the value associated with the key.
     */
    public Piece[][] getPieceMatrixValue(Keys key) {
        return (Piece[][]) settings.get(key);
    }

    /**
     * A method that receives a key for a Difficulty value and returns that value.
     *
     * @param key a key for a Difficulty value.
     * @return the value associated with the key.
     */
    public GameController.Difficulty getDifficultyValue(Keys key) {
        return (GameController.Difficulty) settings.get(key);
    }

    /**
     * A method that receives a key for a CompareGDType value and returns that value.
     *
     * @param key a key for a CompareGDType value.
     * @return the value associated with the key.
     */
    public GameDetailsComparator.CompareGDType getCompareGDTypeValue(Keys key) {
        return (GameDetailsComparator.CompareGDType) settings.get(key);
    }


    /**
     * A method that receives details about an ongoing game and stores them to.
     * the shared preferences.
     *
     * @param details details about an ongoing game.
     */
    public void updateOngoingGameDetails(OngoingGameDetails details) {
        putValue(Keys.LAST_GAME_ID, details.getId());
        putValue(Keys.LAST_TURNS_PLAYED, details.getTurnsPlayed());
        putValue(Keys.LAST_FIRST_PLAYER, details.getFirstPlayer());
        putValue(Keys.LAST_SECOND_PLAYER, details.getSecondPlayer());
        putValue(Keys.LAST_WINNER, details.getGameWinner());
        putValue(Keys.LAST_FIRST_PIECES, details.getFirstPieces());
        putValue(Keys.LAST_SECOND_PIECES, details.getSecondPieces());
        putValue(Keys.LAST_FIRST_AVG, details.getTurnAvgFirst());
        putValue(Keys.LAST_SECOND_AVG, details.getTurnAvgSecond());
        putValue(Keys.LAST_DIFFICULTY, details.getDifficulty());
        putValue(Keys.LAST_FIRST_TURNS, details.getFirstTurnsPlayed());
        putValue(Keys.LAST_GAME_OVER, details.isGameOver());
        putValue(Keys.LAST_BOARD, details.getBoard());
        putValue(Keys.LAST_EMPTY_TO_CHECK, (Set<String>) details.getEmptyToCheck());
        putValue(Keys.LAST_CURRENT_PLAYER, details.getCurrentPlayer());
        putValue(Keys.LAST_BOARD_SIZE, details.getBoardSize());
        putValue(Keys.LAST_START_SIZE, details.getStartSize());
        putValue(Keys.LAST_HUMAN_TURN, details.isHumanTurn());
        putValue(Keys.LAST_TIME_PLAYED, details.getTimePlayed());
    }

    /**
     * A method that checks and returns if there is a saved last game available.
     * If The board state is not the default board state (null), that means there is a last game
     * available to load.
     *
     * @return true if there is a saved game available to load, or false otherwise.
     */
    public boolean isSavedGameAvailable() {
        return getPieceMatrixValue(Keys.LAST_BOARD) != null;
    }

    /**
     * A method that creates and returns an object that contains data about last saved game state.
     *
     * @return an object that contains data about last saved game state.
     */
    public OngoingGameDetails getLastGameState() {
        return new OngoingGameDetails(
                getIntValue(Keys.LAST_TURNS_PLAYED),
                getPieceValue(Keys.LAST_FIRST_PLAYER),
                getPieceValue(Keys.LAST_SECOND_PLAYER),
                getPieceValue(Keys.LAST_WINNER),
                getIntValue(Keys.LAST_FIRST_PIECES),
                getIntValue(Keys.LAST_SECOND_PIECES),
                getLongValue(Keys.LAST_FIRST_AVG),
                getLongValue(Keys.LAST_SECOND_AVG),
                getDifficultyValue(Keys.LAST_DIFFICULTY),
                getIntValue(Keys.LAST_FIRST_TURNS),
                getBooleanValue(Keys.LAST_GAME_OVER),
                getPieceMatrixValue(Keys.LAST_BOARD),
                getStringSetValue(Keys.LAST_EMPTY_TO_CHECK),
                getPieceValue(Keys.LAST_CURRENT_PLAYER),
                getIntValue(Keys.LAST_BOARD_SIZE),
                getIntValue(Keys.LAST_START_SIZE),
                getBooleanValue(Keys.LAST_HUMAN_TURN),
                getIntValue(Keys.LAST_TIME_PLAYED),
                getLongValue(Keys.LAST_GAME_ID)
        );
    }


    /**
     * A method that receives a key as a parameter and resets it's associated value to default.
     *
     * @param key a key to reset.
     */
    public void resetSingleSetting(Keys key) {
        settings.remove(key);
        editor.remove(key.name()).commit();
        syncSingleSetting(key); // restoring default
    }

    /**
     * A method that clears the saved settings so that everything is restored to default.
     */
    public void restoreDefaultSettings() {
        editor.clear().commit();
        syncSettings();
    }

    /**
     * A function that gets a matrix of pieces, and returns it formatted into a String.
     *
     * @param board a matrix of pieces that represents board state.
     * @return the receives board state formatted into a String.
     */
    private String formatBoardToStr(Piece[][] board) {
        StringBuilder res = new StringBuilder(); // resulting formatted board state

        for (int row = 0; row < board.length; row++)
            for (int col = 0; col < board[row].length; col++)
                res
                        .append(Cell.toSquareTag(row, col, board[row][col].name()))
                        .append(DELIMITER);

        return res.substring(0, res.length() - 1);
    }

    /**
     * A function that receives a formatted board String as a parameter and returns it inverted
     * back to the original format.
     *
     * @param board formatted board String.
     * @return the received board inverted to original format, or null if there isn't saved board.
     */
    private Piece[][] invertFormattedBoard(String board) {
        if (board.equals(Keys.LAST_BOARD.defaultValue))
            return null;

        // Declaring a pointer so we won't have to call split function multiple times
        String[] splitBoard = board.split(DELIMITER);

        // calculating board size so we won't have to rely on parameters
        // game board is a matrix, so the size of it is the square root of it
        int boardSize = (int) Math.sqrt(splitBoard.length);

        Piece[][] res = new Piece[boardSize][boardSize]; // the resulting board state

        for (String square: splitBoard)
            res[Cell.getTagRow(square)][Cell.getTagCol(square)] =
                    Piece.valueOf(Cell.getRemainder(square));

        return res;
    }

    /**
     * A static function that returns an available delimiter for classes to use.
     *
     * @return an available delimiter for classes to use.
     * @throws NoSuchElementException when out of free delimiters to grant.
     */
    public static String getAvailableDelimiter() throws NoSuchElementException {
        if (nextAvailableDelimiterIndex >= DELIMITERS.length())
            throw new NoSuchElementException("Out of delimiters");

        return String.valueOf(DELIMITERS.charAt(nextAvailableDelimiterIndex++));
    }

}
