package alpitsolutions.com.popularmovies.views;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.interfaces.OnGetFavoriteEntryUpdateCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDbReviewsCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDbTrailersCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDdMovieCallback;
import alpitsolutions.com.popularmovies.models.TMDbMovie;
import alpitsolutions.com.popularmovies.models.TMDbReview;
import alpitsolutions.com.popularmovies.models.TMDbTrailer;
import alpitsolutions.com.popularmovies.repositories.PopularMoviesRepository;
import alpitsolutions.com.popularmovies.repositories.TMDbRepository;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import alpitsolutions.com.popularmovies.R;
import alpitsolutions.com.popularmovies.viewmodels.MovieDetailViewModelFactory;
import alpitsolutions.com.popularmovies.viewmodels.MovieDetailsViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity implements OnGetFavoriteEntryUpdateCallback {

    private static final String TAG = "AG6/"+ MovieDetailActivity.class.getSimpleName();

    // the keys we need for our saved instance bundle
    private final String KEY_MOVIE_ID = "movie_id";
    private final String KEY_IS_FAVORITE = "is_favorite";

    private TMDbMovie movieData=null;
    private boolean isFavorite;
    private int movieId;

    private MovieDetailsViewModel viewModel;

    @BindView(R.id.iv_movie_details_backdrop) ImageView ivMovieBackdrop;
    @BindView(R.id.tv_movie_details_title) TextView tvMovieTitle;
    @BindView(R.id.tv_movie_details_release_data) TextView tvReleaseDate;
    @BindView(R.id.tv_move_details_rating) TextView tvMovieRating;
    @BindView(R.id.iv_movie_favorite_toggler) ImageView ivMovieToggleFavorite;
    @BindView(R.id.tv_movie_details_overview) TextView tvMovieOverview;
    @BindView(R.id.hsv_movie_details_trailers_container) HorizontalScrollView hsvMovieTrailers;
    @BindView(R.id.ll_movie_details_trailers) LinearLayout llMovieTrailers;
    @BindView(R.id.tv_movie_details_trailers_label) TextView tvMovieTrailersLabel;
    @BindView(R.id.ll_movie_details_reviews) LinearLayout llMovieReviews;
    @BindView(R.id.tv_movie_details_reviews_label) TextView tvMovieReviewsLabel;
    @BindView(R.id.cl_single_movie_data) ConstraintLayout clMovieData;
    @BindView(R.id.pb_loader_state) ProgressBar pbLoadingInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);

        isFavorite = false;

        if (savedInstanceState != null) {
            movieId = savedInstanceState.getInt(KEY_MOVIE_ID);
            isFavorite = savedInstanceState.getBoolean(KEY_IS_FAVORITE);
            showMovieFavoriteStateOnUI(isFavorite);
        }
        else
        {
            Intent callerActivity = getIntent();
            if (callerActivity.hasExtra(TMDbRepository.TMDB_MOVIE_ID))
            {
                /* get the movie id of the the user wants to see the details  of */
                movieId = getIntent().getIntExtra(TMDbRepository.TMDB_MOVIE_ID, -1);
            }
        }

        if (movieId<=0) {
            finish();
        }


        setupViewModel();

        ivMovieToggleFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMovieFavoriteStatus();
            }
        });

        showLoadingInProcessLayout();

        // now get the movie details
        getMovieDetails(movieId);
    }

    /***
     * setup the view model for the detail view activity with the id of the movie we wanna see
     */
    private void setupViewModel()
    {
        MovieDetailViewModelFactory factory = new MovieDetailViewModelFactory(this.getApplication(),movieId);
        viewModel = ViewModelProviders.of(this, factory).get(MovieDetailsViewModel.class);
        viewModel.getFavorite().observe(this, new Observer<FavoritesEntry>() {
            @Override
            public void onChanged(@Nullable FavoritesEntry favoritesEntry) {
                Log.d(TAG, "receiving database update from LiveData");
                updateIsFavorite((favoritesEntry!=null));
            }
        });
    }

    /**
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_MOVIE_ID, movieId);
        outState.putBoolean(KEY_IS_FAVORITE, isFavorite);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void finishWithError(int errorId)
    {
        Log.d(TAG, "finish with error id="+errorId);
        Intent returnIntent = new Intent();
        returnIntent.putExtra(MainActivity.KEY_FINISH_WITH_ERROR_ID, errorId);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    /**
     * adds/removes a movie from the favorite favorite table
     */
    private void toggleMovieFavoriteStatus()
    {
        if (isFavorite)
        {
            viewModel.getRepository().removeAsFavoriteByMoveId(movieId, this);
        }
        else
        {
            if (movieData==null)
                return;

            // add the current movie to the favorite movie data
            FavoritesEntry newfav = new FavoritesEntry(movieId);
            newfav.setBackdrop(movieData.getBackdrop());
            newfav.setOverview(movieData.getOverview());
            newfav.setPosterPath(movieData.getPosterPath());
            newfav.setRating(movieData.getRating());
            newfav.setReleaseDate(movieData.getReleaseDate());
            newfav.setTitle(movieData.getTitle());

            viewModel.getRepository().addAsFavorite(newfav,this);
        }
    }

    /***
     * shows the favorite status with a state specific image
     * @param favoriteState
     */
    private void showMovieFavoriteStateOnUI(boolean favoriteState)
    {
        if (favoriteState)
        {
           ivMovieToggleFavorite.setBackgroundResource(R.drawable.ic_star_yellow);
        }
        else
        {
            ivMovieToggleFavorite.setBackgroundResource(R.drawable.ic_star_black);
        }
    }

    /***
     * show the movie details
     * @param movieId
     */
    private void getMovieDetails(final int movieId) {
        viewModel.getRepository().getMovieData(movieId, new OnGetTMDdMovieCallback() {

            @Override
            public void onStarted() {
                showLoadingInProcessLayout();
            }

            @Override
            public void onSuccess(TMDbMovie movie) {
                // keep the movie data so we can work with them out that method
                movieData = movie;
                tvMovieTitle.setText(movie.getTitle());

                tvReleaseDate.setText("Release Date: "+ movie.getReleaseDate());
                tvMovieRating.setText("Rating: "  + movie.getRating().toString());
                tvMovieOverview.setText(movie.getOverview());

                /* get the image, but before make sure the user isn't already about to finish the activity */
                if (isFinishing()==false) {
                    Glide.with(MovieDetailActivity.this)
                            .load(TMDbRepository.TMDB_IMAGE_BASE_URL_W780 + movie.getBackdrop())
                            .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.BLACK)))
                            .into(ivMovieBackdrop);
                }
                getMovieTrailers(movie);
                getMovieReviews(movie);

                showMovieDetailsLayout();
            }

            @Override
            public void onError() {
                /* we only close the activity in case of an error */
                Log.d(TAG,"ERROR: Requesting MovieDetails...");
                finishWithError(MainActivity.ERROR_ID_CONNECTION_ERROR);
            }
        });
    }

    /***
     * get's the movie trailers
     * @param movie
     */
    private void getMovieTrailers(TMDbMovie movie)
    {
        viewModel.getRepository().getMovieTrailers(movie.getId(), new OnGetTMDbTrailersCallback() {
            @Override
            public void onSuccess(List<TMDbTrailer> trailers) {
                if (trailers!=null && trailers.size()>0) {

                    tvMovieTrailersLabel.setVisibility(View.VISIBLE);
                    hsvMovieTrailers.setVisibility(View.VISIBLE);
                    llMovieTrailers.removeAllViews();

                    for (final TMDbTrailer trailer : trailers) {
                        Log.d(TAG, "trailer: " + trailer.getKey());
                        View parent = getLayoutInflater().inflate(R.layout.thumbnail_trailer, llMovieTrailers, false);
                        ImageView thumbnail = parent.findViewById(R.id.thumbnail);
                        thumbnail.requestLayout();
                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showMovieTrailer(String.format(TMDbRepository.TMDB_YOUTUBE_VIDEO_BASE_URL_FORMATTER, trailer.getKey()));
                            }
                        });
                        Glide.with(MovieDetailActivity.this)
                                .load(String.format(TMDbRepository.TMDB_YOUTUBE_THUMBNAIL_BASE_URL_FORMATTER, trailer.getKey()))
                                .apply(RequestOptions.placeholderOf(R.color.colorPrimary).centerCrop())
                                .into(thumbnail);
                        llMovieTrailers.addView(parent);
                    }
                }
            }

            @Override
            public void onError() {
                Log.d(TAG, "ERROR trailer: ");
                tvMovieTrailersLabel.setVisibility(View.GONE);
            }
        });
    }

    /***
     * gets the movie reviews
     * @param movie
     */
    private void getMovieReviews(TMDbMovie movie)
    {
        viewModel.getRepository().getMovieReviews(movie.getId(), new OnGetTMDbReviewsCallback() {
            @Override
            public void onSuccess(List<TMDbReview> reviews) {
                if (reviews!=null && reviews.size()>0) {
                    tvMovieReviewsLabel.setVisibility(View.VISIBLE);
                    llMovieReviews.setVisibility(View.VISIBLE);
                    llMovieReviews.removeAllViews();
                    for (TMDbReview review : reviews) {
                        Log.d(TAG, "review from : " + review.getAuthor());
                        View parent = getLayoutInflater().inflate(R.layout.single_movie_review, llMovieReviews, false);
                        TextView author = parent.findViewById(R.id.reviewAuthor);
                        TextView content = parent.findViewById(R.id.reviewContent);
                        author.setText(review.getAuthor());
                        content.setText(review.getContent());
                        llMovieReviews.addView(parent);
                    }
                }
            }

            @Override
            public void onError() {
                // Do nothing
            }
        });
    }

    /***
     * shows the successfully loaded detail content of the movie
     */
    private void showMovieDetailsLayout()
    {
        pbLoadingInProgress.setVisibility(View.GONE);
        clMovieData.setVisibility(View.VISIBLE);
    }

    /***
     * shows the user that the loading is still in process
     */
    private void showLoadingInProcessLayout()
    {
        clMovieData.setVisibility(View.GONE);
        pbLoadingInProgress.setVisibility(View.VISIBLE);
    }

    /**
     * shows a specific
     * @param url
     */
    private void showMovieTrailer(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    /**
     * an async process with requests for the favorite_movie table has started
     */
    @Override
    public void hasStartedAsyncFavoriteDbRequest() {
        Log.v(TAG,"hasStartedAsyncFavoriteDbRequest");
    }

    /***
     *
     * @param isFavorite
     */
    private void updateIsFavorite(Boolean isFavorite)
    {
        this.isFavorite = isFavorite;
        Log.v(TAG,"updateIsFavorite="+this.isFavorite);
        showMovieFavoriteStateOnUI(this.isFavorite);
    }

    /**
     *
     */
    @Override
    public void deletingFavoriteSuccessful() {
        showMovieFavoriteStateOnUI(false);
        Toast.makeText(getApplicationContext(),"Removed Favorite",Toast.LENGTH_SHORT).show();
    }

    /**
     *
     */
    @Override
    public void addingFavoriteSuccessful() {
        showMovieFavoriteStateOnUI(true);
        Toast.makeText(getApplicationContext(),"Added Favorite",Toast.LENGTH_SHORT).show();
    }
}
