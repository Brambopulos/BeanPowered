package com.bpimentel.beanpowered3.activities;

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
import android.view.View;
import android.widget.Toast;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;
import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.recyclerstuff.GameAdapter;
import com.bpimentel.beanpowered3.recyclerstuff.ItemClicker;

import java.util.ArrayList;
import java.util.List;

// This activity allows an admin to select games requesting greenlight, and approving or denying
public class IndieMgmtActivity extends AppCompatActivity implements ItemClicker {
    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";

    private BeanLogDAO mBeanLogDAO;
    private int mUserId = -1;

    private RecyclerView indie_list;
    private GameAdapter mAdapter;
    private ArrayList<Game> mGameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indie_mgmt);

        getDatabase();
        validateExtras();
        hookUp();
        createListData();

    }

    // Ensure we know who's logged in. Not used for this activity, but nice to be consistent.
    private void validateExtras() {
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

    // Instantiate all the things, and get the recycler configured
    private void hookUp() {
        mGameList = new ArrayList<>();
        indie_list = findViewById(R.id.indieRecycler);
        indie_list.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GameAdapter(this, mGameList, this);
        indie_list.setAdapter(mAdapter);
        indie_list.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));

    }

    // Add data to the Recycler
    private void createListData() {
        List<Game> imported = mBeanLogDAO.getAllUnlistedGames();
        for(Game game : imported){
            mGameList.add(game);
        }
    }

    // If an entry is selected, prompt the user to do something with it
    @Override
    public void onItemClick(View v, int position) {
        Game game = mBeanLogDAO.getGameByTitle(mGameList.get(position).getTitle());
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.shouldWeSell);

        // If we should sell, move to approveGame
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                approveGame(game);
            }
        });

        // If not, move to denyGame
        alertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                denyGame(game);
            }
        });

        // Let the user go back without making any changes
        alertBuilder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });

        alertBuilder.create().show();
    }

    // If we wanna sell the game, get rid of the request after setting listed to TRUE
    private void approveGame(Game game) {
        game.setListed(true);
        mBeanLogDAO.update(game);
        mGameList.remove(game);
        Toast.makeText(this, getString(R.string.greenlit), Toast.LENGTH_SHORT).show();
        recreate();
    }

    // If we don't wanna sell the game, get rid of the request and acknowledge action.
    private void denyGame(Game game) {
        mBeanLogDAO.delete(game);
        mGameList.remove(game);
        Toast.makeText(this, R.string.denied, Toast.LENGTH_SHORT).show();
        recreate();
    }

    // Admin junk
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
        Intent intent = new Intent(context, IndieMgmtActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        return intent;
    }
}