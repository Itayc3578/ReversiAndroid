package com.itayc.reversi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The helper class to enable convenient interaction with the SQLite database.
 */
public class GameDetailsDataBase extends SQLiteOpenHelper {

    // Attributes

    // Singleton
    private static GameDetailsDataBase gameDetailsDataBase; // to enable singleton implementation

    // Database info

    private static final String DATABASE_NAME = "records.db"; // records database
    private static final String TABLE_RECORD = "tblrecords"; // records table
    private static final int DATABASE_VERSION = 7; // database version, for onUpgrade comparision

    // Columns

    private final String COLUMN_TURNS = "Turns"; // turns played
    private final String COLUMN_FIRST_PLAYER = "FirstPlayer"; // first player
    private final String COLUMN_SECOND_PLAYER = "SecondPLayer"; // second player
    private final String COLUMN_WINNER = "Winner"; // game winner
    private final String COLUMN_FIRST_PIECES = "FirstPieces"; // first player's final piece
    // amount
    private final String COLUMN_SECOND_PIECES = "SecondPieces"; // second player's final
    // piece amount
    private final String COLUMN_FIRST_AVG = "FirstAvg"; // first player's average time per turn
    private final String COLUMN_SECOND_AVG = "SecondAvg"; // second player's average time per turn
    private final String COLUMN_DIFFICULTY = "Difficulty"; // difficulty of the game
    private final String COLUMN_TIME = "Time"; // time played
    private final String COLUMN_DATE = "Date"; // date finished
    private final String COLUMN_ID = "Id"; // game id

    private SQLiteDatabase database; // database table


    // Constructor

    /**
     * A private constructor for the class: receives a Context as a parameter, and initiates the
     * database accordingly.
     *
     * @param context a Context for accessibility.
     */
    private GameDetailsDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // Methods

    /**
     * A getter for singleton instance of the class (receives a context as a parameter).
     *
     * @param context context.
     * @return the singleton instance of the class.
     */
    public static GameDetailsDataBase getInstance(Context context) {
        if (gameDetailsDataBase == null)
                gameDetailsDataBase = new GameDetailsDataBase(context);

        return gameDetailsDataBase;
    }

    /**
     * A method that is invoked when a database is being created: it gets a SQLiteDatabase
     * object as parameter, and executes the command to create the database.
     *
     * @param sqLiteDatabase SQLite platform (to execute the creating command on).
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // database string
        final String CREATE_TABLE_GAME_DETAILS = "CREATE TABLE IF NOT EXISTS "
                + TABLE_RECORD + "( "
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_TIME + " INTEGER, "
                + COLUMN_DATE + " INTEGER,"
                + COLUMN_TURNS + " INTEGER, "
                + COLUMN_FIRST_PLAYER + " TEXT, "
                + COLUMN_SECOND_PLAYER + " TEXT, "
                + COLUMN_WINNER + " TEXT, "
                + COLUMN_FIRST_PIECES + " INTEGER, "
                + COLUMN_SECOND_PIECES + " INTEGER, "
                + COLUMN_FIRST_AVG + " INTEGER, "
                + COLUMN_SECOND_AVG + " INTEGER, "
                + COLUMN_DIFFICULTY + " TEXT);";

        sqLiteDatabase.execSQL(CREATE_TABLE_GAME_DETAILS);
    }

    /**
     * A method that is invoked when a database is being upgraded - upgraded means that the version
     * received in the constructor is different from the version associated with the received
     * name. In that case, the database gets dropped and a new one is created.
     *
     * The method receives as parameters an SQLiteDatabase object as a SQLite platform to execute
     * operations on, the old version of the database and the new one.
     *
     * @param sqLiteDatabase an object that enables performing SQLite operations on.
     * @param oldV the old version of the database.
     * @param newV the new version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldV, int newV) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORD);
        onCreate(sqLiteDatabase);
    }

    /**
     * A method that receives as a parameter an object that represents a finished game and
     * creates a record for it in the database table.
     *
     * @param finishedGameDetails an object that represents a finished game.
     */
    public void createRecord(FinishedGameDetails finishedGameDetails) {
        this.database = getWritableDatabase(); // access to modify database

        ContentValues values = new ContentValues();
        values.put(COLUMN_TURNS, finishedGameDetails.getTurnsPlayed());
        values.put(COLUMN_FIRST_PLAYER, finishedGameDetails.getFirstPlayer().name());
        values.put(COLUMN_SECOND_PLAYER, finishedGameDetails.getSecondPlayer().name());
        values.put(COLUMN_FIRST_AVG, finishedGameDetails.getTurnAvgFirst());
        values.put(COLUMN_SECOND_AVG, finishedGameDetails.getTurnAvgSecond());
        values.put(COLUMN_DIFFICULTY, finishedGameDetails.getDifficulty().name());
        values.put(COLUMN_WINNER, finishedGameDetails.getGameWinner().name());
        values.put(COLUMN_FIRST_PIECES, finishedGameDetails.getFirstPieces());
        values.put(COLUMN_SECOND_PIECES, finishedGameDetails.getSecondPieces());
        values.put(COLUMN_TIME, finishedGameDetails.getTimePlayedRaw());
        values.put(COLUMN_DATE, finishedGameDetails.getDate().getTime());
        long id = this.database.insert(TABLE_RECORD, null, values);
        finishedGameDetails.setId(id);

        this.database.close(); // finished writing into the database
    }

    /**
     * A method that receives an id of a record (of a finished game) as a parameter, and deletes
     * it.
     *
     * @param id an id of the record to delete.
     */
    public void deleteRecordById(long id) {
        this.database = getWritableDatabase(); // access to write to the database

        this.database.delete(TABLE_RECORD, COLUMN_ID + "=?", new String[] {"" + id});

        this.database.close(); // finished modifying the database
    }

    /**
     * A method that retrieves all the records from the database and returns them as a List
     * of FinishedGameDetails.
     *
     * @return all the records in the table as a List of FinishedGameDetails (an object
     * that represents a finished game).
     */
    public List<FinishedGameDetails> fetchAllRecords() {
        this.database = getReadableDatabase(); // access to read from the database

        ArrayList<FinishedGameDetails> list = new ArrayList<>();
        String sortOrder = COLUMN_TURNS + " ASC"; // default: sort by turns played
        Cursor cursor = this.database.query(
                TABLE_RECORD,
                null, // retrieve all records
                null,
                null,
                null,
                null,
                sortOrder);

        while (cursor.moveToNext())
            list.add(makeRecord(cursor));

        cursor.close(); // finished using the cursor

        this.database.close(); // finished reading from the database

        return list;
    }

    /**
     * A method that receives an id as a parameter and returns the column with that id if such a
     * column exists, else it returns null.
     *
     * @param id the id of the requested column.
     * @return the column with the received id (or null there isn't such a column).
     */
    public FinishedGameDetails fetchRecordById(long id) {
        FinishedGameDetails res = null;

        this.database = getReadableDatabase();

        Cursor cursor = this.database.query(
                TABLE_RECORD,
                null,
                COLUMN_ID + "=?",
                new String[]{"" + id},
                null,
                null,
                null);

        if (cursor.moveToFirst())
            res = makeRecord(cursor);

        cursor.close();
        this.database.close();

        return res;
    }

    /**
     * A private method that receives a Cursor as a parameter and returns a FinishedGameDetails
     * object from the record that the Cursor points at.
     *
     * @param cursor a Cursor object that points at the desired record.
     * @return a FinishedGameDetails object made from the record pointed at by the received
     * Cursor.
     */
    private FinishedGameDetails makeRecord(Cursor cursor) {
        return new FinishedGameDetails(
                cursor.getInt(cursor.getColumnIndex(COLUMN_TURNS)),
                Piece.valueOf(cursor.getString(
                        cursor.getColumnIndex(COLUMN_FIRST_PLAYER))),
                Piece.valueOf(cursor.getString(
                        cursor.getColumnIndex(COLUMN_SECOND_PLAYER))),
                Piece.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_WINNER))),
                cursor.getInt(cursor.getColumnIndex(COLUMN_FIRST_PIECES)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_SECOND_PIECES)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_FIRST_AVG)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_SECOND_AVG)),
                GameController.Difficulty.valueOf(cursor.getString(
                        cursor.getColumnIndex(COLUMN_DIFFICULTY))),
                cursor.getInt(cursor.getColumnIndex(COLUMN_TIME)),
                cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE))));
    }
}
