package com.captureait.captureait.model;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.captureait.captureait.R;

import java.util.ArrayList;

/**
 * Custom adapter class used to present the list of current games in the RecyclerView
 * and enable interaction with them.
 */
public class GameCustomAdapter extends RecyclerView.Adapter<GameCustomHolder> {

    /** The context of the application */
    private Context context;

    /** The list of games to display */
    private ArrayList<Game> gamesArrayList;

    /** Listener for game interaction events */
    private GameInteractionListener listener;

    /**
     * Listener interface for game interaction events.
     */
    public interface GameInteractionListener {
        /**
         * Called when a game item is clicked.
         *
         * @param code The code associated with the game.
         * @param finish Indicates if the game is finished.
         */
        void onGameClick(String code, boolean finish);
    }

    /**
     * Constructs a new GameCustomAdapter.
     *
     * @param context The context of the application.
     * @param gamesArrayList The list of games to display.
     * @param listener The listener for game interaction events.
     */
    public GameCustomAdapter(Context context, ArrayList<Game> gamesArrayList, GameInteractionListener listener) {
        this.context = context;
        this.gamesArrayList = gamesArrayList;
        this.listener = listener;
    }

    /**
     * Creates a new GameCustomHolder to represent an item view.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The type of the new View.
     * @return A new GameCustomHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public GameCustomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GameCustomHolder(LayoutInflater.from(context).inflate(R.layout.card_item, parent, false), this);
    }

    /**
     * Binds data to the views inside the GameCustomHolder.
     *
     * @param holder The GameCustomHolder where data should be bound.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull GameCustomHolder holder, int position) {
        holder.txtCode.setText(gamesArrayList.get(position).getCode());

        holder.txtElements1.setText(gamesArrayList.get(position).getDetections()[0]);
        holder.txtElements2.setText(gamesArrayList.get(position).getDetections()[1]);
        holder.txtElements3.setText(gamesArrayList.get(position).getDetections()[2]);
        holder.txtElements4.setText(gamesArrayList.get(position).getDetections()[3]);

        for (int i = 0; i < gamesArrayList.get(position).getPlayersArrayList().size(); i++) {
            if (i == 0) {
                addPlayer(R.drawable.numero1, gamesArrayList.get(position).getPlayersArrayList().get(i).getName(), gamesArrayList.get(position).getPlayersArrayList().get(i).getPoints(), holder.players_container);
            } else if (i == 1) {
                addPlayer(R.drawable.numero2, gamesArrayList.get(position).getPlayersArrayList().get(i).getName(), gamesArrayList.get(position).getPlayersArrayList().get(i).getPoints(), holder.players_container);
            } else if (i == 2) {
                addPlayer(R.drawable.numero3, gamesArrayList.get(position).getPlayersArrayList().get(i).getName(), gamesArrayList.get(position).getPlayersArrayList().get(i).getPoints(), holder.players_container);
            } else {
                addPlayer(R.drawable.numero4, gamesArrayList.get(position).getPlayersArrayList().get(i).getName(), gamesArrayList.get(position).getPlayersArrayList().get(i).getPoints(), holder.players_container);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onGameClick(gamesArrayList.get(holder.getAdapterPosition()).getCode(), gamesArrayList.get(holder.getAdapterPosition()).isFinish());
            }
        });
    }

    /**
     * Adds a player view to the given playersContainer.
     *
     * @param position The position image to display.
     * @param userName The name of the user.
     * @param points The points scored by the user.
     * @param playersContainer The container to add the player view.
     */
    public void addPlayer(int position, String userName, String points, LinearLayout playersContainer) {
        LinearLayout playerLayout = new LinearLayout(context);
        playerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        playerLayout.setOrientation(LinearLayout.HORIZONTAL);
        playerLayout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dpToPx(30), dpToPx(30));
        imageParams.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        imageView.setLayoutParams(imageParams);
        imageView.setImageResource(position);

        TextView textView = new TextView(context);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textView.setLayoutParams(textParams);
        textView.setGravity(Gravity.CENTER);
        textView.setText(userName + ":  " + points + " pts");
        textView.setTextColor(context.getResources().getColor(R.color.white));

        playerLayout.addView(imageView);
        playerLayout.addView(textView);

        playersContainer.addView(playerLayout);
    }

    /**
     * Converts dp to pixels based on the device density.
     *
     * @param dp The value in dp to convert.
     * @return The corresponding value in pixels.
     */
    public int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return gamesArrayList.size();
    }
}
