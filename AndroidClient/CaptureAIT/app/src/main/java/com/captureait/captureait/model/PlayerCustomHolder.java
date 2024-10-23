package com.captureait.captureait.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;

/**
 * Custom ViewHolder for displaying player data in a RecyclerView.
 */
public class PlayerCustomHolder extends RecyclerView.ViewHolder {

    /** Visual element for displaying player position. */
    public ImageView position;

    /** Visual element for displaying player name. */
    public TextView name;

    /** Visual element for displaying player points. */
    public TextView points;

    /**
     * Constructor to initialize the PlayerCustomHolder.
     *
     * @param itemView The view containing the visual elements for a player item.
     */
    public PlayerCustomHolder(@NonNull View itemView) {
        // Call the superclass constructor
        super(itemView);

        // Obtain references to visual elements from the card_item layout
        position = itemView.findViewById(R.id.position);
        name = itemView.findViewById(R.id.nombre);
        points = itemView.findViewById(R.id.puntuacion);
    }
}
