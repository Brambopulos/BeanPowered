package com.bpimentel.beanpowered3.objects;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.bpimentel.beanpowered3.db.AppDatabase;

@Entity(tableName = AppDatabase.REFUND_TABLE)
public class Refund {

    @PrimaryKey(autoGenerate = true)
    private Integer mRefundId;
    private String mReason;
    private String mUser;
    private String mGame;

    public Refund(String reason, String user, String game) {
        this.mReason = reason;
        this.mUser = user;
        this.mGame = game;
    }

    public Integer getRefundId() {
        return mRefundId;
    }

    public void setRefundId(Integer mRefundId) {
        this.mRefundId = mRefundId;
    }

    public String getReason() {
        return mReason;
    }

    public void setReason(String mReason) {
        this.mReason = mReason;
    }

    public String getUser() {
        return mUser;
    }

    public void setUser(String mUser) {
        this.mUser = mUser;
    }

    public String getGame() {
        return mGame;
    }

    public void setGame(String mGame) {
        this.mGame = mGame;
    }
}


