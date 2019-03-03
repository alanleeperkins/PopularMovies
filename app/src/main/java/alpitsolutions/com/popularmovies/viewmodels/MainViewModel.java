package alpitsolutions.com.popularmovies.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.models.TMDbMovie;
import alpitsolutions.com.popularmovies.repositories.PopularMoviesRepository;
import android.util.Log;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private PopularMoviesRepository popularMoviesRepository;

    private LiveData<List<TMDbMovie>> movies;
    private LiveData<List<FavoritesEntry>> favorites;

    public MainViewModel(Application application) {
        super(application);
        popularMoviesRepository = PopularMoviesRepository.getInstance(this.getApplication());
        Log.d(TAG, "MainViewModel init");
    }

    public LiveData<List<TMDbMovie>> getMovies() {
        return movies;
    }

    public LiveData<List<FavoritesEntry>> getFavorites() {
        return favorites;
    }


}
