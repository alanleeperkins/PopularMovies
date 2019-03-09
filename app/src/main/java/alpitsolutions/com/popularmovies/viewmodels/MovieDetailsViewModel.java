package alpitsolutions.com.popularmovies.viewmodels;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.repositories.PopularMoviesRepository;

public class MovieDetailsViewModel extends ViewModel {

    private static final String TAG = "AG6/"+ MovieDetailsViewModel.class.getSimpleName();

    private PopularMoviesRepository popularMoviesRepository;
    private LiveData<FavoritesEntry> favoritesEntryLiveData;

    /***
     *
     * @param movieId
     */
    public MovieDetailsViewModel(@NonNull Application application,int movieId) {

        popularMoviesRepository = PopularMoviesRepository.getInstance(application);
        favoritesEntryLiveData = popularMoviesRepository.favoritesRepository.favoritesDao.loadFavoriteByMovieId(movieId);
        Log.d(TAG, "Getting LiveData FavoriteEntry");
    }

    public LiveData<FavoritesEntry> getFavorite()
    {
        return favoritesEntryLiveData;
    }

    public PopularMoviesRepository getRepository() { return popularMoviesRepository; }
}
