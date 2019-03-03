package alpitsolutions.com.popularmovies.interfaces;

public interface OnGetFavoriteEntryUpdateCallback {
    void hasStartedAsyncFavoriteDbRequest();
    void isFavorite(Boolean isFavorite);
    void deletingFavoriteSuccessful();
    void addingFavoriteSuccessful();
}
