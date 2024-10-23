package com.captureait.captureait.dao;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import com.captureait.captureait.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * DAO class which takes the information from the database and saves it in a user object as cache
 */
public class UserDAO {

    /** User object that represents the current logged-in user */
    private User user;

    /** Firebase authentication instance */
    private FirebaseAuth auth;

    /** Firebase Firestore instance for database operations */
    private FirebaseFirestore fStore;

    /** Firebase Realtime Database instance */
    private DatabaseReference fDatabase;

    /**
     * Constructs the DAO instance and attempts to retrieve user information
     * from Firebase if there is an active user session.
     *
     * @param callback The callback interface to handle asynchronous responses
     */
    public UserDAO(UserCallback callback) {
        auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fDatabase = FirebaseDatabase.getInstance().getReference();

        if(auth.getCurrentUser() != null){
            takeActualUserInformation(callback);
        }else{
            this.user = null;
            callback.onSuccess();
        }
    }

    /**
     * Checks if there is a valid user logged in.
     *
     * @return True if there is a valid user and their email is verified, false otherwise
     */
    public boolean isValid(){
        if(user != null){
            user.getFirebaseUser().reload();

            if(user.getProvider().equals("google.com")){
                return true;
            }else{
                return user != null && user.getFirebaseUser().isEmailVerified();
            }
        }

        return false;
    }

    /**
     * Returns the username of the current user if available.
     *
     * @return The username of the current user or an empty string if not available
     */
    public String getUserName(){
        if(user != null && !user.getUserName().isEmpty()){
            return user.getUserName();
        }else{
            return "";
        }
    }

    /**
     * Returns the current user's information from Firebase.
     *
     * @param callback The callback interface to handle asynchronous responses
     */
    public void takeActualUserInformation(UserCallback callback){
        FirebaseUser firebaseUser = auth.getCurrentUser();
        String id = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        ArrayList<String> friends = new ArrayList<>();

        DocumentReference docRef = fStore.collection("users").document(id);
        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot docSnap = task.getResult();
                if(docSnap.exists()){
                    String userName = docSnap.getString("username");
                    String provider = docSnap.getString("provider");

                    DatabaseReference datRef = fDatabase.child(id).child("friends");
                    datRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot friendsSnap) {
                            if(friendsSnap.exists()){
                                for(DataSnapshot s : friendsSnap.getChildren()){
                                    String friend = Objects.requireNonNull(s.getValue()).toString();
                                    friends.add(friend);
                                }
                            }

                            if(userName.isEmpty()){
                                user = new User(firebaseUser, id, email, provider, "", friends);
                                callback.onSuccess();
                            }else{
                                user = new User(firebaseUser, id, email, provider, userName, friends);
                                callback.onSuccess();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError("DATABASE_ERROR");
                        }
                    });
                }else{
                    callback.onError("DATABASE_ERROR");
                }
            }else{
                callback.onError("DATABASE_ERROR");
            }
        });
    }

    /**
     * Takes the Google account given by the user to authenticate him.
     *
     * @param accountTask The task containing the GoogleSignInAccount
     * @param callback    The callback interface to handle asynchronous responses
     */
    public void googleAuthentication(Task<GoogleSignInAccount> accountTask, UserCallback callback){
        try{
            // Get the authentication credential given
            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

            // Try to authenticate the credential in firebase
            auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // If the task is successful the user is now registered or authenticated (firebase makes no distinction) in the users authentication database
                    if(task.isSuccessful()){
                        // To see if it was registered or reuthenticared see if he has a document in the database, if he has one is reauthenticated if not is registered and you need to create it
                        DocumentReference docRef = fStore.collection("users").document(auth.getCurrentUser().getUid());
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot docSnap = task.getResult();
                                    if(docSnap.exists()){
                                        // Reuthenticated user
                                        takeActualUserInformation(callback);
                                    }else{
                                        // Finish the register creating the user document and the user representation in the model
                                        // Get the user information from firebase
                                        FirebaseUser firebaseUser = auth.getCurrentUser();
                                        String id = firebaseUser.getUid();
                                        String email = firebaseUser.getEmail();
                                        String provider = "google.com";
                                        ArrayList<String> friends = new ArrayList<>();

                                        // Map the information you need to save
                                        Map<String, Object> datauser = new HashMap<>();
                                        datauser.put("username", "");
                                        datauser.put("provider", provider);
                                        datauser.put("totalPoints", 0);
                                        datauser.put("totalTime", 0);
                                        datauser.put("totalGames", 0);
                                        datauser.put("totalWins", 0);

                                        // Save the information in the document
                                        docRef.set(datauser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    user = new User(firebaseUser, id, email, provider, "", friends);
                                                    callback.onSuccess();
                                                }else{
                                                    callback.onError("DATABASE_ERROR");
                                                }
                                            }
                                        });
                                    }
                                }else{
                                    callback.onError("DATABASE_ERROR");
                                }
                            }
                        });
                    }else{
                        callback.onError("DATABASE_ERROR");
                    }
                }
            });

        }catch (ApiException e){
            callback.onError("GOOGLE_ERROR");
        }
    }

    /**
     * Takes the credentials given by the user to authenticate him.
     *
     * @param email    The user's email
     * @param password The user's password
     * @param callback The callback interface to handle asynchronous responses
     */
    public void passwordAuthentication(String email,String password, UserCallback callback){
        // Try to sign in with the credentials given
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // We have an active user to take the information from
                    takeActualUserInformation(callback);
                }else{
                    callback.onError("WRONG_CREDENTIALS");
                }
            }
        });
    }

    /**
     * Takes the information given to register a new user.
     *
     * @param email     The user's email
     * @param password  The user's password
     * @param userName  The user's desired username
     * @param callback  The callback interface to handle asynchronous responses
     */
    public void passwordRegister(String email, String password, String userName, UserCallback callback){
        // First see if the userName is unique
        Query query = fStore.collection("users").whereEqualTo("username", userName);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot qrySnap = task.getResult();
                    if(qrySnap.isEmpty()){
                        // The name is unique so we can continue the register
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    // Finish the register creating the user document and the user representation in the model
                                    // Get the user information from firebase
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    String id = firebaseUser.getUid();
                                    String provider = "password";
                                    ArrayList<String> friends = new ArrayList<>();

                                    DocumentReference docRef = fStore.collection("users").document(auth.getCurrentUser().getUid());

                                    // Map the information you need to save
                                    Map<String, Object> datauser = new HashMap<>();
                                    datauser.put("username", userName);
                                    datauser.put("provider", provider);
                                    datauser.put("totalPoints", 0);
                                    datauser.put("totalTime", 0);
                                    datauser.put("totalGames", 0);
                                    datauser.put("totalWins", 0);

                                    // Save the information in the document
                                    docRef.set(datauser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                user = new User(firebaseUser, id, email, provider, userName, friends);

                                                // Send the verification email to the user before finishing
                                                firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            callback.onSuccess();
                                                        }else{
                                                            callback.onError("VERIFICATION_EMAIL_ERROR");
                                                        }
                                                    }
                                                });
                                            }else{
                                                callback.onError("DATABASE_ERROR");
                                            }
                                        }
                                    });
                                }else{
                                    callback.onError("EMAIL_IN_USE");
                                }
                            }
                        });
                    }else{
                        callback.onError("NAME_IN_USE");
                    }
                }else{
                    callback.onError("DATABASE_ERROR");
                }
            }
        });
    }

    /**
     * Confirms if the introduced name is unique or not and changes it if it's not.
     *
     * @param userName  The user's desired username
     * @param callback  The callback interface to handle asynchronous responses
     */
    public void setUserName(String userName, UserCallback callback){
        // First see if the name is unique using a query
        Query query = fStore.collection("users").whereEqualTo("username", userName);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot qrySnap = task.getResult();
                    if(qrySnap.isEmpty()){
                        // The name is unique so we need to add it to the document of the user
                        DocumentReference docRef = fStore.collection("users").document(user.getId());

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("username", userName);

                        docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Now set the name in the model
                                    user.setUserName(userName);
                                    callback.onSuccess();
                                } else {
                                    callback.onError("DATABASE_ERROR");
                                }
                            }
                        });

                    }else{
                        // The name is in use
                        callback.onError("NAME_IN_USE");
                    }
                }else{
                    callback.onError("DATABASE_ERROR");
                }
            }
        });
    }

    /**
     * Sends the email to reset the password.
     *
     * @param email     The email address of the user
     * @param callback  The callback interface to handle asynchronous responses
     */
    public void resetPassword(String email, UserCallback callback){
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    callback.onSuccess();
                }else{
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        callback.onError("MAIL_NOT_REGISTERED");
                    }else{
                        callback.onError("MAIL_SENT_ERROR");
                    }
                }
            }
        });
    }

    /**
     * Sends another verification email to the user account.
     *
     * @param callback  The callback interface to handle asynchronous responses
     */
    public void resendVerificationEmail(UserCallback callback){
        // Send the verification email to the user before finishing
        user.getFirebaseUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    callback.onSuccess();
                }else{
                    callback.onError("VERIFICATION_EMAIL_ERROR");
                }
            }
        });
    }

    /**
     * Signs out the actual user.
     *
     * @param googleSingInClient The GoogleSignInClient instance to sign out from Google (can be null)
     * @param callback           The callback interface to handle asynchronous responses
     */
    public void signOutActualUser(GoogleSignInClient googleSingInClient, UserCallback callback){
        // Sign out from Firebase
        auth.signOut();

        // If the registration was made with Google, sign out there too
        if(user.getProvider().equals("google.com")){
            googleSingInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        // Set the user to null before returning
                        user = null;
                        callback.onSuccess();
                    }else{
                        // Set the user to null before returning
                        user = null;
                        callback.onError("GOOGLE_ACCOUNT_NOT_CLOSED");
                    }
                }
            });
        }else{
            // Set the user to null before returning
            user = null;
            callback.onSuccess();
        }
    }

    /**
     * Deletes the user from the database if registered using email and password.
     *
     * @param password  The user's password for reauthentication
     * @param callback  The callback interface to handle asynchronous responses
     */
    public void deleteActualUserPassword(String password, UserCallback callback){
        // It's a good practice to reauthenticate the user before deleting
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.getFirebaseUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // Correct user, proceed with deletion
                    // Delete the user document
                    DocumentReference docRef = fStore.collection("users").document(user.getId());
                    docRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Then delete the user from the authentication database
                                user.getFirebaseUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            // Now sign out
                                            signOutActualUser(null, callback);
                                        }else{
                                            callback.onError("DATABASE_ERROR");
                                        }
                                    }
                                });
                            } else {
                                callback.onError("DATABASE_ERROR");
                            }
                        }
                    });
                }else{
                    callback.onError("REAUTHENTICATION_FAILED");
                }
            }
        });
    }

    /**
     * Deletes the user from the database if registered using a Google account.
     *
     * @param googleSignInAccount The GoogleSignInAccount instance for reauthentication
     * @param googleSingInClient  The GoogleSignInClient instance to sign out from Google (can be null)
     * @param callback            The callback interface to handle asynchronous responses
     */
    public void deleteActualUserGoogle(GoogleSignInAccount googleSignInAccount, GoogleSignInClient googleSingInClient, UserCallback callback){
        // It's a good practice to reauthenticate the user before deleting
        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        user.getFirebaseUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // Correct user, proceed with deletion
                    // Delete the user document
                    DocumentReference docRef = fStore.collection("users").document(user.getId());
                    docRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Then delete the user from the authentication database
                                user.getFirebaseUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            // Now sign out
                                            signOutActualUser(googleSingInClient, callback);
                                        }else{
                                            callback.onError("DATABASE_ERROR");
                                        }
                                    }
                                });
                            } else {
                                callback.onError("DATABASE_ERROR");
                            }
                        }
                    });
                }else{
                    callback.onError("REAUTHENTICATION_FAILED");
                }
            }
        });
    }

    /**
     * Gives the provider ID of the current user.
     *
     * @return The provider ID (e.g., "google.com", "password")
     */
    public String getProviderId(){
        return user.getProvider();
    }

    /**
     * Gives the current user's email.
     *
     * @return The email address of the user
     */
    public String getEmail(){
        return user.getEmail();
    }

    /**
     * Gives the current user's ID.
     *
     * @return The unique ID of the user
     */
    public String getUserId(){
        return user.getId();
    }

    /**
     * Gives the current user's profile photograph if the login was made using Google.
     *
     * @return The URI of the user's profile photo, or null if not available or not logged in with Google
     */
    public Uri getUserPhoto(){
        if(user.getProvider().equals("google.com")){
            return user.getFirebaseUser().getPhotoUrl();
        }else{
            return null;
        }
    }

    /**
     * Handles the modification of friends when the user opens the app.
     *
     * @param delFriends The JSONArray of friend IDs to be removed
     * @param addFriends The JSONArray of friend IDs to be added
     * @param callback   The callback interface to handle asynchronous responses
     * @throws JSONException If there is an error processing JSON data
     */
    public void friendsConnectionModification(JSONArray delFriends, JSONArray addFriends, UserCallback callback) throws JSONException {
        for(int i=0; i<delFriends.length(); i++){
            this.removeFriend(delFriends.getString(i), new UserCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        }

        for(int i=0; i<addFriends.length(); i++){
            this.addFriend(addFriends.getString(i), new UserCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
        }

        callback.onSuccess();
    }

    /**
     * Adds a new friend to the user's friend list.
     *
     * @param friendName The name of the friend to add
     * @param callback   The callback interface to handle asynchronous responses
     */
    public void addFriend(String friendName, UserCallback callback){
        // First try to add the friend to the database
        fDatabase.child(user.getId()).child("friends").child(friendName).setValue(friendName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // Add the friend to the model
                    user.addFriend(friendName);
                    callback.onSuccess();
                }else{
                    callback.onError("DATABASE_ERROR");
                }
            }
        });
    }

    /**
     * Removes a friend from the user's friend list.
     *
     * @param friendName The name of the friend to remove
     * @param callback   The callback interface to handle asynchronous responses
     */
    public void removeFriend(String friendName, UserCallback callback){
        // First try to remove the friend from the database
        fDatabase.child(user.getId()).child("friends").child(friendName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // Remove the friend from the model
                    user.removeFriend(friendName);
                    callback.onSuccess();
                }else{
                    callback.onError("DATABASE_ERROR");
                }
            }
        });
    }

    /**
     * Returns the list of all friends the player has.
     *
     * @return The list of friends as an ArrayList of strings
     */
    public ArrayList<String> getFriendsList(){
        return user.getFriends();
    }

    /**
     * Returns a new friend list that matches the characters given in the search string.
     *
     * @param search The search string to match against friend names
     * @return An ArrayList of strings containing matching friend names
     */
    public ArrayList<String> searchFriends(String search){
        // Make a new sublist
        ArrayList<String> sublist = new ArrayList<>();

        // Look for coincidences in the original list
        for(String name : user.getFriends()){
            if(name.contains(search)){
                sublist.add(name);
            }
        }

        // Return the list
        return sublist;
    }

    /**
     * Changes the username of a friend.
     *
     * @param oldUserName The current username of the friend
     * @param newUserName The new username to change to
     * @param callBack    The callback interface to handle asynchronous responses
     */
    public void changeFriendUserName (String oldUserName, String newUserName, UserCallback callBack){
        removeFriend(oldUserName, new UserCallback() {
            @Override
            public void onSuccess() {
                addFriend(newUserName, callBack);
            }

            @Override
            public void onError(String error) {
                callBack.onError("DATABASE_ERROR");
            }
        });
    }

    /**
     * Checks if the introduced username exists.
     *
     * @param userName The username to check
     * @param callback The callback interface to handle asynchronous responses
     */
    public void validUserName(String userName, UserCallback callback){
        // First see if it's the user's own username
        if(userName.equals(user.getUserName())){
            callback.onError("OWN_NAME");
        }else{
            // Check if there is someone named that way
            Query query = fStore.collection("users").whereEqualTo("username", userName);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        QuerySnapshot qrySnap = task.getResult();
                        if(qrySnap.isEmpty()){
                            // No one with that name found
                            callback.onError("NO_PLAYER_FOUND");
                        }else{
                            // Check the list of friends the player had when disconnected
                            DatabaseReference datRef = fDatabase.child(user.getId()).child("friends");
                            datRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot friendsSnap) {
                                    boolean find = false;
                                    if(friendsSnap.exists()){
                                        for(DataSnapshot s : friendsSnap.getChildren()){
                                            if(Objects.requireNonNull(s.getValue()).toString().equals(userName)){
                                                callback.onError("ALREADY_FRIEND");
                                                find = true;
                                                break;
                                            }
                                        }
                                    }
                                    if(!find){
                                        callback.onSuccess();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    callback.onError("DATABASE_ERROR");
                                }
                            });
                        }
                    }else{
                        callback.onError("DATABASE_ERROR");
                    }
                }
            });
        }
    }
}
