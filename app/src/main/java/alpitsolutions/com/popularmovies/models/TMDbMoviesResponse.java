package alpitsolutions.com.popularmovies.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/***
 * the full response of an movies request [not a single movie]
 */
public class TMDbMoviesResponse {

    /*shows the page we're currently in*/
    @SerializedName("page")
    @Expose
    private int page;

    /* shows the full number of possible results */
    @SerializedName("total_results")
    @Expose
    private int totalResults;

    /* a list of the movies we got from the tmdb.org */
    @SerializedName("results")
    @Expose
    private List<TMDbMovie> movies;

    /* shows the number of possible pages we could request */
    @SerializedName("total_pages")
    @Expose
    private int totalPages;

    public int getPage() {

        return page;
    }

    public List<TMDbMovie> getMovies() {
        return movies;
    }
}
