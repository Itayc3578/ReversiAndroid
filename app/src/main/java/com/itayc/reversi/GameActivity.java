package com.itayc.reversi;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The activity in which the game is presented and played.
 */
public class GameActivity extends AppCompatActivity implements View.OnClickListener,
        View.OnTouchListener {

    // Attributes

    private static final int SUPER_MOVE = 10; // number of new pieces per turn that is considered
    // a super move

    private static final int ALPHA_MAX = 255; // alpha for setting view to fully visible
    private static final int ALPHA_INVISIBLE = 0; // alpha for making view invisible
    private static final int ALPHA_HINT = 100; // alpha for making view "hinted"
    private static final int ALPHA_ATTAINABLE = 50; // alpha for attainable squares (long click)

    private static final int START_TIME = 0; // the time to start counting from
    private static final long MIN_PC_TURN_MILLIS = 1000; // minimum delay while playing computer's
    // turn in milliseconds

    private LinearLayout boardContainer; // the container of the board: linear layout
    private int boardSize; // board matrix size (size size)
    private Button[][] board; // the graphic board of the game

    private ConstraintLayout gameUpperContainer; // upper container for displaying game details
    private ImageView ivFirstPlayer; // displays the disc and color of the first player
    private ImageView ivSecondPlayer; // displays the disc and color of the second player
    private TextView tvFirstPieces; // displays the current amount of the first player's pieces
    private TextView tvSecondPieces; // displays the current amount of the second player's pieces
    private TextView tvCurrentPlayer; // TextView that displays the current turn's player
    private TextView tvTurnCount; // how many turns have passed since the game was started
    private TextView tvTime; // displays the counter for passed time since game began

    private ImageButton btnUndo; // button to undo last move
    private ImageButton btnRedo; // button to redo undone move

    private SettingsManager settings; // settings pointer
    private GameDetailsDataBase database; // games' details database
    private MusicControl musicControl; // sounds/music controller

    private Vibrator vibrator; // a Vibrator object to enable vibrating through the game

    // Handler to interact with the posts of the runnable objects
    private final Handler handler = new Handler();

    private int timeCounter; // the counter of the passed time, in seconds
    private int endTime; // the time to stop the counter at (in seconds)
    private boolean isCountTime; // true if counting time is enabled, false otherwise
    private boolean isCountUp; // true if time counter is counting up, false otherwise (down)

    // Controller
    private GameController controller; // the game controller

    private HashMap<String, List<Cell>> validLocations; // valid locations for current turn
    private HashMap<Cell, Integer> prevAttainable; // previous attainable pieces for
    // current selection: keys are the locations on board, and values are previous alphas. Used to
    // change alphas back after attainable squares were displayed

    private String computerChoice; // the choice of the computer for the current turn

    private boolean isInitiating; // true if the game is in initiating stage, or false otherwise
    private boolean isFinished; // true if the game was finished, or false otherwise.

    private FinishedGameDetails finishedGameDetails; // object that contains details about
    // the finished game


    // Methods

    /**
     * The onCreate method (called when the activity is created).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // initiating the vibrator to enable vibrating
        this.vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        // getting the relevant singleton instances

        this.settings = SettingsManager.getInstance(this);
        this.database = GameDetailsDataBase.getInstance(this);
        this.musicControl = MusicControl.getInstance(this);

        // game stages

        this.isInitiating = true;
        this.isFinished = false;


        linkDisplay(); // linking the necessary Views

        this.prevAttainable = new HashMap<>(); // initiating the HashMap

        // Counter related

        this.endTime = this.settings.getIntValue(SettingsManager.Keys.DURATION);
        this.isCountUp = this.settings.getBooleanValue(SettingsManager.Keys.COUNT_TIME_UP);

        // game initiate related
        Intent referrer = getIntent();
        GameController.Difficulty difficulty = (GameController.Difficulty)
                referrer.getSerializableExtra(SelectDifficultyActivity.DIFFICULTY);

        if (difficulty == null) // quick game requested
            quickGame();
        else
            initGame(null, difficulty); // difficulty is stated
    }

    /**
     * A private method to start a quick game: that is, initiating a game according to default
     * settings if there is not a saved game to load. If there is a saved game to load, then a
     * dialog pops up to ask the user whether he wishes to load it (or it gets automatically
     * loaded if auto loading is enabled in the settings).
     */
    private void quickGame() {
        // if there is a saved state to load, then pop a dialog that asks the user if he wishes to
        // load it (or automatically load it if user declared so in the settings), otherwise
        // initiate a new game
        if (this.settings.isSavedGameAvailable())
            // if auto loading is enabled
            if (this.settings.getBooleanValue(SettingsManager.Keys.ALWAYS_LOAD_SAVED_GAME))
                initGame();
            else // need to pop up dialog to ask if player wishes to load the saved state
                loadGameDialog();
        else // there isn't a saved game state to load
            initGame(null,
                    this.settings.getDifficultyValue(SettingsManager.Keys.DIFFICULTY));
    }

    /**
     * A private method that links the necessary views to their counterparts in the activity
     */
    private void linkDisplay() {
        this.boardContainer = findViewById(R.id.boardContainer);
        this.gameUpperContainer = findViewById(R.id.gameUpperContainer);

        FrameLayout firstContainer = findViewById(R.id.firstPiecesContainer);
        FrameLayout secondContainer = findViewById(R.id.secondPiecesContainer);

        this.ivFirstPlayer = firstContainer.findViewById(R.id.ivPiece);
        this.tvFirstPieces = firstContainer.findViewById(R.id.tvPiece);

        this.ivSecondPlayer = secondContainer.findViewById(R.id.ivPiece);
        this.tvSecondPieces = secondContainer.findViewById(R.id.tvPiece);

        this.tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer);
        this.tvTurnCount = findViewById(R.id.tvTurnCount);
        this.tvTime = findViewById(R.id.tvTime);

        this.btnUndo = findViewById(R.id.btnUndo);
        this.btnRedo = findViewById(R.id.btnRedo);
        btnSetEnabledWithAlpha(this.btnUndo, false);
        btnSetEnabledWithAlpha(this.btnRedo, false);
    }

    /**
     * A private method that pops a dialog that asks the player whether he wishes to load a
     * saved game or to start a new one.
     */
    private void loadGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Load Game");
        builder.setMessage("Do you wish to load last saved game?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                initGame();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                initGame(null,
                        settings.getDifficultyValue(SettingsManager.Keys.DIFFICULTY));
                dialogInterface.dismiss();
            }
        });

        builder.setNeutralButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

        playSound(MusicControl.Sound.DIALOG_POPUP);
    }

    /**
     * A private method that loads a saved game.
     *
     * Note that this method should be called only if there is a saved game in the settings. If
     * there isn't, it finishes the activity and pops up an error Toast.
     */
    private void initGame() {
        OngoingGameDetails gameState = this.settings.getLastGameState();

        if (gameState != null)
            initGame(gameState, this.settings.getDifficultyValue(SettingsManager.Keys.DIFFICULTY));
        else { // shouldn't happen
            Toast.makeText(this,
                    "Some error occurred, please try again", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * A private method that receives as parameters an object that contains details about a
     * saved game and a default difficulty, and initiates the game accordingly (initiates a new
     * default game if the received object is null).
     *
     * @param gameState an object that contains details about a saved game.
     * @param defaultDifficulty default difficulty for new games.
     */
    private void initGame(OngoingGameDetails gameState,
                          GameController.Difficulty defaultDifficulty) {

        this.boardSize = this.settings.getIntValue(SettingsManager.Keys.BOARD_SIZE);

        Piece firstPlayer = this.settings.getPieceValue(SettingsManager.Keys.FIRST_PLAYER_PIECE);
        Piece secondPlayer = this.settings.getPieceValue(SettingsManager.Keys.SECOND_PLAYER_PIECE);

        // Initiate the controller with default values for new games (and current game to load)
        this.controller = new GameController(
                firstPlayer,
                secondPlayer,
                this.settings.getIntValue(SettingsManager.Keys.START_SIZE),
                this.boardSize,
                this.settings.getBooleanValue(SettingsManager.Keys.FIRST_PLAYER_STARTS) ?
                        firstPlayer: secondPlayer,
                defaultDifficulty,
                this.settings.getBooleanValue(SettingsManager.Keys.IS_HUMAN_STARTS),
                gameState
        );

        int resetTimeTo = START_TIME; // time to start counting at

        if (gameState != null) { // saved game is to be loaded
            Toast.makeText(this, "Loaded saved game", Toast.LENGTH_SHORT).show();
            resetTimeTo = gameState.getTimePlayed();
            this.boardSize = gameState.getBoardSize();
        }

        this.gameUpperContainer.setVisibility(View.VISIBLE);

        initBoard();
        newGame(resetTimeTo);
    }

    /**
     * A private method that initiates the graphic game board: creates a LinearLayout matrix
     * according to the board size.
     */
    @SuppressLint("ClickableViewAccessibility") // deliberately not consuming the click
    private void initBoard()
    {
        this.board = new Button[this.boardSize][this.boardSize];

        for (int row = 0; row < this.boardSize; row++)
        {
            LinearLayout rowContainer = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1);

            rowContainer.setLayoutParams(rowParams);
            rowContainer.setOrientation(LinearLayout.HORIZONTAL);
            rowContainer.setWeightSum(this.boardSize);

            for (int col = 0; col < this.boardSize; col++)
            {
                this.board[row][col] = new Button(this);
                Button currentBtn = this.board[row][col];

                FrameLayout currentCell = new FrameLayout(this);

                LinearLayout.LayoutParams cellParams =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1);
                currentCell.setLayoutParams(cellParams);

                currentBtn.setTag(Cell.toSquareTag(row, col));
                currentBtn.setSoundEffectsEnabled(false);

                currentBtn.setBackgroundResource(R.drawable.disc_img);
                currentBtn.setOnClickListener(this);
                currentBtn.setOnTouchListener(this);

                ImageView squareImg = new ImageView(this);
                squareImg.setImageResource(R.drawable.square_bg);

                currentCell.addView(squareImg);
                currentCell.addView(currentBtn);
                rowContainer.addView(currentCell);

                syncSquareColor(row, col);
                viewSetEnabled(currentBtn, false);

                currentBtn.post(new MarginRunnable(currentCell, currentBtn, 9));

                final int autoMinSize = 1;
                final int autoSizeStep = 1;
                final int autoMaxSize = 35;
                int units = TypedValue.COMPLEX_UNIT_DIP;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    currentBtn.setAutoSizeTextTypeUniformWithConfiguration(
                            autoMinSize, autoMaxSize, autoSizeStep,
                            units);
                else
                    TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(currentBtn,
                            autoMinSize + 20,
                            autoMaxSize + 20,
                            autoSizeStep + 5,
                            units);

                currentBtn.setTextColor(Color.BLACK);
            }
            this.boardContainer.addView(rowContainer);
        }
    }

    /**
     * A method to be called when player clicks the undo button. calls the undo method of the
     * game controller and synchronizes the graphics accordingly.
     *
     * @param view the view that was clicked that invoked the method.
     */
    public void undo(View view) {
        this.controller.undo();
        syncUndoRedo();
    }

    /**
     * A method to be called when player clicks the redo button. calls the redo method of the
     * game controller and synchronizes the graphics accordingly.
     *
     * @param view the view that was clicked that invoked the method.
     */
    public void redo(View view) {
        this.controller.redo();
        syncUndoRedo();
    }

    /**
     * A private method that is called after an undo or a redo was executed. It synchronizes
     * the graphics accordingly (and plays relevant sound).
     */
    private void syncUndoRedo() {
        playSound(MusicControl.Sound.PIECE_MOVED);
        syncBoardToModel();
        displayPlayerTurn();
    }

    /**
     * A method (that overrides super's onClick), that is called when a user has clicked on a
     * square that he wishes to place his piece on for the current turn's move.
     *
     * @param view the view that was clicked that invoked the method.
     */
    @Override
    public void onClick(View view) {
        // Disabling old valid choices (new turn -> new valid choices)
        disableOldValidChoices();

        nextTurn(view.getTag().toString());

        playComputerTurn(); // play computer's turn if possible
    }

    /**
     * Initiating the Runnable that holds the code that calls for the computer to calculate and
     * decide its next move. it also checks if the time taken for the computer to make its choice
     * was less than the standard computer turn time: if it was, then it plays the decided move
     * after the standard computer turn delay is over.
     *
     * The calculations are made in a background thread for better user experience.
     */
    private final Runnable makeComputerChoice = new Runnable() {
        @Override
        public void run() {
            long turnStartTime = System.currentTimeMillis();

            computerChoice = controller.computerTurn(validLocations);

            long diff = MIN_PC_TURN_MILLIS - System.currentTimeMillis() + turnStartTime;

            if (diff > 0) // took less than standard time for the computer to decide it's next move
                handler.postDelayed(playComputerChoice, diff);
            else
                handler.post(playComputerChoice);
        }
    };

    /**
     * Initiating the Runnable that holds the code that practically plays the computer's turn
     * after it has calculated the desired move for the current turn.
     */
    private final Runnable playComputerChoice = new Runnable() {
        @Override
        public void run() {
            // to prevent playing computer's turn if game ended while it was calculating
            if (!controller.isGameOver()) {
                nextTurn(computerChoice); // play the turn

                playComputerTurn(); // play again if it's still computer's turn
            }
        }
    };

    /**
     * A private method that plays a turn of the computer (if it's indeed a computer's turn).
     */
    private void playComputerTurn() {
        if (!this.controller.isHumanTurn() && !this.controller.isGameOver())
            this.handler.post(this.makeComputerChoice);
    }

    /**
     * A private method that disables old valid choices.
     */
    private void disableOldValidChoices() {
        if (this.validLocations != null) // if it's null it was never enabled...
            for (String currentSquare : this.validLocations.keySet()) {
                int row = Cell.getTagRow(currentSquare);
                int col = Cell.getTagCol(currentSquare);

                disableSquare(row, col);
                this.board[row][col].getBackground().setAlpha(ALPHA_INVISIBLE);
            }
    }

    /**
     * A private method that gets as a parameter a String tag that represents the desired square
     * to play the next turn to, and plays it.
     *
     * @param tagCurrentMove a String tag that represents the desired square on the board
     *                       to place the current turn's piece on.
     */
    private void nextTurn(String tagCurrentMove) {
        // Playing piece changed sound
        playSound(MusicControl.Sound.PIECE_MOVED);

        List<Cell> turnChange = this.validLocations.get(tagCurrentMove);

        this.controller.nextTurn(turnChange);

//        assert turnChange != null && turnChange.size() > 0; // shouldn't be null

        if (Objects.requireNonNull(turnChange).size() >= SUPER_MOVE) // super move detected
            superMove();

        for (Cell cell: turnChange)
            syncSquareColor(cell.getRow(), cell.getCol());

        displayPlayerTurn();
    }

    /**
     * A private method to be invoked when a "super move" was played. It cheers the player by a
     * toast, a vibrate, and a sound.
     */
    private void superMove() {
        Toast.makeText(this, "Woooah!", Toast.LENGTH_SHORT).show();

        if (this.settings.getBooleanValue(SettingsManager.Keys.VIBRATE))
            this.vibrator.vibrate(500);

        playSound(MusicControl.Sound.SUPER_MOVE);
    }

    /**
     * A private method that receives as parameters a row and a column that represent a location
     * of a square in the board and "disables" that square. That means setting them not enabled
     * (disabled) and clearing their text.
     *
     * @param row an index that represents a row in the board matrix.
     * @param col an index that represents a column in the board matrix.
     */
    private void disableSquare(int row, int col) {
        viewSetEnabled(this.board[row][col], false);
        this.board[row][col].setText("");
    }

    /**
     * A private method that retrieves the valid choices for the turn from the controller,
     * and displays them properly on the game screen.
     */
    private void showValidLocations() {
        int currentColor = this.controller.getCurrentPlayer().getColor();

        for (String currentSquare : this.validLocations.keySet()) {
            int row = Cell.getTagRow(currentSquare);
            int col = Cell.getTagCol(currentSquare);

            viewSetEnabled(this.board[row][col], true);

            // get the drawable
            GradientDrawable currentDrawable =
                    (GradientDrawable) this.board[row][col].getBackground();

            currentDrawable.setColor(currentColor);

            this.board[row][col].getBackground().setAlpha(ALPHA_HINT);
            if (settings.getBooleanValue(SettingsManager.Keys.SHOW_HINTS))
                this.board[row][col].setText(String.valueOf(Objects.requireNonNull(
                        this.validLocations.get(currentSquare)).size()));
        }
    }

    /**
     * A method that is invoked when a touch on an enabled square is detected. It indicates
     * the possible obtainable squares of the move to the touched square.
     *
     * @param view the view that was touched that invoked the method.
     * @param motionEvent the motion event that was triggered.
     * @return true if the method consumes other methods that are to be invoked,
     * or false otherwise (consumes means to prevent other methods that should be invoked
     * after the event that triggered that method from being invoked).
     */
    @SuppressLint("ClickableViewAccessibility") // deliberately not consuming the click
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // for more readable code
        int row, col;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: // touched
                // for each cell in the current selection
                for (Cell currentAttainable: Objects.requireNonNull(
                        this.validLocations.get(view.getTag().toString()))) {

                    row = currentAttainable.getRow();
                    col = currentAttainable.getCol();
                    Drawable currentBg = this.board[row][col].getBackground();

                            // saving the previous alpha in the respectable cell in the dictionary
                    this.prevAttainable.put(currentAttainable, currentBg.getAlpha());

                    // setting the alpha to the declared alpha for this event
                    currentBg.setAlpha(ALPHA_ATTAINABLE);
                }

                break;
            case MotionEvent.ACTION_UP: // no longer touched

                // restoring the original/previous alpha for each piece
                for (Cell current: this.prevAttainable.keySet()) {
                    row = current.getRow();
                    col = current.getCol();

                    this.board[row][col].getBackground().setAlpha(Objects.requireNonNull(
                            this.prevAttainable.get(current)));
                }

                // clear the dictionary for future uses
                this.prevAttainable.clear();
                break;
        }
        return false; // to enable OnClick to be executed as well
    }

    /**
     * A private method that gets a row and a column of a square in the board and synchronizes
     * it with the corresponding square in the board model (the game logic).
     *
     * @param row an index that represents a row in the board.
     * @param col an index that represents a column in the board.
     */
    private void syncSquareColor(int row, int col) {
        Button current = this.board[row][col]; // get a reference to the square

        Piece currentPiece = this.controller.getPieceByLocation(row, col);
        int currentColor = currentPiece.getColor();

        if (currentPiece.isValid()) { // not empty

            // get the drawable disc
            GradientDrawable currentDrawable = (GradientDrawable) current.getBackground();

            // change piece color
            currentDrawable.setColor(currentColor);

            currentDrawable.setAlpha(ALPHA_MAX); // make piece disc visible
        }
        else // Empty
            current.getBackground().setAlpha(ALPHA_INVISIBLE); // make piece disc invisible
    }

    /**
     * A private method that displays on the screen the details of the game. It also checks for
     * game's end and calls the relevant method to finish it.
     */
    private void displayPlayerTurn() {
        String display; // to avoid concentrating strings in setText

        display = "Turn: " + this.controller.getTurnsCount();
        this.tvTurnCount.setText(display);

        Piece firstPlayer = this.controller.getFirstPlayer();
        Piece secondPlayer = this.controller.getSecondPlayer();

        tvFirstPieces.setText(String.valueOf(this.controller.getPieceAmount(firstPlayer)));
        tvSecondPieces.setText(String.valueOf(this.controller.getPieceAmount(secondPlayer)));

        if (this.controller.isGameOver()) { // game is over
            if (!this.isFinished)
                finishGame();
        }
        else { // game isn't over
            this.validLocations = this.controller.getValidChoices();
            if (this.controller.isHumanTurn()) // no need to display current turn state for bot turn
                showValidLocations();

            display = "Current Player: " + this.controller.getCurrentPlayer();
            this.tvCurrentPlayer.setText(display);

            btnSetEnabledWithAlpha(this.btnUndo, this.controller.isUndoPossible());
            btnSetEnabledWithAlpha(this.btnRedo, this.controller.isRedoPossible());
        }
    }

    /**
     * A private method that gets as parameters an ImageButton and a boolean that indicates if the
     * button is to be enabled (true) or disabled (false). It enables or disables it according
     * to the receives boolean. That means setting the appropriate alpha and sets the relevant
     * attributes to either enabled or disabled.
     *
     * @param btn the ImageButton to enable or disable.
     * @param isEnable true if the received button is to be enabled, or false otherwise (disabled).
     */
    private void btnSetEnabledWithAlpha(ImageButton btn, boolean isEnable) {
        btn.setImageAlpha(isEnable ? 255 : 75);
        viewSetEnabled(btn, isEnable);
    }

    /**
     * A private method that gets as parameters a View and a boolean that indicates if the
     * View is to be enabled (true) or disabled (false). It sets the clickable and enabled
     * attributes of the View according to the received boolean.
     *
     * @param view a View to enable or disable.
     * @param isEnable true if the received View is to be enabled, or false otherwise (disabled).
     */
    private void viewSetEnabled(View view, boolean isEnable) {
        view.setClickable(isEnable); // not entirely necessary
        view.setEnabled(isEnable);
    }

    /**
     * A private method that finishes the game.
     */
    private void finishGame() {
        this.isFinished = true;
        playSound(MusicControl.Sound.GAME_OVER);

        pauseTimer();

        saveEndgameStats();
        displayEndgame();
        dialogStats();

        // Lock the squares, game over
        for (int i = 0; i < this.boardSize; i++)
            for (int j = 0; j < this.boardSize; j++)
                viewSetEnabled(this.board[i][j], false);

        btnSetEnabledWithAlpha(this.btnUndo, false);
        btnSetEnabledWithAlpha(this.btnRedo, false);
    }

    /**
     * A private method that saves the game stats after it ends. If necessary, also updates the
     * records database.
     */
    private void saveEndgameStats() {

        // if the game is not being finished in initiating stage
        if (!this.isInitiating) {
            updateFinishedAndDB();
        }
        else { // finished game is being finished in initiating stage (a game is being loaded)

            this.finishedGameDetails = this.database.fetchRecordById(
                    this.settings.getLongValue(SettingsManager.Keys.LAST_GAME_ID));

            if (this.finishedGameDetails == null) { // game wasn't saved yet
                updateFinishedAndDB();
                saveOngoingGameStats();
            }
        }
    }

    /**
     * A private method that updates the database as well as the relevant attribute about
     * the finished game.
     */
    private void updateFinishedAndDB() {
        this.finishedGameDetails =
                new FinishedGameDetails(this.controller.getCurrentGameState(), this.timeCounter);

        this.database.createRecord(this.finishedGameDetails);
    }

    /**
     * A private method that, after the game has ended, displays the final result: the winner or
     * the draw.
     */
    private void displayEndgame() {
        Piece winner = this.controller.getWinner();

        String display = "Game Over, " + (!winner.isValid() ?
                "It's a Draw!" : "Winner: " + winner);

        this.tvCurrentPlayer.setText(display);
    }

    /**
     * A private method that updates the last saved game state to the current game state.
     */
    private void saveOngoingGameStats() {

        OngoingGameDetails currentState =
                new OngoingGameDetails(this.controller.getCurrentGameState(), this.timeCounter);


        if (this.controller.isGameOver())
            currentState.setId(this.finishedGameDetails.getId());

        this.settings.updateOngoingGameDetails(currentState);
    }

    /**
     * A private method that, after the game is finished, pops a "card" that contains data about
     * the finished game.
     */
    private void dialogStats() {
        final Dialog statsDialog = new Dialog(this);
        statsDialog.setContentView(R.layout.extended_details_row);
        statsDialog.setCanceledOnTouchOutside(false);

        // Set the width to match parent
        Window dialogWindow = statsDialog.getWindow();
        dialogWindow.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        ImageButton closeBtn = statsDialog.findViewById(R.id.rBtnClose);
        closeBtn.setVisibility(View.VISIBLE);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statsDialog.cancel();

                if (settings.getBooleanValue(SettingsManager.Keys.DIALOG_ENDGAME))
                    displayDialogEndgame();
            }
        });

        View dialogView = dialogWindow.getDecorView();
        dialogView.setBackgroundColor(Color.LTGRAY);
        GameDetailsAdapter.updateExtendedRow(dialogView, this.finishedGameDetails);
        statsDialog.show();
    }

    /**
     * A private method that, after a game was finished, displays a dialog that offers the
     * player to share the finished game, to start a new game, or to cancel the dialog.
     */
    private void displayDialogEndgame() {
        final Dialog d = new Dialog(this);
        d.setContentView(R.layout.game_over_dialog);
        d.setCanceledOnTouchOutside(false);
        d.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        android.widget.Button btnClose = d.findViewById(R.id.dBtnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.cancel();
            }
        });

        android.widget.Button btnNewGame = d.findViewById(R.id.dBtnNewGame);
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newGame(view);
                d.cancel();
            }
        });

        ImageButton btnShare = d.findViewById(R.id.dBtnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share game stats");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "I have just finished a Reversi game with those stats: "
                                + finishedGameDetails);

                startActivity(Intent.createChooser(shareIntent, "How do you want to share?"));

            }
        });

        TextView tvDisplay = d.findViewById(R.id.tvDisplay);
        tvDisplay.setText(this.tvCurrentPlayer.getText().toString());

        d.show();
    }

    /**
     * A method that is invoked when players clicks the new game button.
     *
     * @param view the view that was clicked that invoked the method.
     */
    public void newGame(View view) {
        removeComputerCallbacks();

        newGame(START_TIME);
    }

    /**
     * A private method that receives as a parameter the time to start the timer at, and starts
     * a new game.
     *
     * @param setTimeTo the time to start the timer at.
     */
    private void newGame(int setTimeTo) {
        this.isFinished = false;
        playSound(MusicControl.Sound.GAME_START);

        // if it is not the first run
        if (!this.isInitiating) { // to prevent unnecessary code execution
            this.controller.newGame();

            int defaultBoardSize = this.settings.getIntValue(SettingsManager.Keys.BOARD_SIZE);
            if (this.boardSize != defaultBoardSize) { // new board size
                this.boardSize = defaultBoardSize;
                this.boardContainer.removeAllViews(); // clear board container
                initBoard();
            }
            else
                syncBoardToModel();
        }

        setTimer(setTimeTo);

        displayPlayerTurn();

        syncDisplayColors(this.controller.getFirstPlayer().getColor(),
                this.controller.getSecondPlayer().getColor());

        if (this.isInitiating)
            this.isInitiating = false;

        playComputerTurn(); // if it is computer's turn, it will play
    }

    /**
     * A private method that synchronizes the display game board (View) with the logic
     * board (Model). It also disables all squares.
     */
    private void syncBoardToModel() {
        for (int i = 0; i < this.boardSize; i++)
            for (int j = 0; j < this.boardSize; j++) {
                syncSquareColor(i, j);
                disableSquare(i, j);
            }
    }

    /**
     * A private method that gets as parameters the color ids of the first player and of the
     * second player, and sets the display accordingly.
     *
     * @param firstColor the color id of the first player.
     * @param secondColor the color id of the second player.
     */
    private void syncDisplayColors(int firstColor, int secondColor) {
        this.ivFirstPlayer.setColorFilter(firstColor);
        this.tvFirstPieces.setTextColor(GameDetailsAdapter.contrastColor(firstColor));

        this.ivSecondPlayer.setColorFilter(secondColor);
        this.tvSecondPieces.setTextColor(GameDetailsAdapter.contrastColor(secondColor));
    }

    /**
     * Initiating time Runnable attribute that hold the code that enables updating time.
     */
    private final Runnable timeRun = new Runnable() {
        @Override
        public void run() {
            displayTime();

            if (isCountTime) {
                timeCounter++;

                handler.postDelayed(this, 1000);
            }
        }
    };

    /**
     * A private method that displays the time on the screen. It also finishes the game if
     * the timer reached the ending time.
     */
    @SuppressLint("DefaultLocale") // presenting time (uniform) and not a date
    private void displayTime() {
        int displayTime = this.isCountUp ? this.timeCounter : this.endTime - this.timeCounter;

        // Finishing game
        if (this.timeCounter >= this.endTime) {
            this.tvTime.setTextColor(Color.RED);
            this.isCountTime = false;

            if (!this.isFinished)
                forceFinish();
        }

        this.tvTime.setText(String.format("Time: %02d:%02d", displayTime / 60, displayTime % 60));
    }

    /**
     * A private method that forces the game to finish.
     */
    private void forceFinish() {
        this.controller.forceFinish();
        finishGame();
        disableOldValidChoices();
    }

    /**
     * A private method that resumes the timer.
     */
    private void resumeTimer() {
        this.isCountTime = true;
        this.handler.post(this.timeRun);
    }

    /**
     * A private method that pauses the timer.
     */
    private void pauseTimer() {
        this.isCountTime = false;
    }

    /**
     * A private method that gets a time as a parameter and resets the timer, beginning at
     * that time.
     *
     * @param setTo a time to reset the timer to.
     */
    private void setTimer(int setTo) {
        this.tvTime.setTextColor(Color.BLACK);
        this.timeCounter = setTo;
        displayTime();

        if (!isCountTime)
            resumeTimer();
    }

    /**
     * A private method that gets as a parameter a sound and plays it if sound is enabled
     * in the settings.
     *
     * @param sound a sound to play.
     */
    private void playSound(MusicControl.Sound sound) {
        if (this.settings.getBooleanValue(SettingsManager.Keys.SOUND))
            this.musicControl.playSound(sound);
    }

    /**
     * A method that is invoked when the player has pressed the back button of his phone.
     * If undo is not possible, that means no changes were made, so it calls the super method.
     * If undo is possible, that means that the game state has changed, so if the user enabled
     * the setting to ask before saving the game, it pops a dialog that asks him whether he wishes
     * to save the game or not. If it is disabled (auto-saving is enabled), than the current
     * game state is saved and then the super method is invoked.
     */
    @Override
    public void onBackPressed() {
        // if game state has changed
        if (this.controller.isStateChanged())
            if (this.settings.getBooleanValue(SettingsManager.Keys.IS_ASK_BEFORE_SAVE))
                quitDialog();
            else {
                saveOngoingGameStats();
                super.onBackPressed();
            }
        else // game state wasn't changed, no point saving the game
            super.onBackPressed();
    }

    /**
     * A private method that pops a dialog before leaving the activity. It asks the user whether
     * he wishes to save the current game state, discard it, or to cancel and continue playing,
     * and acts based on the decision of the player.
     */
    private void quitDialog() {
        pauseTimer();
        playSound(MusicControl.Sound.DIALOG_POPUP);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                resumeTimer();
            }
        });

        builder.setTitle("Quit Game");
        builder.setMessage("Do you wish to save the game state?");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveOngoingGameStats();
                dialogInterface.dismiss();
                GameActivity.super.onBackPressed();
            }
        });

        builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                GameActivity.super.onBackPressed();
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * A method that is called when the activity is destroyed. It removes callbacks to the
     * time handler.
     */
    @Override
    protected void onDestroy() {
        if (this.handler != null) {
            this.handler.removeCallbacks(this.timeRun);
            removeComputerCallbacks();
        }
        super.onDestroy();
    }

    /**
     * A private method that removes callbacks to computer-turn related Runnable objects
     * from the handler.
     */
    private void removeComputerCallbacks() {
        this.handler.removeCallbacks(this.playComputerChoice);
        this.handler.removeCallbacks(this.makeComputerChoice);
    }
}