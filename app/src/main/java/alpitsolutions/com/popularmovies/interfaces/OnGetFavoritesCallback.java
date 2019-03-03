package alpitsolutions.com.popularmovies.interfaces;

import alpitsolutions.com.popularmovies.database.FavoritesEntry;

import java.util.List;

public interface OnGetFavoritesCallback {
    void onStarted();
    void onSuccess(List<FavoritesEntry> favorites);
    void onError();
}
