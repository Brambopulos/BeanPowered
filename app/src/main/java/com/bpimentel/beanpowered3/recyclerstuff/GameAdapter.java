package com.bpimentel.beanpowered3.recyclerstuff;

import static java.lang.String.format;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.R;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameHolder> implements onLongItemClickListener{
    // 2 Classes, an adapter and a holder.

    // Adapter
    private final ItemClicker gameListener;
    private Context mContext;
    private ArrayList<Game> games;
    private onLongItemClickListener mOnLongItemClickListener;

    public GameAdapter(Context context, ArrayList<Game> games, ItemClicker gameListener) {
        this.gameListener = gameListener;
        this.mContext = context;
        this.games = games;
    }

    @NonNull
    @Override
    public GameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate
                (R.layout.store_game_recycler,parent,false);

        return new GameHolder(view);
    }

    public ArrayList<Game> getListOfGames(){
        return this.games;
    }

    public void setOnLongItemClickListener(onLongItemClickListener onLongItemClickListener) {
        mOnLongItemClickListener = onLongItemClickListener;
    }

    @Override
    public void onBindViewHolder(@NonNull GameAdapter.GameHolder holder, int position) {
        Game game = games.get(position);
        holder.setDetails(game);

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
        return games.size();
    }

    @Override
    public void ItemLongClicked(View v, int position) {

    }

    class GameHolder extends RecyclerView.ViewHolder{

        private TextView gameTitle, gamePub, gamePrice, gameNoReviews;
        private RatingBar gameReviews;

        public GameHolder(@NonNull View itemView) {
            super(itemView);
            gameTitle = itemView.findViewById(R.id.gameTitle);
            gamePub = itemView.findViewById(R.id.gamePub);
            gamePrice = itemView.findViewById(R.id.gamePrice);
            gameReviews = itemView.findViewById(R.id.storeRating);
            gameNoReviews = itemView.findViewById(R.id.storeNoRatings);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(gameListener != null){
                        int position = getAdapterPosition();

                        if(position != RecyclerView.NO_POSITION){
                            gameListener.onItemClick(view, position);
                        }
                    }
                }
            });

        }

        void setDetails(Game game){
            gameTitle.setText(game.getTitle());
            gamePub.setText("By: " + game.getPublisher());
            gamePrice.setText("$" + format("%.2f", game.getPrice()));
            if(game.getRatings().size() > 0){
                gameReviews.setVisibility(View.VISIBLE);
                gameReviews.setRating(game.getSumOfRatings());
                gameNoReviews.setVisibility(View.INVISIBLE);
            }
        }

    }

}
