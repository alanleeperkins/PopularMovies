package alpitsolutions.com.popularmovies.viewmodels;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

public class MovieDetailViewModelFactory implements ViewModelProvider.Factory {

    private static final String TAG = "AG6/"+ MovieDetailViewModelFactory.class.getSimpleName();

    private Application application;
    private int movieId;

    public MovieDetailViewModelFactory(Application application, int movieId)
    {
        this.application = application;
        this.movieId = movieId;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MovieDetailsViewModel(application, movieId);
    }
}
