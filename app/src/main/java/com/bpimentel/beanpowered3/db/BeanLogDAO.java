package com.bpimentel.beanpowered3.db;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.bpimentel.beanpowered3.objects.Game;

import com.bpimentel.beanpowered3.objects.Refund;
import com.bpimentel.beanpowered3.objects.User;

import java.util.HashMap;
import java.util.List;

@Dao
public interface BeanLogDAO {
    @Insert
    void insert(Game... games);

    @Update
    void update(Game... games);

    @Delete
    void delete(Game game);

    @Query("SELECT * FROM " + AppDatabase.GAMELOG_TABLE + " ORDER BY mTitle DESC")
    List<Game> getAllGames();

    @Query("SELECT * FROM " + AppDatabase.GAMELOG_TABLE + " WHERE mListed = 1 ORDER BY mTitle ASC")
    List<Game> getAllListedGames();

    @Query("SELECT * FROM " + AppDatabase.GAMELOG_TABLE + " WHERE mListed = 0 ORDER BY mTitle ASC")
    List<Game> getAllUnlistedGames();

    @Query("SELECT * FROM " + AppDatabase.GAMELOG_TABLE + " WHERE mGameID = :gameId")
    Game getGameById(int gameId);

    @Query("SELECT * FROM " + AppDatabase.GAMELOG_TABLE + " WHERE mTitle = :title")
    Game getGameByTitle(String title);

    @Query("SELECT * FROM " + AppDatabase.GAMELOG_TABLE + " WHERE mPublisher = :publisher")
    Game getGameByPublisher(String publisher);

    @Insert
    void insert(User... users);

    @Update
    void update(User... users);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM " + AppDatabase.USER_TABLE + " ORDER BY mUserId ASC")
    List<User> getAllUsers();

    @Query("SELECT * FROM " + AppDatabase.USER_TABLE + " WHERE mUserName = :username")
    User getUserByUsername(String username);

    @Query("SELECT * FROM " + AppDatabase.USER_TABLE + " WHERE mUserId = :userId")
    User getUserById(int userId);

    @Insert
    void insert(Refund... refunds);

    @Update
    void update(Refund... refunds);

    @Delete
    void delete(Refund refund);

    @Query("SELECT * FROM " + AppDatabase.REFUND_TABLE + " WHERE mRefundId = :refundId")
    Refund getRefundById(int refundId);

    @Query("SELECT * FROM " + AppDatabase.REFUND_TABLE + " WHERE mUser = :user")
    List<Refund> getRefundsByUser(String user);

    @Query("SELECT * FROM " + AppDatabase.REFUND_TABLE + " WHERE mGame = :game")
    Refund getRefundByGame(String game);

    @Query("SELECT * FROM " + AppDatabase.REFUND_TABLE + " ORDER BY mRefundId ASC")
    List<Refund> getAllRefunds();

}
