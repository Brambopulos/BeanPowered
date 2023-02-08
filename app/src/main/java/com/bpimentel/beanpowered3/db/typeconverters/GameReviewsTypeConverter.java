package com.bpimentel.beanpowered3.db.typeconverters;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import com.bpimentel.beanpowered3.objects.Rating;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class GameReviewsTypeConverter {
    @TypeConverter
    public String fromHashMap(HashMap<String, Rating> input){
        return TextUtils.join(",", input.keySet()).concat(":::")
                .concat(TextUtils.join(",,,", input.values()));
    }

    @TypeConverter
    public HashMap<String, Rating> toHashMap(String value){
        if(value.length()<5){
            return new HashMap<>();
        }
        final String[] splitted = value.split(":::");
        final String[] keys = splitted[0].split(",");
        final Iterator<String> iter = Arrays.asList(splitted[1].split(",,,")).iterator();
        final HashMap<String, Rating> parsed = new HashMap<>();
        for(String key : keys){
            String ratingString = iter.next();
            String[] ratingComponents = ratingString.split("=");

            Rating rating = new Rating(Float.parseFloat(ratingComponents[0]),
                    ratingComponents[1]);
            parsed.put(key, rating);
        }

        return parsed;
    }
}




