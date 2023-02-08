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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.style.TtsSpan;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.recyclerstuff.GameAdapter;
import com.bpimentel.beanpowered3.recyclerstuff.ItemClicker;
import com.bpimentel.beanpowered3.recyclerstuff.onLongItemClickListener;

import java.util.ArrayList;
import java.util.List;

// StoreActivity defines how the store activity works
// The store will allow the user to go to one of multiple game pages, much like Steam.
public class StoreActivity extends AppCompatActivity implements ItemClicker {
    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";

    private BeanLogDAO mBeanLogDAO;

    private int mUserId = -1;
    private int mCurrentItemPosition;

    private RecyclerView game_list;
    private GameAdapter mAdapter;
    private EditText mSearch;

    private boolean isFiltered = false;

    private ArrayList<Game> mGameArrayList;
    private ArrayList<Game> mFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        getDatabase();
        validateExtras();
        hookUp();
        setUpListener(mAdapter);

        setDisplayBalance();

        createListData();
    }

    private void setUpListener(GameAdapter adapter) {
        // Set up listener for long press of a RecyclerView item
        adapter.setOnLongItemClickListener(new onLongItemClickListener() {
            @Override
            public void ItemLongClicked(View v, int position) {
                mCurrentItemPosition = position;
                v.showContextMenu();
            }
        });
    }

    // Ensure user passed from last intent
    private void validateExtras() {
        try{
            mUserId = getIntent().getIntExtra(USER_ID_KEY, -1);
            if(mUserId == -1){
                throw new Exception(getString(R.string.userIdNotPassed));
            }
        } catch(Exception e){
            System.out.println(getString(R.string.userIdNotPassed));
            Toast.makeText(getApplicationContext(),
                    getString(R.string.IdError), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Configure the balance shown for the user
    private void setDisplayBalance() {
        User current = mBeanLogDAO.getUserById(mUserId);
        TextView balance = findViewById(R.id.storeBalance);
        balance.setText("$" + format("%.2f", current.getBalance()));
    }


    // Set up Views and configure search bar to filter the RecyclerView
    private void hookUp(){
        game_list = findViewById(R.id.gameListView);
        game_list.setLayoutManager(new LinearLayoutManager(this));
        mGameArrayList = new ArrayList<>();
        mFilter = new ArrayList<>();
        mAdapter = new GameAdapter(this, mGameArrayList, this);
        game_list.setAdapter(mAdapter);
        game_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        registerForContextMenu(game_list);

        mSearch = findViewById(R.id.storeSearch);
        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mFilter.clear();

                if(editable.toString().isEmpty()){
                    isFiltered = false;
                    resetAdapter();

                } else {
                    isFiltered = true;
                    filter(editable.toString());

                }
            }
        });


    }

    // Set up context menu actions.
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(contextMenu, view, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_mgmt_menu, contextMenu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        Game game;
        if(isFiltered){
            game = mBeanLogDAO.getGameByTitle(mFilter
                    .get(mCurrentItemPosition).
                    getTitle());
        } else {
            game = mBeanLogDAO.getGameByTitle(mGameArrayList
                    .get(mCurrentItemPosition).
                    getTitle());
        }


        switch (menuItem.getItemId()) {

            // If the user selected to change title, publisher, or price, create an EditText
            case R.id.storeEditTitle:
            case R.id.storeEditPub:
            case R.id.storeEditPrice:
                setField(game, menuItem);
                return true;

            // In case the user wants to delete another user, make a boolean logic popup
            case R.id.storeDeleteGame:
                deleteGame(game);
                return true;
        }

        return false;
    }

    private void deleteGame(Game game) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Delete game " + game.getTitle() + getString(R.string.questionMark));
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mBeanLogDAO.delete(game);
                mGameArrayList.remove(game);
                Toast.makeText(getApplicationContext(), "Game deleted",
                        Toast.LENGTH_SHORT).show();
                recreate();
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        alertBuilder.show();
    }

    private void setField(Game game, MenuItem menuItem) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);

        if(menuItem.equals(R.id.storeEditPrice)){
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        alertBuilder.setView(input);
        alertBuilder.setMessage(menuItem.getTitle() + " for game " + game.getTitle() + ":");
        alertBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(input.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), getText(R.string.enterValue), Toast.LENGTH_LONG).show();
                } else {
                    String textInput = input.getText().toString();

                    if (menuItem.getItemId() == R.id.storeEditTitle) {
                        game.setTitle(textInput);
                    } else if (menuItem.getItemId() == R.id.storeEditPub) {
                        game.setPublisher(textInput);
                    } else if (menuItem.getItemId() == R.id.storeEditPrice) {
                        game.setPrice(Double.parseDouble(textInput));
                    }
                    mBeanLogDAO.update(game);
                    createListData();
                    Toast.makeText(getApplicationContext(),
                            menuItem.getTitle() + " succeeded!", Toast.LENGTH_LONG).show();
                    recreate();
                }
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        alertBuilder.create().show();
    }

    // Generate data for the unfiltered list of games
    private void createListData() {
        // Method is for adding the data to the recyclerView
        // Make adapter and game object class
        List<Game> imported = mBeanLogDAO.getAllListedGames();
        for (Game game : imported) {
            mGameArrayList.add(game);
        }
    }

    // Reset the adapter after an update
    private void resetAdapter() {
        game_list.setAdapter(mAdapter);
        registerForContextMenu(game_list);
    }

    // Add a game to the filter if it matches the search text, by publisher or by title
    private void filter(String toString) {
        for(Game game : mGameArrayList){
            if(game.getTitle().toLowerCase().contains(toString.toLowerCase()) ||
                    game.getPublisher().toLowerCase().contains(toString.toLowerCase())){
                mFilter.add(game);
            }
            GameAdapter mFilterAdapter = new GameAdapter(this, mFilter, this);
            game_list.setAdapter(mFilterAdapter);
            registerForContextMenu(game_list);
            setUpListener(mFilterAdapter);

            mAdapter.notifyDataSetChanged();
        }
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

    public static Intent intentFactory(Context context, int userId){
        Intent intent = new Intent(context, StoreActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        return intent;
    }

    @Override
    public void onItemClick(View v, int position) {
        Intent intent;
        if(isFiltered) {
            intent = GameCardActivity.intentFactory(this,
                    mFilter.get(position).getGameId(), mUserId);
        } else {
            intent = GameCardActivity.intentFactory(this,
                    mGameArrayList.get(position).getGameId(), mUserId);
        }
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        game_list.getAdapter().notifyDataSetChanged();

    }

    @Override
    public void onRestart() {
        super.onRestart();
        recreate();
    }
}