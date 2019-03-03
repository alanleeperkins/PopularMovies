package alpitsolutions.com.popularmovies.interfaces;

import alpitsolutions.com.popularmovies.models.TMDbMovie;

import java.util.List;

public interface OnGetTMDdMoviesCallback {
    void onStarted();
    void onSuccess(List<TMDbMovie> movies, int page);
    void onError();
}
