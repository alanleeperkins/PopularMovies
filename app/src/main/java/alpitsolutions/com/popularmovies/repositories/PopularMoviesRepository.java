package alpitsolutions.com.popularmovies.repositories;

import android.app.Application;

import alpitsolutions.com.popularmovies.database.FavoritesDao;
import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.interfaces.OnGetFavoriteEntryUpdateCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetFavoritesCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDbReviewsCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDbTrailersCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDdMovieCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDdMoviesCallback;
import android.util.Log;

public class PopularMoviesRepository {

    private static final String TAG = PopularMoviesRepository.class.getSimpleName();

    public static final String FILTER_TYPE_NONE = "filter_none";
    public static final String FILTER_TYPE_FAVORITES = "filter_favorites";

    private static PopularMoviesRepository sInstance;
    private TMDbRepository themoviedbRepository;
    public FavoritesRepository favoritesRepository;

    /**
     * returns a new object of our popular movies repository for remote and local data access
     *
     * @param application
     */
    private PopularMoviesRepository(Application application) {
        themoviedbRepository = TMDbRepository.getInstance();
        favoritesRepository = new FavoritesRepository(application);
    }

    /**
     * returns an instance of our popular movies repository for remote and local data access
     *
     * @param application
     * @return
     */
    public static synchronized PopularMoviesRepository getInstance(Application application) {
        if (sInstance == null) {
            Log.d(TAG, "NEW INSTANCE OF PopularMoviesRepository");
            sInstance = new PopularMoviesRepository(application);
        } else {
            Log.d(TAG, "GRAB EXISTENT INSTANCE OF PopularMoviesRepository");
        }
        return sInstance;
    }

    /**
     *
     * @param pageNumber
     * @param sortingBy
     * @param tmdbCallback
     */
    public void getMoviesSortedPaged(int pageNumber, String sortingBy, final OnGetTMDdMoviesCallback tmdbCallback) {
        themoviedbRepository.getMoviesSortedPaged(pageNumber,sortingBy, tmdbCallback );
    }

    /**
     *
     * @param movieId
     * @param tmdbCallback
     */
    public void getMovieData(int movieId, final OnGetTMDdMovieCallback tmdbCallback) {
        themoviedbRepository.getMovieData(movieId, tmdbCallback);
    }

    /**
     *
     * @param movieId
     * @param tmdbCallback
     */
    public void getMovieTrailers(int movieId, final OnGetTMDbTrailersCallback tmdbCallback)
    {
        themoviedbRepository.getMovieTrailers(movieId, tmdbCallback);
    }

    /**
     *
     * @param movieId
     * @param tmdbReviewsCallback
     */
    public void getMovieReviews(int movieId, final OnGetTMDbReviewsCallback tmdbReviewsCallback)
    {
        themoviedbRepository.getMovieReviews(movieId, tmdbReviewsCallback);
    }

    /**
     *
     * @param favorite
     * @param listener
     */
    public void addAsFavorite(FavoritesEntry favorite, OnGetFavoriteEntryUpdateCallback listener)
    {
        favoritesRepository.insert(favorite, listener);
    }

    /**
     *
     * @param movieId
     * @param listener
     */
    public void removeAsFavoriteByMoveId(Integer movieId, OnGetFavoriteEntryUpdateCallback listener)
    {
        favoritesRepository.deleteByMovieId(movieId, listener);
    }

}
