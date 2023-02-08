package com.bpimentel.beanpowered3.objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.bpimentel.beanpowered3.db.AppDatabase;

import java.util.HashMap;
import java.util.Objects;

@Entity(tableName = AppDatabase.GAMELOG_TABLE)
public class Game {

    @PrimaryKey(autoGenerate = true)
    private int mGameId;

    private String mTitle;
    private String mPublisher;
    private double mPrice;
    private boolean mListed;
    private HashMap<String, Rating> ratings;

    public Game(String title, String publisher, double price, boolean listed) {
        mTitle = title;
        mPublisher = publisher;
        mPrice = price;
        this.ratings = new HashMap<>();
        mListed = listed;
    }

    @Override
    public String toString() {
        return "Game{" +
                "mTitle='" + mTitle + '\'' +
                ", mPublisher='" + mPublisher + '\'' +
                ", mPrice=" + mPrice +
                '}';
    }

    public boolean isListed() {
        return mListed;
    }

    public void setListed(boolean mListed) {
        this.mListed = mListed;
    }

    public int getGameId() {
        return mGameId;
    }

    public void setGameId(int gameId) {
        mGameId = gameId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean beenRatedBy(User user){
        if(ratings.isEmpty()) return false;

        if(ratings.keySet().contains(user.getUserName())){ return true;}
        return false;
    }

    public boolean beenRatedBy(String user){
        if(ratings.keySet().contains(user)){ return true;}
        return false;
    }

    public void addRating(User user, Rating rating){
        ratings.put(user.getUserName(), rating);
    }

    public void removeRating(String user){
        ratings.remove(user);
    }

    public void addRating(String user, Rating rating){
        ratings.put(user, rating);
    }

    public String getPublisher() {
        return mPublisher;
    }

    public Float getSumOfRatings(){
        Float avg = Float.valueOf(0);
        Integer count = 0;
        for(Rating rating : ratings.values()){
            avg += rating.getRating();
            count++;
        }
        if(count > 0){
            return avg/count;
        } else {
            return Float.valueOf(0);
        }

    }

    public void setPublisher(String publisher) {
        mPublisher = publisher;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return mTitle.equals(game.mTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTitle);
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }

    public HashMap<String, Rating> getRatings() {
        return ratings;
    }

    public void setRatings(HashMap<String, Rating> ratings) {
        this.ratings = ratings;
    }
}
