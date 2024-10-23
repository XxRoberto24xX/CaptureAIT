package com.captureait.captureait.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;

import java.util.ArrayList;

/**
 * Shows the information of the friend list passed as argument
 */
public class FriendCustomAdapter extends RecyclerView.Adapter<FriendCustomHolder> {

    /** The context in which the adapter is used */
    public Context context;

    /** The list of friends to be displayed */
    private ArrayList<String> friendsArrayList;

    /** The listener for interaction events */
    private FriendInteractionListener listener;


    /**
     * Listener to notify the interactions of the displayed elements
     */
    public interface FriendInteractionListener {
        /**
         * Called when the user wants to delete a user from the list
         */
        void onFriendDelete(String deleteUserName);
    }

    /**
     * Constructs an InviteFriendCustomAdapter.
     *
     * @param context The context in which the adapter is used.
     * @param friendsArrayList The list of friends to be displayed.
     * @param listener The listener for interaction events.
     */
    public FriendCustomAdapter(Context context, ArrayList<String> friendsArrayList, FriendInteractionListener listener) {
        this.context = context;
        this.friendsArrayList = friendsArrayList;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new FriendCustomHolder to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new FriendCustomHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public FriendCustomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We need to return a new CustomHolder to use
        return new FriendCustomHolder(LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The FriendCustomHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull FriendCustomHolder holder, int position) {
        // Put the information in the elements
        holder.txtName.setText(friendsArrayList.get(position));

        // Set the button actions
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onFriendDelete(friendsArrayList.get(holder.getAdapterPosition()));
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
        return friendsArrayList.size();
    }
}
