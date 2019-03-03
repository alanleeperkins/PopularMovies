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

    private FavoritesDao favoritesDao;
    private LiveData<List<FavoritesEntry>> favoriteEntryList;

    public FavoritesRepository(Application application) {
        FavoritesDatabase database = FavoritesDatabase.getInstance(application);
        favoritesDao = database.favoritesDao();
    }

    public void insert(FavoritesEntry favorite, OnGetFavoriteEntryUpdateCallback listener) {
        new InsertFavoriteAsyncTask(favoritesDao,listener).execute(favorite);
    }

    public void update(FavoritesEntry favorite) {
        new UpdateFavoriteAsyncTask(favoritesDao).execute(favorite);
    }

    public void deleteByMovieId(Integer movieId, OnGetFavoriteEntryUpdateCallback listener) {
        new DeleteFavoriteAsyncTask(favoritesDao,listener).execute(movieId);
    }

    public void deleteAllFavorites() {
        new DeleteAllFavoritesAsyncTask(favoritesDao).execute();
    }

    public void getAllFavorites(OnGetFavoritesCallback listener) {
       new GetAllFavoriteAsyncTask(favoritesDao,listener).execute();
    }

    public void checkIsFavoriteByMovieId(Integer movieId, OnGetFavoriteEntryUpdateCallback listener) {
        new CheckIsFavoriteByMovieIdAsyncTask(favoritesDao,listener).execute(movieId);
    }

    /***
     *
     */
    private static class GetAllFavoriteAsyncTask extends AsyncTask<Void, Void, List<FavoritesEntry>> {
        private FavoritesDao favoritesDao;

        OnGetFavoritesCallback listener;

        private GetAllFavoriteAsyncTask(FavoritesDao favoritesDao, OnGetFavoritesCallback listener) {
            this.favoritesDao = favoritesDao;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<FavoritesEntry> doInBackground(Void... voids) {

            return favoritesDao.loadAllFavorites();
        }

        @Override
        protected void onPostExecute(List<FavoritesEntry> resultData) {

            if(resultData==null)
                listener.onError();
            else
                listener.onSuccess(resultData);
        }
    }

    /***
     *
     */
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

    /***
     *
     */
    private static class UpdateFavoriteAsyncTask extends AsyncTask<FavoritesEntry, Void, Void> {
        private FavoritesDao favoritesDao;

        private UpdateFavoriteAsyncTask(FavoritesDao favoritesDao) {
            this.favoritesDao = favoritesDao;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(FavoritesEntry... notes) {
            favoritesDao.updateFavorite(notes[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    /**
     *
     */
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

    /**
     *
     */
    private static class CheckIsFavoriteByMovieIdAsyncTask extends AsyncTask<Integer, Void, Boolean> {
        private FavoritesDao favoritesDao;

        OnGetFavoriteEntryUpdateCallback listener;

        private CheckIsFavoriteByMovieIdAsyncTask(FavoritesDao favoritesDao, OnGetFavoriteEntryUpdateCallback listener) {
            this.favoritesDao = favoritesDao;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            listener.hasStartedAsyncFavoriteDbRequest();
        }

        @Override
        protected Boolean doInBackground(Integer... movieIds) {

           List<FavoritesEntry> fav = favoritesDao.loadFavoriteByMovieId(movieIds[0]);

           return (fav!=null && fav.size()!=0);
        }

        @Override
        protected void onPostExecute(Boolean isFavorite) {

            listener.isFavorite(isFavorite);
        }
    }


    private static class DeleteAllFavoritesAsyncTask extends AsyncTask<Void, Void, Void> {
        private FavoritesDao favoritesDao;

        private DeleteAllFavoritesAsyncTask(FavoritesDao favoritesDao) {
            this.favoritesDao = favoritesDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            favoritesDao.deleteAllFavorites();
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
