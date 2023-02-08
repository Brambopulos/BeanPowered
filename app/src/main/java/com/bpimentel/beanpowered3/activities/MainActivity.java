package com.bpimentel.beanpowered3.activities;

import static java.lang.String.format;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";
    private static final String PREFERENCES_KEY = "com.bpimentel.beanpowered3.prefKey";
    private Button mStoreButton;
    private Button mSecondButton;
    private Button mThirdButton;
    private Button mFourthButton;
    private Button mLogout;

    private BeanLogDAO mBeanLogDAO;
    private SharedPreferences mPreferences = null;
    private int mUserId = -1;
    private User mUser;
    private TextView mDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDatabase();
        initializeStore();
        checkForUser();
        loginUser(mUserId);
//        debug();

        mStoreButton = findViewById(R.id.storeButton);
        mSecondButton = findViewById(R.id.libraryButton);
        mThirdButton = findViewById(R.id.indieButton);
        mFourthButton = findViewById(R.id.surveyButton);
        mLogout = findViewById(R.id.logoutButton);

        boolean isAdmin = privilegeCheck();
        setListeners(isAdmin);
    }

    // Tell application to refresh when we come back from another Activity.
    @Override
    public void onRestart() {
        super.onRestart();
        recreate();
    }

    private boolean privilegeCheck() {
        // Change home page based on account privilege
        if(mUser == null){
            return false;
        }

        if(mUser.isAdmin()){
            // Pretend user doesn't have any money
            TextView mBalance = findViewById(R.id.balanceText);
            mBalance.setVisibility(View.INVISIBLE);
            mBalance = findViewById(R.id.balance);
            mBalance.setVisibility(View.INVISIBLE);

            // Choose listeners based on privilege
            return true;
        }
        return false;
    }

    private void setListeners(boolean admin) {
        // Set static Store button
        mStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openStore(); }
        });

        // Set static Logout button
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });

        // Using value from privilegeCheck, decide what to do with the rest of the buttons
        if(admin){
            // Set Button Two to User Management Activity
            mSecondButton.setText("User Management");
            mSecondButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openUserMgmt();
                }
            });

            // Set Button Three to Green-light Admin Activity
            mThirdButton.setText("Manage Greenlight Requests");
            // PLACE INDIE ADMIN HERE
            mThirdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openIndieMgmt();
                }
            });

            // Set Button Four to Refund Admin Activity
            mFourthButton.setText("Manage Refund Requests");
            // REPLACE REFUND BUTTON HERE
            mFourthButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openRefundMgmt();
                }
            });

        } else {
            // Set Button Two to Library Activity
            mSecondButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openLibrary();
                }
            });

            // Set Button Three to Green-light Request Activity
            mThirdButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { openIndie(); }
            });

            // Set Button Three to Survey Activity
            mFourthButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openSurvey();
                }
            });
        }
    }

    private void openRefundMgmt() {
        Intent intent = RefundMgmtActivity.intentFactory(getApplicationContext());
        startActivity(intent);
    }

    private void openIndieMgmt() {
        Intent intent = IndieMgmtActivity.intentFactory(getApplicationContext(), mUser.getUserId());
        startActivity(intent);
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // All of our new activity methods will go here:
    // Open StoreActivity
    private void openIndie() {
        Intent intent = IndieActivity.intentFactory(getApplicationContext(), mUser.getUserId());
        startActivity(intent);
    }

    private void openStore() {
        Intent intent = StoreActivity.intentFactory(getApplicationContext(), mUser.getUserId());
        startActivity(intent);
    }

    // Open LibraryActivity
    private void openLibrary() {
        Intent intent = LibraryActivity.intentFactory(getApplicationContext(),
                mUser.getUserId(), mUser.getUserId());
        startActivity(intent);
    }

    // Open UserMgmtActivity
    private void openUserMgmt() {
        Intent intent = UserMgmtActivity.intentFactory(getApplicationContext(), mUser.getUserId());
        startActivity(intent);
    }

    private void openSurvey() {
        Intent intent = SurveyActivity.intentFactory(getApplicationContext(), mUser.getUserId());
        startActivity(intent);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // All of our methods relating to the user currently logged in will go here
    private void checkForUser() {
        // First, check for user in intent
        mUserId = getIntent().getIntExtra(USER_ID_KEY, -1);

        if(mUserId != -1){
            return;
        }

        // Next, look for user in preferences
        if (mPreferences == null) {
            getPrefs();
        }

        mUserId = mPreferences.getInt(USER_ID_KEY, -1);

        if (mUserId != -1) {
            return;
        }

        // Finally, are there ANY users?
        List<User> users = mBeanLogDAO.getAllUsers();
        if(users.size() <= 0){
            User defaultUser = new User("testuser1", "M0dal", false);

            User admin = new User("admin2", "admin", true, 9999.99);
            mBeanLogDAO.insert(admin, defaultUser);
        }

        Intent intent = LoginActivity.intentFactory(this);
        startActivity(intent);

    }

    // Add user to current preferences, and make all context-aware fields relevant to user
    private void loginUser(int userId) {
        mUser = mBeanLogDAO.getUserById(userId);
        addUserToPref(userId);
        if(mUser != null){
            TextView textView = (TextView) findViewById(R.id.currentUserText);
            textView.setText(mUser.getUserName());
            textView = (TextView) findViewById(R.id.balance);
            textView.setText("$" + format("%.2f", mUser.getBalance()));
        }
    }

    // Log out the user
    private void logoutUser() {
        // Build an alert which prompts the user if they want to log out or not
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.logout);
        alertBuilder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Upon confirmation of logout, wipe user data from intent, preference,
                        // and local variables. Run checkForUser to verify that all user data was
                        // wiped and return to loginActivity.
                        clearUserFromIntent();
                        clearUserFromPref();
                        mUserId = -1;
                        checkForUser();
                    }
                });
        alertBuilder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
        // Display alert
        alertBuilder.create().show();
    }

    // Retrieve preferences
    private void getPrefs() {
        mPreferences = this.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    // Add user to preference for reuse... Could probably make more use out of this
    private void addUserToPref(int userId) {
        if(mPreferences == null){
            getPrefs();
        }
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(USER_ID_KEY, userId);
        editor.apply();
    }

    // Wipe user from intent, for logging out
    private void clearUserFromIntent() {
        getIntent().putExtra(USER_ID_KEY, -1);
    }

    // Wipe user from pref, for logging out
    private void clearUserFromPref() {
        addUserToPref(-1);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Fill the store with some junk
    private void initializeStore() {
        if(mBeanLogDAO.getAllGames().size() <= 0){
            Game skrim = new Game("TESV Skyrim", "Bethesda",
                    60.00, true);
            Game obliv = new Game("TESIV Oblivion", "Bethesda",
                    60.00, true);
            Game morro = new Game("TESIII Morrowind", "Bethesda",
                    60.00, true);
            Game dagger = new Game("TESII Daggerfall", "Bethesda",
                    60.00, true);
            Game pee = new Game("Parappa the Rapper", "NanaOn-Sha",
                    20.00, true);
            Game pee2 = new Game("Parappa the Rapper 2", "NanaOn-Sha",
                    20.00, true);
            Game botw = new Game("TLoZ Breath of the Wild", "Nintendo",
                    60.00, true);
            Game ss = new Game("TLoZ Skyward Sword", "Nintendo",
                    60.00, true);
            Game lttp = new Game("TLoZ A Link To The Past", "Nintendo",
                    30.00, true);
            Game unleashed = new Game("Sonic Unleashed", "SEGA",
                    40.00, true);
            Game sa2b = new Game("Sonic Adventure 2 Battle", "SEGA",
                    60.00, true);
            Game sadx = new Game("Sonic Adventure DX", "SEGA",
                    25.00, true);
            Game frontiers = new Game("Sonic Frontiers", "SEGA",
                    60.00, true);
            Game mania = new Game("Sonic Mania", "SEGA",
                    30.00, true);
            Game inf = new Game("inFAMOUS", "Sucker Punch",
                    40.00, true);
            Game inf2 = new Game("inFAMOUS 2", "Sucker Punch",
                    40.00, true);
            Game infss = new Game("inFAMOUS Second Son", "Sucker Punch",
                    60.00, true);

            mBeanLogDAO.insert(botw, sadx, lttp, frontiers, ss, unleashed, sa2b, skrim, mania,
                    pee, pee2, inf, inf2, infss, obliv, morro, dagger);
        }
    }

    // Prepare DAO
    private void getDatabase() {
        mBeanLogDAO = Room.databaseBuilder(this, // Build DAO in *this* context
                        AppDatabase.class, // We will build one of these
                        AppDatabase.DB_NAME) // Name of DB to build (constant in AppDatabase)
                .allowMainThreadQueries() // Generally don't wanna do this because the main thread
                // does a lot. Create an Asynchronous Task and an Asynchronous Method to take it
                // off the main thread. This will keep the app from hanging every time there's a
                // database operation.
                .build() // Construct the thing
                .getBeanLogDAO(); // Only one instance of the AppDB will exist at a time;
    }

    // Intent factory for this activity, retrieve userId from previous context
    public static Intent intentFactory(Context context, int userId){
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(USER_ID_KEY, userId);

        return intent;
    }

    private void debug() {
        mDebug = findViewById(R.id.debugHomePage);
        mDebug.setVisibility(View.VISIBLE);
        mDebug.setMovementMethod(new ScrollingMovementMethod());
        List<User> users = mBeanLogDAO.getAllUsers();

        StringBuilder sb = new StringBuilder();

        sb.append("All users:\n");

        sb.append(mUser);
        for(User u : users){
            sb.append(getString(R.string.dividerString));
            sb.append("\n");
            sb.append(u);
            sb.append("\n");
            sb.append(getString(R.string.dividerString));
            sb.append("\n");

        }

        sb.append("All Games\n");
        List<Game> games = mBeanLogDAO.getAllGames();
        for(Game game : games){
            sb.append(getString(R.string.dividerString));
            sb.append("\n");
            sb.append(game);
            sb.append("\n");
            sb.append(getString(R.string.dividerString));
            sb.append("\n");

        }

        mDebug.setText(sb.toString());
    }

}