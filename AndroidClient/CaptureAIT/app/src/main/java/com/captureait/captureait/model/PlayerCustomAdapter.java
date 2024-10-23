package com.captureait.captureait.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;

import java.util.ArrayList;

/**
 * Custom adapter for displaying player data in a RecyclerView.
 */
public class PlayerCustomAdapter extends RecyclerView.Adapter<PlayerCustomHolder> {

    /** The context in which the adapter is operating. */
    public Context context;

    /** The list of players to display. */
    public ArrayList<Player> playerArrayList = new ArrayList<>();

    /**
     * Constructs a new PlayerCustomAdapter.
     *
     * @param context The context in which the adapter is operating.
     * @param playerArrayList The list of players to display.
     */
    public PlayerCustomAdapter(Context context, ArrayList<Player> playerArrayList) {
        this.context = context;
        this.playerArrayList = playerArrayList;
    }

    /**
     * Called when RecyclerView needs a new PlayerCustomHolder to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new PlayerCustomHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public PlayerCustomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We need to return a new CustomHolder to use
        return new PlayerCustomHolder(LayoutInflater.from(context).inflate(R.layout.player_item, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The PlayerCustomHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PlayerCustomHolder holder, int position) {
        // When we have each element we change the content of the card_item
        switch (position) {
            case 0:
                holder.position.setImageResource(R.drawable.numero1);
                break;
            case 1:
                holder.position.setImageResource(R.drawable.numero2);
                break;
            case 2:
                holder.position.setImageResource(R.drawable.numero3);
                break;
            case 3:
                holder.position.setImageResource(R.drawable.numero4);
                break;
        }
        holder.name.setText(playerArrayList.get(position).getName());
        holder.points.setText(playerArrayList.get(position).getPoints());
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        // Return the list elements
        return playerArrayList.size();
    }
}
