package com.bpimentel.beanpowered3.recyclerstuff;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.Rating;

import java.util.ArrayList;
import java.util.HashMap;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingHolder> implements onLongItemClickListener {
    private Context mContext;
    private HashMap<String, Rating> userRatings;
    private ArrayList<Rating> ratings;
    private ArrayList<String> users;
    private int position;
    private onLongItemClickListener mOnLongItemClickListener;

    public RatingAdapter(Context mContext, HashMap<String, Rating> userRatings) {
        this.mContext = mContext;
        this.userRatings = userRatings;

    }

    public void setOnLongItemClickListener(onLongItemClickListener onLongItemClickListener) {
        mOnLongItemClickListener = onLongItemClickListener;
    }

    @NonNull
    @Override
    public RatingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate
                (R.layout.rating_recycler,parent,false);

        return new RatingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingAdapter.RatingHolder holder, @SuppressLint("RecyclerView") int position) {
        users = new ArrayList<>();
        ratings = new ArrayList<>();
        for (String user : userRatings.keySet()) {
            System.out.println("It's passing the constructor");
            users.add(user);
            System.out.println(user +  ", see?");

            ratings.add(userRatings.get(user));
            System.out.println(userRatings.get(user) + " right??");
        }

        Rating rating = ratings.get(position);
        System.out.println(ratings);
        String user = users.get(position);
        System.out.println(users);
        holder.setDetails(rating, user);

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

    public Rating getRatingByPosition(int position){
        return ratings.get(position);
    }

    public String getUserWhoRated(Rating rating){
        return users.get(ratings.indexOf(rating));
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int getItemCount() {
        return userRatings.size();
    }

    @Override
    public void ItemLongClicked(View v, int position) {

    }

    public class RatingHolder extends RecyclerView.ViewHolder {

        private final TextView review, username;
        private final RatingBar stars;

        public RatingHolder(@NonNull View itemView) {
            super(itemView);
            review = itemView.findViewById(R.id.recyclerReview);
            username = itemView.findViewById(R.id.reviewUser);
            stars = itemView.findViewById(R.id.recyclerRatingBar);


        }

        void setDetails(Rating rating, String user) {
            System.out.println(user + " is gay");
            username.setText(user);
            review.setText(rating.getReview());
            System.out.println(rating.getReview());
            if(rating.getRating() != 0){
                stars.setRating(rating.getRating());
                System.out.println(rating.getRating());
            }
        }
    }
}
