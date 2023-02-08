package com.bpimentel.beanpowered3.activities;

import static java.lang.String.format;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.Rating;
import com.bpimentel.beanpowered3.objects.Refund;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;
import com.bpimentel.beanpowered3.recyclerstuff.RatingAdapter;
import com.bpimentel.beanpowered3.recyclerstuff.onLongItemClickListener;

import java.util.HashMap;

// This class handles all things that can occur between a user and a video game.
public class GameCardActivity extends AppCompatActivity {
    private static final String GAME_ID = "com.bpimentel.beanpowered3.gameId";
    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";

    private BeanLogDAO mBeanLogDAO;
    private int mGameId = -1;
    private int mUserId = -1;

    private User mUser;
    private Game mGame;

    private Button mPurchase;
    private Button mReview;
    private Button mRefund;
    private Button mPlay;

    private TextView mNoReviews;
    private RatingBar mAllReviews;

    private RecyclerView review_list;
    private RatingAdapter mAdapter;
    private HashMap<String, Rating> mReviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_card);

        // Connect to database
        getDatabase();

        // Verify Game and User ID passed successfully
        validateExtras();

        // Instantiate View based on game context
        initializeCard();

        // Set up reviews.
        // Should only do this if any reviews exist... But the long click listener depends on it
        // being initialized. Oh whale
        initializeRecycler();

        // Hook up buttons
        setListeners();

        // Populate review RecyclerView
        createListData();

    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // All onCreate stuff
    /////////////////////////////////////////////////////////////////////////////////////////
    // Confirm user and game IDs. Kick back if they're not there for some reason
    private void validateExtras() {
        // Ensure that we successfully received an ID for the user
        try {
            mUserId = getIntent().getIntExtra(USER_ID_KEY, -1);
            mGameId = getIntent().getIntExtra(GAME_ID, -1);
            if (mUserId == -1 || mGameId == -1) {
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

    private void initializeCard() {
        // Connect the Views
        mPlay = findViewById(R.id.playButton);
        mPurchase = findViewById(R.id.purchaseButton);
        mNoReviews = findViewById(R.id.gameCardRatings);
        mAllReviews = findViewById(R.id.gameCardRatingBar);
        mReview = findViewById(R.id.reviewButton);
        mRefund = findViewById(R.id.refundButton);

        // Retrieve user by ID
        mUser = mBeanLogDAO.getUserById(mUserId);
        mGame = mBeanLogDAO.getGameById(mGameId);

        // Set Views to reflect current game details
        TextView gameTitle, gamePub;
        gameTitle = findViewById(R.id.gameCardTitle);
        gameTitle.setText(mGame.getTitle());
        gamePub = findViewById(R.id.gameCardPub);
        gamePub.setText(mGame.getPublisher());

        // Check if user owns game. If so, set the Views to acknowledge that
        // Change functionality to interact with an owned game
        if (mUser.hasGame(mGame)) {
            mPurchase.setVisibility(View.INVISIBLE);
            mPlay.setVisibility(View.VISIBLE);
            mReview.setVisibility(View.VISIBLE);
            mRefund.setVisibility(View.VISIBLE);
            TextView playTime = findViewById(R.id.playTime);
            playTime.setText("You've played this game for " + mUser.getPlayTime(mGame) + " hours!");

            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playGame();
                }
            });

            // If the user doesn't own the game, prompt them to purchase it.
        } else {
            mPurchase.setText("Purchase for $" + format("%.2f", mGame.getPrice()));
        }

        // If there are reviews, show them instead of the filler text.
        if(mGame.getRatings().size() > 0){
            mNoReviews.setVisibility(View.INVISIBLE);
            mAllReviews.setVisibility(View.VISIBLE);
            mAllReviews.setRating(mGame.getSumOfRatings());
        }
    }

    // Set up recycler to handle review stuff
    private void initializeRecycler() {
        // Initialize ArrayList
        mReviewList = new HashMap<>();

        // Attach RecyclerView to view, set layout
        review_list = findViewById(R.id.reviewRecycler);
        review_list.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RatingAdapter(this, mReviewList);
        review_list.setAdapter(mAdapter);
        review_list.addItemDecoration(new DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL));
    }


    private void setListeners() {
        // Set up listener for long press of a RecyclerView item
        mAdapter.setOnLongItemClickListener(new onLongItemClickListener() {
            @Override
            public void ItemLongClicked(View v, int position) {
                Rating toDelete = mAdapter.getRatingByPosition(position);
                deleteReview(toDelete);
            }
        });

        mPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                purchaseGame();
            }
        });

        mReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveReview();
            }
        });

        mRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestRefund();
            }
        });
    }

    private void createListData() {
        HashMap<String, Rating> imported = new HashMap<>();
        imported = mGame.getRatings();
        for(String user : imported.keySet()){
            mReviewList.put(user, imported.get(user));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // All game interaction stuff
    /////////////////////////////////////////////////////////////////////////////////////////

    // Simple activity that adds a quantity of hours (Integer) to an owned game
    private void playGame() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final EditText hourInput = new EditText(this);
        hourInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        alertBuilder.setView(hourInput);
        alertBuilder.setMessage(getString(R.string.playTimePrompt)
                + mGame.getTitle()
                + getString(R.string.questionMark));
        alertBuilder.setPositiveButton(R.string.playConfirm, new DialogInterface.OnClickListener() {
            @Override
            // Handle case in which user entered nothing
            public void onClick(DialogInterface dialogInterface, int i) {
                if (hourInput.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), getText(R.string.enterValue),
                            Toast.LENGTH_LONG).show();
                } else {
                    Integer hours = Integer.parseInt(hourInput.getText().toString());

                    // If playing for over 100 hours, tell them to get a life
                    if(hours > 100){
                        Toast.makeText(getApplicationContext(),
                                "Dude, get some sleep <3 (touch grass)",
                                Toast.LENGTH_LONG).show();

                        // Set time played to sum of itself and the new value under 100hrs
                    } else {
                        mUser.setHoursPlayed(mGame, mUser.getPlayTime(mGame) + hours);
                        mBeanLogDAO.update(mUser);
                        Toast.makeText(getApplicationContext(),
                                "Played for " + hours + " hours!",
                                Toast.LENGTH_LONG).show();
                        recreate();
                    }
                }
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });
        alertBuilder.show();

    }

    // Prompt the user to buy the game
    private void purchaseGame() {
        androidx.appcompat.app.AlertDialog.Builder alertBuilder =
                new AlertDialog.Builder(this);

        alertBuilder.setMessage(getString(R.string.purchaseForSure) +
                format("%.2f", mGame.getPrice()) + "?");

        // If yes, add the game to the user's library and remove the balance
        alertBuilder.setPositiveButton(getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(checkBal()){
                            checkHasGame();
                            mUser.addGame(mGame);
                            mUser.setBalance(mUser.getBalance() - mGame.getPrice());

                            // If the publisher is an existing user, give them all the proceeds
                            if(mBeanLogDAO.getAllUsers().contains(
                                    mBeanLogDAO.getUserByUsername(
                                            mGame.getPublisher()))){
                                User publisher = mBeanLogDAO.getUserByUsername(mGame.getPublisher());
                                publisher.setBalance(publisher.getBalance() + mGame.getPrice());
                                mBeanLogDAO.update(publisher);
                            }
                            mBeanLogDAO.update(mUser);
                            finish();
                        }
                    }
                });

        alertBuilder.setNegativeButton(getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //We don't really need to do anything here.

                    }
                });

        alertBuilder.create().show();

    }


    // If user has game, give them a reminder
    private void checkHasGame() {
        if(mUser.hasGame(mGame)){
            androidx.appcompat.app.AlertDialog.Builder alertBuilder =
                    new AlertDialog.Builder(this);
            alertBuilder.setMessage(R.string.alreadyHasGame);
            alertBuilder.setNeutralButton(R.string.trueThat, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }

            });
            alertBuilder.create().show();

        }
    }

    // Return true if the user can afford the game they want
    private boolean checkBal() {
        if (mUser.getBalance() < mGame.getPrice()) {
            androidx.appcompat.app.AlertDialog.Builder alertBuilder =
                    new AlertDialog.Builder(this);
            alertBuilder.setMessage(getString(R.string.poorManError));
            alertBuilder.setNeutralButton(R.string.fineThen, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alertBuilder.create().show();
            return false;
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // All review stuff
    /////////////////////////////////////////////////////////////////////////////////////////

    // Move user to the Review Activity
    private void leaveReview() {
        Intent intent = ReviewActivity.intentFactory(this, mGameId, mUserId);
        startActivity(intent);
    }

    private void deleteReview(Rating rating) {
        if(!mUser.isAdmin()){
            Toast.makeText(this, R.string.mustBeAdmin, Toast.LENGTH_SHORT);
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(getString(R.string.deleteReviewPrompt)
                + mAdapter.getUserWhoRated(rating)
                + getString(R.string.questionMark));

        // If yes, get rid of the Rating object, refresh the data, and notify user
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mGame.removeRating(mAdapter.getUserWhoRated(rating));
                mReviewList.remove(mAdapter.getUserWhoRated(rating));
                mBeanLogDAO.update(mGame);
                mAdapter.notifyDataSetChanged();
                mAllReviews.setRating(mGame.getSumOfRatings());
                Toast.makeText(getApplicationContext(),
                        getString(R.string.opinionSilenced), Toast.LENGTH_LONG).show();
            }
        });

        // Do nothing
        alertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        alertBuilder.create().show();
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // All refund stuff
    /////////////////////////////////////////////////////////////////////////////////////////
    // Ask for a refund
    private void requestRefund(){

        // If game is played for over 5 hours, automatically reject refund request
        if(mUser.getPlayTime(mGame) > 5){
            Toast.makeText(this,
                    R.string.cannotReturn,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(getString(R.string.whyReturn) + mGame.getTitle());
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        alertBuilder.setView(input);
        alertBuilder.setPositiveButton(R.string.mainSubmitButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        refundConfirm(input);
                    }
        });

                alertBuilder.create().show();
    }

    // Action to take if the user tries to get a refund. Pulled into its own method for Context
    private void refundConfirm(EditText input) {
        for(Refund refund : mBeanLogDAO.getRefundsByUser(mUser.getUserName())) {
            if (refund.getGame().equals(mGame.getTitle())) {
                Toast.makeText(this, R.string.refundRequestedAlready,
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String refundReason = input.getText().toString();
        Refund refund = new Refund(refundReason,
                mUser.getUserName(),
                mGame.getTitle());
        Toast.makeText(this, getString(R.string.refundSubmitSuccess),
                Toast.LENGTH_SHORT).show();
        mBeanLogDAO.insert(refund);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // Standard junk
    /////////////////////////////////////////////////////////////////////////////////////////

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
        Intent intent = new Intent(context, GameCardActivity.class);
        intent.putExtra(GAME_ID, gameId);
        intent.putExtra(USER_ID_KEY, userId);
        return intent;
    }


    // Mod onRestart to refresh the display
    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
        mAllReviews.setRating(mGame.getSumOfRatings());
    }

    // Mod onResume to refresh the display but less intensely.
    @Override
    public void onResume() {
        super.onResume();
        mAllReviews.setRating(mGame.getSumOfRatings());
        review_list.getAdapter().notifyDataSetChanged();
    }

}