package alpitsolutions.com.popularmovies.interfaces;

import alpitsolutions.com.popularmovies.models.TMDbReview;

import java.util.List;

public interface OnGetTMDbReviewsCallback {

    void onSuccess(List<TMDbReview> reviews);

    void onError();
}
