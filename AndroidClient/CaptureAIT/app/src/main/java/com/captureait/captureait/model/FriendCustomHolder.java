package com.captureait.captureait.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;

/**
 * ViewHolder class for friend item in a RecyclerView.
 */
public class FriendCustomHolder extends RecyclerView.ViewHolder {

    /** Visual Elements */
    public TextView txtName;
    public ImageView btnDelete;

    /**
     * Constructs a FriendCustomHolder.
     *
     * @param itemView The view of the item.
     */
    public FriendCustomHolder(@NonNull View itemView) {
        super(itemView);

        // Take the visual element references from the card_item
        txtName = itemView.findViewById(R.id.txtName);
        btnDelete = itemView.findViewById(R.id.btnDelete);
    }
}
