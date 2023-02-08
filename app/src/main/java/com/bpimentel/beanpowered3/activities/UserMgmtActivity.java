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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;
import com.bpimentel.beanpowered3.recyclerstuff.UserAdapter;
import com.bpimentel.beanpowered3.recyclerstuff.onLongItemClickListener;

import java.util.ArrayList;
import java.util.List;

    /* UserManagement defines the activity in which an admin user can view, edit, add, and delete
    users from the database. The program will validate that the user attempting to access this
    activity is an admin user before proceeding
    */

public class UserMgmtActivity extends AppCompatActivity {
    private static final String USER_ID_KEY = "com.bpimentel.beanpowered3.userIdKey";
    private BeanLogDAO mBeanLogDAO;
    RecyclerView user_list;
    private int mUserId = -1;
    private ArrayList<User> mUserArrayList;
    private UserAdapter mAdapter;
    private int mCurrentItemPosition;
    private Button mAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        // All onCreates do the same thing in essence, so let this one be the defacto standard that
        // explains them all:
        // Load database with DAO
        getDatabase();

        // Verify extra
        verifyExtra();

        // Hook up the buttons and other elements in the user interface to the code.
        hookUpScreen();

        // Attach listeners (for cleanliness' sake)
        setUpListeners();

        // Populate list for use in RecyclerView
        createListData();
    }

    // Ensure user was passed successfully, and validate that they're an admin.
    private void verifyExtra() {
        try{
            // Ensure that the user has been passed to this context via intent extra
            mUserId = getIntent().getIntExtra(USER_ID_KEY, -1);
            if(mUserId == -1){
                throw new Exception();
            }

            // Ensure that the user is an admin
            // We do this in case the user un-admins themselves and don't have permission to do more
            if(!mBeanLogDAO.getUserById(mUserId).isAdmin()){
                throw new Exception(getString(R.string.userNotAdmin));
            }
        }
        // Push the user back to the Home Page if they do not belong here for some reason.
        catch(Exception e){
            System.out.println(getString(R.string.userIdNotPassed));
            Intent intent = MainActivity.intentFactory(this, mUserId);
            startActivity(intent);
        }
    }

    private void hookUpScreen() {

        // Initialize ArrayList
        mUserArrayList = new ArrayList<>();

        // Attach RecyclerView to view, set layout
        user_list = findViewById(R.id.userListView);
        user_list.setLayoutManager(new LinearLayoutManager(this));

        // Set up adapter, hook it up to Recyclerview, and complete layout
        mAdapter = new UserAdapter(this, mUserArrayList);
        user_list.setAdapter(mAdapter);
        user_list.addItemDecoration(new DividerItemDecoration(this,
                LinearLayoutManager.VERTICAL));

        // Give the RecyclerView a Context Menu
        registerForContextMenu(user_list);

        // Set up button(s)
        mAddUser = findViewById(R.id.addUser);
    }

    private void setUpListeners() {

        // Set up listener for long press of a RecyclerView item
        mAdapter.setOnLongItemClickListener(new onLongItemClickListener() {
            @Override
            public void ItemLongClicked(View v, int position) {
                mCurrentItemPosition = position;
                v.showContextMenu();
            }
        });

        // Set up listener for add user button
        mAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser();
            }
        });
    }

    // Get list ready with all users in the database
    private void createListData() {
        List<User> imported = mBeanLogDAO.getAllUsers();
        for(User user : imported){
            mUserArrayList.add(user);
        }
    }

    // Begin an addUser activity, to do that thing it says its gonna do.
    private void addUser(){
        Intent intent = AddUserActivity.intentFactory(getApplicationContext());
        startActivity(intent);
    }

    // Prep this intent
    public static Intent intentFactory(Context context, int userId){
        Intent intent = new Intent(context, UserMgmtActivity.class);
        intent.putExtra(USER_ID_KEY, userId);
        return intent;
    }

    // Set up context menu actions.
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(contextMenu, view, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_mgmt_menu, contextMenu);

    }

    // Causes the display to redraw if returned to, allows new users to populate into RecyclerView
    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    // Decide what happens if the context menu is opened
    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        User user = mBeanLogDAO.getUserByUsername(mUserArrayList.
                get(mCurrentItemPosition).
                getUserName());

        switch (menuItem.getItemId()) {

            // If the user selected to change username, password, or balance, create a field popup
            case R.id.modName:
            case R.id.modPass:
            case R.id.modBal:
                setField(user, menuItem);
                return true;

            // If editing a user's library, bring them to the Library activity with the userId;
            case R.id.editLib:
                Intent intent = LibraryActivity.intentFactory(this, mUserId, user.getUserId());
                startActivity(intent);
                return true;

            // In case the user wants to mod priv, make it a simple boolean logic popup
            case R.id.modPriv:
                setPrivilege(user);
                return true;

            // In case the user wants to delete another user, make a boolean logic popup
            case R.id.deleteUser:
                deleteUser(user);
                return true;
        }

        return false;
    }

    // Create a popup window that checks to make sure the user doesn't delete itself, and confirms
    private void deleteUser(User user) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Delete user " + user.getUserName().toString() + getString(R.string.questionMark));
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(user.equals(mBeanLogDAO.getUserById(mUserId))){
                    Toast.makeText(getApplicationContext(),
                            "You cannot delete yourself!", Toast.LENGTH_LONG).show();
                    return;
                }
                mBeanLogDAO.delete(user);
                mUserArrayList.remove(user);
                Toast.makeText(getApplicationContext(),
                        "User deleted... You animal", Toast.LENGTH_LONG).show();
                recreate();
            }
        });
        alertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        alertBuilder.create().show();
    }

    // Create a popup window that will flip the value for the selected user's admin status
    private void setPrivilege(User user) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Change admin status for user "+ user.getUserName() +"?");
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.setAdmin(!user.isAdmin());
                mBeanLogDAO.update(user);
                Toast.makeText(getApplicationContext(),
                        "Privilege changed!", Toast.LENGTH_LONG).show();
                recreate();
            }
        });
        alertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { }
        });

        alertBuilder.create().show();
    }

    // Create a popup window with a field that will update one of three user attributes.
    private void setField(User user, MenuItem item) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);

        if (item.equals(R.id.modPass)) {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            input.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        alertBuilder.setView(input);
        alertBuilder.setMessage(item.getTitle() + " for user " + user.getUserName() + ":");
        alertBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(input.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), getText(R.string.enterValue), Toast.LENGTH_LONG).show();
                } else {
                    String textInput = input.getText().toString();

                    if (item.getTitle().equals("Change Username")) {
                        user.setUserName(textInput);
                    } else if (item.getTitle().equals("Change Password")) {
                        user.setPassword(textInput);
                    } else if (item.getTitle().equals("Change Balance")) {
                        user.setBalance(Double.parseDouble(textInput));
                    }
                    mBeanLogDAO.update(user);
                    Toast.makeText(getApplicationContext(),
                            item.getTitle() + " succeeded!", Toast.LENGTH_LONG).show();
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