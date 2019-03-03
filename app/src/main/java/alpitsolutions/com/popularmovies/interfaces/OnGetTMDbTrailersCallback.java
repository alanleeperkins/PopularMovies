package alpitsolutions.com.popularmovies.interfaces;

import alpitsolutions.com.popularmovies.models.TMDbTrailer;

import java.util.List;

public interface OnGetTMDbTrailersCallback {
    void onSuccess(List<TMDbTrailer> trailers);
    void onError();
}
