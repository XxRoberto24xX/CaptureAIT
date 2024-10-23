package com.captureait.captureait.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;

/**
 * Custom ViewHolder for displaying a message in a RecyclerView.
 */
public class MessageCustomHolder extends RecyclerView.ViewHolder {

    /** Visual element for accepting a message. */
    public ImageView btnAccept;

    /** Visual element for deleting a message. */
    public ImageView btnDelete;

    /** Text view displaying the title of the message. */
    public TextView txtTitle;

    /** Text view displaying additional information about the message. */
    public TextView txtInfo;

    /**
     * Constructs a new MessageCustomHolder.
     *
     * @param itemView The view containing the visual elements for a message item.
     */
    public MessageCustomHolder(@NonNull View itemView) {
        super(itemView);

        // Take the visual element references from the message_item layout
        btnAccept = itemView.findViewById(R.id.btnAccept);
        btnDelete = itemView.findViewById(R.id.btnDelete);
        txtTitle = itemView.findViewById(R.id.txtTitle);
        txtInfo = itemView.findViewById(R.id.txtInfo);
    }
}
