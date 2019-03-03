package alpitsolutions.com.popularmovies.views;

import android.content.Intent;
import alpitsolutions.com.popularmovies.adapters.FavoriteAdapter;
import alpitsolutions.com.popularmovies.adapters.TMDbMovieAdapter;
import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.interfaces.OnGetFavoritesCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDdMoviesCallback;
import alpitsolutions.com.popularmovies.interfaces.OnMovieClickCallback;
import alpitsolutions.com.popularmovies.models.TMDbMovie;
import alpitsolutions.com.popularmovies.repositories.PopularMoviesRepository;
import alpitsolutions.com.popularmovies.repositories.TMDbRepository;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import alpitsolutions.com.popularmovies.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /* the keys we need for our saved instance bundle */
    private final String KEY_FILTERING_TYPE = "filtering_type";
    private final String KEY_SORTING_TYPE = "sorting_type";
    private final String KEY_CURRENT_HIGHEST_FETCHED_PAGE = "current_highest_fetched_page";

    private final int MAIN_ACTIVITY_RESULT_REQUEST_CODE = 1234;

    public static final String KEY_RESULT_DATA_CHANGED = "data_changed";

    /* the number of columns we want to show [based on the orientation] */
    private final int NUMBER_OF_COLUMNS = 2;
    /* indicates if a fetching process is currently running */
    private boolean isRunningMoviesFetching;
    /* shows the highest page we currently have the content of */
    private int currentHighestFetchedPage;
    /* shows the current filtering type */
    private String moviesFilteringType;
    /* shows the current sorting order of the movie list */
    private String moviesSortingType;

    private boolean isSetRefreshList;

    /* the repository of our remote movie data and local favorites */
    private PopularMoviesRepository popularMoviesRepository;

    @BindView(R.id.movies_listing_layout) LinearLayout loMoviesListingLayout;
    @BindView(R.id.tv_error_info) TextView tvErrorInfo;
    @BindView(R.id.movies_list) RecyclerView rvMoviesRecyclerView;
    @BindView(R.id.info_sorting_order) TextView tvInfoSortingOrder;
    /* the moviesAdapter for showing our items */
    private TMDbMovieAdapter moviesAdapter;
    private FavoriteAdapter favoritesAdapter;


    RecyclerView.OnScrollListener onScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /* default filter type*/
        moviesFilteringType = PopularMoviesRepository.FILTER_TYPE_NONE;
        /* default sorting type */
        moviesSortingType = TMDbRepository.TMDB_SORTING_BY_POPULAR;
        /* we start with page 1 on requesting the movies */
        currentHighestFetchedPage = 1;
        /* get's triggered after finishing the detailview */
        isSetRefreshList = false;

        // let's check for some saved instance states
        if (savedInstanceState != null) {
            moviesFilteringType = savedInstanceState.getString(KEY_FILTERING_TYPE);
            moviesSortingType = savedInstanceState.getString(KEY_SORTING_TYPE);
            currentHighestFetchedPage = savedInstanceState.getInt(KEY_CURRENT_HIGHEST_FETCHED_PAGE);
        }

        /* get an instance of our repository */
        popularMoviesRepository = PopularMoviesRepository.getInstance(getApplication());

        rvMoviesRecyclerView.setLayoutManager( new GridLayoutManager(this,NUMBER_OF_COLUMNS));

        /* define the scroll listener here, so we can add an remove it whenever necessary */
        onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {

                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int firstVisibleItem = 0;
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager)
                    firstVisibleItem = ((GridLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                if (firstVisibleItem + visibleItemCount >= totalItemCount / 2) {
                    if (!isRunningMoviesFetching) {
                        getMoviesPaged(currentHighestFetchedPage + 1);
                    }
                }
            }
        };

        /* add the scroll listener for fetching */
        /* TODO: REMOVE ME! */
        addOnScrollListener();

        /* now let's load the first page of the movies we wanna see */
        ReloadMovies();
    }

    /***
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(KEY_FILTERING_TYPE, moviesFilteringType);
        outState.putString(KEY_SORTING_TYPE, moviesSortingType);
        outState.putInt(KEY_CURRENT_HIGHEST_FETCHED_PAGE, currentHighestFetchedPage);
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /***
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume MainActivity Refresh="+isSetRefreshList);
        if (isSetRefreshList)
        {
            isSetRefreshList = false;
            ReloadMovies();
        }
    }

    /**
     * removes all other scroll listeners an adds our
     */
    private void resetOnScrollListener()
    {
        removeOnScrollListener();
        addOnScrollListener();
    }

    /**
     *
     */
    private void addOnScrollListener() {
        rvMoviesRecyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     *
     */
    private void removeOnScrollListener() {
        rvMoviesRecyclerView.clearOnScrollListeners();
    }

    /***
     * set the current page back to 1 and reload all the movies [and genres if necessary]
     * @return
     */
    private Boolean ReloadMovies()
    {
        /* we always scroll back to the top when reloading */
        rvMoviesRecyclerView.scrollToPosition(0);

        if (moviesFilteringType == PopularMoviesRepository.FILTER_TYPE_NONE) {
            /* make sure we always go back to page 1 when reloading */
            currentHighestFetchedPage = 1;
            /* reset the scroll listener cause maybe it's been removed */
            resetOnScrollListener();
            /*load the moves remote with paging */
            getMoviesPaged(currentHighestFetchedPage);
        }
        else if (moviesFilteringType == PopularMoviesRepository.FILTER_TYPE_FAVORITES) {
            /*we don't need the scroll listener here*/
            removeOnScrollListener();
            getFavoritesNotPaged();
        }
        else {
            // eerm... well...
            return false;
        }

        return true;
    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_standard,menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_sort_popularty:
                moviesFilteringType = PopularMoviesRepository.FILTER_TYPE_NONE;
                moviesSortingType = TMDbRepository.TMDB_SORTING_BY_POPULAR;
                if (!ReloadMovies())
                {
                    Log.d(TAG, "FAILED: ReloadMovies TMDB_SORTING_BY_POPULAR");
                }
                return true;

            case R.id.action_sort_top_rated:
                moviesFilteringType = PopularMoviesRepository.FILTER_TYPE_NONE;
                moviesSortingType = TMDbRepository.TMDB_SORTING_BY_TOP_RATED;
                if (!ReloadMovies())
                {
                    Log.d(TAG, "FAILED: ReloadMovies TMDB_SORTING_BY_TOP_RATED");
                }
                return true;

            case R.id.action_filter_favorites:
                moviesFilteringType = PopularMoviesRepository.FILTER_TYPE_FAVORITES;
                if (!ReloadMovies())
                {
                    Log.d(TAG, "FAILED: ReloadMovies FILTER_TYPE_FAVORITES");
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***
     * show the current set sorting order information the the user on the UI
     */
    private void updateTitle()
    {
        if (moviesFilteringType.equals(PopularMoviesRepository.FILTER_TYPE_FAVORITES))
        {
            tvInfoSortingOrder.setText(getResources().getString(R.string.filtered_by_favorites));
        }
        else
        {
            if (moviesSortingType.equals(TMDbRepository.TMDB_SORTING_BY_TOP_RATED)) {
                tvInfoSortingOrder.setText(getResources().getString(R.string.sorted_by_top_rated));
            }
            else {
                tvInfoSortingOrder.setText(getResources().getString(R.string.sorted_by_popularity));
            }
        }
    }


    /***
     *
     */
    private void getFavoritesNotPaged() {
        popularMoviesRepository.getAllFavorites(new OnGetFavoritesCallback() {
            @Override
            public void onStarted() {
                Log.d(TAG,"start fetching favorites");
            }

            @Override
            public void onSuccess(List<FavoritesEntry> favorites) {

                if (favoritesAdapter == null) {
                    favoritesAdapter = new FavoriteAdapter(favorites, clickOnMovieCallback);
                    rvMoviesRecyclerView.setAdapter(favoritesAdapter);
                    moviesAdapter = null;
                }
                else
                {
                    favoritesAdapter.clearFavoritesList();
                    favoritesAdapter.appendFavoritesToList(favorites);
                }

                updateTitle();
                showMovieListingLayout();
            }

            @Override
            public void onError() {

            }
        });
    }

    /***
     * tries to fetch the movies based on the pageNumber number we gave
     * @param pageNumber
     */
    private void getMoviesPaged(final int pageNumber) {

        // fetching is running, so prevent other from calling it again
        isRunningMoviesFetching = true;

        popularMoviesRepository.getMoviesSortedPaged(pageNumber, moviesSortingType, new OnGetTMDdMoviesCallback() {
            @Override
            public void onStarted() {
                Log.d(TAG,"start fetching pageNumber#"+pageNumber);
            }

            @Override
            public void onSuccess(List<TMDbMovie> movies, int page) {
                if (moviesAdapter == null) {
                    moviesAdapter = new TMDbMovieAdapter(movies, clickOnMovieCallback);
                    rvMoviesRecyclerView.setAdapter(moviesAdapter);
                    favoritesAdapter = null;
                } else {
                    if (page == 1) {
                        moviesAdapter.clearMoviesList();
                    }
                    moviesAdapter.appendMoviesToList(movies);
                }
                currentHighestFetchedPage = page;

                updateTitle();
                showMovieListingLayout();

                isRunningMoviesFetching = false;
            }

            @Override
            public void onError()
            {
                showConnectivityErrorMessage();
                isRunningMoviesFetching = false;
            }
        });
    }

    /***
     * the callback when the user clicks on the movie image
     */
    OnMovieClickCallback clickOnMovieCallback = new OnMovieClickCallback()
    {
        @Override
        public void onClick(TMDbMovie movie) {
            Log.v("onClick (TMDbMovie)","Movie #" + movie.getTitle() + " clicked.");
            showClickOnMovieInfo(movie);
        }

        @Override
        public void onClick(FavoritesEntry favorite) {
            Log.v("onClick (favorite)","FID:" + favorite.toString() + " clicked.");
            showClickOnFavoriteInfo(favorite);
        }
    };

    /**
     * someone clicked on the movie image, so we open the MovieDetail Activity
     * @param movie
     */
    private void showClickOnMovieInfo(TMDbMovie movie)
    {
        if (movie==null)
            return;

        startMovieDetailActivity(movie.getId(),false);
    }

    /**
     * someone clicked on the favorite movie image, so we open the MovieDetail Activity
     * @param favorite
     */
    private void showClickOnFavoriteInfo(FavoritesEntry favorite)
    {
        if (favorite==null)
            return;

        startMovieDetailActivity(favorite.getMovieId(),true);
    }

    /**
     *
     * @param movieId
     */
    private void startMovieDetailActivity(int movieId, boolean handleResult )
    {
        /* start the intent with the movie id */
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra(TMDbRepository.TMDB_MOVIE_ID, movieId);
        if (handleResult)
            startActivityForResult(intent,MAIN_ACTIVITY_RESULT_REQUEST_CODE);
        else
            startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != MAIN_ACTIVITY_RESULT_REQUEST_CODE)
            return;
        if (resultCode != RESULT_OK )
            return;
        if (data == null )
            return;

        boolean dataChanged = data.getBooleanExtra(KEY_RESULT_DATA_CHANGED,false);
        Log.d(TAG,"RESULT datachanged="+dataChanged);
        isSetRefreshList = dataChanged;
    }

    /***
     * show a Toast with an error message, if there is any problem with the internet connectivity
     */
    private void showConnectivityErrorMessage() {

        if (moviesAdapter== null || moviesAdapter.getItemCount()==0)
            showErrorLayout();
        else
            Toast.makeText(MainActivity.this, getResources().getString(R.string.error_message_connectivity), Toast.LENGTH_SHORT).show();
    }

    /***
     * shows the error message, in case of an connection error
     */
    private void showErrorLayout()
    {
        loMoviesListingLayout.setVisibility(View.INVISIBLE);
        tvErrorInfo.setVisibility(View.VISIBLE);
    }

    /***
     * shows the loaded movie list
     */
    private void showMovieListingLayout()
    {
        loMoviesListingLayout.setVisibility(View.VISIBLE);
        tvErrorInfo.setVisibility(View.INVISIBLE);
    }

}
