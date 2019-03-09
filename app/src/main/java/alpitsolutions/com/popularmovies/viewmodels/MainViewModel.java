package alpitsolutions.com.popularmovies.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.models.TMDbMovie;
import alpitsolutions.com.popularmovies.repositories.PopularMoviesRepository;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = "AG6/"+ MainViewModel.class.getSimpleName();

    private PopularMoviesRepository popularMoviesRepository;
    private @Nullable List<TMDbMovie> movies;
    private LiveData<List<FavoritesEntry>> favorites;

    public MainViewModel(@NonNull Application application) {
        super(application);
        popularMoviesRepository = PopularMoviesRepository.getInstance(this.getApplication());
        Log.d(TAG, "MainViewModel init");

        Log.d(TAG, "Getting init LiveData<FavoriteEntries>");
        favorites = popularMoviesRepository.favoritesRepository.favoritesDao.loadAllFavorites();
    }

    public void setMovies(List<TMDbMovie> movies) {
        this.movies = movies;
    }

    public List<TMDbMovie> getMovies() {
        return movies;
    }

    public LiveData<List<FavoritesEntry>> getFavorites() {
        return favorites;
    }

    public PopularMoviesRepository getRepository() { return popularMoviesRepository; }
}
