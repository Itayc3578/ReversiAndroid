package com.itayc.reversi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Adapter for available colors.
 */
public class ColorAdapter extends ArrayAdapter<Piece> {

    // Attributes

    private final Context context; // the Context
    private final List<Piece> list; // the list


    // Constructor

    /**
     * The constructor of the class: receives a Context as a parameter and initiates the
     * instance.
     *
     * @param context a context (to enable accessibility).
     */
    public ColorAdapter(@NonNull Context context) {
        super(context, R.layout.spinner_disc);

        this.context = context;
        this.list = Piece.getValidValues();
    }


    // Methods

    /**
     * The getDropDownView method of the adapter: receives as parameters the position of the
     * current View (piece), a View to enable recycling Views and thus improve performance and the
     * parent that this view will eventually be attached to, and updates the view accordingly.
     *
     * @param position the position of the current View (piece) in the list.
     * @param convertView a View (to enable recycling Views and thus improve performance).
     * @param parent the parent that this view will eventually be attached to.
     * @return the resulting View (piece) after it was updated.
     */
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // graphics
        View rowView = inflater.inflate(R.layout.spinner_disc, parent,false);

        Piece currentDisc = this.list.get(position);

        TextView tvRow = rowView.findViewById(R.id.spnTvDisc);
        ImageView ivDisc = rowView.findViewById(R.id.spnIvDisc);

        tvRow.setText(currentDisc.toString());
        ivDisc.setColorFilter(currentDisc.getColor());

        return rowView;
    }

    /**
     * The getView method of the adapter: receives as parameters the position of the current
     * View (piece), a View to enable recycling Views and thus improve performance and the parent
     * that this view will eventually be attached to, and updates the view accordingly.
     *
     * @param position the position of the current View (piece) in the list.
     * @param convertView a View (to enable recycling Views and thus improve performance).
     * @param parent the parent that this view will eventually be attached to.
     * @return the resulting View (piece) after it was updated.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = getDropDownView(position, convertView, parent);
        rowView.setBackgroundColor(Color.TRANSPARENT);
        return rowView;
    }

    /**
     * A method that receives an item as a parameter and returns it's position in the adapter list.
     **
     * @param item the item of which to return the position.
     * @return the position of the received item in that adapter list.
     */
    @Override
    public int getPosition(@Nullable Piece item) {
        return this.list.indexOf(item);
    }

    /**
     * A method that gets an index of an item as a parameter and returns the item at that index.
     *
     * @param position the index of the desired item.
     * @return the item at the received position.
     */
    @Nullable
    @Override
    public Piece getItem(int position) {
        return this.list.get(position);
    }

    /**
     * A method that returns the size of the adapter list or 0 if it's null.
     *
     * @return the size of the adapter list or 0 if it's null.
     */
    @Override
    public int getCount() {
        if (this.list == null)
            return 0;
        return this.list.size();
    }
}
