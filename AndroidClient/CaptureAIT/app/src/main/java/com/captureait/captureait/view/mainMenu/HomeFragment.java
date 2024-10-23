package com.captureait.captureait.view.mainMenu;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.model.Game;
import com.captureait.captureait.model.GameCustomAdapter;
import com.captureait.captureait.view.game.FinishActivity;
import com.captureait.captureait.view.game.GameActivity;
import com.captureait.captureait.view.game.NewGameActivity;

import java.util.ArrayList;

/**
 * Shows the list of all the games the user is active in and allows him to enter them again as well as showing their information.
 */
public class HomeFragment extends Fragment implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private RecyclerView recyclerView;
    private TextView textNoGames;

    /**
     * Default constructor for HomeFragment.
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is created.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Register the activity in the centralController
        controller.addActiveViewListener(this);
    }

    /**
     * Called to create the view hierarchy associated with the fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views in the fragment.
     * @param container          This is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Take the view reference
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ArrayList<Game> temporalGamesList = controller.getGamesList();
        textNoGames = view.findViewById(R.id.textNoGames);
        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // This should be done by the userInterfaceThread as it has to change it
        getActivity().runOnUiThread(()->{
            setList(temporalGamesList);
        });

        // Inflate the layout for this fragment
        return view;
    }

    /**
     * Sets the list of games in the RecyclerView.
     *
     * @param gameArrayList The ArrayList containing the list of games to display.
     */
    public void setList(ArrayList<Game> gameArrayList){
        // See if the list is empty
        if(gameArrayList.isEmpty()){
            textNoGames.setText("No hay Ninguna Partida Activa");
        }else{
            textNoGames.setText("");
        }

        // Show the games in the screen
        GameCustomAdapter customAdapter = new GameCustomAdapter(getContext(), gameArrayList, new GameCustomAdapter.GameInteractionListener() {
            @Override
            public void onGameClick(String code, boolean finish) {
                if(finish){
                    Intent finishActivityIntent  = new Intent(getContext(), FinishActivity.class);
                    finishActivityIntent.putExtra("code", code);
                    startActivity(finishActivityIntent);
                }else{
                    Intent gameActivityIntent  = new Intent(getContext(), GameActivity.class);
                    gameActivityIntent.putExtra("code", code);
                    startActivity(gameActivityIntent);
                }
            }
        });
        recyclerView.setAdapter(customAdapter);
    }

    /**
     * Called when the initialization has finished and you are a listener of the event. This method is implemented from the ActiveViewListener interface.
     *
     * @param info Additional information related to the initialization.
     */
    @Override
    public void onInitializationFinished(String info) {

    }

    /**
     * Called when there was an update in the model and you are a listener of the event. This method is implemented from the ActiveViewListener interface.
     *
     * @param updatedElement The element in the model that was updated.
     */
    @Override
    public void onUpdate(String updatedElement) {
        if(updatedElement.equals("list_games_changed")){
            // This should be done by the userInterfaceThread as it has to change it
            getActivity().runOnUiThread(()->{
                setList(controller.getGamesList());
            });
        }
    }

    /**
     * Called when the view is been destroyed, removes the listener from the controller.
     */
    @Override
    public void onDestroy() {
        controller.removeActiveViewListener(this);
        super.onDestroy();
    }

}

