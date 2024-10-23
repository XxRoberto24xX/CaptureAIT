package com.captureait.captureait.controller;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import com.captureait.captureait.dao.GamesDAO;
import com.captureait.captureait.dao.MessagesDAO;
import com.captureait.captureait.dao.StatsCallback;
import com.captureait.captureait.dao.StatsDAO;
import com.captureait.captureait.dao.UserCallback;
import com.captureait.captureait.dao.UserDAO;
import com.captureait.captureait.model.Game;
import com.captureait.captureait.model.Message;
import com.captureait.captureait.model.Player;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CentralController {

    /** Saves the singleton instance of the central controller. */
    private static CentralController instance;

    /** Callback used to communicate with the views when there is an update in the data or the singleton instance is ready. */
    private ActiveViewListener finishListener;
    private ArrayList<ActiveViewListener> listViewListeners;

    /** SocketIO instance connection. */
    private Socket mSocket;

    /** Model Management DAO's. */
    private UserDAO userDAO;
    private GamesDAO gamesDAO;
    private MessagesDAO messagesDAO;
    private StatsDAO statsDAO;

    /** Detection Temporal Save. */
    private ArrayList<String> detectionsList, positionsList;

    /**
     * Returns the single instance of CentralController.
     *
     * @return The single instance of CentralController
     */
    public static synchronized CentralController getInstance() {
        if(instance == null) {
            instance = new CentralController();
        }
        return instance;
    }

    /**
     * Constructs the singleton CentralController instance
     */
    private CentralController(){
        try {
            //this.mSocket = IO.socket("http://192.168.1.17:8001");    //SALAMANCA
            //this.mSocket = IO.socket("http://192.168.1.40:8001");    //PUEBLO
            this.mSocket = IO.socket("http://192.168.70.83:8001");    //FACULTAD
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.listViewListeners = new ArrayList<>();

        /* Set the server event listeners */
        this.setServerEventsListeners();
    }

    /**
     * Initializes the CentralController, setting up necessary dependencies and listeners.
     */
    public void initializeCentralController(){
        this.userDAO = new UserDAO(new UserCallback() {
            @Override
            public void onSuccess() {
                finishListener.onInitializationFinished("CORRECT");
            }

            @Override
            public void onError(String error) {
                finishListener.onInitializationFinished("ERROR");
                Log.e("UserDAO", "Error loading the user");
            }
        });
    }

    /**
     * Sets the listener for when initialization finishes.
     *
     * @param listener The listener to be notified when initialization finishes
     */
    public void listenInitializationFinished(ActiveViewListener listener){
        finishListener = listener;
    }

    /**
     * Adds an active view listener to the list of listeners.
     *
     * @param listener The listener to be added
     */
    public void addActiveViewListener(ActiveViewListener listener){
        listViewListeners.add(listener);
    }

    /**
     * Removes an active view listener from the list of listeners.
     *
     * @param listener The listener to be removed
     */
    public void removeActiveViewListener(ActiveViewListener listener){
        listViewListeners.remove(listener);
    }

    /**
     * Sets the server events the app needs to listen to change the model and update the view
     */
    public void setServerEventsListeners(){
        // Received when a player you share a room with has changed his name
        mSocket.on("cooplayer_name_changed", args -> {
            try {
                // Extract the information from the json
                JSONObject jsonObject = new JSONObject(args[0].toString());
                String oldUserName = jsonObject.getString("oldName");
                String newUserName = jsonObject.getString("newName");

                // Change the model
                ArrayList<String> codesChanged = gamesDAO.nameHasChange(newUserName, oldUserName);

                // Tell the view to update
                updateView("list_games_changed");
                for(String code : codesChanged){
                    updateView(code);
                }
            } catch (JSONException e) {
                // Log error for exception handling
                Log.e("ServerEvent", "cooplayer_name_changed");
                throw new RuntimeException(e);
            }
        });

        // Received when a friend changes his name
        mSocket.on("friend_name_changed", args -> {
            try{
                // Extract the information from the json
                JSONObject jsonObject = new JSONObject(args[0].toString());
                String oldUserName = jsonObject.getString("oldName");
                String newUserName = jsonObject.getString("newName");

                // Change the model
                userDAO.changeFriendUserName(oldUserName, newUserName, new UserCallback() {
                    @Override
                    public void onSuccess() {
                        // Tell the view to update
                        updateView("list_friends_changed");
                    }

                    @Override
                    public void onError(String error) {
                        // Set the error in the log
                        Log.e("ServerEvent", "friend_name_changed");
                    }
                });
            } catch (JSONException e) {
                // Log error for exception handling
                Log.e("ServerEvent", "friend_name_changed");
                throw new RuntimeException(e);
            }
        });

        // Received when a player you have a game with scores points
        mSocket.on("update", args -> {
            Log.i("afdasfa", "recibi una actualizacion");
            try{
                // Extract the information from the json
                JSONObject jsonObject = new JSONObject(args[0].toString());
                String code = jsonObject.getString("code");
                String userName = jsonObject.getString("user");
                String points = jsonObject.getString("points");
                boolean finish = jsonObject.getBoolean("finish");
                long time = jsonObject.getLong("time");

                // Change the model
                gamesDAO.updateGame(code, userName, points, finish, time);

                // Update the stats if the game has finished
                if(finish){
                    // Take the player information
                    Player player = new Player("", "");
                    boolean win = false;
                    for(int i=0; i<gamesDAO.getGame(code).getPlayersArrayList().size(); i++){
                        if(gamesDAO.getGame(code).getPlayersArrayList().get(i).getName().equals(userDAO.getUserName())){
                            player = gamesDAO.getGame(code).getPlayersArrayList().get(i);
                            if(i == 0){
                                win = true;
                            }
                            break;
                        }
                    }

                    // Use that information to update the stats
                    statsDAO.updateStats(Integer.parseInt(player.getPoints()), time, win, new StatsCallback() {
                        @Override
                        public void onSuccess() {
                            // Tell the view to update
                            updateView("stats_changed");
                            updateView("list_games_changed");
                            updateView(code);
                        }

                        @Override
                        public void onError(String error) {
                            // Mark the error in the log
                            Log.e("stats Update error", "enamble tu update the player stats");
                        }
                    });
                }else{
                    // If not just continue
                    // Tell the view to update
                    updateView("stats_changed");
                    updateView("list_games_changed");
                    updateView(code);
                }

            } catch (JSONException e) {
                // Log error for exception handling
                Log.e("ServerEvent", "update");
                throw new RuntimeException(e);
            }
        });

        // Received when a player in a game decides to leave it
        mSocket.on("abandon", args -> {
            try{
                // Extract the information from the json
                JSONObject jsonObject = new JSONObject(args[0].toString());
                String code = jsonObject.getString("code");
                String userNameLeft = jsonObject.getString("nameLeft");
                boolean finish = jsonObject.getBoolean("finish");
                long time = jsonObject.getLong("time");

                // Change the model
                gamesDAO.deletePlayer(code, userNameLeft, finish, time);

                // Update the stats if the game has finished
                if(finish){
                    // Take the player information
                    Player player = new Player("", "");
                    boolean win = false;
                    for(int i=0; i<gamesDAO.getGame(code).getPlayersArrayList().size(); i++){
                        if(gamesDAO.getGame(code).getPlayersArrayList().get(i).getName().equals(userDAO.getUserName())){
                            player = gamesDAO.getGame(code).getPlayersArrayList().get(i);
                            if(i == 0){
                                win = true;
                            }
                            break;
                        }
                    }

                    // Use that information to update the stats
                    statsDAO.updateStats(Integer.parseInt(player.getPoints()), time, win, new StatsCallback() {
                        @Override
                        public void onSuccess() {
                            // Tell the view to update
                            updateView("stats_changed");
                            updateView("list_games_changed");
                            updateView(code);
                        }

                        @Override
                        public void onError(String error) {
                            // Mark the error in the log
                            Log.e("stats Update error", "enamble tu update the player stats");
                        }
                    });
                }else {
                    // If not just continue
                    // Tell the view to update
                    updateView("stats_changed");
                    updateView("list_games_changed");
                    updateView(code);
                }
            } catch (JSONException e) {
                // Log error for exception handling
                Log.e("ServerEvent", "finish");
                throw new RuntimeException(e);
            }
        });

        // Received when a player deletes you form the friends list
        mSocket.on("delete_friend", args -> {
            // Extract the information given
            String delUserName = args[0].toString();

            // Change the model
            userDAO.removeFriend(delUserName, new UserCallback() {
                @Override
                public void onSuccess() {
                    // Tell the view to update
                    updateView("list_friends_changed");
                }

                @Override
                public void onError(String error) {
                    // Set the error in the log
                    Log.e("ServerEvent", "delete_friend");
                }
            });
        });

        // Received when a player accepts one of your friend requests
        mSocket.on("accepted_friend_request", args -> {
            // Extract the information given
            String addUserName = args[0].toString();

            // Change the model
            userDAO.addFriend(addUserName, new UserCallback() {
                @Override
                public void onSuccess() {
                    // Tell the view to update
                    updateView("list_friends_changed");
                }

                @Override
                public void onError(String error) {
                    // Set the error in the log
                    Log.e("ServerEvent", "accepted_friend_request");
                }
            });
        });

        // Received when a game you were expecting to start starts
        mSocket.on("ready", args -> {
            try{
                // Extract the information from the json
                JSONObject jsonObject = new JSONObject(args[0].toString());
                String code = jsonObject.getString("code");
                JSONArray names = jsonObject.getJSONArray("names");             //has a matrix with the name of the players
                JSONArray detections = jsonObject.getJSONArray("detections");   //has all the detections of a game

                // Modify the model
                gamesDAO.newBlankGame(code, names, detections);

                // Tell the view to update
                updateView("list_games_changed");
                updateView(code);
            }catch (JSONException e) {
                // Log error for exception handling
                Log.e("ServerEvent", "join_new");
                throw new RuntimeException(e);
            }
        });

        // Received when a new message arrives to the mailBox
        mSocket.on("new_message", args -> {
            try{
                // Extract the information from the json
                JSONObject jsonObject = new JSONObject(args[0].toString());
                String type = jsonObject.getString("type");
                String userName = jsonObject.getString("name");
                String code = jsonObject.getString("code");

                // Modify the model
                messagesDAO.addMessage(type, userName, code);

                // Tell the view to update
                updateView("list_messages_changed");
                updateView(code);
            }catch (JSONException e) {
                // Log error for exception handling
                Log.e("ServerEvent", "new_message");
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Sets up the socket connection and handles connection timeouts.
     *
     * @param callback The callback interface to handle asynchronous responses
     */
    public void socketConnection(CentralControllerCallBack callback){
        Handler handler = new Handler();
        Runnable timeoutRunnable = () -> {
            // Stop listening the connexion event
            mSocket.off(Socket.EVENT_CONNECT);

            // Send a disconnect in case the server had received the connexion out of time
            mSocket.disconnect();

            // Inform the view
            callback.onError("CONNECTION_TIMEOUT");
        };

        // Start the connexion timeout timer
        handler.postDelayed(timeoutRunnable, 10000);

        // Set the listener to the server connection event
        mSocket.on(Socket.EVENT_CONNECT, args -> {
            // As we are already connected stop listening the connection event
            mSocket.off(Socket.EVENT_CONNECT);

            // Cancel the timer
            handler.removeCallbacks(timeoutRunnable);

            // see if there is any user already authenticated and gather his information
            getUserState(callback);
        });

        // Try to connect to SocketIO server
        mSocket.connect();
    }

    /**
     * Takes all the information about the user and gives his actual complete state after gathering all his information
     *
     * @param callBack The callback interface to handle asynchronous responses
     */
    public void getUserState(CentralControllerCallBack callBack){
        // See if there is any available user
        if(userDAO.isValid()){
            // See if he has username
            String userName = userDAO.getUserName();
            if(!userName.isEmpty()){
                gatherUserInfo(userName, callBack);
            }else{
                callBack.onSuccess("NO_NAME");
            }
        }else{
            callBack.onSuccess("NO_USER_OR_VALIDATION");
        }
    }


    /**
     * Connects the user to the server personal room and starts gathering all the user's info to let him enter the app.
     * First, returns the user statistics and then listens for a 'join_server' event from the socket.
     * Upon receiving the event, processes the received JSON data to initialize game, message, and friend information.
     * Notifies the callback with 'USER_READY' upon successful processing of user information.
     *
     * @param userName The username of the user to gather information for.
     * @param callBack The callback to notify upon successful completion or error.
     */
    private void gatherUserInfo(String userName, CentralControllerCallBack callBack){
        /* First take the user statistics */
        this.statsDAO = new StatsDAO(userDAO.getUserId() ,new StatsCallback() {
            @Override
            public void onSuccess() {
                /* Set a response listener */
                mSocket.on("join_server", args -> {
                    /* Stop listening the event */
                    mSocket.off("join_server");

                    /* Take the given information */
                    try{
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        JSONArray codesGiven = jsonObject.getJSONArray("codes");        //gives the codes back to make error solving (if there was a code in database which should have been deleted)
                        JSONArray names = jsonObject.getJSONArray("names");             //has a matrix with the name of the players in each game
                        JSONArray points = jsonObject.getJSONArray("points");           //has a matrix with the points of each player in each game
                        JSONArray finishes = jsonObject.getJSONArray("finishes");       //has the state of the games
                        JSONArray detections = jsonObject.getJSONArray("detections");   //has all the detections of all the game

                        JSONArray delFr = jsonObject.getJSONArray("delFr");             //list with friends who deleted you when you where out
                        JSONArray addFr = jsonObject.getJSONArray("addFr");             //list of players who accepted your friend request while you where out

                        JSONArray mailBox = jsonObject.getJSONArray("mailBox");       //list with all the messages the user has

                        /* Create the games DAO using the information given */
                        gamesDAO = new GamesDAO(codesGiven, names, points, finishes, detections);

                        /* Create the messagesDAO */
                        messagesDAO = new MessagesDAO(mailBox);

                        /* Make the changes needed in the friends list */
                        userDAO.friendsConnectionModification(delFr, addFr, new UserCallback() {
                            @Override
                            public void onSuccess() {
                                callBack.onSuccess("USER_READY");
                            }

                            @Override
                            public void onError(String error) {
                                callBack.onError(error);
                            }
                        });

                    }catch (JSONException e){
                        callBack.onError("JSON_ERROR");
                    }

                });

                /* Send the petition to the sever */
                mSocket.emit("join_server", userName);
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Takes the Google account given by the user to authenticate him.
     * Initiates Google authentication through UserDAO and triggers getUserState upon successful authentication.
     *
     * @param accountTask The task containing GoogleSignInAccount.
     * @param callBack    The callback to notify upon successful authentication or error.
     */
    public void googleAuthentication(Task<GoogleSignInAccount> accountTask, CentralControllerCallBack callBack){
        userDAO.googleAuthentication(accountTask, new UserCallback() {
            @Override
            public void onSuccess() {
                getUserState(callBack);
            }

            @Override
            public void onError(String error) {
                callBack.onError("AUTHENTICATION_FAILED");
            }
        });
    }

    /**
     * Takes the credentials given by the user to authenticate him.
     * Initiates password authentication through UserDAO and triggers getUserState upon successful authentication.
     *
     * @param email    The email address provided by the user.
     * @param password The password provided by the user.
     * @param callBack The callback to notify upon successful authentication or error.
     */
    public void passwordAuthentication(String email, String password, CentralControllerCallBack callBack){
        userDAO.passwordAuthentication(email, password, new UserCallback() {
            @Override
            public void onSuccess() {
                getUserState(callBack);
            }

            @Override
            public void onError(String error) {
                if(error.equals("WRONG_CREDENTIALS")){
                    callBack.onError(error);
                }
                callBack.onError("AUTHENTICATION_FAILED");
            }
        });
    }

    /**
     * Registers a new user with the provided email, password, and username.
     * Initiates registration through UserDAO and notifies the callback upon success or error.
     *
     * @param email    The email address for registration.
     * @param password The password for registration.
     * @param userName The username for registration.
     * @param callBack The callback to notify upon successful registration or error.
     */
    public void passwordRegister(String email, String password, String userName, CentralControllerCallBack callBack){
        userDAO.passwordRegister(email, password, userName, new UserCallback() {
            @Override
            public void onSuccess() {
                callBack.onSuccess("USER_REGISTERED");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Sets the username for the first time.
     * Initiates setting username through UserDAO and triggers getUserState upon successful operation.
     *
     * @param userName The username to set.
     * @param callBack The callback to notify upon successful setting or error.
     */
    public void setUserName(String userName, CentralControllerCallBack callBack){
        userDAO.setUserName(userName, new UserCallback() {
            @Override
            public void onSuccess() {
                getUserState(callBack);
            }

            @Override
            public void onError(String error) {
                if(error.equals("NAME_IN_USE")){
                    callBack.onError(error);
                }else{
                    // Handle other errors if needed
                }
                callBack.onError(error);
            }
        });
    }

    /**
     * Sends an email to reset the password associated with the provided email address.
     *
     * @param email    The email address for password reset.
     * @param callBack The callback to notify upon success or error.
     */
    public void resetPassword(String email, CentralControllerCallBack callBack){
        userDAO.resetPassword(email, new UserCallback() {
            @Override
            public void onSuccess() {
                callBack.onSuccess("MAIL_SENT");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Resends the verification email to the user.
     *
     * @param callBack The callback to notify upon success or error.
     */
    public void resendVerificationEmail(CentralControllerCallBack callBack){
        userDAO.resendVerificationEmail(new UserCallback() {
            @Override
            public void onSuccess() {
                callBack.onSuccess("EMAIL_SENT");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Signs out the current user.
     * Initiates sign out through UserDAO, disconnects from the server, and notifies the callback upon successful sign out.
     *
     * @param googleSingInClient The GoogleSignInClient instance.
     * @param callBack            The callback to notify upon successful sign out or error.
     */
    public void signOutActualUser(GoogleSignInClient googleSingInClient, CentralControllerCallBack callBack){
        userDAO.signOutActualUser(googleSingInClient, new UserCallback() {
            @Override
            public void onSuccess() {
                // Disconnect from the server
                disconnect();

                // Notify the callback
                callBack.onSuccess("USER_SIGN_OUT");
            }

            @Override
            public void onError(String error) {
                // This can only happen with google accounts and only after the user had been deauthenticated so it has no point to treat it as an error but as an abnormal finish
                callBack.onSuccess(error);
            }
        });
    }


    /**
     * Deletes the user from the system if he used the email and password method.
     * Deletes all user's friends, notifies the server, disconnects from the server, and triggers the callback upon successful deletion.
     *
     * @param password The password for user authentication.
     * @param callBack The callback to notify upon successful deletion or error.
     */
    public void deleteActualUserPassword(String password, CentralControllerCallBack callBack){
        String userName = userDAO.getUserName();
        ArrayList<String> friendsList = userDAO.getFriendsList();

        // Delete all the friends
        for(String f : friendsList){
            deleteFriend(f, new CentralControllerCallBack() {
                @Override
                public void onSuccess(String info) {

                }

                @Override
                public void onError(String error) {

                }
            });
        }

        userDAO.deleteActualUserPassword(password, new UserCallback() {
            @Override
            public void onSuccess() {
                // Turn the array to a JSON and inform about the change to the server to multicast to the interested people the change
                JSONArray jsonArrayFriends = new JSONArray(friendsList);

                // Inform the server
                mSocket.emit("delete_user", userName, jsonArrayFriends);

                // Disconnect from the server
                disconnect();

                // Callback on successful deletion
                callBack.onSuccess("USER_DELETED");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Deletes the user from the system if he used the Google account method.
     * Deletes all user's friends, notifies the server, disconnects from the server, and triggers the callback upon successful deletion.
     *
     * @param googleSignInAccount The GoogleSignInAccount for user authentication.
     * @param googleSingInClient  The GoogleSignInClient instance.
     * @param callBack            The callback to notify upon successful deletion or error.
     */
    public void deleteActualUserGoogle(GoogleSignInAccount googleSignInAccount, GoogleSignInClient googleSingInClient, CentralControllerCallBack callBack){
        String userName = userDAO.getUserName();
        ArrayList<String> friendsList = userDAO.getFriendsList();

        // Delete all the friends
        for(String f : friendsList){
            deleteFriend(f, new CentralControllerCallBack() {
                @Override
                public void onSuccess(String info) {

                }

                @Override
                public void onError(String error) {

                }
            });
        }

        userDAO.deleteActualUserGoogle(googleSignInAccount, googleSingInClient, new UserCallback() {
            @Override
            public void onSuccess() {
                // Turn the array to a JSON and inform about the change to the server to multicast to the interested people the change
                JSONArray jsonArrayFriends = new JSONArray(friendsList);

                // Inform the server
                mSocket.emit("delete_user", userName, jsonArrayFriends);

                // Disconnect from the server
                disconnect();

                // Callback on successful deletion
                callBack.onSuccess("USER_DELETED");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Returns the authentication provider ID.
     *
     * @return The authentication provider ID.
     */
    public String getProviderId(){
        return userDAO.getProviderId();
    }

    /**
     * Returns the user's username.
     *
     * @return The user's username, or an empty string if UserDAO is null.
     */
    public String getUserName(){
        if(userDAO == null){
            Log.w("hfdsalhkñfsdahjkñlfasd", "userdao null");
            return "";
        }else{
            return userDAO.getUserName();
        }
    }

    /**
     * Returns the user's email address.
     *
     * @return The user's email address, or an empty string if UserDAO is null.
     */
    public String getEmail(){
        if(userDAO == null){
            Log.w("hfdsalhkñfsdahjkñlfasd", "userdao null");
            return "";
        }else{
            return userDAO.getEmail();
        }
    }

    /**
     * Returns the user's ID.
     *
     * @return The user's ID.
     */
    public String getUserId(){
        return userDAO.getUserId();
    }

    /**
     * Returns the user's profile photograph if the login was made using Google.
     *
     * @return The user's profile photograph URI, or null if not available.
     */
    public Uri getUserPhoto(){
        if(userDAO.getUserPhoto() != null){
            return userDAO.getUserPhoto();
        }else{
            return null;
        }
    }


    /**
     * Changes the actual userName associated with the user's account.
     * Saves the old userName, attempts to update it in the database, and notifies friends and the server about the change.
     *
     * @param userName The new userName to be set.
     * @param callBack The callback to notify upon successful userName change or error.
     */
    public void changeUserName(String userName, CentralControllerCallBack callBack){
        // Save the old name
        String oldName = userDAO.getUserName();

        // First try to change it in the database and continue if it succeeds
        userDAO.setUserName(userName, new UserCallback() {
            @Override
            public void onSuccess() {
                // Finish updating the model changing the name in the games you have
                gamesDAO.nameHasChange(userName, oldName);

                // Inform the rest of your friends about the change
                ArrayList<String> friends = userDAO.getFriendsList();

                // Turn the array to a JSON and inform about the change to the server to multicast to the interested people the change
                JSONArray jsonArrayFriends = new JSONArray(friends);
                mSocket.emit("name_change", oldName, userName, jsonArrayFriends);

                // Inform about the model change
                updateView("name_changed");

                callBack.onSuccess("NAME_CHANGED");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Returns the list of games associated with the user's account.
     *
     * @return The list of games.
     */
    public ArrayList<Game> getGamesList(){
        return gamesDAO.getGamesArrayList();
    }

    /**
     * Returns the list of friends associated with the user's account.
     *
     * @return The list of friends.
     */
    public ArrayList<String> getFriendsList(){
        return userDAO.getFriendsList();
    }

    /**
     * Tries to create a new game room on the server.
     *
     * @param numPlayers The number of players for the new game.
     * @param callback   The callback to notify with the created room code or error.
     */
    public void createRoom(String numPlayers, CentralControllerCallBack callback){
        // Set the listener to the ack event
        mSocket.on("create_room", args -> {
            // Stop listening this event
            mSocket.off("create_room");

            // Take the room code passed
            String roomCode = args[0].toString();

            // Inform the view
            callback.onSuccess(roomCode);
        });

        // Send the event
        mSocket.emit("create_room", Integer.parseInt(numPlayers), userDAO.getUserName());
    }

    /**
     * Tries to join a created game room on the server.
     *
     * @param code    The room code to join.
     * @param callBack The callback to notify upon successful join or error.
     */
    public void joinRoom(String code, CentralControllerCallBack callBack){
        // Set the listener to the ack event
        mSocket.on("join_new", args -> {
            // Stop listening this event
            mSocket.off("join_new");

            // Extract the information from the json
            try{
                // Extract the information from the json
                JSONObject jsonObject = new JSONObject(args[0].toString());
                String state = jsonObject.getString("state");

                // Depending on the state you continue differently
                if(state.equals("wait")){
                    // You need to wait a new event telling the game has to start ("ready")
                    callBack.onSuccess("GAME_JOIN_ACHIEVED_WAIT");
                }else if(state.equals("ready")){
                    // You occupied the last place available and the game can start
                    // Take the rest of the information out of the json
                    JSONArray names = jsonObject.getJSONArray("names");             //has a matrix with the name of the players
                    JSONArray detections = jsonObject.getJSONArray("detections");   //has all the detections of a game

                    // Modify the model
                    gamesDAO.newBlankGame(code, names, detections);

                    // Tell the view to update
                    updateView("list_games_changed");

                    // Make the callback
                    callBack.onSuccess("GAME_JOIN_ACHIEVED_READY");
                }else{
                    // There was a error joining the game
                    callBack.onSuccess("GAME_JOIN_ERROR");
                }
            } catch (JSONException e) {
                callBack.onError("JSON_ERROR");
            }

        });

        // Send the event
        mSocket.emit("join_new", code, userDAO.getUserName());
    }

    /**
     * Leaves the game room before it starts.
     *
     * @param code The room code to leave.
     */
    public void leaveWaitingRoom(String code){
        mSocket.emit("leave_wait", code, userDAO.getUserName());
    }

    /**
     * Returns a new list of friends that match the characters given.
     *
     * @param search The characters to search for in friends' names.
     * @return The filtered list of friends.
     */
    public ArrayList<String> searchFriends(String search){
        if(search.isEmpty()){
            return userDAO.getFriendsList();
        }else{
            return userDAO.searchFriends(search);
        }
    }

    /**
     * Sends a game invitation message to another friend.
     *
     * @param invitedUserName The username of the friend to invite.
     * @param code            The game room code to include in the invitation.
     */
    public void sendGameInvitation(String invitedUserName, String code){
        mSocket.emit("invite_request", userDAO.getUserName(), invitedUserName, code);
    }

    /**
     * Returns the information of a specific game.
     *
     * @param code The game code to retrieve.
     * @return The Game object associated with the code.
     */
    public Game getGame(String code){
        return gamesDAO.getGame(code);
    }

    /**
     * Deletes the game from the model and informs the server.
     *
     * @param code The game code to delete.
     */
    public void abandonGame(String code){
        // Delete the game from the model
        gamesDAO.deleteGame(code);

        // Inform the server you are leaving
        mSocket.emit("abandon", code, userDAO.getUserName());

        // Tell the view to update
        updateView("list_games_changed");
    }

    /**
     * Sends an HTTP request with the photo to detect elements.
     *
     * @param codifiedPhoto The encoded photo to send in the request.
     * @param code          The code associated with the request.
     */
    public void sendPhoto(String codifiedPhoto, String code){
        // Build the OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .writeTimeout(5000, TimeUnit.MILLISECONDS)
                .build();

        // Start the connection on a new thread
        new Thread(() -> {
            // Send the request to the server with the image as body
            RequestBody requestBody = new FormBody.Builder()
                    .add("image", codifiedPhoto)
                    .add("name", userDAO.getUserName())
                    .add("code", code)
                    .build();

            Request request = new Request.Builder()
                    //.url("http://192.168.1.17:8001/evaluate")   //SALAMANCA
                    //.url("http://192.168.1.40:8001/evaluate")   //PUEBLO
                    .url("http://192.168.70.83:8001/evaluate")   //FACULTAD
                    .post(requestBody)
                    .build();

            // Wait for the callBack
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    // Take the detection response
                    assert response.body() != null;
                    String detection = response.body().string();

                    // Make a new instance of the lists
                    detectionsList = new ArrayList<>();
                    positionsList = new ArrayList<>();

                    try{
                        // Extract the information from the json
                        JSONObject jsonObject = new JSONObject(detection);
                        JSONArray jsonArrayDetections = jsonObject.getJSONArray("elements");
                        JSONArray jsonArrayPositions = jsonObject.getJSONArray("positions");

                        // See if there are any detections
                        if(jsonArrayDetections.getString(0).equals("Nada")){
                            // Just set the lists to null
                            detectionsList = null;
                            positionsList = null;

                            // Tell the view to update
                            updateView("detection");
                        }else{
                            // Set the lists information
                            for(int i=0; i<jsonArrayDetections.length(); i++){
                                detectionsList.add(jsonArrayDetections.get(i).toString());
                                positionsList.add(jsonArrayPositions.get(i).toString());
                            }

                            // Send the update event to the server
                            mSocket.emit("update", code, userDAO.getUserName(), jsonArrayDetections);

                            // Tell the view to update
                            updateView("detection");
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

        }).start();
    }

    /**
     * Returns the list of the last detections made.
     *
     * @return The list of detections.
     */
    public ArrayList<String> getDetectionsList(){
        return detectionsList;
    }

    /**
     * Returns the list of positions of the detections.
     *
     * @return The list of positions.
     */
    public ArrayList<String> getPositionsList(){
        return positionsList;
    }

    /**
     * Sends a friend request to the selected userName.
     *
     * @param invitedUserName The userName of the friend to send the request to.
     * @param callBack        The callback to notify upon success or error.
     */
    public void sendFriendRequest(String invitedUserName, CentralControllerCallBack callBack){
        // First see if the userName selected exists
        userDAO.validUserName(invitedUserName, new UserCallback() {
            @Override
            public void onSuccess() {
                // Send the request to the other player
                mSocket.emit("friend_request", userDAO.getUserName(), invitedUserName);
                callBack.onSuccess("MESSAGE_SENT");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }


    /**
     * Adds a new friend to the model.
     *
     * @param addUserName The userName of the friend to add.
     * @param callBack    The callback to notify upon success or error.
     */
    public void acceptFriendRequest(String addUserName, CentralControllerCallBack callBack){
        // Change the model
        userDAO.addFriend(addUserName, new UserCallback() {
            @Override
            public void onSuccess() {
                // Inform the server about the request accepted
                mSocket.emit("accepted_friend_request", userDAO.getUserName(), addUserName);

                // Tell the view to update
                updateView("list_friends_changed");

                // Make the callback
                callBack.onSuccess("FRIEND_REQUEST_ACCEPTED");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Removes a friend from the model and informs them about the broken bond.
     *
     * @param deletedUserName The userName of the friend to remove.
     * @param callBack        The callback to notify upon success or error.
     */
    public void deleteFriend(String deletedUserName, CentralControllerCallBack callBack){
        userDAO.removeFriend(deletedUserName, new UserCallback() {
            @Override
            public void onSuccess() {
                // Inform the server about the change
                mSocket.emit("delete_friend", userDAO.getUserName(), deletedUserName);

                // Tell the view to update
                updateView("list_friends_changed");

                // Make the callback
                callBack.onSuccess("FRIEND_DELETED");
            }

            @Override
            public void onError(String error) {
                callBack.onError(error);
            }
        });
    }

    /**
     * Returns the messages list.
     *
     * @return The list of messages.
     */
    public ArrayList<Message> getMessages(){
        return messagesDAO.getMessageArrayList();
    }

    /**
     * Removes a message from the mailBox.
     *
     * @param type     The type of message to delete.
     * @param userName The userName associated with the message.
     * @param code     The code of the message.
     */
    public void deleteMessage(String type, String userName, String code){
        // Delete the message from the model
        messagesDAO.deleteMessage(type, userName, code);

        // Inform the server to delete the message
        mSocket.emit("delete_message", type, userName, code, userDAO.getUserName());

        // Tell the view to update
        updateView("list_messages_changed");
    }

    /**
     * Returns the total points from the statistics.
     *
     * @return The total points.
     */
    public int getTotalPoints() {
        return statsDAO.getTotalPoints();
    }

    /**
     * Returns the total time from the statistics.
     *
     * @return The total time.
     */
    public String getTotalTime() {
        return statsDAO.getTotalTime();
    }

    /**
     * Returns the total number of finished games from the statistics.
     *
     * @return The total number of finished games.
     */
    public int getTotalFinishedGames() {
        return statsDAO.getTotalFinishedGames();
    }

    /**
     * Returns the total number of wins from the statistics.
     *
     * @return The total number of wins.
     */
    public int getTotalWins() {
        return statsDAO.getTotalWins();
    }

    /**
     * Returns the victory percentage from the statistics.
     *
     * @return The victory percentage.
     */
    public float getVictoryPercentage() {
        return statsDAO.getVictoryPercentage();
    }

    /**
     * Informs the active views about a change in the model so they can update.
     *
     * @param updatedElement The element that was updated.
     */
    public void updateView(String updatedElement){
        for(ActiveViewListener listener : listViewListeners){
            listener.onUpdate(updatedElement);
        }
    }

    /**
     * Sends the finish confirmation of a room to the server and deletes the game from the list.
     *
     * @param code The code of the game room to finish.
     */
    public void sendFinishConfirmation(String code){
        // Inform the server
        mSocket.emit("ack_finish", code, userDAO.getUserName());

        // Delete the game from the model
        gamesDAO.deleteGame(code);

        // Tell the view to update
        updateView("list_games_changed");
    }

    /**
     * Informs the server that the user has left.
     */
    public void disconnect(){
        if(!userDAO.getUserName().isEmpty()){
            mSocket.emit("leave_server", userDAO.getUserName());
        }
    }
}
