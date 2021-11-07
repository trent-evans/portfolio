package com.example.knight_radiant_app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {User.class}, version = 2, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    public static volatile UserDatabase dbInstance;
    public abstract UserDao userDao();

    static synchronized UserDatabase getDatabase(final Context context){
        if(dbInstance == null){
            dbInstance = Room.databaseBuilder(context.getApplicationContext(),
                    UserDatabase.class, "user.db").build();
        }
        return dbInstance;
    }

//    static final Migration MIGRATION_1_2 = new Migration(1,2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE user_table ADD COLUMN authenticate TEXT NOT NULL DEFAULT 'password'");
//        }
//    };

//    static final Migration MIGRATION_1_3 = new Migration(1,3) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE user_table RENAME COLUMN fitness_goals TO verify_user");
//        }
//    };

//    static final Migration MIGRATION_2_3 = new Migration(2,3) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE user_table RENAME COLUMN fitness_goals TO verify_user");
//        }
//    };
}
