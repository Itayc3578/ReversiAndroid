package com.itayc.reversi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * The adapter for the different initiative sizesL boardSize and startSize.
 */
public class InitSizesAdapter extends ArrayAdapter<Integer> {

    // Attributes

    private final Context context; // the Context
    private List<Integer> list; // the list of the sizes


    // Constructor

    /**
     * The constructor of the class: receives a Context as a parameter and initiates the
     * instance.
     *
     * @param context a context (to enable accessibility).
     */
    public InitSizesAdapter(@NonNull Context context) {
        super(context, R.layout.spinner_sizes);
        this.context = context;
    }


    // Methods

    /**
     * The getDropDownView method of the adapter: receives as parameters the position of the
     * current View (size), a View to enable recycling Views and thus improve performance and the
     * parent that this view will eventually be attached to, and updates the view accordingly.
     *
     * @param position the position of the current View (size) in the list.
     * @param convertView a View (to enable recycling Views and thus improve performance).
     * @param parent the parent that this view will eventually be attached to.
     * @return the resulting View (size) after it was updated.
     */
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)
                this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // graphics
        View rowView = inflater.inflate(R.layout.spinner_sizes, parent,false);


        TextView tvRow = rowView.findViewById(R.id.tvRowSizes);

        tvRow.setText(String.valueOf(this.list.get(position)));

        return rowView;
    }

    /**
     * The getView method of the adapter: receives as parameters the position of the current
     * View (size), a View to enable recycling Views and thus improve performance and the parent
     * that this view will eventually be attached to, and updates the view accordingly.
     *
     * @param position the position of the current View (size) in the list.
     * @param convertView a View (to enable recycling Views and thus improve performance).
     * @param parent the parent that this view will eventually be attached to.
     * @return the resulting View (size) after it was updated.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }

    /**
     * A method that receives a reference of a List of Integers as a parameter and sets the
     * current list to be the received List (and notifies the adapter).
     *
     * @param list a reference of a List of Integers to set the adapter list to.
     */
    public void updateList(List<Integer> list) {
        this.list = list;
        notifyDataSetChanged();
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

    /**
     * A method that gets an index of an item as a parameter and returns the item at that index.
     *
     * @param position the index of the desired item.
     * @return the item at the received position.
     */
    @Nullable
    @Override
    public Integer getItem(int position) {
        return this.list.get(position);
    }

    /**
     * A method that receives an item as a parameter and returns it's position in the adapter list.
     *
     * The method relies on binary search since the list should be sorted.
     *
     * @param item the item of which to return the position.
     * @return the position of the received item in that adapter list.
     */
    public int getPosition(int item) {
        return binarySearch(this.list, item);
    }


    /**
     * A method that receives as parameters a sorted List of Integers and an item to search for,
     * and searches for it using binary search algorithm: calls an overloaded sub method
     * based on the algorithm.
     *
     * @param list A sorted List of Integers to search the item for.
     * @param item the item to search for.
     * @return the index of the received item in the list, or -1 if it doesn't exist.
     */
    private static int binarySearch(List<Integer> list, int item) {
        return binarySearch(list, item, 0, list.size() - 1);
    }

    /**
     * A method that receives as parameters a sorted List of Integers, an item to search for,
     * the starting position to search at and the last position to search at, and searches
     * for it using (recursive) binary search algorithm: calls an overloaded sub method
     * based on the algorithm.
     *
     * @param list a sorted List of Integers to search the item for.
     * @param item the item to search for.
     * @param start the position to start the searching at.
     * @param end the last position to search at.
     * @return the index of the received item in the list or -1 if it doesn't exist.
     */
    private static int binarySearch(List<Integer> list, int item, int start, int end) {

        if (start <= end) {
            int mid = (start + end) / 2;

            if (list.get(mid) < item)
                return binarySearch(list, item, mid + 1, end);

            if (list.get(mid) > item)
                return binarySearch(list, item, start, mid - 1);

            return mid;
        }

        return -1;
    }
}
