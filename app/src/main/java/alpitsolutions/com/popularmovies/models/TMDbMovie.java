package alpitsolutions.com.popularmovies.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/***
 * the data of one single movie from TMDb
 * */
public class TMDbMovie {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("overview")
    @Expose
    private String overview;

    @SerializedName("backdrop_path")
    @Expose
    private String backdrop;

    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("poster_path")
    @Expose
    private String posterPath;

    @SerializedName("release_date")
    @Expose
    private String releaseDate;

    @SerializedName("vote_average")
    @Expose
    private float rating;

    public int getId() {

        return id;
    }

    public String getOverview() {

        return overview;
    }

    public String getBackdrop() {

        return backdrop;
    }

    public String getTitle() {

        return title;
    }

    public String getPosterPath() {

        return posterPath;
    }

    public String getReleaseDate() {

        return releaseDate;
    }

    public Float getRating() {

        return rating;
    }

}