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
import com.bpimentel.beanpowered3.objects.Rating;
import com.bpimentel.beanpowered3.objects.Refund;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.recyclerstuff.ItemClicker;
import com.bpimentel.beanpowered3.recyclerstuff.RatingAdapter;
import com.bpimentel.beanpowered3.recyclerstuff.RefundAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RefundMgmtActivity extends AppCompatActivity implements ItemClicker {
    private BeanLogDAO mBeanLogDAO;
    private RecyclerView refund_list;
    private RefundAdapter mAdapter;
    private ArrayList<Refund> mRefundList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund_mgmt);

        getDatabase();
        hookUp();
        createListData();

    }

    // Set up views and initialize RecyclerView
    private void hookUp(){
        mRefundList = new ArrayList<>();

        refund_list = findViewById(R.id.refundRecycler);
        refund_list.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RefundAdapter(this, mRefundList, this);
        refund_list.setAdapter(mAdapter);
        refund_list.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


    }

    // Create data for RecyclerView
    private void createListData() {
        List<Refund> imported = mBeanLogDAO.getAllRefunds();
        for(Refund refund : imported){
            mRefundList.add(refund);
        }
    }

    // If request denied, get rid of the request
    private void denyRequest(Refund refund) {
        mRefundList.remove(refund);
        mBeanLogDAO.delete(refund);
        recreate();
    }

    // If request approved, give the user back their money, remove the game from their library
    private void approveRequest(Refund refund) {
        User user = mBeanLogDAO.getUserByUsername(refund.getUser());
        Game game = mBeanLogDAO.getGameByTitle(refund.getGame());
        user.removeGame(game);
        user.setBalance(game.getPrice() + user.getBalance());
        if(mBeanLogDAO.getAllUsers().contains(mBeanLogDAO.getUserByUsername(game.getPublisher()))){
            User publisher = mBeanLogDAO.getUserByUsername(game.getPublisher());
            publisher.setBalance(publisher.getBalance() - game.getPrice());
            mBeanLogDAO.update(publisher);
        }
        mBeanLogDAO.update(user);
        mRefundList.remove(refund);
        mBeanLogDAO.delete(refund);
        Toast.makeText(this,
                "Request accepted, " + user.getUserName() + " refunded",
                Toast.LENGTH_SHORT).show();
        recreate();
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

    public static Intent intentFactory(Context context){
        Intent intent = new Intent(context, RefundMgmtActivity.class);
        return intent;
    }

    @Override
    public void onItemClick(View v, int position) {
        Refund refund = mBeanLogDAO.getRefundById(mRefundList.get(position).getRefundId());
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Approve refund request?");
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                approveRequest(refund);
            }
        });
        alertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                denyRequest(refund);
            }
        });
        alertBuilder.setNeutralButton(R.string.back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });

        alertBuilder.create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        mAdapter.notifyDataSetChanged();
    }
}