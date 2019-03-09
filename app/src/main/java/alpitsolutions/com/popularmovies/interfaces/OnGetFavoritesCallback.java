package alpitsolutions.com.popularmovies.interfaces;

import android.arch.lifecycle.LiveData;

import alpitsolutions.com.popularmovies.database.FavoritesEntry;

import java.util.List;

public interface OnGetFavoritesCallback {
    void onStarted();
    void onSuccess(LiveData<List<FavoritesEntry>> favorites);
    void onError();
}
