package com.bpimentel.beanpowered3.objects;

public class Rating {
    private Float mRating;
    private String mReview;
    public Rating(Float mRating, String mReview) {
        this.mRating = mRating;
        this.mReview = mReview;
    }

    public Float getRating() {
        return mRating;
    }

    public String getReview() {
        return mReview;
    }

    @Override
    public String toString() {
        return mRating + "=" + mReview;
    }
}
