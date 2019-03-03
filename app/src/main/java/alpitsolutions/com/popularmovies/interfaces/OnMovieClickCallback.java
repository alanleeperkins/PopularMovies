package alpitsolutions.com.popularmovies.interfaces;

import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.models.TMDbMovie;

public interface OnMovieClickCallback {
    void onClick(TMDbMovie movie);
    void onClick(FavoritesEntry favorite);
}
