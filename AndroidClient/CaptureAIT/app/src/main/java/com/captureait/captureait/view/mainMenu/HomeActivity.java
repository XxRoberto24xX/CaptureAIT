package com.captureait.captureait.view.mainMenu;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.captureait.captureait.R;
import com.captureait.captureait.controller.ActiveViewListener;
import com.captureait.captureait.controller.CentralController;
import com.captureait.captureait.controller.CentralControllerCallBack;
import com.captureait.captureait.view.authentication.LoginActivity;
import com.captureait.captureait.view.game.NewGameActivity;
import com.captureait.captureait.view.mailBox.MessagesActivity;
import com.captureait.captureait.view.userData.GoogleSessionActivity;
import com.captureait.captureait.view.userData.PasswordSessionActivity;
import com.captureait.captureait.view.userData.StatsActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * The principal activity and home of the app.
 */
public class HomeActivity extends AppCompatActivity implements ActiveViewListener{

    /** Controller instance. */
    private CentralController controller;

    /** All the visual elements of the view */
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    private CircleImageView userImg, nav_photo;
    private TextView viewuserName, userEmail, nav_userName, nav_email;
    private View headerView;

    /** Working progressbar */
    private ProgressDialog mProgressBar;

    /** Fragments the activity needs to show */
    private HomeFragment homeFragment;
    private FriendsFragment friendsFragment;

    /** Activity events */
    private final String ACCOUNT_OUT = "ACCOUNT_OUT";

    /** Receiver for event between activities */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACCOUNT_OUT)) {
                runOnUiThread(()->{
                    /* Finish this activity */
                    finish();
                });
            }
        }
    };

    /**
     * Called when the activity is first created. Initializes the activity, including setting up visual elements,
     * registering click listeners, and initializing necessary controllers.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Take the controller instance
        controller = CentralController.getInstance();

        // Register the activity in the centralController
        controller.addActiveViewListener(this);

        // Take the visual elements using de id
        viewuserName = findViewById(R.id.userNombre);
        userEmail = findViewById(R.id.userEmail);

        userImg = findViewById(R.id.userImagen);

        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navigationView = findViewById(R.id.navigationView);
        floatingActionButton = findViewById(R.id.fab);

        headerView = navigationView.getHeaderView(0);

        nav_photo = headerView.findViewById(R.id.nav_photo);
        nav_userName = headerView.findViewById(R.id.nav_userName);
        nav_email = headerView.findViewById(R.id.nav_email);

        // We create the progress bar before showing it
        mProgressBar = new ProgressDialog(HomeActivity.this);

        // Set the listener for the session end
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, new IntentFilter(ACCOUNT_OUT), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(broadcastReceiver, new IntentFilter(ACCOUNT_OUT));
        }

        // Crete an instance of the fragments you need to show and show the home as default
        homeFragment = new HomeFragment();
        friendsFragment = new FriendsFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.frame1, homeFragment).commit();

        // Set the toolbar with the hamburger button
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Take off the default shadow of the bottomNavigationalView
        bottomNavigationView.setBackground(null);

        // Setting the user information in the screen and in the navigational view
        viewuserName.setText(controller.getUserName());
        nav_userName.setText(controller.getUserName());

        userEmail.setText(controller.getEmail());
        nav_email.setText(controller.getEmail());

        if(controller.getProviderId().equals("google.com")){
            if(controller.getUserPhoto() != null){
                Glide.with(this).load(controller.getUserPhoto()).into(userImg);
                Glide.with(this).load(controller.getUserPhoto()).into(nav_photo);
            }
        }

        // Set the navigation view behavior
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.nav_messages){
                    Intent intent  = new Intent(HomeActivity.this, MessagesActivity.class);
                    startActivity(intent);

                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;

                } else if(menuItem.getItemId() == R.id.nav_account){
                    // See first the provider method
                    if(controller.getProviderId().equals("google.com")){
                        Intent intent  = new Intent(HomeActivity.this, GoogleSessionActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent  = new Intent(HomeActivity.this, PasswordSessionActivity.class);
                        startActivity(intent);
                    }
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;

                } else if(menuItem.getItemId() == R.id.nav_stats){
                    Intent intent  = new Intent(HomeActivity.this, StatsActivity.class);
                    startActivity(intent);

                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if(menuItem.getItemId() == R.id.nav_logOut){
                    // Show the progressBar
                    mProgressBar.setTitle("LogOut");
                    mProgressBar.setMessage("Cerrando sesión, espere un momento..");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();

                    // Prepare the session options
                    GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.client_id))
                            .requestEmail()
                            .build();
                    GoogleSignInClient googleSingInClient = GoogleSignIn.getClient(HomeActivity.this, options);

                    controller.signOutActualUser(googleSingInClient, new CentralControllerCallBack() {
                        @Override
                        public void onSuccess(String info) {
                            if(!info.equals("USER_SIGN_OUT")){
                                // Is an abnormal finish but not an error itself
                                Log.w("SesiónActivity", "Google account unable to be shut");
                            }

                            // Take out the progressBar
                            mProgressBar.dismiss();

                            // Go back to Login and close this Activity
                            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(loginActivity);
                            finish();
                        }

                        @Override
                        public void onError(String error) {

                        }
                    });
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });

        // Set the bottom navigation view behavior
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.menuHome){
                    // Show the home fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame1, homeFragment).commit();
                    return true;
                } else if (menuItem.getItemId() == R.id.menuFriends){
                    // Show the friends fragment
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame1, friendsFragment).commit();
                    return true;
                }
                return false;
            }
        });

        // Set the floating button behaviour
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to the next Activity
                Intent intent  = new Intent(HomeActivity.this, NewGameActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Called when the initialization has finished and you are a listener of the event.
     * This method is implemented from the ActiveViewListener interface.
     * @param info Information regarding initialization
     */
    @Override
    public void onInitializationFinished(String info) {

    }

    /**
     * Called when there was an update in the model and you are a listener of the event.
     * This method is implemented from the ActiveViewListener interface.
     * @param updatedElement The element that was updated
     */
    @Override
    public void onUpdate(String updatedElement) {
        if(updatedElement.equals("name_changed")){
            // Change the name in the toolbar
            viewuserName.setText(controller.getUserName());
            nav_userName.setText(controller.getUserName());
        }
    }

    /**
     * Called when the user press the back button of the phone, closes the app or the drawer if its open
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Called when the view is been destroyed, removes the listener from the controller and unregisters the broadcastReceiver.
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        controller.removeActiveViewListener(this);
        super.onDestroy();
    }
}
