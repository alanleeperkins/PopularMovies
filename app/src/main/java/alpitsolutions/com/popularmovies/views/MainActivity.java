package alpitsolutions.com.popularmovies.views;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import alpitsolutions.com.popularmovies.adapters.FavoriteAdapter;
import alpitsolutions.com.popularmovies.adapters.TMDbMovieAdapter;
import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDdMoviesCallback;
import alpitsolutions.com.popularmovies.interfaces.OnMovieClickCallback;
import alpitsolutions.com.popularmovies.models.TMDbMovie;
import alpitsolutions.com.popularmovies.repositories.PopularMoviesRepository;
import alpitsolutions.com.popularmovies.repositories.TMDbRepository;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import alpitsolutions.com.popularmovies.viewmodels.MainViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "AG6/" + MainActivity.class.getSimpleName();

    /* the keys we need for our saved instance bundle */
    private final String KEY_FILTERING_TYPE = "filtering_type";
    private final String KEY_SORTING_TYPE = "sorting_type";
    private final String KEY_CURRENT_HIGHEST_FETCHED_PAGE = "current_highest_fetched_page";

    private final int MAIN_ACTIVITY_RESULT_REQUEST_CODE = 1234;

    public static final String KEY_FINISH_WITH_ERROR_ID ="finish_with_error_id";

    public static final int ERROR_ID_NO_ERROR = 0;
    public static final int ERROR_ID_GENERAL_ERROR = -1;
    public static final int ERROR_ID_CONNECTION_ERROR = -2;

    public enum eAdapterType { None, MovieDbAdapter, FavoritesDbAdapter };
    public eAdapterType activeAdapterType = eAdapterType.None;

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

    private boolean isSetShowErrorMessage;
    private int lastErrorMessageId;

    private MainViewModel viewModel;

    @BindView(R.id.movies_listing_layout) LinearLayout loMoviesListingLayout;
    @BindView(R.id.tv_error_info) TextView tvErrorInfo;
    @BindView(R.id.tv_no_favorite_info) TextView tvNoFavoritesInfo;
    @BindView(R.id.movies_list) RecyclerView rvMoviesRecyclerView;
    @BindView(R.id.info_sorting_order) TextView tvInfoSortingOrder;
    @BindView(R.id.pullToRefresh) SwipeRefreshLayout pullToRefresh;

    /* the moviesAdapter for showing our items */
    private TMDbMovieAdapter moviesAdapter;
    private FavoriteAdapter favoritesAdapter;


    RecyclerView.OnScrollListener onScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /* default filter type */
        moviesFilteringType = PopularMoviesRepository.FILTER_TYPE_NONE;
        /* default sorting type */
        moviesSortingType = TMDbRepository.TMDB_SORTING_BY_POPULAR;
        /* we start with page 1 on requesting the movies */
        currentHighestFetchedPage = 1;

        isSetShowErrorMessage = false;
        lastErrorMessageId = ERROR_ID_NO_ERROR;

        // let's check for some saved instance states
        if (savedInstanceState != null) {
            moviesFilteringType = savedInstanceState.getString(KEY_FILTERING_TYPE);
            moviesSortingType = savedInstanceState.getString(KEY_SORTING_TYPE);
            currentHighestFetchedPage = savedInstanceState.getInt(KEY_CURRENT_HIGHEST_FETCHED_PAGE);
        }

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

        setupViewModel();
        setupSwipeRefresher();

        /* add the scroll listener for fetching */
        /* TODO: REMOVE ME! */
        addOnScrollListener();

        /* now let's load the first page of the movies we wanna see */
        reloadActiveData();
    }

    /***
     * setup the view model for your Main Activity and start observing our Favorites
     */
    private void setupViewModel()
    {
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getFavorites().observe(this, new Observer<List<FavoritesEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoritesEntry> favoritesEntries) {
                if (favoritesEntries!=null) {
                    if (favoritesAdapter == null)
                        favoritesAdapter = new FavoriteAdapter(favoritesEntries, clickOnMovieCallback);
                    else
                        favoritesAdapter.setFavoritesEntryList(favoritesEntries);
                    Log.d(TAG, "Update Favorites List, FavCnt:" + favoritesEntries.size());
                }
                else {
                    Log.d(TAG, "Update Favorites List, FavCnt:NULL");
                }

                // update the ui
                if(getActiveAdapterType() == eAdapterType.FavoritesDbAdapter) {
                    showFavorites();
                }
            }
        });
    }

    /***
     *
     */
    private void setupSwipeRefresher()
    {
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG,"swipe refresh detected");
                reloadActiveData();
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    private eAdapterType getActiveAdapterType() {return activeAdapterType;}

    /***
     *
     * @param adapterType
     */
    private void setActiveAdapterType(eAdapterType adapterType)
    {
        if (adapterType.equals(activeAdapterType))
        {
            Log.d(TAG,"adapter '"+adapterType +"' already set!");
            return;
        }

        if (adapterType == eAdapterType.FavoritesDbAdapter) {
            rvMoviesRecyclerView.setAdapter(favoritesAdapter);
            activeAdapterType = eAdapterType.FavoritesDbAdapter;
        }
        else
        {
            rvMoviesRecyclerView.setAdapter(moviesAdapter);
            activeAdapterType = eAdapterType.MovieDbAdapter;
        }
        Log.d(TAG,"new active adapter ='"+adapterType+"'!!!");
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

        Log.d(TAG,"onResume MainActivity lastErrorMessageId="+lastErrorMessageId);
        if (isSetShowErrorMessage)
        {
            isSetShowErrorMessage = false;
            switch (lastErrorMessageId)
            {
                case ERROR_ID_NO_ERROR:
                    // everything is ok
                    break;
                case ERROR_ID_GENERAL_ERROR:
                    break;
                case ERROR_ID_CONNECTION_ERROR:
                    showConnectivityErrorMessage();
                    break;
            }
        }
    }

    /***
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != MAIN_ACTIVITY_RESULT_REQUEST_CODE)
            return;
        if (resultCode != RESULT_OK )
            return;
        if (data == null )
            return;

        lastErrorMessageId = data.getIntExtra(KEY_FINISH_WITH_ERROR_ID,ERROR_ID_NO_ERROR);
        Log.d(TAG,"RESULT lastErrorMessageId="+lastErrorMessageId);
        isSetShowErrorMessage = lastErrorMessageId!=ERROR_ID_NO_ERROR;
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
    private Boolean reloadActiveData()
    {
        Log.d(TAG,"reloadActiveData");
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
            showFavorites();
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
                if (!reloadActiveData())
                {
                    Log.d(TAG, "FAILED: reloadActiveData TMDB_SORTING_BY_POPULAR");
                }
                return true;

            case R.id.action_sort_top_rated:
                moviesFilteringType = PopularMoviesRepository.FILTER_TYPE_NONE;
                moviesSortingType = TMDbRepository.TMDB_SORTING_BY_TOP_RATED;
                if (!reloadActiveData())
                {
                    Log.d(TAG, "FAILED: reloadActiveData TMDB_SORTING_BY_TOP_RATED");
                }
                return true;

            case R.id.action_filter_favorites:
                moviesFilteringType = PopularMoviesRepository.FILTER_TYPE_FAVORITES;
                if (!reloadActiveData())
                {
                    Log.d(TAG, "FAILED: reloadActiveData FILTER_TYPE_FAVORITES");
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
     * show the favorites in the recycler view
     */
    private void showFavorites() {

        setActiveAdapterType(eAdapterType.FavoritesDbAdapter);

        if (favoritesAdapter.getItemCount()==0)
        {
            showNoFavoritesLayout();
        }
        else
        {
            updateTitle();
            showMovieListingLayout();
        }
    }

    /***
     * tries to fetch the movies based on the pageNumber number we gave
     * @param pageNumber
     */
    private void getMoviesPaged(final int pageNumber) {

        // fetching is running, so prevent other from calling it again
        isRunningMoviesFetching = true;
        viewModel.getRepository().getMoviesSortedPaged(pageNumber, moviesSortingType, new OnGetTMDdMoviesCallback() {
            @Override
            public void onStarted() {
                Log.d(TAG,"start fetching pageNumber#"+pageNumber);
            }

            @Override
            public void onSuccess(List<TMDbMovie> movies, int page) {
                Log.d(TAG,"onSuccess movies="+movies.size());
                if(page == 1 || viewModel.getMovies() == null || viewModel.getMovies().size() == 0)
                {
                    Log.d(TAG,"overwrite movies="+movies.size());
                    viewModel.setMovies(movies);
                }
                else
                {
                    for (TMDbMovie movie:movies) {
                        viewModel.getMovies().add(movie);
                    }
                    Log.d(TAG,"added new movies="+movies.size());
                }
                Log.d(TAG,"new movie size="+viewModel.getMovies().size());

                if (moviesAdapter == null) {
                    moviesAdapter = new TMDbMovieAdapter(viewModel.getMovies(), clickOnMovieCallback);
                }
                else {
                    moviesAdapter.setMoviesList(viewModel.getMovies());
                }

                currentHighestFetchedPage = page;

                setActiveAdapterType(eAdapterType.MovieDbAdapter);
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

        startMovieDetailActivity(movie.getId(),true);
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
        tvNoFavoritesInfo.setVisibility(View.GONE);
        loMoviesListingLayout.setVisibility(View.GONE);
        tvErrorInfo.setVisibility(View.VISIBLE);
    }

    /***
     * tell the user that there are no favorites yet
     */
    private void showNoFavoritesLayout()
    {
        tvNoFavoritesInfo.setVisibility(View.VISIBLE);
        loMoviesListingLayout.setVisibility(View.GONE);
        tvErrorInfo.setVisibility(View.GONE);
    }

    /***
     * shows the loaded movie list
     */
    private void showMovieListingLayout()
    {
        loMoviesListingLayout.setVisibility(View.VISIBLE);
        tvErrorInfo.setVisibility(View.GONE);
        tvNoFavoritesInfo.setVisibility(View.GONE);
    }

}
