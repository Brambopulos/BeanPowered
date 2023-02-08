package com.bpimentel.beanpowered3.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;
import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.objects.User;

import java.util.ArrayList;
import java.util.List;

public class IndieActivity extends AppCompatActivity {
    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";

    private BeanLogDAO mBeanLogDAO;

    private int mUserId = -1;
    private User mUser;

    private Button mCancel, mSubmit;

    private EditText mTitle, mPrice;

    private List<Game> mGameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indie);

        getDatabase();
        validateUser();
        hookUp();
        setListeners();

    }

    // Ensure user was passed successfully
    private void validateUser() {
        // Ensure that we successfully received an ID for the user
        try {
            mUserId = getIntent().getIntExtra(USER_ID_KEY, -1);
            if (mUserId == -1) {
                throw new Exception(getString(R.string.userIdNotPassed));
            }
        } catch (Exception e) {
            System.out.println(getString(R.string.userIdNotPassed));
            Toast.makeText(getApplicationContext(),
                    getString(R.string.userIdNotPassed),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Connect View to backend bits
    private void hookUp() {
        mCancel = findViewById(R.id.indieCancel);
        mSubmit = findViewById(R.id.indieSubmit);
        mTitle = findViewById(R.id.indieGameTitleEntry);
        mPrice = findViewById(R.id.indieGamePriceEntry);
    }

    // Set button events
    private void setListeners() {
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitGame();
            }

            // Create a new game to submit for greenlighting
            private void submitGame() {
                mUser = mBeanLogDAO.getUserById(mUserId);
                mGameList = new ArrayList<>();
                mGameList = mBeanLogDAO.getAllGames();

                // If the user failed to enter a field, chastise them
                if(mTitle.getText().toString().equals("") ||
                mPrice.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),
                            getText(R.string.emptyFields), Toast.LENGTH_LONG).show();

                    // If the game is already being sold, chastise again
                } else if(mGameList.contains(mBeanLogDAO.getGameByTitle(mTitle.getText().toString()))){
                    Toast.makeText(getApplicationContext(),
                            R.string.weSellThis, Toast.LENGTH_LONG).show();

                    // If the game is legit, make a new game object where listed = false and add to DB
                } else {
                    Game newGame = new Game(mTitle.getText().toString(), mUser.getUserName(),
                            Double.parseDouble(mPrice.getText().toString()), false);
                    mBeanLogDAO.insert(newGame);
                    Toast.makeText(getApplicationContext(),
                            R.string.gameSubmitted, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        // Send the user back to the last Activity
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Admin junk
    public static Intent intentFactory(Context context, int userId){
        Intent intent = new Intent(context, IndieActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        return intent;
    }

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
}