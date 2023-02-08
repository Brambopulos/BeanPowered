package com.bpimentel.beanpowered3.db.typeconverters;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateTypeConverter {
    @TypeConverter
    public long dateToLong(Date date){
        return date.getTime();
    }

    @TypeConverter
    public Date longToDate(long time){
        return new Date(time);
    }
}
