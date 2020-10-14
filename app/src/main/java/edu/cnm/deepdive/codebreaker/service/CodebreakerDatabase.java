package edu.cnm.deepdive.codebreaker.service;

import android.app.Application;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import edu.cnm.deepdive.codebreaker.model.dao.GameDao;
import edu.cnm.deepdive.codebreaker.model.entity.Game;
import edu.cnm.deepdive.codebreaker.service.CodebreakerDatabase.Converters;
import java.util.Date;

@Database(entities = {Game.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class CodebreakerDatabase extends RoomDatabase {

  private static final String DB_NAME = "codebreaker_db";

  private static Application context;

  public static void setContext(Application context) {
    CodebreakerDatabase.context = context;
  }

  public static CodebreakerDatabase getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public abstract GameDao getGameDao();

  private static class InstanceHolder {

    private static final CodebreakerDatabase INSTANCE =
        Room.databaseBuilder(context, CodebreakerDatabase.class, DB_NAME)
            .build();

  }

  public static class Converters {

    @TypeConverter
    public static Long dateToLong(Date value) {
      return (value != null) ? value.getTime() : null;
    }

    @TypeConverter
    public static Date longToDate(Long value) {
      return (value != null) ? new Date(value) : null;
    }

  }

}




