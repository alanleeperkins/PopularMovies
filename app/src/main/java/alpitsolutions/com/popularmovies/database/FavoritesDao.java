package alpitsolutions.com.popularmovies.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface FavoritesDao {

    @Query("SELECT * FROM favorites ORDER BY id")
    List<FavoritesEntry> loadAllFavorites();

    @Insert
    void insertFavorite(FavoritesEntry favoritesEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFavorite(FavoritesEntry favoritesEntry);

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    void deleteFavoriteByMovieId(int movieId);

    @Query("DELETE FROM favorites")
    void deleteAllFavorites();

    @Query("SELECT * FROM favorites WHERE movieId = :movieId")
    List<FavoritesEntry> loadFavoriteByMovieId(int movieId);

}
