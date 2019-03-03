package alpitsolutions.com.popularmovies.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TMDbTrailerResponse {
    @SerializedName("results")
    @Expose
    private List<TMDbTrailer> tmdbTrailers;

    public List<TMDbTrailer> getTMDbTrailers() {
        return tmdbTrailers;
    }

    public void setTMDbTrailers(List<TMDbTrailer> trailers) {
        this.tmdbTrailers = trailers;
    }
}
