package com.bpimentel.beanpowered3.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.bpimentel.beanpowered3.db.typeconverters.GameReviewsTypeConverter;
import com.bpimentel.beanpowered3.objects.Game;
import com.bpimentel.beanpowered3.objects.Refund;
import com.bpimentel.beanpowered3.objects.User;
import com.bpimentel.beanpowered3.db.typeconverters.DateTypeConverter;
import com.bpimentel.beanpowered3.db.typeconverters.GameHoursTypeConverter;
import com.bpimentel.beanpowered3.db.typeconverters.ReviewsTypeConverter;

@Database(entities = {Game.class, User.class, Refund.class}, version = 4)
@TypeConverters({DateTypeConverter.class, GameHoursTypeConverter.class,
        ReviewsTypeConverter.class, GameReviewsTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public static final String DB_NAME = "gameDB";
    public static final String GAMELOG_TABLE = "gameTable";
    public static final String USER_TABLE = "userTable";
    public static final String REFUND_TABLE = "refundTable";

    public abstract BeanLogDAO getBeanLogDAO();
}
