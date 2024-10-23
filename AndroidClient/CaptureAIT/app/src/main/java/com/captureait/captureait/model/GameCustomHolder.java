package com.captureait.captureait.model;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;

/**
 * Custom holder class used to present the list of current games in the RecyclerView.
 */
public class GameCustomHolder extends RecyclerView.ViewHolder {

    /** Different visual elements for displaying the game information. */
    public TextView txtCode, txtElements1, txtElements2, txtElements3, txtElements4;

    /** Visual elements which makes as container to show the players information. */
    public LinearLayout players_container;

    /* METHODS */

    /**
     * Constructs a new GameCustomHolder object.
     *
     * @param itemView The View object containing the layout for the item.
     * @param adapter The adapter associated with the holder.
     */
    public GameCustomHolder(@NonNull View itemView, GameCustomAdapter adapter) {
        // Call the superclass creation method
        super(itemView);

        // Take the visual element references from the card_item layout
        txtCode = itemView.findViewById(R.id.txtCode);
        txtElements1 = itemView.findViewById(R.id.txtElements1);
        txtElements2 = itemView.findViewById(R.id.txtElements2);
        txtElements3 = itemView.findViewById(R.id.txtElements3);
        txtElements4 = itemView.findViewById(R.id.txtElements4);
        players_container = itemView.findViewById(R.id.players_container);
    }
}
