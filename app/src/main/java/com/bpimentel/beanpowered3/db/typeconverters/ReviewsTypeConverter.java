package com.bpimentel.beanpowered3.db.typeconverters;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import com.bpimentel.beanpowered3.objects.Rating;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ReviewsTypeConverter {
    @TypeConverter
    public String fromRatingList(List<Rating> input){
        ArrayList<Float> stars = new ArrayList<>();
        ArrayList<String> reviews = new ArrayList<>();

        for(Rating rating : input){
            stars.add(rating.getRating());
            reviews.add(rating.getReview());
        }
        return TextUtils.join(",", stars).concat(":")
                .concat(TextUtils.join(",", reviews));

    }

    @TypeConverter
    public List<Rating> toList(String value){
        if(value.length()<5){
            return new ArrayList<Rating>();
        }

        final String[] splitted = value.split(":");
        final String[] keys = splitted[0].split(",");
        final Iterator<String> iter = Arrays.asList(splitted[1].split(",")).iterator();
        final List<Rating> parsed = new ArrayList<>();

        for(String key : keys){
            Rating rating = new Rating(Float.parseFloat(key), iter.next());
            parsed.add(rating);
        }

        return parsed;
    }
}

