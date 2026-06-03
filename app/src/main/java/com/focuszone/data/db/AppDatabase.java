package com.focuszone.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {SessionEntity.class}, version = 2, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "focuszone.db";
    private static volatile AppDatabase instance;

    public abstract SessionDao sessionDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_completedAt ON sessions(completedAt)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_sessions_type_wasSkipped ON sessions(type, wasSkipped)");
        }
    };
}
