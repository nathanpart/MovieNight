package partridge.nathan.movienight.models;


import android.os.Bundle;
import android.util.SparseArray;

import partridge.nathan.movienight.activities.ActivityUtils;


public class GenreMap {
    // Infrastucture
    private GenreNotifications mUiCallback;
    private SparseArray<Genre> mGenres;
    private ActivityUtils mActivityUtils;


    // UI Notification Callback
    private static final int REFRESH_NET_ERROR = 1;
    private static final int REFRESH_DONE = 2;

    public String[] getGenreLabels(boolean isMovie) {
        return new String[0];
    }

    public boolean[] getIsSelectedGenres(boolean isMovie) {
        return new boolean[0];

    }

    public void setIsSelectedGenres(boolean isMovie, boolean[] genresIsSelectd) {

    }

    interface GenreNotifications {
        public void alert(int notification);
    }


    public GenreMap(ActivityUtils activity) {
        mGenres = new SparseArray<Genre>();
        mActivityUtils = activity;
    }

    //Call UI notification call back making sure it runs in UI thread
    private void sendUiNotification(int notification) {
        mActivityUtils.switchToUiTread(()->mUiCallback.alert(notification));
    }

    // I don't think the genre lists change that often, so we hang on to it.  Only check for
    // updates every so often.
    void saveGenres(Bundle bundle) {
        bundle.putSparseParcelableArray("GENRE_DATA", mGenres);
    }

    void loadGenres(Bundle bundle) {
        mGenres = bundle.getSparseParcelableArray("GENRE_DATA");
        if (mGenres == null) {
            mGenres = new SparseArray<>();
            refresh();
        }
    }

    public void onGenreRefresh(SparseArray<Genre> genres, int responseCode, boolean comError) {
        if (genres.size() != 0) mGenres = genres;
        if (comError) {
            sendUiNotification(REFRESH_NET_ERROR);
        } else {
            sendUiNotification(REFRESH_DONE);
        }

    }

    private void refresh() {
    }
}
