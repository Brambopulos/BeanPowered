package com.bpimentel.beanpowered3.objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.bpimentel.beanpowered3.db.AppDatabase;

import java.util.HashMap;
import java.util.Objects;

@Entity(tableName = AppDatabase.USER_TABLE)
public class User {

    @PrimaryKey(autoGenerate = true)
    private int mUserId;

    private String mUserName;
    private String mPassword;
    private boolean isAdmin;
    private double mBalance;
    private HashMap<String, Integer> games;

    public User(String userName, String password, boolean admin) {
        isAdmin = admin;
        mUserName = userName;
        mPassword = password;
        games = new HashMap<>();
        mBalance = 0.0;
    }

    public User(String userName, String password, boolean admin, double money) {
        isAdmin = admin;
        mUserName = userName;
        mPassword = password;
        games = new HashMap<>();
        mBalance = money;
    }

    public void setHoursPlayed(Game game, Integer hours){
        games.replace(game.getTitle(), hours);
    }

    public double getBalance() {
        return mBalance;
    }

    public void setBalance(double balance) {
        mBalance = balance;
    }

    public HashMap<String, Integer> getGames() {
        return games;
    }

    public void setGames(HashMap<String, Integer> games) {
        this.games = games;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Integer playTime(Game game){
        return games.get(game.getTitle());
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }

    public boolean hasGame(String gameTitle){
        return(games.containsKey(gameTitle));
    }

    public Integer getPlayTime(Game game){ return(games.get(game.getTitle()));}

    public boolean hasGame(Game game){
        return(games.containsKey(game.getTitle()));
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public void addGame(Game game){
        games.put(game.getTitle(), 0);
    }

    public void removeGame(Game game) {games.remove(game.getTitle());}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(mUserName, user.mUserName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUserName);
    }

    @Override
    public String toString() {
        return "User{" +
                "mUserId=" + mUserId +
                ", mUserName='" + mUserName + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", isAdmin=" + isAdmin +
                ", mBalance=" + mBalance +
                '}';
    }
}
