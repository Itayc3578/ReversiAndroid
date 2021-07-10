package com.itayc.reversi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Adapter for finished game details.
 */
public class GameDetailsAdapter extends ArrayAdapter<FinishedGameDetails> {

    // Attributes

    private final Context context; // the Context for accessibility
    private final ArrayList<FinishedGameDetails> list; // the list to adapt

    private final Set<Integer> toDelete; // a Set to track items designated for delete
    private boolean isDeleteMode; // true if delete mode is enabled, false otherwise


    // Constructor

    /**
     * A constructor for the adapter class. Receives as parameters a Context and a list to
     * adapt, and initiates the adapter accordingly.
     *
     * @param context a context (for accessibility).
     * @param list a list to adapt.
     */
    public GameDetailsAdapter(Context context, ArrayList<FinishedGameDetails> list) {
        super(context, R.layout.game_details_row, list);
        this.context = context;
        this.list = list;

        this.isDeleteMode = false;
        this.toDelete = new HashSet<>();
    }


    // Methods

    /**
     * The getView method of the adapter: receives as parameters the position of the current
     * View (row), a View to enable recycling Views and thus improve performance and the parent
     * that this view will eventually be attached to, and updates the view accordingly.
     *
     * @param position the position of the current View (row) in the list.
     * @param convertView a View (to enable recycling Views and thus improve performance).
     * @param parent the parent that this view will eventually be attached to.
     * @return the resulting View (row) after it was updated.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // graphics
        View rowView = inflater.inflate(R.layout.game_details_row, parent,false);

        // Object that represents FinishedGameDetails
        FinishedGameDetails finishedGameDetails = this.list.get(position);

        updateRow(rowView, finishedGameDetails);

        if (position % 2 == 0)
            rowView.setBackgroundColor(Color.DKGRAY);
        else
            rowView.setBackgroundColor(Color.LTGRAY);

        if (this.isDeleteMode) {
            CheckBox currentCheckBox = rowView.findViewById(R.id.cbRow);
            currentCheckBox.setVisibility(View.VISIBLE);

            if (this.toDelete.contains(position))
                currentCheckBox.setChecked(true); // default is false
        }

        return rowView;
    }

    /**
     * A static method that receives as parameters a View that represents a row of details and
     * details to update that row to, and updates the received row View accordingly.
     *
     * @param rowView a view that represents a row of finished game details.
     * @param finishedGameDetails an object that contains details about a finished game.
     */
    public static void updateRow(View rowView, FinishedGameDetails finishedGameDetails) {
        Piece firstPlayer = finishedGameDetails.getFirstPlayer();
        Piece secondPlayer = finishedGameDetails.getSecondPlayer();
        Piece winner = finishedGameDetails.getGameWinner();

        int firstColor = firstPlayer.getColor();
        int secondColor = secondPlayer.getColor();

        TextView tvFirstPlayer = rowView.findViewById(R.id.tvFirstPlayer);
        tvFirstPlayer.setText(firstPlayer.toString());
        tvFirstPlayer.setTextColor(firstColor);

        TextView tvSecondPlayer = rowView.findViewById(R.id.tvSecondPlayer);
        tvSecondPlayer.setText(secondPlayer.toString());
        tvSecondPlayer.setTextColor(secondColor);

        TextView tvWinner = rowView.findViewById(R.id.tvWinner);
        tvWinner.setText(winner != Piece.EMPTY ? "Winner: " + winner : "It's a Draw!");
        tvWinner.setTextColor(winner.getColor());

        TextView tvTimePassed = rowView.findViewById(R.id.tvTimePassed);
        tvTimePassed.setText("Time Passed: " + finishedGameDetails.getTimePlayedFormatted());

        TextView tvTurnsPlayed = rowView.findViewById(R.id.tvTurnsPlayed);
        tvTurnsPlayed.setText("Turns Played: " + finishedGameDetails.getTurnsPlayed());


        FrameLayout firstPiecesContainer = rowView.findViewById(R.id.rowFirstPiecesContainer);
        FrameLayout secondPiecesContainer = rowView.findViewById(R.id.rowSecondPiecesContainer);

        TextView tvFirstPieces = firstPiecesContainer.findViewById(R.id.tvPiece);
        tvFirstPieces.setText(String.valueOf(finishedGameDetails.getFirstPieces()));
        tvFirstPieces.setTextColor(contrastColor(firstColor));

        TextView tvSecondPieces = secondPiecesContainer.findViewById(R.id.tvPiece);
        tvSecondPieces.setText(String.valueOf(finishedGameDetails.getSecondPieces()));
        tvSecondPieces.setTextColor(contrastColor(secondColor));

        ImageView ivFirstPieces = firstPiecesContainer.findViewById(R.id.ivPiece);
        ivFirstPieces.setColorFilter(firstColor);

        ImageView ivSecondPieces = secondPiecesContainer.findViewById(R.id.ivPiece);
        ivSecondPieces.setColorFilter(secondColor);


        TextView tvGameId = rowView.findViewById(R.id.tvGameId);
        long id = finishedGameDetails.getId();

        // checking if id is set
        tvGameId.setText(id == -1 ? "" : "Game Id: " + id);
    }

    /**
     * A static method that receives as parameters a View that represents an extended row of
     * details and details to update that row to, and updates the received row View accordingly.
     *
     * @param rowView a view that represents an extended row of finished game details.
     * @param finishedGameDetails an object that contains details about a finished game.
     */
    public static void updateExtendedRow(View rowView, FinishedGameDetails finishedGameDetails) {
        updateRow(rowView, finishedGameDetails);

        TextView tvDate = rowView.findViewById(R.id.tvDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        tvDate.setText("Date Finished: " + dateFormat.format(finishedGameDetails.getDate()));

        TextView tvGameDifficulty = rowView.findViewById(R.id.tvGameDifficulty);
        tvGameDifficulty.setText("Difficulty: " + finishedGameDetails.getDifficulty());

        int firstPlayerColor = finishedGameDetails.getFirstPlayer().getColor();
        int secondPlayerColor = finishedGameDetails.getSecondPlayer().getColor();

        TextView tvFirstAvg = rowView.findViewById(R.id.tvAvgFirst);
        tvFirstAvg.setText(convertAvgToStr(finishedGameDetails.getTurnAvgFirst()));

        TextView tvSecondAvg = rowView.findViewById(R.id.tvAvgSecond);
        tvSecondAvg.setText(convertAvgToStr(finishedGameDetails.getTurnAvgSecond()));

        ImageView ivFirstAvg = rowView.findViewById(R.id.ivFirstAvg);
        ivFirstAvg.setColorFilter(firstPlayerColor);

        ImageView ivSecondAvg = rowView.findViewById(R.id.ivSecondAvg);
        ivSecondAvg.setColorFilter(secondPlayerColor);
    }

    /**
     * A private static method that gets a color id as a parameter and returns the contrast
     * (black or white) of it: if it's dark it will return white color id, otherwise it will
     * return black color id.
     *
     * Note that i did not devise that formula.
     *
     * @param color the color of which to return the contrast.
     * @return black or white color id based on the received color (light or dark).
     */
    public static int contrastColor(int color) {
        double darkness = 1- (0.299 * Color.red(color) + 0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;

        return darkness < 0.5 ? Color.BLACK: Color.WHITE;
    }

    /**
     * A private static method that receives a float that represents an average as a parameter,
     * and returns a String that represents that average in a presentable way.
     *
     * @param avg a float that represents an average (of a player's turn time).
     * @return a String that represents that average in a presentable way.
     */
    private static String convertAvgToStr(float avg) {

        // default avg means player didn't play
        if (avg == GameController.INIT_AVG)
            return "Didn't play";

        double currentAvg = avg / 1000;

        if (currentAvg / 60 >= 1)
            return String.format("%.3f minutes", currentAvg / 60);

        return String.format("%.3f seconds", currentAvg % 60);
    }

    /**
     * A setter method for the isDeleteMode attribute: receives a boolean as a parameter and
     * updates the isDeleteMode attribute accordingly.
     *
     * @param isDeleteMode true if delete mode is to be enabled, false otherwise.
     */
    public void setDeleteMode(boolean isDeleteMode) {
        this.isDeleteMode = isDeleteMode;
    }

    /**
     * A method that receives an index of a row in the adapter list as a parameter, and if that
     * row is checked it will be unchecked, otherwise (if it's unchecked) it checks it. "Checked"
     * means it is destined to be deleted. Likewise, "Unchecked" means it is no longer destined
     * to be deleted.
     *
     * The method is called after a row is clicked in delete mode (hence the name).
     *
     * @param i an index of a row in the adapter.
     */
    public void clickedItem(int i) {
        if (this.toDelete.contains(i))
            this.toDelete.remove(i);
        else
            this.toDelete.add(i);
    }

    /**
     * A method that clears the Set that contains the items to delete from the adapter list.
     */
    public void clearToDelete() {
        this.toDelete.clear();
    }

    /**
     * A method that "selects" all the rows (that means, mark them as destined to be deleted).
     */
    public void selectAll() {
        for (int i = 0; i < this.list.size(); i++)
            this.toDelete.add(i);
    }

    /**
     * A getter for the Set that contains the indexes of the rows to be deleted.
     *
     * @return the Set that contains the indexes of the rows to be deleted.
     */
    public Set<Integer> getToDelete () {
        return this.toDelete;
    }

    /**
     * A method that returns the amount of rows that are marked to be deleted.
     *
     * @return the amount of rows that are marked to be deleted.
     */
    public int toDeleteLength() {
        return this.toDelete.size();
    }
}
