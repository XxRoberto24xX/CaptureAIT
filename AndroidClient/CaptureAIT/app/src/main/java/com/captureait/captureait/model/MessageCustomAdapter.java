package com.captureait.captureait.model;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;

import java.util.ArrayList;

/**
 * Custom adapter for displaying messages in a RecyclerView.
 * Shows all the messages to the user and allows him to manage them.
 */
public class MessageCustomAdapter extends RecyclerView.Adapter<MessageCustomHolder> {

    /** The context in which the adapter is operating. */
    private Context context;

    /** The list of messages to display. */
    private ArrayList<Message> messageArrayList;

    /** Listener interface to handle interactions with the displayed messages. */
    private MessageInteractionListener listener;


    /**
     * Listener interface to notify interactions with the displayed messages.
     */
    public interface MessageInteractionListener {

        /**
         * Called when the user clicks an element of the list to delete.
         *
         * @param type The type of the message.
         * @param userName The name associated with the message.
         * @param code The code of the message.
         */
        void onMessageDelete(String type, String userName, String code);

        /**
         * Called when the user accepts a friend request.
         *
         * @param type The type of the message.
         * @param userName The name associated with the message.
         * @param code The code of the message.
         */
        void onFriendRequestAccepted(String type, String userName, String code);

        /**
         * Called when the user accepts a game invitation.
         *
         * @param type The type of the message.
         * @param userName The name associated with the message.
         * @param code The code of the message.
         */
        void onGameInvitationAccepted(String type, String userName, String code);
    }

    /**
     * Constructs a new MessageCustomAdapter.
     *
     * @param context The context in which the adapter is operating.
     * @param messageArrayList The list of messages to display.
     * @param listener The listener to handle interactions with the displayed messages.
     */
    public MessageCustomAdapter(Context context, ArrayList<Message> messageArrayList, MessageInteractionListener listener){
        this.context = context;
        this.messageArrayList = messageArrayList;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new MessageCustomHolder to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new MessageCustomHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public MessageCustomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We need to return a new CustomHolder to use
        return new MessageCustomHolder(LayoutInflater.from(context).inflate(R.layout.message_item, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The MessageCustomHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MessageCustomHolder holder, int position) {
        // When we have each element we change the content of the card_item
        if(messageArrayList.get(position).getType().equals("msgFr")){
            holder.txtTitle.setText("Solicitud de Amistad");
            holder.txtInfo.setText("El usuario " + messageArrayList.get(position).getName() + " te ha enviado una solicitud de amistad");
        }else if(messageArrayList.get(position).getType().equals("msgInv")){
            holder.txtTitle.setText("Invitación a Partida");
            holder.txtInfo.setText("El usuario " + messageArrayList.get(position).getName() + " te ha enviado una invitacion a la partida con codigo: " + messageArrayList.get(position).getCode());
        }

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete the message in the model
                listener.onMessageDelete(messageArrayList.get(holder.getAdapterPosition()).getType(), messageArrayList.get(holder.getAdapterPosition()).getName(),messageArrayList.get(holder.getAdapterPosition()).getCode());
            }
        });

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // See what type of message it is
                if(messageArrayList.get(holder.getAdapterPosition()).getType().equals("msgFr")){
                    listener.onFriendRequestAccepted(messageArrayList.get(holder.getAdapterPosition()).getType(), messageArrayList.get(holder.getAdapterPosition()).getName(),messageArrayList.get(holder.getAdapterPosition()).getCode());
                }else if(messageArrayList.get(holder.getAdapterPosition()).getType().equals("msgInv")){
                    listener.onGameInvitationAccepted(messageArrayList.get(holder.getAdapterPosition()).getType(), messageArrayList.get(holder.getAdapterPosition()).getName(),messageArrayList.get(holder.getAdapterPosition()).getCode());
                }
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        // Return the list elements
        return messageArrayList.size();
    }
}
