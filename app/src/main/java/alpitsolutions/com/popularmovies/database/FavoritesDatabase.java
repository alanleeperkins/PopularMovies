package alpitsolutions.com.popularmovies.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

@Database(entities = {FavoritesEntry.class}, version = 1, exportSchema = false)
public abstract class FavoritesDatabase extends RoomDatabase{

    private static final String TAG = FavoritesDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "popularmovies";
    private static FavoritesDatabase sInstance;

    public static synchronized FavoritesDatabase getInstance(Context context)
    {
        if (sInstance == null) {
            Log.d(TAG, "Creating new database instance");
            sInstance = Room.databaseBuilder(context.getApplicationContext(),
                    FavoritesDatabase.class, FavoritesDatabase.DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        Log.d(TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract FavoritesDao favoritesDao();
}
