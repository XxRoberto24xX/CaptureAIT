package com.captureait.captureait.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;

/**
 * ViewHolder class for the invite friend item in a RecyclerView.
 */
public class InviteFriendHolder extends RecyclerView.ViewHolder {

    /** The TextView for displaying the friend's name */
    public TextView txtName;

    /** The ImageView for the add button */
    public ImageView btnAdd;

    /**
     * Constructs an InviteFriendHolder.
     *
     * @param itemView The view of the item.
     */
    public InviteFriendHolder(@NonNull View itemView) {
        super(itemView);

        // Take the visual element references from the card_item
        txtName = itemView.findViewById(R.id.txtName);
        btnAdd = itemView.findViewById(R.id.btnAdd);
    }
}
