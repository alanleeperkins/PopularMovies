package alpitsolutions.com.popularmovies.adapters;

import alpitsolutions.com.popularmovies.interfaces.OnMovieClickCallback;
import alpitsolutions.com.popularmovies.models.TMDbMovie;
import alpitsolutions.com.popularmovies.repositories.TMDbRepository;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import alpitsolutions.com.popularmovies.R;

public class TMDbMovieAdapter extends RecyclerView.Adapter<TMDbMovieAdapter.TMDBMovieViewHolder>{

    /* the list of all the current loaded movie data */
    private List<TMDbMovie> moviesList;

    /* the callback when someone clicks on a movie image */
    final private OnMovieClickCallback onMovieClickCallback;

    public TMDbMovieAdapter(List<TMDbMovie> moviesList, OnMovieClickCallback onMovieClickCallback) {
        this.moviesList = moviesList;
        this.onMovieClickCallback = onMovieClickCallback;
    }

    /***
     *
     * @param moviesList
     */
    public void setMoviesList(List<TMDbMovie> moviesList)
    {
        this.moviesList = moviesList;
        notifyDataSetChanged();
    }

    /***
     * create a new RecyclerView
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public TMDBMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_grid, parent, false);
        return new TMDBMovieViewHolder(view);
    }

    /***
     * updates the TMDBMovieViewHolder contents with the new item at the given position
     * @param tmdbMovieViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull TMDBMovieViewHolder tmdbMovieViewHolder, int position) {
        tmdbMovieViewHolder.bind(moviesList.get(position));
    }

    /***
     * returns the number of items we have in our movies list
     * @return
     */
    @Override
    public int getItemCount() {

        return moviesList.size();
    }


    /***
     * the view holder for our movies, each view holder contains one single movie data shown in the RecyclerView
     */
    class TMDBMovieViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        TMDbMovie tmdbMovie;


        /**
         *
         * @param viewItem
         */
        public TMDBMovieViewHolder(View viewItem) {
            super(viewItem);
            imgPoster = viewItem.findViewById(R.id.iv_movie_poster);

            itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    onMovieClickCallback.onClick(tmdbMovie);
                }
            });
        }

        /**
         *
         * @param movie
         */
        public void bind(TMDbMovie movie) {

            tmdbMovie = movie;


            //tmdbMovie.getRating();
            /* use Glide to get the image */
            Glide.with(itemView)
                    .load(TMDbRepository.TMDB_IMAGE_BASE_URL_W185 + movie.getPosterPath())
                    .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.BLACK)))
                    .into(imgPoster);
        }
    }
}
