package alpitsolutions.com.popularmovies.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoritesEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int movieId;
    private String overview;
    private String backdrop;
    private String title;
    private String posterPath;
    private String releaseDate;
    private float rating;

    @Ignore
    public FavoritesEntry(int movieId) {
        this.movieId = movieId;
    }

    public FavoritesEntry(int id, int movieId, String overview, String backdrop, String title, String posterPath, String releaseDate, float rating) {
        this.id = id;
        this.movieId = movieId;
        this.overview = overview;
        this.backdrop = backdrop;
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }


    @Override
    public String toString() {
        return "FID:"+id+" MID:"+movieId+" Title:"+title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(String backdrop) {
        this.backdrop = backdrop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }
}
