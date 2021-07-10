package com.itayc.reversi;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static com.itayc.reversi.SettingsActivity.getVisibilityConstant;

/**
 * The activity in which the saved game records (that are stored on the database) are displayed
 * and managed.
 */
public class RecordsActivity extends AppCompatActivity
        implements AdapterView.OnItemLongClickListener,
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    // Attributes

    private ConstraintLayout upperDefault; // default upper container
    private ConstraintLayout upperDelete; // delete mode upper container
    private boolean isDeleteMode; // true if delete mode is enabled, false otherwise
    private ImageButton btnEnterDeleteMode;

    private ImageButton deleteSelectedBtn; // button to delete selected items
    private CheckBox cbSelectAll; // checkbox that selects all rows
    private TextView tvDisplaySelected; // TextView to display amount of selected rows

    private ListView lvRecords; // the ListView that holds the records
    private ArrayList<FinishedGameDetails> games; // the game records array
    private GameDetailsAdapter adapter; // the adapter for the records

    private SettingsManager settings; //  the settings (singleton) instance
    private GameDetailsDataBase dataBase; // the database (singleton) instance

    private GameDetailsComparator comparator; // the comparator to enable sorting
    private Spinner spnSort; // the spinner of the sorting options
    private boolean isSortAsc; // true if sort order is ascending, or false otherwise
    private ImageButton btnAscDesc; // the button to shift the sorting order


    // Methods

    /**
     * The onCreate method (called when the activity is created).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        // get relevant singletons
        this.settings = SettingsManager.getInstance(this);
        this.dataBase = GameDetailsDataBase.getInstance(this);

        // link the necessary display Views
        linkDisplay();

        // fetch the records from the database
        this.games = (ArrayList<FinishedGameDetails>) this.dataBase.fetchAllRecords();

        this.isDeleteMode = false; // delete mode is disabled upon first entry
        if (this.games.isEmpty())
            this.btnEnterDeleteMode.setVisibility(View.INVISIBLE);

        // retrieve sorting settings
        this.isSortAsc = settings.getBooleanValue(SettingsManager.Keys.RECORDS_SORT_ASCENDING);
        GameDetailsComparator.CompareGDType compareAttr =
                settings.getCompareGDTypeValue(SettingsManager.Keys.RECORDS_SORT);

        this.comparator = new GameDetailsComparator(compareAttr, this.isSortAsc);
        ArrayAdapter<GameDetailsComparator.CompareGDType> adapterSort = new ArrayAdapter<>(
                this,
                R.layout.support_simple_spinner_dropdown_item,
                GameDetailsComparator.CompareGDType.values());

        this.spnSort.setAdapter(adapterSort);

        this.spnSort.setSelection(adapterSort.getPosition(compareAttr));
        Collections.sort(this.games, this.comparator);

        // sorting order
        updateOrderBtn();

        this.adapter = new GameDetailsAdapter(this, this.games);
        this.lvRecords.setAdapter(this.adapter);

        setListeners();
    }

    /**
     * A private method that links the display Views to their corresponding attributes.
     */
    private void linkDisplay() {
        this.upperDefault = findViewById(R.id.recordsUpperDefault);
        this.upperDelete = findViewById(R.id.recordsUpperDelete);
        this.btnEnterDeleteMode = findViewById(R.id.btnEnterDeleteMode);

        this.deleteSelectedBtn = findViewById(R.id.btnDeleteSelected);
        this.cbSelectAll = findViewById(R.id.cbSelectAll);
        this.tvDisplaySelected = findViewById(R.id.tvDisplaySelected);

        this.lvRecords = findViewById(R.id.lvRecords);

        this.spnSort = findViewById(R.id.spnSort);
        this.btnAscDesc = findViewById(R.id.btnAscDesc);
    }


    /**
     * A private method that sets the relevant listeners.
     */
    private void setListeners() {
        this.spnSort.setOnItemSelectedListener(this);
        this.lvRecords.setOnItemLongClickListener(this);
        this.lvRecords.setOnItemClickListener(this);
    }

    /**
     * A listener for long clicks: the usage is for the long clicks on the records, so that
     * they will turn on the delete mode when long clicked.
     *
     * @param adapterView the parent AdapterView of the clicked View.
     * @param view the clicked View.
     * @param i the clicked item's position in the list.
     * @param l the row id of the clicked item.
     * @return true if the method consumed the click, or false otherwise (that means, return
     * true if no other listeners/events should be invoked as a result from the long click
     * after the method was invoked, or false otherwise).
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (!this.isDeleteMode) {
            this.adapter.clickedItem(i);
            toggleDeleteMode();
        }
        return true;
    }

    /**
     * A listener for selected item: the usage is for setting the sorting option.
     *
     * @param adapterView the parent AdapterView of the selected View.
     * @param view the selected View.
     * @param i the clicked item's position in the list.
     * @param l the row id of the clicked item.
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        GameDetailsComparator.CompareGDType sortBy = (GameDetailsComparator.CompareGDType)
                adapterView.getItemAtPosition(i);
        this.settings.putValue(SettingsManager.Keys.RECORDS_SORT, sortBy);

        this.comparator.setCompareAttr(sortBy);
        Collections.sort(this.games, this.comparator);

        this.adapter.notifyDataSetChanged();

        scrollTop();
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
     * A method that is called when an item is clicked: the usage is for catching clicks on
     * the record rows (row clicked in delete mode - check/uncheck; row clicked not in delete
     * mode - display extended row).
     *
     * @param adapterView the parent AdapterView of the clicked View.
     * @param view the clicked View.
     * @param i the clicked item's position in the list.
     * @param l the row id of the clicked item.
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (this.isDeleteMode) {
            this.adapter.clickedItem(i);
            syncAdapterAndBtn();
        }
        else
            displayExtendedRow(this.games.get(i));
    }

    /**
     * A method that alternates/shifts the sorting order.
     *
     * @param view the clicked View that invoked the method.
     */
    public void changeOrder(View view) {
        this.isSortAsc = !this.isSortAsc; // update attribute
        this.settings.putValue(SettingsManager.Keys.RECORDS_SORT_ASCENDING, this.isSortAsc);
        this.comparator.setAscending(this.isSortAsc); // update adapter

        Collections.reverse(this.games); // reverse the order

        this.adapter.notifyDataSetChanged(); // notify adapter and refresh

        updateOrderBtn(); // change buttons

        scrollTop();
    }

    /**
     * A method that synchronizes the sorting order button with the sorting order.
     */
    private void updateOrderBtn() {
        this.btnAscDesc.setImageResource(this.isSortAsc ?
                R.drawable.sort_down : R.drawable.sort_up);
    }

    /**
     * A method that scrolls to the top of the list (if it isn't empty).
     */
    private void scrollTop() {
        if (!this.adapter.isEmpty())
            this.lvRecords.smoothScrollToPosition(0);
    }

    /**
     * A method (calls the method) that toggles delete mode.
     *
     * @param view the clicked View that invoked the method.
     */
    public void toggleDeleteMode(View view) {
        toggleDeleteMode();
    }

    /**
     * A method that toggles the delete mode.
     */
    private void toggleDeleteMode() {
        if (this.isDeleteMode) // not really needed
            this.adapter.clearToDelete();

        this.isDeleteMode = !this.isDeleteMode;

        this.upperDefault.setVisibility(getVisibilityConstant(!this.isDeleteMode));
        this.upperDelete.setVisibility(getVisibilityConstant(this.isDeleteMode));

        this.adapter.setDeleteMode(this.isDeleteMode);

        syncAdapterAndBtn();
    }

    /**
     * A method that pops up a dialog that displays an extended game record row.
     *
     * @param rowDetails an object that contains data about a finished game.
     */
    private void displayExtendedRow(FinishedGameDetails rowDetails) {
        final Dialog statsDialog = new Dialog(this);
        statsDialog.setContentView(R.layout.extended_details_row);

        // Set the width to match parent
        Window dialogWindow = statsDialog.getWindow();
        dialogWindow.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        View dialogView = dialogWindow.getDecorView();
        dialogView.setBackgroundColor(Color.LTGRAY);

        GameDetailsAdapter.updateExtendedRow(dialogView, rowDetails);
        statsDialog.show();
    }

    /**
     * A method that selects all the rows (as designated to be deleted).
     *
     * @param view the clicked View that invoked the method.
     */
    public void selectAll(View view) {
        if (this.cbSelectAll.isChecked())
            this.adapter.selectAll();
        else
            this.adapter.clearToDelete();

        syncAdapterAndBtn();
    }

    /**
     * A method that is called when a player wishes to delete selected items.
     *
     * @param view the clicked View that invoked the method.
     */
    public void deleteSelectedClicked(View view) {
        dialogDelete();
    }

    /**
     * A private method that pops up a dialog that makes sure the player wishes to delete
     * the selected items.
     */
    private void dialogDelete() {
        int toDelLength = this.adapter.toDeleteLength();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove selected records");
        builder.setMessage(toDelLength == 1 ?  "Are you sure you wish to delete this record?" :
                "Are you sure you wish to delete " +toDelLength +" selected records?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteRecords(adapter.getToDelete());
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * A private method that gets as a parameter a Set that contains records to be deleted,
     * and deletes them.
     *
     * @param recordsToDelete a Set that contains records to be deleted.
     */
    private void deleteRecords(Set<Integer> recordsToDelete) {
        int offset = 0; // because ArrayList updates dynamically

        for (int indexToDel: recordsToDelete) {
            indexToDel -= offset;
            this.dataBase.deleteRecordById(this.games.get(indexToDel).getId()); // delete from DB.
            this.games.remove(indexToDel); // WRONG, need to handle
            offset++;
        }

        if (this.isDeleteMode) {
            toggleDeleteMode(); // finished delete mode

            if (this.games.isEmpty())
                this.btnEnterDeleteMode.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * A private method that synchronizes the display according to the amount of selected items.
     */
    private void syncAdapterAndBtn() {
        int selectedCount = this.adapter.toDeleteLength();
        String displayText;

        if (selectedCount == 0) { // none selected
            this.deleteSelectedBtn.setVisibility(View.INVISIBLE);
            displayText = "Select Records";
        }
        else {
            this.deleteSelectedBtn.setVisibility(View.VISIBLE);
            displayText = selectedCount + " Selected";
        }

        this.tvDisplaySelected.setText(displayText);

        this.cbSelectAll.setChecked(selectedCount == this.adapter.getCount());

        this.adapter.notifyDataSetChanged();
    }

    /**
     * An override for the onBackPressed method: if dark mode is activated, then disable it.
     * Otherwise, invoke the super method.
     */
    @Override
    public void onBackPressed() {
        if (this.isDeleteMode)
            toggleDeleteMode();
        else
            super.onBackPressed();
    }
}