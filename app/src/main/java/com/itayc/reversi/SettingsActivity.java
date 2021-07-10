package com.itayc.reversi;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * The activity in which the user interacts with and manages the settings.
 */
public class SettingsActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener,
        RadioGroup.OnCheckedChangeListener, View.OnLongClickListener {

    // Attributes

    private static final int MIN_DURATION = 5; // minimum duration for the game
    private static final int MIN_MINUTES = 0; // minimum minutes for timer pickers
    private static final int MAX_MINUTES = 60; // maximum minutes for timer pickers
    private static final int MIN_SECONDS = 0; // minimum seconds for timer pickers
    private static final int MAX_SECONDS = 59; // maximum seconds for timer pickers

    private static final int REQUEST_IMAGE_CAPTURE = 1; // request code for image capture
    private static final int REQUEST_PICK_PHOTO = 2; // request code for picking photo
    private static final int REQUEST_CAM_PERMISSION = 3; // request code for camera permission

    private static final String PICTURE_PROFILE_NAME = "ProfileImg.png"; // profile picture
    // file name

    private Intent bgMusicIntent; // Intent to enable interacting with background music service

    private ConstraintLayout rootSettings; // the root constraint of the settings activity
    private ConstraintSet settingsSet; // a constraint set to edit constraint attributes in code
    private ConstraintLayout choosePicContainer; // the container for the picture pick section
    private ImageButton btnToggleExpandedSettings; // the button to expand/collapse settings
    private boolean isExpandSettings; // true if settings are expanded, false otherwise

    private ToggleButton tbBgMusic; // the switch to enable/disable background music
    private ToggleButton tbSound; // the switch to enable/disable sound
    private ToggleButton tbVibrate; // the switch to enable/disable vibrate
    private SwitchCompat switchEndgameDialog; // the switch to enable/disable endgame dialog
    private SwitchCompat switchShowHints; // the switch to enable/disable showing in-game hints
    private SwitchCompat switchAskBeforeSave; // the switch to enable/disable asking before
    // game is saved
    private SwitchCompat switchAlwaysLoadSavedGame; // the switch to enable/disable auto-loading
    // saved games
    private ToggleButton tbStartPlayer; // the ToggleButton to determine which player starts
    private ToggleButton tbTimeCounting; // the ToggleButton to determine whether timer counting
    // will be up or down

    private TextView tvToggleExpandSettings; // TextView to display the action of clicking the
    // button to expand/collapse settings

    private RadioGroup rgSizes; // RadioGroup to choose which way to set the sizes of the game
    private TextView tvBoardSize; // TextView that indicates that the row is of board size
    private EditText etBoardSize; // EditText (numeric) of choosing board size manually
    private EditText etStartSize; // EditText (numeric) of choosing start size manually
    private Spinner spnBoardSize; // Spinner to choose board size from
    private Spinner spnStartSize; // Spinner to choose start size from
    private InitSizesAdapter sizesAdapter; // the adapter of the spinners of sizes

    private AlertDialog pickerDialog; // the dialog that pops up timer pickers
    private View timerPickerLayout; // the layout of the timer pickers
    private NumberPicker durationMinutesPicker; // the timer picker of the ending minutes
    private NumberPicker durationSecondsPicker; // the timer picker of the ending seconds

    private PopupWindow popupWindow; // popup window to display description of a setting
    private TextView tvPopup; // the TextView in the popup that displays the description

    private Spinner spnFirstColor; // the spinner that enables choosing the first player's color
    private Spinner spnSecondColor; // the spinner that enables choosing the second player's color
    private ColorAdapter colorAdapter; // the adapter for the player-color choosing spinners

    private Spinner spnDifficulty; // the spinner that enables selecting default difficulty
    private ArrayAdapter<GameController.Difficulty> difficultyAdapter; // the adapter for the
    // spinner of selecting a difficulty

    private ToggleButton tbHumanStarts; // ToggleButton to determine whether starting player is
    // the computer or a human
    private ImageButton btnResetHumanStarts; // the button that resets the humanStarts setting

    private SettingsManager settings; // the singleton instance of the settings
    private MusicControl musicControl; // the singleton instance of the music controller

    private Bitmap picImageBitmap; // the bitmap of the profile image
    private ImageView picImg; // the ImageView to display the profile image


    // Methods

    /**
     * The onCreate method (called when the activity is created).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // getting singleton instances
        settings =  SettingsManager.getInstance(this);
        musicControl = MusicControl.getInstance(this);

        bgMusicIntent = new Intent(this, BgMusicService.class);

        linkDisplay(); // linking the display Views to the corresponding attributes

        setListeners(); // settings relevant listeners

        syncSettings(); // synchronizing settings

        loadImage(); // loading profile image
    }

    /**
     * A private method that links the display graphics to the relevant View attributes.
     */
    private void linkDisplay() {
        // Linking

        rootSettings = findViewById(R.id.rootSettings);
        settingsSet = new ConstraintSet();
        settingsSet.clone(rootSettings);
        choosePicContainer = findViewById(R.id.takePicContainer);
        btnToggleExpandedSettings = findViewById(R.id.btnToggleExpandedSettings);

        tbBgMusic = findViewById(R.id.tbBackgroundMusic);
        tbSound = findViewById(R.id.tbSound);
        tbVibrate = findViewById(R.id.tbVibrate);
        switchEndgameDialog = findViewById(R.id.switchEndgameDialog);
        switchShowHints = findViewById(R.id.switchHint);
        switchAskBeforeSave = findViewById(R.id.switchAskBeforeSave);
        switchAlwaysLoadSavedGame = findViewById(R.id.switchAlwaysLoadSavedGame);
        tbStartPlayer = findViewById(R.id.tbStartPlayer);
        tbTimeCounting = findViewById(R.id.tbTimeCounting);

        linkPickers();

        tvToggleExpandSettings = findViewById(R.id.tvTogglePic);

        tvBoardSize = findViewById(R.id.tvBoardSize);
        rgSizes = findViewById(R.id.sizesRG);
        etBoardSize = findViewById(R.id.etBoardSize);
        etStartSize = findViewById(R.id.etStartSize);

        spnFirstColor = findViewById(R.id.spnFirstColor);
        spnSecondColor = findViewById(R.id.spnSecondColor);

        spnDifficulty = findViewById(R.id.spnDifficulty);

        tbHumanStarts = findViewById(R.id.tbHumanStarts);
        btnResetHumanStarts = findViewById(R.id.btnResetHumanStarts);

        picImg = findViewById(R.id.imgShowPic);

        spnBoardSize = findViewById(R.id.spnBoardSize);
        spnStartSize = findViewById(R.id.spnStartSize);

        linkAdapters();
    }

    /**
     * A private method that links the NumberPickers and sets their minimum/maximum values.
     */
    private void linkPickers() {
        timerPickerLayout = getLayoutInflater().inflate(R.layout.timer_picker, null,
                false);

        durationMinutesPicker = timerPickerLayout.findViewById(R.id.durationMinutesPicker);
        durationMinutesPicker = timerPickerLayout.findViewById(R.id.durationMinutesPicker);
        durationSecondsPicker = timerPickerLayout.findViewById(R.id.durationSecondsPicker);

        durationMinutesPicker.setMaxValue(MAX_MINUTES);
        durationMinutesPicker.setMinValue(MIN_MINUTES);
        durationSecondsPicker.setMaxValue(MAX_SECONDS);
        durationSecondsPicker.setMinValue(MIN_SECONDS);
    }

    /**
     * A private method that links the relevant adapters to the relevant lists and Views.
     */
    private void linkAdapters() {
        // Sizes pickers

        sizesAdapter = new InitSizesAdapter(this);

        spnBoardSize.setAdapter(sizesAdapter);
        spnStartSize.setAdapter(sizesAdapter);

        // Color/player pickers

        colorAdapter = new ColorAdapter(this);

        spnFirstColor.setAdapter(colorAdapter);
        spnSecondColor.setAdapter(colorAdapter);

        difficultyAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, GameController.Difficulty.values());

        spnDifficulty.setAdapter(difficultyAdapter);
    }

    /**
     * A private method that sets the relevant listeners.
     */
    private void setListeners() {
        // Listeners

        tbBgMusic.setOnCheckedChangeListener(this);
        tbSound.setOnCheckedChangeListener(this);
        tbVibrate.setOnCheckedChangeListener(this);
        switchEndgameDialog.setOnCheckedChangeListener(this);
        switchShowHints.setOnCheckedChangeListener(this);
        switchAskBeforeSave.setOnCheckedChangeListener(this);
        switchAlwaysLoadSavedGame.setOnCheckedChangeListener(this);
        tbStartPlayer.setOnCheckedChangeListener(this);
        tbTimeCounting.setOnCheckedChangeListener(this);

        setTimePickerDialog();

        spnFirstColor.setOnItemSelectedListener(this);
        spnSecondColor.setOnItemSelectedListener(this);

        rgSizes.setOnCheckedChangeListener(this);
        spnBoardSize.setOnItemSelectedListener(this);
        spnStartSize.setOnItemSelectedListener(this);

        setStartSizeListeners();
        setBoardSizeListeners();

        spnDifficulty.setOnItemSelectedListener(this);

        tbHumanStarts.setOnCheckedChangeListener(this);

        initPopups();
    }

    /**
     * A private method that initiates the timer pickers dialog.
     */
    private void setTimePickerDialog() {
        AlertDialog.Builder pickerDialBuilder = new AlertDialog.Builder(this);
        pickerDialBuilder.setCancelable(false);
        pickerDialBuilder.setView(timerPickerLayout);

        pickerDialBuilder.setNeutralButton("Cancel", null);

        pickerDialBuilder.setPositiveButton("Submit", null);

        pickerDialog = pickerDialBuilder.create();

        pickerDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnSubmit = pickerDialog.getButton(DialogInterface.BUTTON_POSITIVE);

                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (updateTimerPicker()) {
                            pickerDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Success",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            playSound(MusicControl.Sound.INVALID_SELECTION);
                            Toast.makeText(SettingsActivity.this, "Invalid" +
                                    " selection, minimum match duration is " + MIN_DURATION / 60 +
                                    " minutes and " + MIN_DURATION % 60 +" seconds.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    /**
     * A private method that sets relevant listeners for when the start size EditText is
     * submitted.
     */
    private void setStartSizeListeners() {
        // Another option: use TextWatcher -> afterTextChanged()

        etStartSize.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    checkSizeValidity(etStartSize, spnBoardSize);

                    hideKeyboard();

                    return true; // no need for other listeners
                }
                return false;
            }
        });
        etStartSize.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) // changed focus (changed field)
                    checkSizeValidity(etStartSize, spnBoardSize);
            }
        });
    }

    /**
     * A private method that sets relevant listeners for when the board size EditText is
     * submitted.
     */
    private void setBoardSizeListeners() {
        // Another option: use TextWatcher -> afterTextChanged()

        etBoardSize.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    checkSizeValidity(etBoardSize, spnStartSize);
                    hideKeyboard();

                    return true; // no need for other listeners
                }
                return false;
            }
        });
        etBoardSize.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) // changed focus (changed field)
                    checkSizeValidity(etBoardSize, spnStartSize);
            }
        });
    }

    /**
     * A private method that initiates the popup descriptions of the relevant Views.
     */
    private void initPopups() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup popupView = (ViewGroup) inflater.inflate(R.layout.boardsize_pop_up, null);
        tvPopup = popupView.findViewById(R.id.tvPopup);

        popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT, true);

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                v.performClick();
                return true; // consumed
            }
        });


        switchEndgameDialog.setOnLongClickListener(this);
        switchShowHints.setOnLongClickListener(this);
        tvBoardSize.setOnLongClickListener(this);
    }

    /**
     * A private method that synchronizes the settings with the display.
     */
    private void syncSettings() {
        tbBgMusic.setChecked(settings.getBooleanValue(SettingsManager.Keys.BACKGROUND_MUSIC));
        tbSound.setChecked(settings.getBooleanValue(SettingsManager.Keys.SOUND));
        tbVibrate.setChecked(settings.getBooleanValue(SettingsManager.Keys.VIBRATE));
        switchEndgameDialog.setChecked(settings.getBooleanValue(
                SettingsManager.Keys.DIALOG_ENDGAME));
        switchShowHints.setChecked(settings.getBooleanValue(SettingsManager.Keys.SHOW_HINTS));
        switchAskBeforeSave.setChecked(settings.getBooleanValue(
                SettingsManager.Keys.IS_ASK_BEFORE_SAVE));
        switchAlwaysLoadSavedGame.setChecked(settings.getBooleanValue(
                SettingsManager.Keys.ALWAYS_LOAD_SAVED_GAME));
        tbStartPlayer.setChecked(settings.getBooleanValue(
                SettingsManager.Keys.FIRST_PLAYER_STARTS));
        tbTimeCounting.setChecked(settings.getBooleanValue(SettingsManager.Keys.COUNT_TIME_UP));

        spnFirstColor.setSelection(colorAdapter.getPosition(
                settings.getPieceValue(SettingsManager.Keys.FIRST_PLAYER_PIECE)));
        spnSecondColor.setSelection(colorAdapter.getPosition(
                settings.getPieceValue(SettingsManager.Keys.SECOND_PLAYER_PIECE)));

        spnDifficulty.setSelection(difficultyAdapter.getPosition(
                settings.getDifficultyValue(SettingsManager.Keys.DIFFICULTY)));

        isExpandSettings = settings.getBooleanValue(SettingsManager.Keys.EXPAND_SETTINGS);
        syncExpandedSettings();
        syncSizes();

        syncHumanStarts();
    }

    /**
     * A method that synchronizes the graphics with the current expanding preference -
     * expand or collapse.
     */
    private void syncExpandedSettings() {
        hideKeyboard();

        choosePicContainer.setVisibility(getVisibilityConstant(!isExpandSettings));

        if (isExpandSettings) {
            tvToggleExpandSettings.setText(R.string.collapse_settings);
            btnToggleExpandedSettings.setImageResource(R.drawable.collapse_icon);

            settingsSet.clear(R.id.btnToggleExpandedSettings, ConstraintSet.TOP);
            settingsSet.connect(R.id.btnToggleExpandedSettings, ConstraintSet.BOTTOM,
                    R.id.rootSettings,
                    ConstraintSet.BOTTOM);
        }
        else {
            tvToggleExpandSettings.setText(R.string.expand_settings);
            btnToggleExpandedSettings.setImageResource(R.drawable.expand_icon);

            settingsSet.clear(R.id.btnToggleExpandedSettings, ConstraintSet.BOTTOM);
            settingsSet.connect(R.id.btnToggleExpandedSettings, ConstraintSet.TOP,
                    R.id.glSettingsBottom,
                    ConstraintSet.TOP);
        }
        settingsSet.applyTo(rootSettings);

        toggleEnabledSizeInp();
        if (spnStartSize.getVisibility() == View.VISIBLE)
            spnStartSize.setSelection(sizesAdapter.getPosition(
                    settings.getIntValue(SettingsManager.Keys.START_SIZE)));
        else
            spnBoardSize.setSelection(sizesAdapter.getPosition(
                    settings.getIntValue(SettingsManager.Keys.BOARD_SIZE)
            ));
    }

    /**
     * A private method that synchronizes the size picking Spinners and EditTexts according
     * to the settings.
     */
    private void syncSizes() {
        if (spnStartSize.getVisibility() == View.VISIBLE) {

            int currentValue = settings.getIntValue(SettingsManager.Keys.BOARD_SIZE);
            etBoardSize.setText(String.valueOf(currentValue));

            syncSize(etBoardSize, BoardModel.getMatchingStartSizes(currentValue), spnStartSize);

            spnStartSize.setSelection(sizesAdapter.getPosition(
                    settings.getIntValue(SettingsManager.Keys.START_SIZE)));
        }
        else {
            int currentValue = settings.getIntValue(SettingsManager.Keys.START_SIZE);
            etStartSize.setText(String.valueOf(currentValue));

            syncSize(etStartSize, BoardModel.getMatchingBoardSizes(currentValue), spnBoardSize);

            spnBoardSize.setSelection(sizesAdapter.getPosition(
                    settings.getIntValue(SettingsManager.Keys.BOARD_SIZE)));
        }
    }

    /**
     * A method that gets as parameters the current size to synchronize's EditText, its settings
     * key, the other size's Spinner, the list of the matching sizes to the current size and
     * the input to update, and synchronizes the settings and the graphics accordingly.
     *
     * @param currentEt     the current size to synchronize's EditText.
     * @param currentKey    the current size to synchronize's settings key.
     * @param otherSpn      the other size's spinner.
     * @param matchingSizes the matching sizes list for the current size.
     * @param input         the selected size.
     */
    private void syncSize(EditText currentEt, SettingsManager.Keys currentKey, Spinner otherSpn,
                          List<Integer> matchingSizes, int input) {

        settings.putValue(currentKey, input);
        syncSize(currentEt, matchingSizes, otherSpn);
    }

    /**
     * A method that gets as parameters the current size to synchronize's EditText, the list
     * of the matching sizes to the current size and the other size's Spinner,
     * and synchronizes the graphics accordingly.
     *
     * @param currentEt     the current size to synchronize's EditText.
     * @param matchingSizes the matching sizes list for the current size.
     * @param otherSpn      the other size's spinner.
     */
    private void syncSize(EditText currentEt, List<Integer> matchingSizes, Spinner otherSpn) {
        currentEt.setError(null);
        sizesAdapter.updateList(matchingSizes); // notifies internally
        otherSpn.setEnabled(sizesAdapter.getCount() > 1);
    }

    /**
     * A private method that synchronizes the isHumanStarts setting with the preferences.
     */
    private void syncHumanStarts() {
        if (((GameController.Difficulty) spnDifficulty.getSelectedItem()).isVsComputer())
            tbHumanStarts.setChecked(settings.getBooleanValue(
                    SettingsManager.Keys.IS_HUMAN_STARTS));
        else
            tbHumanStarts.setChecked(true);
    }

    /**
     * A private method that synchronizes the match duration picker with the settings.
     */
    private void syncDurationPicker() {
        int duration = settings.getIntValue(SettingsManager.Keys.DURATION);

        durationMinutesPicker.setValue(duration / 60);
        durationSecondsPicker.setValue(duration % 60);
    }

    /**
     * A listener for checked changed event: the usage is for detecting when a Switch's or a
     * ToggleButton's checked state is changed, so that the settings will be synchronized
     * accordingly.
     *
     * @param compoundButton the button that was checked/unchecked.
     * @param b              the new state of the button: true means it's now on, false means it's
     *                       off.
     */
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId())
        {
            case R.id.tbBackgroundMusic:
                settings.putValue(SettingsManager.Keys.BACKGROUND_MUSIC, b);
                if (b)
                    startService(bgMusicIntent);
                else
                    stopService(bgMusicIntent);
                break;

            case R.id.tbSound:
                settings.putValue(SettingsManager.Keys.SOUND, b);
                break;

            case R.id.tbVibrate:
                settings.putValue(SettingsManager.Keys.VIBRATE, b);
                break;

            case R.id.switchEndgameDialog:
                settings.putValue(SettingsManager.Keys.DIALOG_ENDGAME, b);
                break;

            case R.id.switchHint:
                settings.putValue(SettingsManager.Keys.SHOW_HINTS, b);
                break;

            case R.id.switchAskBeforeSave:
                settings.putValue(SettingsManager.Keys.IS_ASK_BEFORE_SAVE, b);
                break;

            case R.id.switchAlwaysLoadSavedGame:
                settings.putValue(SettingsManager.Keys.ALWAYS_LOAD_SAVED_GAME, b);
                break;

            case R.id.tbStartPlayer:
                settings.putValue(SettingsManager.Keys.FIRST_PLAYER_STARTS, b);
                break;

            case R.id.tbTimeCounting:
                settings.putValue(SettingsManager.Keys.COUNT_TIME_UP, b);
                break;

            case R.id.tbHumanStarts:
                settings.putValue(SettingsManager.Keys.IS_HUMAN_STARTS, b);
                break;

            default:
                Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A private method that loads the profile picture.
     */
    private void loadImage() {
        // loading picked imaged (if one exists)
        Bitmap loaded = loadImageBitmap(PICTURE_PROFILE_NAME);

        if (loaded != null)
            picImg.setImageBitmap(loaded);
        else
            Toast.makeText(this,
                    "Could not find picture to load", Toast.LENGTH_SHORT).show();
    }

    /**
     * A private method that lets the user take a picture (for the profile), using an implicit
     * intent.
     */
    public void takePic() {
        // Check permission
        String camPermission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(this, camPermission) !=
                PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {camPermission},
                    REQUEST_CAM_PERMISSION);
        else {
            Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * A listener for when an activity that was started for a result returns a result: the usage
     * is for when a profile picture is picked/taken, so that it will be displayed.
     *
     * @param requestCode the request code of the activity.
     * @param resultCode  the result code of the activity.
     * @param data        the data returned by the activity (or null if there isn't).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            picImageBitmap = (Bitmap) data.getExtras().get("data");
            picImg.setImageBitmap(picImageBitmap);

            savePic();
        }
        else if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK)
        {
            assert data != null; // result is ok
            Uri uri = data.getData();
            try {
                picImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                picImg.setImageBitmap(picImageBitmap);
                savePic();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * A private method that saves the current profile image.
     */
    private void savePic() {
        FileOutputStream fos;
        try {
            fos = openFileOutput(PICTURE_PROFILE_NAME, Context.MODE_PRIVATE);
            picImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) { // including FileNotFoundException
            e.printStackTrace();
        }
    }

    /**
     * A method that receives a name of a picture as a parameter and returns the bitmap of
     * the picture saved by that name.
     *
     * @param name a name of a saved picture.
     * @return the picture that is saved by the received name.
     */
    public Bitmap loadImageBitmap(String name) {
        FileInputStream fileInputStream;
        Bitmap bitmap = null;
        try{
            fileInputStream = openFileInput(name);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * A method that pops up a dialog that asks the user whether he wishes to take or to pick
     * the new profile picture.
     *
     * @param view the clicked View that invoked the method.
     */
    public void setAction(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Action");
        builder.setMessage("Do you wish to continue with Gallery or Camera?");
        builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pickGallery();
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                takePic();
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * A private method that enables the user to pick a profile picture from the gallery (using
     * an implicit intent).
     */
    private void pickGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_PICK_PHOTO);
    }

    /**
     * A listener for when an item is selected from an AdapterView: the usage is for
     * catching when an item is selected from a Spinner.
     *
     * @param adapterView the parent AdapterView of the selected View.
     * @param view the selected View.
     * @param i the clicked item's position in the list.
     * @param l the row id of the clicked item.
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.spnFirstColor:
                colorSelected(adapterView,
                        settings.getPieceValue(SettingsManager.Keys.FIRST_PLAYER_PIECE),
                        settings.getPieceValue(SettingsManager.Keys.SECOND_PLAYER_PIECE),
                        colorAdapter.getItem(i),
                        SettingsManager.Keys.FIRST_PLAYER_PIECE);
                break;

            case R.id.spnSecondColor:
                colorSelected(adapterView,
                        settings.getPieceValue(SettingsManager.Keys.SECOND_PLAYER_PIECE),
                        settings.getPieceValue(SettingsManager.Keys.FIRST_PLAYER_PIECE),
                        (Piece) adapterView.getItemAtPosition(i),
                        SettingsManager.Keys.SECOND_PLAYER_PIECE);
                break;

            case R.id.spnBoardSize:
                sizeSelected(SettingsManager.Keys.BOARD_SIZE, i);
                break;

            case R.id.spnStartSize:
                sizeSelected(SettingsManager.Keys.START_SIZE, i);
                break;

            case R.id.spnDifficulty:
                difficultySelected((GameController.Difficulty) adapterView.getItemAtPosition(i));
                break;

            default:
                Toast.makeText(this, "WARNING: Unknown Adapter Selected!",
                        Toast.LENGTH_SHORT).show();
        }
    }

    private int isInitColorSelection = 0; // to dismiss unnecessary initiative invokes.

    /**
     * A method that is called when a color of a player was selected from a spinner. It
     * behaves appropriately.
     *
     * @param adapterView   the parent AdapterView of the selected color View.
     * @param currentColor  the current color of the selected player (to prevent unnecessary
     *                      interaction with the settings).
     * @param otherColor    the color of the other player to prevent having same color for both
     *                      players.
     * @param selectedColor the selected color.
     * @param currentKey    the setting key of the selected player's piece color.
     */
    private void colorSelected(AdapterView<?> adapterView, Piece currentColor, Piece otherColor,
                               Piece selectedColor, SettingsManager.Keys currentKey) {
        // To prevent unnecessary run: first time is init, second time is fetching settings
        if (isInitColorSelection < 2) {
            isInitColorSelection++;
            return;
        }

        if (otherColor == selectedColor) {
            Toast.makeText(this, "Both players can't have same colors",
                    Toast.LENGTH_SHORT).show();
            playSound(MusicControl.Sound.INVALID_SELECTION);
            adapterView.setSelection(colorAdapter.getPosition(currentColor));
        }
        else if (currentColor != selectedColor)
            settings.putValue(currentKey, selectedColor);
    }

    /**
     * A method that is called when a size is selected from a spinner: saves it in the settings.
     *
     * @param key the settings key of the selected size.
     * @param i   the index of the selecte size in the adapter list.
     */
    private void sizeSelected(SettingsManager.Keys key, int i) {
        settings.putValue(key, sizesAdapter.getItem(i));
    }

    /**
     * A method that is called when a difficulty is selected from the difficulty spinner.
     *
     * @param difficulty the selected difficulty.
     */
    private void difficultySelected(GameController.Difficulty difficulty) {
        settings.putValue(SettingsManager.Keys.DIFFICULTY, difficulty);

        if (!difficulty.isVsComputer()) {
            tbHumanStarts.setChecked(true);
            setEnabledHumanStarts(false);
        }
        else
            setEnabledHumanStarts(true);
    }

    /**
     * A method that gets a boolean as a parameter and enables/disables the HumanStarts relevant
     * Views accordingly.
     *
     * @param isEnabled true if to enable, false if to disable.
     */
    private void setEnabledHumanStarts(boolean isEnabled) {
        tbHumanStarts.setEnabled(isEnabled);
        btnResetHumanStarts.setEnabled(isEnabled);
        btnResetHumanStarts.setAlpha(isEnabled ? 1f : 0.5f);
    }

    /**
     * A method that is called when no item is selected : that means, when the previously selected
     * item is removed from the adapter list or the adapter list becomes empty.
     *
     * (There is currently no usage for this method here, it is implemented because it's mandatory
     * to implement since OnItemSelectedListener is implemented).
     *
     * @param adapterView the AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * A method that is invoked when the user wishes to reset his settings.
     *
     * @param view the clicked View that invoked the method.
     */
    public void restoreDefaultsClicked(View view) {
        dialogResetSettings();
    }

    /**
     * A private method that pops up a dialog that ensures that the user wishes to reset all
     * his settings.
     */
    private void dialogResetSettings() {
        final AlertDialog.Builder resetDialog = new AlertDialog.Builder(this);
        resetDialog.setTitle("Restore Default Settings");
        resetDialog.setMessage("Are you sure you wish to restore default settings? " +
                "All your settings will be wiped, including game record sorting preferences");

        resetDialog.setPositiveButton("Restore Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                restoreDefaultSettings();
                dialogInterface.dismiss();
            }
        });

        resetDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        resetDialog.create().show();

        playSound(MusicControl.Sound.DIALOG_POPUP);
    }

    private void restoreDefaultSettings() {
        // If Records activity is enabled to stay unclosed, updating onResume there is required

        settings.restoreDefaultSettings();
        syncSettings();
    }

    /**
     * A method that is invoked when the user wishes to reset a single setting.
     *
     * @param view the clicked reset button that invoked the method (that is associated with
     *             the setting desired to reset).
     */
    public void resetSetting(View view) {
        switch (view.getId()) {
            case R.id.btnResetSound:
                settings.resetSingleSetting(SettingsManager.Keys.SOUND);
                tbSound.setChecked(settings.getBooleanValue(SettingsManager.Keys.SOUND));
                break;

            case R.id.btnResetVibrate:
                settings.resetSingleSetting(SettingsManager.Keys.VIBRATE);
                tbVibrate.setChecked(settings.getBooleanValue(SettingsManager.Keys.VIBRATE));
                break;

            case R.id.btnResetBgMusic:
                settings.resetSingleSetting(SettingsManager.Keys.BACKGROUND_MUSIC);
                tbBgMusic.setChecked(
                        settings.getBooleanValue(SettingsManager.Keys.BACKGROUND_MUSIC));
                break;

            case R.id.btnResetEndgameDial:
                settings.resetSingleSetting(SettingsManager.Keys.DIALOG_ENDGAME);
                switchEndgameDialog.setChecked(
                        settings.getBooleanValue(SettingsManager.Keys.DIALOG_ENDGAME));
                break;

            case R.id.btnResetShowHints:
                settings.resetSingleSetting(SettingsManager.Keys.SHOW_HINTS);
                switchShowHints.setChecked(settings.getBooleanValue(
                        SettingsManager.Keys.SHOW_HINTS));
                break;

            case R.id.btnResetFirstColor:
            case R.id.btnResetSecondColor: // resetting should be both of them
                settings.resetSingleSetting(SettingsManager.Keys.FIRST_PLAYER_PIECE);
                settings.resetSingleSetting(SettingsManager.Keys.SECOND_PLAYER_PIECE);
                spnFirstColor.setSelection(colorAdapter.getPosition(
                        settings.getPieceValue(SettingsManager.Keys.FIRST_PLAYER_PIECE)));
                spnSecondColor.setSelection(colorAdapter.getPosition(
                        settings.getPieceValue(SettingsManager.Keys.SECOND_PLAYER_PIECE)));
                break;

            case R.id.btnResetBoardSize:
            case R.id.btnResetStartSize: // resetting should be both of them
                settings.resetSingleSetting(SettingsManager.Keys.BOARD_SIZE);
                settings.resetSingleSetting(SettingsManager.Keys.START_SIZE);

                syncSizes();
                break;

            case R.id.btnResetAskBeforeSave:
                settings.resetSingleSetting(SettingsManager.Keys.IS_ASK_BEFORE_SAVE);
                switchAskBeforeSave.setChecked(settings.getBooleanValue(
                        SettingsManager.Keys.IS_ASK_BEFORE_SAVE));
                break;

            case R.id.btnResetAlwaysLoadSavedGame:
                settings.resetSingleSetting(SettingsManager.Keys.ALWAYS_LOAD_SAVED_GAME);
                switchAlwaysLoadSavedGame.setChecked(settings.getBooleanValue(
                        SettingsManager.Keys.ALWAYS_LOAD_SAVED_GAME));
                break;

            case R.id.btnResetStartPlayer:
                settings.resetSingleSetting(SettingsManager.Keys.FIRST_PLAYER_STARTS);
                tbStartPlayer.setChecked(settings.getBooleanValue(
                        SettingsManager.Keys.FIRST_PLAYER_STARTS));
                break;

            case R.id.btnResetTimeCounting:
                settings.resetSingleSetting(SettingsManager.Keys.COUNT_TIME_UP);
                tbTimeCounting.setChecked(settings.getBooleanValue(
                        SettingsManager.Keys.COUNT_TIME_UP));
                break;

            case R.id.btnResetDuration:
                settings.resetSingleSetting(SettingsManager.Keys.DURATION);
                syncDurationPicker();
                break;

            case R.id.btnResetDifficulty:
                settings.resetSingleSetting(SettingsManager.Keys.DIFFICULTY);
                spnDifficulty.setSelection(difficultyAdapter.getPosition(
                        settings.getDifficultyValue(SettingsManager.Keys.DIFFICULTY)));
                break;

            case R.id.btnResetHumanStarts:
                settings.resetSingleSetting(SettingsManager.Keys.IS_HUMAN_STARTS);
                tbHumanStarts.setChecked(settings.getBooleanValue(
                        SettingsManager.Keys.IS_HUMAN_STARTS));
                break;

            default:
                Toast.makeText(this, "Invalid reset button selected?",
                        Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A method that expands/collapses the settings.
     *
     * @param view the clicked View that invoked the method.
     */
    public void toggleExpandedSettings(View view) {
        isExpandSettings = !isExpandSettings;
        syncExpandedSettings();
    }

    /**
     * A listener for when a radio from a RadioGroup is changed: the usage is for when the user
     * wishes to choose a specific size by an EditText instead of a spinner.
     *
     * @param radioGroup the parent RadioGroup of the selected radio.
     * @param i          the position of the selected radio in the RadioGroup.
     */
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        toggleEnabledSizeInp();
    }

    /**
     * A private method that synchronizes the display with the selected radio that indicates
     * whether the user wishes to select the size by an EditText or by a spinner.
     */
    private void toggleEnabledSizeInp() {
        EditText currentEt, otherEt;
        Spinner currentSpn, otherSpn;
        SettingsManager.Keys currentKey, otherKey;
        List<Integer> matchingSizes;

        // Start size selected
        if (rgSizes.getCheckedRadioButtonId() == R.id.rbStartSize) {
            currentEt = etStartSize;
            currentSpn = spnStartSize;
            currentKey = SettingsManager.Keys.START_SIZE;

            otherEt = etBoardSize;
            otherSpn = spnBoardSize;
            otherKey = SettingsManager.Keys.BOARD_SIZE;

            matchingSizes = BoardModel.getMatchingBoardSizes(settings.getIntValue(currentKey));
        } // Board size selected
        else {
            currentEt = etBoardSize;
            currentSpn = spnBoardSize;
            currentKey = SettingsManager.Keys.BOARD_SIZE;

            otherEt = etStartSize;
            otherSpn = spnStartSize;
            otherKey = SettingsManager.Keys.START_SIZE;

            matchingSizes = BoardModel.getMatchingStartSizes(settings.getIntValue(currentKey));
        }

        otherEt.setEnabled(false);
        currentEt.setEnabled(true);

        if (currentSpn.getVisibility() == View.VISIBLE) {
            currentSpn.setVisibility(View.GONE); // hide Spinner
            currentEt.setVisibility(View.VISIBLE); // show EditText
        }
        currentEt.setText(String.valueOf(settings.getIntValue(currentKey)));

        otherEt.setVisibility(View.GONE);
        otherSpn.setVisibility(View.VISIBLE);

        syncSize(currentEt, matchingSizes, otherSpn);

        otherSpn.setSelection(sizesAdapter.getPosition(settings.getIntValue(otherKey)));
    }

    /**
     * A method that is invoked when the user wishes to choose the game duration.
     *
     * @param view the clicked View that invoked the method.
     */
    public void displayTimerPicker(View view) {
        syncDurationPicker();

        pickerDialog.show();
    }

    /**
     * A private method that updates the selected game duration setting (if it's valid).
     *
     * @return true if the operation successfully updated the game duration or false
     * otherwise (the selected duration wasn't valid).
     */
    private boolean updateTimerPicker() {
        int totalTime = durationMinutesPicker.getValue() * 60 + durationSecondsPicker.getValue();

        if (totalTime < MIN_DURATION) // invalid duration
            return false;

        settings.putValue(SettingsManager.Keys.DURATION, totalTime);

        return true;
    }

    /**
     * A private method that gets as parameters an EditText of a field to check and the
     * Spinner that is not associated with the input field, and checks the validity of the
     * field accordingly.
     *
     * @param field    an EditText field of which to check the input.
     * @param otherSpn the Spinner that is not the spinner associated with the received field.
     */
    private void checkSizeValidity(EditText field, Spinner otherSpn) {
        String input = field.getText().toString();

        if (input.isEmpty()
                || field == this.etStartSize && !checkStartSizeValidity(input)
                || field == this.etBoardSize && !checkBoardSizeValidity(input)) { // invalid

            playSound(MusicControl.Sound.INVALID_SELECTION);
            field.setError("Invalid size: long-click \"board size\" for more info");
            otherSpn.setEnabled(false);
        }
    }

    /**
     * A private method that gets an input of a start size as a parameter and checks its
     * validity: returns true if it's valid or false otherwise.
     *
     * @param input the start size to check.
     * @return true if the input is valid, or false if its invalid.
     */
    private boolean checkStartSizeValidity(String input) {
        int inpInt = Integer.parseInt(input);
        List<Integer> matchingBoardSize = BoardModel.getMatchingBoardSizes(inpInt);

        if (matchingBoardSize.isEmpty()) // invalid
            return false;

        syncSize(etStartSize, SettingsManager.Keys.START_SIZE, spnBoardSize,
                matchingBoardSize, inpInt);
        return true;
    }

    /**
     * A private method that gets an input of a board size as a parameter and checks its
     * validity: returns true if it's valid or false otherwise.
     *
     * @param input the board size to check.
     * @return true if the input is valid, or false if its invalid.
     */
    private boolean checkBoardSizeValidity(String input) {
        int inpInt = Integer.parseInt(input);
        List<Integer> matchingStartSizes = BoardModel.getMatchingStartSizes(inpInt);

        if (!matchingStartSizes.isEmpty()) { // valid
            syncSize(etBoardSize, SettingsManager.Keys.BOARD_SIZE, spnStartSize,
                    matchingStartSizes, inpInt);

            return true;
        }

        return false;
    }

    /**
     * A private method that gets as a parameter a sound and plays it if sound is enabled
     * in the settings.
     *
     * @param sound a sound to play.
     */
    private void playSound(MusicControl.Sound sound) {
        if (settings.getBooleanValue(SettingsManager.Keys.SOUND))
            musicControl.playSound(sound);
    }

    /**
     * A listener for capturing long clicks: its usage is to enable the user to get a description
     * of a setting with an ambiguous name.
     *
     * @param view the clicked View that invoked the method.
     * @return true if the method consumed the click, or false otherwise (that means, return
     * true if no other listeners/events should be invoked as a result from the long click
     * after the method was invoked, or false otherwise).
     */
    @Override
    public boolean onLongClick(View view) {
        String display = "";

        switch (view.getId()) {
            case R.id.tvBoardSize:
                display = String.format("Board size ranges between %d and %d. In addition, it " +
                        "has to enable sub-matrix of starting pieces to be located exactly in " +
                        "the middle of the board",
                        BoardModel.MIN_BOARD_SIZE,
                        BoardModel.MAX_BOARD_SIZE);
                break;

            case R.id.switchEndgameDialog:
                display = "Enable pop up dialog after game ends";
                break;

            case R.id.switchHint:
                display = "Display hints that indicate attainable amount of additional pieces";
                break;

            case R.id.switchAskBeforeSave:
                display = "Enable this if you wish to be asked before saving the game when " +
                        "quitting. Disabled means game will be automatically saved when leaving";
                break;

        }

        tvPopup.setText(display);
        popupWindow.showAsDropDown(view, 2, 5);
        return false;
    }

    /**
     * A private method that hides the keyboard of the user.
     */
    private void hideKeyboard() {
        InputMethodManager imm=
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etStartSize.getWindowToken(), 0);
    }

    /**
     * A method that is called when the activity is destroyed. It saves the expanding settings
     * status.
     */
    @Override
    protected void onDestroy() {
        settings.putValue(SettingsManager.Keys.EXPAND_SETTINGS, isExpandSettings);
        super.onDestroy();
    }

    /**
     * A static method that receives a boolean that indicates if a View is visible. If it is
     * visible it returns the View constant that stands for visible. If it isn't, it returns
     * the constant that stands for gone visibility.
     *
     * @param isVisible true if the View is visible or false otherwise.
     * @return the View constant that stands for visible if the received boolean is true.
     * If it isn't, it returns the constant that stands for gone visibility.
     */
    public static int getVisibilityConstant(boolean isVisible) {
        return isVisible ? View.VISIBLE : View.GONE;
    }
}