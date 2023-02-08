package com.bpimentel.beanpowered3.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;
import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.objects.Rating;
import com.bpimentel.beanpowered3.objects.User;

public class ReviewActivity extends AppCompatActivity {
    private static final String GAME_ID = "com.bpimentel.beanpowered3.gameId";
    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";

    private BeanLogDAO mBeanLogDAO;

    private Game mGame;

    private int mUserId = -1;
    private int mGameId = -1;

    private EditText mReview;

    private RatingBar mRating;

    private Button mSubmit;
    private Button mCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        getDatabase();
        validateExtras();
        hookUp();
        setListeners();

    }

    // ensure User and Game have been passed from last activity
    private void validateExtras() {
        try{
            mGameId = getIntent().getIntExtra(GAME_ID, -1);
            mUserId = getIntent().getIntExtra(USER_ID_KEY, -1);
            if(mUserId == -1 || mGameId == -1){
                throw new Exception(getString(R.string.IdError));
            }
        } catch(Exception e){
            System.out.println(getString(R.string.IdError));
            Toast.makeText(getApplicationContext(),
                    getString(R.string.IdError), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Hook up Views, and enable the review RatingBar if there is more than one review
    private void hookUp() {
        mSubmit = findViewById(R.id.submitButton);
        mRating = findViewById(R.id.ratingRating);
        mCancel = findViewById(R.id.ratingCancelButton);
        mReview = findViewById(R.id.leaveReview);
        TextView mTitle = findViewById(R.id.gameReviewTitle);
        TextView mPub = findViewById(R.id.gameReviewPub);
        RatingBar mExistingRatings = findViewById(R.id.existingRatings);
        mGame = mBeanLogDAO.getGameById(mGameId);
        mTitle.setText(mGame.getTitle());
        mPub.setText(mGame.getPublisher());
        if(!mGame.getRatings().isEmpty()){
            mExistingRatings.setRating(mGame.getSumOfRatings());
            mExistingRatings.setVisibility(View.VISIBLE);

            TextView noRatings = findViewById(R.id.gameReviewRating);
            noRatings.setVisibility(View.INVISIBLE);
        }
    }

    // Hook up butts
    private void setListeners() {
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRating();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Attempt to create a new rating from user input
    private void addRating() {
        User mUser = mBeanLogDAO.getUserById(mUserId);
        mGame = mBeanLogDAO.getGameById(mGameId);

        // If user didn't enter review text, shit on them
        if(TextUtils.isEmpty(mReview.getText().toString())){
            Toast.makeText(getApplicationContext(), R.string.emptyFields,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // If user hasn't played the game for at least 5 hours, don't let them review
        if(mUser.playTime(mGame) < 5){
            Toast.makeText(getApplicationContext(), R.string.notPlayedEnough,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // If the user already rated the game, don't let them post another one
        if(mGame.beenRatedBy(mUser)){
            Toast.makeText(getApplicationContext(),
                    mUser.getUserName() + " has already reviewed " + mGame.getTitle() + "!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Otherwise, add a rating with the given input
        Float ratingFloat = mRating.getRating();
        String reviewString = mReview.getText().toString();
        Rating newRating = new Rating(ratingFloat, reviewString);
        mGame.addRating(mUser, newRating);
        mBeanLogDAO.update(mGame);
        Toast.makeText(getApplicationContext(), R.string.reviewPosted,
                Toast.LENGTH_LONG).show();
        finish();
    }

    // Admin stuff
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

    public static Intent intentFactory(Context context, int gameId, int userId){
        Intent intent = new Intent(context, ReviewActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        intent.putExtra(GAME_ID, gameId);
        return intent;
    }
}