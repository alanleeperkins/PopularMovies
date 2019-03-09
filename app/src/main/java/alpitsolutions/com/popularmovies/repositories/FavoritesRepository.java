package alpitsolutions.com.popularmovies.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import alpitsolutions.com.popularmovies.database.FavoritesDao;
import alpitsolutions.com.popularmovies.database.FavoritesDatabase;
import alpitsolutions.com.popularmovies.database.FavoritesEntry;
import alpitsolutions.com.popularmovies.interfaces.OnGetFavoriteEntryUpdateCallback;
import alpitsolutions.com.popularmovies.interfaces.OnGetFavoritesCallback;
import android.os.AsyncTask;

import java.util.List;

public class FavoritesRepository {

    public FavoritesDao favoritesDao;

    public FavoritesRepository(Application application) {
        FavoritesDatabase database = FavoritesDatabase.getInstance(application);
        favoritesDao = database.favoritesDao();
    }

    public void insert(FavoritesEntry favorite, OnGetFavoriteEntryUpdateCallback listener) {
        new InsertFavoriteAsyncTask(favoritesDao,listener).execute(favorite);
    }


    public void deleteByMovieId(Integer movieId, OnGetFavoriteEntryUpdateCallback listener) {
        new DeleteFavoriteAsyncTask(favoritesDao,listener).execute(movieId);
    }

    private static class InsertFavoriteAsyncTask extends AsyncTask<FavoritesEntry, Void, Void> {
        private FavoritesDao favoritesDao;

        OnGetFavoriteEntryUpdateCallback listener;

        private InsertFavoriteAsyncTask(FavoritesDao favoritesDao, OnGetFavoriteEntryUpdateCallback listener) {
            this.favoritesDao = favoritesDao;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(FavoritesEntry... notes) {
            favoritesDao.insertFavorite(notes[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listener.addingFavoriteSuccessful();
        }
    }

    private static class DeleteFavoriteAsyncTask extends AsyncTask<Integer, Void, Void> {
        private FavoritesDao favoritesDao;

        OnGetFavoriteEntryUpdateCallback listener;

        private DeleteFavoriteAsyncTask(FavoritesDao favoritesDao, OnGetFavoriteEntryUpdateCallback listener) {
            this.favoritesDao = favoritesDao;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Integer... movieIds) {
            favoritesDao.deleteFavoriteByMovieId(movieIds[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listener.deletingFavoriteSuccessful();
        }
    }

}
