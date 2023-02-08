package com.bpimentel.beanpowered3.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;

// This activity allows an admin to create a new User and specify the User's attributes.
public class AddUserActivity extends AppCompatActivity {
    private BeanLogDAO mBeanLogDAO;

    private EditText mUserName;
    private EditText mUserPass;
    private EditText mBalance;

    private CheckBox mAdminCheck;

    private Button mAddUser;
    private Button mCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        getDatabase();

        hookUp();

        setListeners();

    }

    // Configure on-screen elements
    private void hookUp() {
        mAddUser = findViewById(R.id.submitUser);
        mCancel = findViewById(R.id.addUserCancel);

        mAdminCheck = findViewById(R.id.adminCheck);

        mUserName = findViewById(R.id.newUserName);
        mUserPass = findViewById(R.id.newUserPass);
        mBalance = findViewById(R.id.newUserBal);
    }

    // Configure listeners for both buttons
    private void setListeners(){
        mAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser();
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { finish(); }
        });
    }

    // Main function, retrieve user input and apply them to a new User
    private void addUser() {
        String username;
        String password;
        double balance;
        boolean admin;

        // If any of the EditText fields are empty, don't do anything and report an error
        if(TextUtils.isEmpty(mUserName.getText().toString()) ||
                TextUtils.isEmpty(mUserPass.getText().toString()) ||
                TextUtils.isEmpty(mBalance.getText().toString())){
            Toast.makeText(getApplicationContext(),
                    getText(R.string.emptyFields), Toast.LENGTH_LONG).show();
            Log.d("BEANPOWERED", getString(R.string.emptyFields));
        } else {

            // Set local variables to user input
            username = mUserName.getText().toString();
            if(mBeanLogDAO.getUserByUsername(username) != null){
                Toast.makeText(this, "User: " + username + " already exists",
                        Toast.LENGTH_SHORT).show();
                return;
            }


            password = mUserPass.getText().toString();
            balance = Double.parseDouble(mBalance.getText().toString());
            admin = mAdminCheck.isChecked();

            // Create new user with user input
            User newUser = new User(username, password, admin, balance);

            // Add new user to database
            mBeanLogDAO.insert(newUser);

            // Return to previous activity
            finish();
        }
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

    public static Intent intentFactory(Context context){
        Intent intent = new Intent(context, AddUserActivity.class);
        return intent;
    }

}