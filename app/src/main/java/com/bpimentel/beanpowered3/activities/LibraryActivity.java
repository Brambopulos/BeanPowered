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
import android.text.InputType;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;
import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.recyclerstuff.ItemClicker;
import com.bpimentel.beanpowered3.recyclerstuff.OwnedGameAdapter;
import com.bpimentel.beanpowered3.recyclerstuff.onLongItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// This activity allows a user to see a simplified list of all the games they've purchased
public class LibraryActivity extends AppCompatActivity implements ItemClicker {
    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";
    private static final String LIBRARY_USER_ID = "com.bpimentel.beanpowered3.libraryUserId";

    private BeanLogDAO mBeanLogDAO;

    private int mCurrentUserId = -1;
    private int mLibraryUserId = -1;
    private int mCurrentItemPosition;

    private User mCurrentUser;
    private User mLibraryUser;

    private ArrayList<Game> mGameArrayList;
    private ArrayList<Integer> mGameHours;

    private OwnedGameAdapter mAdapter;
    private RecyclerView game_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        getDatabase();

        validateExtras();

        hookUp();

        setLibraryTitle();

        canDelete();

        createListData();
        mAdapter.notifyDataSetChanged();

    }

    // Ensure user logged in is passed, and we know whose library we're operating on
    private void validateExtras() {
        try{
            mLibraryUserId = getIntent().getIntExtra(LIBRARY_USER_ID, -1);
            mCurrentUserId = getIntent().getIntExtra(USER_ID_KEY, -1);
            if(mLibraryUserId == -1 || mCurrentUserId == -1){
                throw new Exception(getString(R.string.userIdNotPassed));
            }
        } catch(Exception e){
            System.out.println(getString(R.string.IdError));
            Toast.makeText(getApplicationContext(),
                    getString(R.string.IdError), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Ensure the user is allowed to delete a game from the library.
    private void canDelete() {
        mCurrentUser = mBeanLogDAO.getUserById(mCurrentUserId);
        if(mCurrentUser.isAdmin()){
            mAdapter.setOnLongItemClickListener(new onLongItemClickListener() {
                @Override
                public void ItemLongClicked(View v, int position) {
                    mCurrentItemPosition = position;
                    v.showContextMenu();

                }
            });
        }
    }

    // If the user is an admin and clicks on a game, allow them to delete it from library
    private void deleteGame(Game game) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Delete " + game.getTitle() + " from user library?");
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mLibraryUser.removeGame(game);
                mBeanLogDAO.update(mLibraryUser);
                mGameArrayList.remove(game);
                Toast.makeText(getApplicationContext(),
                        game.getTitle() + " deleted from " +
                                mLibraryUser.getUserName() + "'s library!", Toast.LENGTH_LONG);
                recreate();
            }
        });
        alertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        alertBuilder.create().show();
    }

    // Change library title based on ownership
    private void setLibraryTitle() {
        TextView title = findViewById(R.id.libraryTitle);

        // If the user is looking at their own library, change the title accordingly
        if(mCurrentUserId == mLibraryUserId){
            title.setText(R.string.yourLibrary);

            // If an admin is looking at another user's library, change the title accordingly
        } else {
            title.setText(mBeanLogDAO.getUserById(mLibraryUserId).getUserName() + "'s Library");
            TextView desc = (TextView) findViewById(R.id.libraryDescription);
            desc.setText("See all of " + mBeanLogDAO.getUserById(mLibraryUserId).getUserName()
                    + "'s games!");
        }
    }

    // Initialize all the things
    private void hookUp() {

        // Set up RecyclerView
        game_list = findViewById(R.id.gameListView);
        game_list.setLayoutManager(new LinearLayoutManager(this));
        mGameArrayList = new ArrayList<>();
        mGameHours = new ArrayList<>();
        mAdapter = new OwnedGameAdapter(this, mGameArrayList, this, mGameHours);
        game_list.setAdapter(mAdapter);
        game_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        registerForContextMenu(game_list);
    }

    // Populate RecyclerView with game and hours
    private void createListData() {
        HashMap<String, Integer> userGames;
        mLibraryUser = mBeanLogDAO.getUserById(mLibraryUserId);
        userGames = mLibraryUser.getGames();
        List<Game> imported = new ArrayList<>();
        for (String game : userGames.keySet()){
            imported.add(mBeanLogDAO.getGameByTitle(game));
        }

        for (Game game : imported){
            mGameArrayList.add(game);
        }
        for (Integer hours : userGames.values()){
            mGameHours.add(hours);
            System.out.println(hours);
        }

    }

    // Set up context menu actions.
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(contextMenu, view, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.library_menu, contextMenu);

    }

    // Change the playtime of a game for a given user. Useful if they want a refund and played for too long
    private void setPlayTime(Game game) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final EditText hourInput = new EditText(this);
        hourInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        alertBuilder.setView(hourInput);
        alertBuilder.setMessage("Edit time spent by " + mLibraryUser.getUserName() +
                " playing game " + game.getTitle() + ":");
        alertBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (hourInput.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), getText(R.string.enterValue), Toast.LENGTH_LONG).show();
                } else {
                    Integer hours = Integer.parseInt(hourInput.getText().toString());
                    mLibraryUser.setHoursPlayed(game, hours);
                    mBeanLogDAO.update(mLibraryUser);
                    Toast.makeText(getApplicationContext(),
                            "Playtime change succeeded!", Toast.LENGTH_LONG).show();
                    recreate();
                }
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });
        alertBuilder.show();

    }

    // Admin stuff and standalone overrides
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

    public static Intent intentFactory(Context context, int userId, int libraryUserId){
        Intent intent = new Intent(context, LibraryActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        intent.putExtra(LIBRARY_USER_ID, libraryUserId);
        return intent;
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent = GameCardActivity.intentFactory(this, mGameArrayList.get(position).getGameId(), mCurrentUserId);
        startActivity(intent);
    }

    // On restart, reset the menu
    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    // Decide what happens if the context menu is opened
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        Game game = mBeanLogDAO.getGameByTitle(mGameArrayList.
                get(mCurrentItemPosition).
                getTitle());

        switch (menuItem.getItemId()) {

            // If the user selected to change username, password, or balance, create a field popup

            case R.id.editPlayTime:
                setPlayTime(game);
                return true;

            // In case the user wants to delete another user, make a boolean logic popup
            case R.id.deleteGame:
                deleteGame(game);
                return true;
        }

        return false;
    }
}