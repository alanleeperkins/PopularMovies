package alpitsolutions.com.popularmovies.adapters;

import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.interfaces.OnMovieClickCallback;
import alpitsolutions.com.popularmovies.repositories.TMDbRepository;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import alpitsolutions.com.popularmovies.R;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>{

    /* the list of all the current loaded movie data */
    private List<FavoritesEntry> favoritesEntryList;

    /* the callback when someone clicks on a movie image */
    final private OnMovieClickCallback onMovieClickCallback;

    /**
     *
     * @param favoritesEntryList
     * @param onMovieClickCallback
     */
    public FavoriteAdapter(List<FavoritesEntry> favoritesEntryList, OnMovieClickCallback onMovieClickCallback) {
        this.favoritesEntryList = favoritesEntryList;
        this.onMovieClickCallback = onMovieClickCallback;
    }


    /***
     *
     * @param favoritesEntryList
     */
    public void setFavoritesEntryList(List<FavoritesEntry> favoritesEntryList)
    {
        this.favoritesEntryList = favoritesEntryList;
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
    public FavoriteAdapter.FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_grid, parent, false);
        return new FavoriteAdapter.FavoriteViewHolder(view);
    }

    /***
     * updates the TMDBMovieViewHolder contents with the new item at the given position
     * @param favoriteViewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull FavoriteAdapter.FavoriteViewHolder favoriteViewHolder, int position) {
        favoriteViewHolder.bind(favoritesEntryList.get(position));
    }

    /***
     * returns the number of items we have in our movies list
     * @return
     */
    @Override
    public int getItemCount() {
        return favoritesEntryList.size();
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        FavoritesEntry favoritesEntry;

        public FavoriteViewHolder(@NonNull View viewItem) {
            super(viewItem);
            imgPoster = viewItem.findViewById(R.id.iv_movie_poster);

            itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    onMovieClickCallback.onClick(favoritesEntry);
                }
            });
        }

        public void bind(FavoritesEntry favorite) {

            favoritesEntry = favorite;

            /* use Glide to get the image */
            Glide.with(itemView)
                    .load(TMDbRepository.TMDB_IMAGE_BASE_URL_W185 + favorite.getPosterPath())
                    .apply(RequestOptions.placeholderOf(new ColorDrawable(Color.BLACK)))
                    .into(imgPoster);
        }
    }
}
