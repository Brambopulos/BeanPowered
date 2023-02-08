package com.bpimentel.beanpowered3.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;
import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.objects.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SurveyActivity extends AppCompatActivity {
    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";

    private BeanLogDAO mBeanLogDAO;
    private int mUserId = -1;
    private User mUser;
    private RadioButton mButtOne, mButtTwo, mButtThree, mButtFour, mButtFive;
    private TextView mGameOne, mGameTwo, mGameThree, mGameFour, mGameFive;
    private Button mCancel, mSubmit;
    private List<Game> mGameList;
    private ArrayList<TextView> mGameTitles;
    private ArrayList<RadioButton> mGameButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        getDatabase();
        validateUser();
        hookUp();
        generateGames();
        setGames();
        setListeners();

    }

    // Ensure user passed from last intent
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

    // Configure views. I could have made the games inhabit the Radio buttons that correspond to
    // them, but that didn't look as pretty. So enjoy:
    private void hookUp() {
        mButtOne = findViewById(R.id.surveyButtonOne);
        mButtTwo = findViewById(R.id.surveyButtonTwo);
        mButtThree = findViewById(R.id.surveyButtonThree);
        mButtFour = findViewById(R.id.surveyButtonFour);
        mButtFive = findViewById(R.id.surveyButtonFive);

        mGameOne = findViewById(R.id.surveyGame1);
        mGameTwo = findViewById(R.id.surveyGame2);
        mGameThree = findViewById(R.id.surveyGame3);
        mGameFour = findViewById(R.id.surveyGame4);
        mGameFive = findViewById(R.id.surveyGame5);

        mCancel = findViewById(R.id.surveyCancel);
        mSubmit = findViewById(R.id.surveySubmit);

        mGameTitles = new ArrayList<>();
        mGameTitles.add(mGameOne);
        mGameTitles.add(mGameTwo);
        mGameTitles.add(mGameThree);
        mGameTitles.add(mGameFour);
        mGameTitles.add(mGameFive);

        mGameButtons = new ArrayList<>();
        mGameButtons.add(mButtOne);
        mGameButtons.add(mButtTwo);
        mGameButtons.add(mButtThree);
        mGameButtons.add(mButtFour);
        mGameButtons.add(mButtFive);
    }

    // Configure buttons
    private void setListeners() {
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitSurvey();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Configure the survey to show five random games
    private void setGames() {
        Game surveyItem;
        Random mRandom = new Random();

        // Ensure at least one Sonic game makes its way onto the survey
        while (mGameOne.getText().equals("Game 1")) {
            int index = mRandom.nextInt(mGameList.size());
            surveyItem = mGameList.get(index);

            if (surveyItem.getTitle().contains("Sonic")) {
                mGameOne.setText(surveyItem.getTitle());
                mGameList.remove(surveyItem);
            }
        }

        // Populate the last four survey options randomly
        for (int i = 1; i < mGameTitles.size(); i++) {
            int index = mRandom.nextInt(mGameList.size());
            surveyItem = mGameList.get(index);
            mGameTitles.get(i).setText(surveyItem.getTitle());
            mGameList.remove(surveyItem);
        }
    }

    // Generate the games which can be surveyed from database
    private void generateGames() {
         mGameList = new ArrayList();
         mGameList = mBeanLogDAO.getAllListedGames();

         // Ensure there are enough games to fill the survey activity, at least 5
         if(mGameList.size() <= 5){
             Toast.makeText(this, R.string.notEnoughGames,
                     Toast.LENGTH_LONG);
             finish();
         }
    }

    // When finished, ensure that the user selected an option and reward them
    private void submitSurvey() {
        mUser = mBeanLogDAO.getUserById(mUserId);
        for (int i = 0; i < mGameTitles.size(); i++) {
            if (mGameButtons.get(i).isChecked()) {

                // Reward the user for choosing correctly
                if (mGameTitles.get(i).getText().toString().contains("Sonic")) {
                    Toast.makeText(this,
                            R.string.sonicWins,
                            Toast.LENGTH_SHORT).show();
                    mUser.setBalance(mUser.getBalance() + 100);
                    mBeanLogDAO.update(mUser);
                    finish();
                    return;

                    // Reward the user for having an opinion that we can sell
                } else {
                    Toast.makeText(this,
                            R.string.okayChoice,
                            Toast.LENGTH_SHORT).show();
                    mUser.setBalance(mUser.getBalance() + 20);
                    mBeanLogDAO.update(mUser);
                    finish();
                    return;
                }
            }
        }
        Toast.makeText(this,
                R.string.justPickOne,
                Toast.LENGTH_SHORT).show();
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

    public static Intent intentFactory(Context context, int userId){
        Intent intent = new Intent(context, SurveyActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        return intent;
    }
}