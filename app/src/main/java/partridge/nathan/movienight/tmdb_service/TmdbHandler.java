package partridge.nathan.movienight.tmdb_service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Locale;


public class TmdbHandler extends Handler {
    private static final String TAG = TmdbHandler.class.getSimpleName();
    static final int REQ_MOVIE_LIST = 2;
    static final int REQ_TV_LIST = 3;
    static final int REQ_MOVIE_GENRES = 4;
    static final int REQ_TV_GENRES = 5;
    static final int REQ_GRIM_REAPER = 6;

    private static final String[] CMD_LABELS = {"", "", "REQUEST MOVIE LIST", "REQUEST TV LIST",
                                               "REQUEST MOVIE GENRES", "REQUEST TV GENRES",
                                               "GRIM REAPER"};
    private JobController mJobController;

    TmdbHandler(Context context, TmdbService service) {
        super();
        mJobController = new JobController(context, service);
    }

    @Override
    public void handleMessage(Message msg) {
        int cmd = msg.what;
        int handle = msg.arg1;

        // peekData instead of getData as we want null returned if no bundle not a created empty Bundle
        Bundle params = msg.peekData();

        Log.d(TAG, String.format("Handle: %d, Dispatching request: %s", handle, CMD_LABELS[cmd]));
        mJobController.dumpDb();
        switch (cmd) {

            case REQ_MOVIE_GENRES:
                mJobController.getGenreList(JobController.MOVIE, handle);
                break;

            case REQ_TV_GENRES:
                mJobController.getGenreList(JobController.TV, handle);
                break;

            case REQ_MOVIE_LIST:
                mJobController.getListings(JobController.MOVIE, handle, params);
                break;

            case REQ_TV_LIST:
                mJobController.getListings(JobController.TV, handle, params);
                break;

            case REQ_GRIM_REAPER:
                mJobController.grimReaper();
                break;

            default:
                throw new RuntimeException(String.format(Locale.US, "Unknown job code: %d", cmd));
        }
        mJobController.dumpDb();
    }
}
