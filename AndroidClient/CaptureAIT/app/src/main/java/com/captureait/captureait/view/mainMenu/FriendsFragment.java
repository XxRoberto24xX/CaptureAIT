package com.captureait.captureait.view.mainMenu;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.captureait.captureait.model.FriendCustomAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/**
 * Shows a list of friends and allows the user to manage friend requests and interactions.
 */
public class FriendsFragment extends Fragment implements ActiveViewListener {

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private Button btnFriendRequest;
    private RecyclerView recyclerView;
    private TextInputEditText labelName;

    /**
     * Default constructor for FriendsFragment.
     */
    public FriendsFragment() {
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
        // Take the references of the visual elements
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        labelName = view.findViewById(R.id.labelName);
        btnFriendRequest = view.findViewById(R.id.btnFriendRequest);
        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // This should be done by the userInterfaceThread as it has to change it
        getActivity().runOnUiThread(()->{
            setList(controller.getFriendsList());
        });

        // Set the button behavior
        btnFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save the userName the
                String invitedUserName = labelName.getText().toString();

                // Validate the value
                if(invitedUserName.isEmpty() || invitedUserName.length() < 5 ||invitedUserName.length() > 15){
                    labelName.setError("El nombre de usuario debe tener almenos 5 caracteres y maximo 15");
                }else{
                    controller.sendFriendRequest(invitedUserName, new CentralControllerCallBack() {
                        @Override
                        public void onSuccess(String info) {
                            // Inform the user about the request has been sent
                            alertDialog("Friend Request", "Invitación de Amistad enviada", "Aceptar");
                        }

                        @Override
                        public void onError(String error) {
                            switch (error){
                                case "NO_PLAYER_FOUND":
                                    labelName.setError("No existe ningun jugador con ese nombre");
                                    break;
                                case "OWN_NAME":
                                    labelName.setError("No te puedes enviar una invitación de amistad");
                                    break;
                                case "ALREADY_FRIEND":
                                    labelName.setError("Ya sois amigos");
                                    break;
                                default:
                                    labelName.setError("Error al conectar con la base de datos");
                                    break;
                            }
                        }
                    });
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    /**
     * Sets the list of friends in the RecyclerView.
     *
     * @param friendsArrayList The ArrayList containing the list of friends to display.
     */
    public void setList(ArrayList<String> friendsArrayList){
        // Show the friends in the screen
        FriendCustomAdapter customAdapter = new FriendCustomAdapter(getContext(), friendsArrayList, new FriendCustomAdapter.FriendInteractionListener() {
            @Override
            public void onFriendDelete(String deleteUserName) {
                controller.deleteFriend(deleteUserName, new CentralControllerCallBack() {
                    @Override
                    public void onSuccess(String info) {

                    }

                    @Override
                    public void onError(String error) {
                        // Inform the user about the error
                        alertDialog("Delete Friend", "Error al borrar al amigo", "Aceptar");

                        // Put the error in the log
                        Log.e("Friend Fragment", "DATABASE_ERROR");
                    }
                });
            }
        });
        recyclerView.setAdapter(customAdapter);
    }

    /**
     * Shows a dialog to inform the user.
     *
     * @param title   The title of the dialog.
     * @param message The message to display in the dialog.
     * @param button  The text for the positive button of the dialog.
     */
    public void alertDialog(String title, String message, String button){
        // Building the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Create the content of the AlertDialog
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Closes the dialog
                        dialogInterface.cancel();
                    }
                })
                .create()
                .show();
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
        if(updatedElement.equals("list_friends_changed")){
            // This should be done by the userInterfaceThread as it has to change it
            getActivity().runOnUiThread(()->{
                setList(controller.getFriendsList());
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
