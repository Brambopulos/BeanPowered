package com.bpimentel.beanpowered3.recyclerstuff;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.User;

import java.util.ArrayList;



public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> implements onLongItemClickListener {
    // 2 Classes, an adapter and a holder.

    // Adapter
    private Context mContext;
    private ArrayList<User> users;
    private int position;
    private onLongItemClickListener mOnLongItemClickListener;

    public UserAdapter(Context context, ArrayList<User> users) {
        this.mContext = context;
        this.users = users;
    }

    public void setOnLongItemClickListener(onLongItemClickListener onLongItemClickListener) {
        mOnLongItemClickListener = onLongItemClickListener;
    }

    @Override
    public void ItemLongClicked(View v, int position) {

    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @NonNull
    @Override
    public UserAdapter.UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(mContext).inflate
                (R.layout.user_recycler,parent,false);

        return new UserAdapter.UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserHolder holder, @SuppressLint("RecyclerView") int position) {
        User user = users.get(position);
        holder.setDetails(user);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mOnLongItemClickListener != null){
                    mOnLongItemClickListener.ItemLongClicked(view, position);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class UserHolder extends RecyclerView.ViewHolder {

        private final TextView username, password, userBalance, adminStatus;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            password = itemView.findViewById(R.id.password);
            userBalance = itemView.findViewById(R.id.userBalance);
            adminStatus = itemView.findViewById(R.id.adminStatus);


        }

        void setDetails(User user) {
            username.setText(user.getUserName());
            password.setText(" / " + user.getPassword());
            userBalance.setText("$" + format("%.2f", user.getBalance()));
            if(user.isAdmin()){
                adminStatus.setText("Admin");
            } else {
                adminStatus.setText("Normie");
            }
        }

    }
}

