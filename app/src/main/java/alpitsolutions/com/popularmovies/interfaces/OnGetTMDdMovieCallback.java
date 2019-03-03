package alpitsolutions.com.popularmovies.interfaces;

import alpitsolutions.com.popularmovies.models.TMDbMovie;

public interface OnGetTMDdMovieCallback {
    void onStarted();

    void onSuccess(TMDbMovie movie);

    void onError();
}
