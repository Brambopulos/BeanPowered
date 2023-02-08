package com.bpimentel.beanpowered3.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.db.AppDatabase;
import com.bpimentel.beanpowered3.db.BeanLogDAO;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private BeanLogDAO mBeanLogDAO;

    private TextView mDebug;

    private EditText mUsernameField;
    private EditText mPasswordField;

    private String mUsername;
    private String mPassword;

    private Button mButton;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        wireDisplay();

        getDatabase();

//        debug();

    }

    // Debug that shows out users and games
    private void debug() {
        mDebug = findViewById(R.id.loginDebug);
        mDebug.setVisibility(View.VISIBLE);
        mDebug.setMovementMethod(new ScrollingMovementMethod());
        List<User> users = mBeanLogDAO.getAllUsers();

        StringBuilder sb = new StringBuilder();

        sb.append("All users:\n");

        for(User u : users){
            sb.append(u);
            sb.append("\n");
        }


        sb.append("All games\n");
        List<Game> games = mBeanLogDAO.getAllGames();
        for(Game game : games){
            sb.append(game);
        }

        mDebug.setText(sb.toString());
    }

    // Hook up Views (see, I'm learning!)
    private void wireDisplay() {
        mUsernameField = findViewById(R.id.editTextLoginName);
        mPasswordField = findViewById(R.id.editTextLoginPassword);

        mButton = findViewById(R.id.buttonLogin);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getValuesFromDisplay();
                if(checkForUserInDB()){
                    if(!validatePassword()) {
                        Toast.makeText(LoginActivity.this, "Invalid Password!", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = MainActivity.intentFactory(getApplicationContext(), mUser.getUserId());
                        startActivity(intent);
                    }
                }
            }
        });
    }

    // Ensure user entered their password correctly
    private boolean validatePassword() {
        return mUser.getPassword().equals(mPassword);
    }

    private void getValuesFromDisplay() {
        mUsername = mUsernameField.getText().toString();
        mPassword = mPasswordField.getText().toString();
    }

    // Ensure user exists in the database
    private boolean checkForUserInDB() {
        mUser = mBeanLogDAO.getUserByUsername(mUsername);
        if(mUser == null){
            Toast.makeText(this, "no user " + mUsername + " found", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Admin crap
    private void getDatabase(){
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
        Intent intent = new Intent(context, LoginActivity.class);

        return intent;
    }

}