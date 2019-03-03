package alpitsolutions.com.popularmovies.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TMDbReviewResponse {
    @SerializedName("results")
    @Expose
    private List<TMDbReview> reviews;

    public List<TMDbReview> getReviews() {
        return reviews;
    }

    public void setReviews(List<TMDbReview> reviews) {
        this.reviews = reviews;
    }

}
