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

import com.bpimentel.beanpowered3.R;
import com.bpimentel.beanpowered3.objects.Game;

import java.util.ArrayList;

import kotlin.reflect.KVisibility;

public class OwnedGameAdapter extends RecyclerView.Adapter<OwnedGameAdapter.OwnedGameHolder> implements onLongItemClickListener{
    private final ItemClicker gameListener;
    private Context mContext;
    private ArrayList<Game> games;
    private ArrayList<Integer> mHours;
    private onLongItemClickListener mOnLongItemClickListener;

    public OwnedGameAdapter(Context context, ArrayList<Game> games,
                            ItemClicker gameListener, ArrayList<Integer> mHours) {
        this.gameListener = gameListener;
        this.mContext = context;
        this.games = games;
        this.mHours = mHours;
    }

    public void setOnLongItemClickListener(onLongItemClickListener onLongItemClickListener) {
        mOnLongItemClickListener = onLongItemClickListener;
    }

    @NonNull
    @Override
    public OwnedGameHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate
                (R.layout.owned_game_recycler,parent,false);

        return new OwnedGameHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnedGameAdapter.OwnedGameHolder holder, int position) {
        Game game = games.get(position);
        System.out.println(game);
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

    public class OwnedGameHolder extends RecyclerView.ViewHolder{

        private TextView gameTitle, gamePub, gameHours;
        private RatingBar gameRating;

        public OwnedGameHolder(@NonNull View itemView) {
            super(itemView);
            gameTitle = itemView.findViewById(R.id.ownedGameTitle);
            gamePub = itemView.findViewById(R.id.ownedGamePub);
            gameHours = itemView.findViewById(R.id.ownedGameHours);
            gameRating = itemView.findViewById(R.id.ownedGameRating);

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
            TextView noRatings = itemView.findViewById(R.id.ownedGameNoRatings);
            noRatings.setVisibility(View.INVISIBLE);
            gameTitle.setText(game.getTitle());
            gamePub.setText("By: " + game.getPublisher());
            gameHours.setText(mHours.get(games.indexOf(game)) + " hours");

            if(game.getRatings().size() > 0){
                gameRating.setVisibility(View.VISIBLE);
                gameRating.setRating(game.getSumOfRatings());
            }
        }

    }





}
