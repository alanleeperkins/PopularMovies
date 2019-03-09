package alpitsolutions.com.popularmovies.interfaces;

public interface OnGetFavoriteEntryUpdateCallback {
    void hasStartedAsyncFavoriteDbRequest();
    void deletingFavoriteSuccessful();
    void addingFavoriteSuccessful();
}
