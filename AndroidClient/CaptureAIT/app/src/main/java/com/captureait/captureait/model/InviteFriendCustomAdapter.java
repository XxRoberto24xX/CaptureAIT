package com.captureait.captureait.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.CentralController;

import java.util.ArrayList;

/**
 * Shows the list of friends given and allows the user to send them an invitation to the game the user is waiting for.
 */
public class InviteFriendCustomAdapter extends RecyclerView.Adapter<InviteFriendHolder> {

    /** The context in which the adapter is used */
    public Context context;

    /** The list of friends to be displayed */
    private ArrayList<String> friendsArrayList;

    /** The listener for interaction events */
    private InviteFriendInteractionListener listener;

    /**
     * Listener to notify the interactions of the displayed elements.
     */
    public interface InviteFriendInteractionListener {
        /**
         * Called when the user wants to invite a friend from the list.
         *
         * @param invitedUserName The name of the invited user.
         */
        void onInviteFriend(String invitedUserName);
    }

    /**
     * Constructs an InviteFriendCustomAdapter.
     *
     * @param context The context in which the adapter is used.
     * @param friendsArrayList The list of friends to be displayed.
     * @param listener The listener for interaction events.
     */
    public InviteFriendCustomAdapter(Context context, ArrayList<String> friendsArrayList, InviteFriendInteractionListener listener) {
        this.context = context;
        this.friendsArrayList = friendsArrayList;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new InviteFriendHolder to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new InviteFriendHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public InviteFriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We need to return a new CustomHolder to use
        return new InviteFriendHolder(LayoutInflater.from(context).inflate(R.layout.invitefriend_item, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The InviteFriendHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull InviteFriendHolder holder, int position) {
        // Put the information in the elements
        holder.txtName.setText(friendsArrayList.get(position));

        // Set the button actions
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onInviteFriend(friendsArrayList.get(holder.getAdapterPosition()));
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
        return friendsArrayList.size();
    }
}
