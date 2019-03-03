package alpitsolutions.com.popularmovies.interfaces;

import alpitsolutions.com.popularmovies.models.TMDbMovie;
import alpitsolutions.com.popularmovies.models.TMDbMoviesResponse;
import alpitsolutions.com.popularmovies.models.TMDbReviewResponse;
import alpitsolutions.com.popularmovies.models.TMDbTrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/***
 * the api for our communication to themoviedb.org
 * we use retrofit here just to make it simpler and more secure
 */
public interface TMDbApi {
    @GET("movie/popular")
    Call<TMDbMoviesResponse> getMoviesSortingOrderPopularity(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/top_rated")
    Call<TMDbMoviesResponse> getMoviesSortingOrderTopRated(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/{movie_id}")
    Call<TMDbMovie> getSingleMovieData(
            @Path("movie_id") int id,
            @Query("api_key") String apiKEy,
            @Query("language") String language
    );

    @GET("movie/{movie_id}/videos")
    Call<TMDbTrailerResponse> getTMDbTrailers(
            @Path("movie_id") int id,
            @Query("api_key") String apiKEy,
            @Query("language") String language
    );

    @GET("movie/{movie_id}/reviews")
    Call<TMDbReviewResponse> getTMDbReviews(
            @Path("movie_id") int id,
            @Query("api_key") String apiKEy,
            @Query("language") String language
    );
}