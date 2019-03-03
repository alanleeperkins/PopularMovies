package alpitsolutions.com.popularmovies.repositories;

import alpitsolutions.com.popularmovies.interfaces.OnGetTMDbReviewsCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDbTrailersCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDdMovieCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetTMDdMoviesCallback;
import alpitsolutions.com.popularmovies.interfaces.TMDbApi;
import alpitsolutions.com.popularmovies.models.TMDbMovie;
import alpitsolutions.com.popularmovies.models.TMDbMoviesResponse;
import alpitsolutions.com.popularmovies.models.TMDbReviewResponse;
import alpitsolutions.com.popularmovies.models.TMDbTrailerResponse;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/***
 * our TMDb Repository to manage all requests we have to send to TMBd
 * we use retrofit here just to make it simpler and more secure
 */
public class TMDbRepository {

    private static final String TAG = TMDbRepository.class.getSimpleName();

    /* the base url of themovie.db.org for the movie data */
    public static final String TMDB_BASE_URL = "https://api.themoviedb.org/3/";

    /* the base url of themovie.db.org for the movie images with a width of 185px */
    public static final String TMDB_IMAGE_BASE_URL_W185 = "http://image.tmdb.org/t/p/w185";

    /* the base url of themovie.db.org for the movie images with a width of 780px */
    public static final String TMDB_IMAGE_BASE_URL_W780 = "http://image.tmdb.org/t/p/w780";

    public static final String TMDB_YOUTUBE_VIDEO_BASE_URL_FORMATTER = "http://www.youtube.com/watch?v=%s";
    public static final String TMDB_YOUTUBE_THUMBNAIL_BASE_URL_FORMATTER = "http://img.youtube.com/vi/%s/0.jpg";

    /* define the language for the response  */
    private static final String TMDB_LANGUAGE = "en-US";
    /* request a list of the most popular movies */
    public static final String TMDB_SORTING_BY_POPULAR = "popular";
    /* request a list of all top rated movies */
    public static final String TMDB_SORTING_BY_TOP_RATED = "top_rated";

    public static final String TMDB_MOVIE_ID  ="movie_id";

    /* TODO: Make sure you have a legal api-key */
    private static final String TMDB_API_KEY = "b973102d600e8a1683d0c2d3f52aeb81";

    private static TMDbRepository repository;

    private TMDbApi tmdbApi;

    private TMDbRepository(TMDbApi tmdbApi) {

        this.tmdbApi = tmdbApi;
    }

    /***
     * returns an instance of our TMDb Repository
     * @return
     */
    public static TMDbRepository getInstance() {
        if (repository == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(TMDB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            repository = new TMDbRepository(retrofit.create(TMDbApi.class));
        }

        return repository;
    }

    /***
     *
     * @param movieId
     * @param tmdbCallback
     */
    public void getMovieData(int movieId, final OnGetTMDdMovieCallback tmdbCallback) {
        tmdbCallback.onStarted();

        tmdbApi.getSingleMovieData(movieId, TMDB_API_KEY, TMDB_LANGUAGE)
                .enqueue(new Callback<TMDbMovie>() {
                    @Override
                    public void onResponse(Call<TMDbMovie> call, Response<TMDbMovie> tmdbResponse) {
                        if (tmdbResponse.isSuccessful()) {
                            TMDbMovie movie = tmdbResponse.body();
                            if (movie != null) {
                                tmdbCallback.onSuccess(movie);
                            } else {
                                tmdbCallback.onError();
                            }
                        } else {
                            tmdbCallback.onError();
                        }

                    }

                    @Override
                    public void onFailure(Call<TMDbMovie> call, Throwable t)
                    {
                        tmdbCallback.onError();
                    }
                });
    }


    /***
     *
     * @param pageNumber
     * @param sortingBy
     * @param tmdbCallback
     */
    public void getMoviesSortedPaged(int pageNumber, String sortingBy, final OnGetTMDdMoviesCallback tmdbCallback) {
        Log.d(TAG, "load page #" + pageNumber + "sortingby: "+sortingBy);
        tmdbCallback.onStarted();
        Callback<TMDbMoviesResponse> call = new Callback<TMDbMoviesResponse>() {
            @Override
            public void onResponse(Call<TMDbMoviesResponse> call, Response<TMDbMoviesResponse> response) {
                if (response.isSuccessful()) {
                    TMDbMoviesResponse moviesResponse = response.body();
                    if (moviesResponse != null && moviesResponse.getMovies() != null) {
                        tmdbCallback.onSuccess(moviesResponse.getMovies(),moviesResponse.getPage());
                    } else {
                        tmdbCallback.onError();
                    }
                } else {
                    tmdbCallback.onError();
                }
            }

            @Override
            public void onFailure(Call<TMDbMoviesResponse> call, Throwable t) {
                tmdbCallback.onError();
            }
        };

        switch (sortingBy) {
            case TMDB_SORTING_BY_TOP_RATED:
                tmdbApi.getMoviesSortingOrderTopRated(TMDB_API_KEY, TMDB_LANGUAGE, pageNumber).enqueue(call);
                break;

            case TMDB_SORTING_BY_POPULAR:
            default:
                tmdbApi.getMoviesSortingOrderPopularity(TMDB_API_KEY, TMDB_LANGUAGE, pageNumber).enqueue(call);
                break;
        }
    }

    /**
     * tries to get the trailers for the movie
     * @param movieId
     * @param tmdbCallback
     */
    public void getMovieTrailers(int movieId, final OnGetTMDbTrailersCallback tmdbCallback)
    {
        tmdbApi.getTMDbTrailers(movieId,TMDB_API_KEY,TMDB_LANGUAGE)
                .enqueue(new Callback<TMDbTrailerResponse>() {
                    @Override
                    public void onResponse(Call<TMDbTrailerResponse> call, Response<TMDbTrailerResponse> response) {
                        if (response.isSuccessful()) {
                            TMDbTrailerResponse tmDbTrailerResponse = response.body();
                            if (tmDbTrailerResponse != null && tmDbTrailerResponse.getTMDbTrailers() != null)
                                tmdbCallback.onSuccess(tmDbTrailerResponse.getTMDbTrailers());
                            else
                                tmdbCallback.onError();
                        }
                        else
                        {
                            tmdbCallback.onError();
                        }
                    }

                    @Override
                    public void onFailure(Call<TMDbTrailerResponse> call, Throwable t) {
                        tmdbCallback.onError();
                    }
                });
    }

    /***
     *
     * @param movieId
     * @param tmdbReviewsCallback
     */
    public void getMovieReviews(int movieId, final OnGetTMDbReviewsCallback tmdbReviewsCallback)
    {
        tmdbApi.getTMDbReviews(movieId,TMDB_API_KEY,TMDB_LANGUAGE)
                .enqueue(new Callback<TMDbReviewResponse>() {
                    @Override
                    public void onResponse(Call<TMDbReviewResponse> call, Response<TMDbReviewResponse> response) {
                        if (response.isSuccessful()) {
                            TMDbReviewResponse reviewResponse = response.body();
                            if (reviewResponse != null && reviewResponse.getReviews() != null) {
                                tmdbReviewsCallback.onSuccess(reviewResponse.getReviews());
                            } else {
                                tmdbReviewsCallback.onError();
                            }
                        } else {
                            tmdbReviewsCallback.onError();
                        }
                    }

                    @Override
                    public void onFailure(Call<TMDbReviewResponse> call, Throwable t) {
                        tmdbReviewsCallback.onError();
                    }
                });
    }

}
