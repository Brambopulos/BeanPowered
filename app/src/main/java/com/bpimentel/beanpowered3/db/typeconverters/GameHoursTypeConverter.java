package com.bpimentel.beanpowered3.db.typeconverters;

import android.text.TextUtils;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class GameHoursTypeConverter {
    @TypeConverter
    public String fromHashMap(HashMap<String, Integer> input){
        return TextUtils.join(",", input.keySet()).concat(":")
                .concat(TextUtils.join(",", input.values()));
    }

    @TypeConverter
    public HashMap<String, Integer> toHashMap(String value){
        if(value.length()<5){
            return new HashMap<>();
        }

        final String[] splitted = value.split(":");
        final String[] keys = splitted[0].split(",");
        final Iterator<String> iter = Arrays.asList(splitted[1].split(",")).iterator();
        final HashMap<String, Integer> parsed = new HashMap<>();
        for(String key : keys){
            Integer hours = Integer.parseInt(iter.next());
            parsed.put(key, hours);
        }

        return parsed;
    }
}




