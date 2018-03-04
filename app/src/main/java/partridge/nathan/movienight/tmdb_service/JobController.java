package partridge.nathan.movienight.tmdb_service;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import partridge.nathan.movienight.models.Genre;
import partridge.nathan.movienight.models.ListInfo;
import partridge.nathan.movienight.models.ListItem;

class JobController {
    private static final String TAG = JobController.class.getSimpleName();
    // Valid type strings
    static final String MOVIE = "movie";
    static final String TV = "tv";

    // Params bundle parameter keys
    static final String ORDER_BY = "order_by";
    static final String ORDER_DIR = "order_dir";
    static final String PAGE = "page";
    static final String MIN_VOTES = "min_votes";
    static final String MIN_RATING = "min_rating";
    static final String START_DATE = "start_date";
    static final String END_DATE = "end_date";
    static final String GENRE_LIST = "genre_list";
    private final Context mContext;


    private AccessTMDb mAccessTMDb;
    private Cache mCache;

    // Params
    private int mOrderBy;
    private int mOrderDir;
    private int mPage;
    private int mMinVotes;
    private double mMinRating;
    private String mStartDate;
    private String mEndDate;
    private ArrayList<Integer> mGenres;
    private String mGenresAsString;
    private TmdbService mService;

    private String networkErrMsg = "Network Error. Please try again later.";
    private String noNetworkMsg = "Network Unavailable. Please try again.";

    JobController(Context context, TmdbService service) {
        mAccessTMDb = new AccessTMDb(context);
        mCache = new Cache(context);
        mContext = context;
        mService = service;
    }

    void dumpDb() {
        mCache.dumpdbs();
    }

    private void unpackPrams(Bundle params) {
        mOrderBy = params.getInt(ORDER_BY);
        mOrderDir = params.getBoolean(ORDER_DIR) ? 1 : 0;
        mPage = params.getInt(PAGE);
        mMinVotes = params.getInt(MIN_VOTES);
        mMinRating = params.getDouble(MIN_RATING);
        mStartDate = params.getString(START_DATE);
        mEndDate = params.getString(END_DATE);
        mGenres = params.getIntegerArrayList(GENRE_LIST);
        Log.d(TAG, "Unbundled parameters:");
        Log.d(TAG, String.format("    Order By: %d", mOrderBy));
        Log.d(TAG, String.format("   Oder Dir:  %s", (mOrderDir == 1) ? "ASC" : "DESC"));
        Log.d(TAG, String.format("   Page:      %d", mPage));
        Log.d(TAG, String.format("   Min Vote:  %d", mMinVotes));
        Log.d(TAG, String.format("   Rating:    %f", mMinRating));
        Log.d(TAG, String.format("   Start:     %s", mStartDate));
        Log.d(TAG, String.format("   End:       %s", mEndDate));
        Log.d(TAG, String.format("   Genres:    %s", mGenres.toString()));
    }

    private void getGenreString() {
        boolean first = true;
        StringBuilder genresAsString = new StringBuilder();
        for (int genre: mGenres) {
            genresAsString.append(first ? String.format(Locale.US, "%d", genre) :
                    String.format(Locale.US, ",%d", genre));
            first = false;
        }
        mGenresAsString = Uri.encode(genresAsString.toString());
        Log.d(TAG, String.format("Genre String: %s", mGenresAsString));
    }

    private void sendReply(int handle, int replyCode, Bundle replyData) {
        Message msg = Message.obtain();

        msg.what = replyCode;
        msg.arg1 = handle;
        msg.setData(replyData);
        mService.sendMessage(msg);
    }

    private void returnNetworkError(int handle, String msg, boolean isGenre) {
        Bundle reply = new Bundle();
        Log.e(TAG, String.format("Returning error type: %s: %s",
                isGenre ? "GENRE LIST" : "NETWORK", msg));
        reply.putString(OnReqCompletedHandler.ERROR_MSG, msg);
        sendReply(handle,
                isGenre ? OnReqCompletedHandler.GENRE_NET_ERROR : OnReqCompletedHandler.NETWORK_ERROR,
                reply);
    }

    void getGenreList(String type, int handle) {
        List<Genre> genres;

        genres = mCache.queryGenreList(type);
        if (genres == null) {
            if (isNetworkAvailable()) {
                try {
                    genres = mAccessTMDb.getGenreList(type);
                } catch (IOException e) {
                    returnNetworkError(handle, networkErrMsg, true);
                    return;
                }
                mCache.cacheGenreList(genres, type);
            } else {
                returnNetworkError(handle, noNetworkMsg, true);
            }
        }

        Bundle reply = new Bundle();
        Log.d(TAG, String.format("Returning Genre list type %s", type.equals(TV) ? "TV" : "MOVIE"));
        if (genres != null) {
            for (Genre item : genres) {
                Log.d(TAG, "  Genre Item:");
                item.dump(TAG);
            }
        }
        reply.putParcelableArrayList(OnReqCompletedHandler.GENRE_LIST, (ArrayList<Genre>) genres);
        int resultCode = type.equals(TV) ?
                OnReqCompletedHandler.TV_GENRES :
                OnReqCompletedHandler.MOVIE_GENRES;
        sendReply(handle, resultCode, reply);
    }

    void getListings(String type, int handle, Bundle params) {
        ListInfo listInfo;
        List<ListItem> shows = new ArrayList<>();

        unpackPrams(params);
        getGenreString();

        try {
            listInfo = getList(type);
            if (listInfo == null) {
                returnNetworkError(handle, noNetworkMsg, false);
                return;
            } else {
                List<Integer> mediaIds = listInfo.getMediaIds();
                List<Long> detailIds = listInfo.getDetailIds();

                for (int i = 0; i < mediaIds.size(); i++) {
                    ListItem show;
                    int mediaId = mediaIds.get(i);
                    Long detailId = detailIds.get(i);
                    if (detailId == null) {
                        // No detail cache record id so directly fetch from TMDb
                        show = mAccessTMDb.getDetails(type, mediaId);
                        mCache.cacheDetails(show, listInfo.getListId(), mAccessTMDb.getCacheTime());
                    } else {
                        // Have a show detail Id query detail cache
                        show = mCache.queryDetails(type, detailId);
                        if (show == null) {
                            // Not found, so access TMDb to fetch it
                            show = mAccessTMDb.getDetails(type, mediaId);
                            mCache.cacheDetails(show, listInfo.getListId(), mAccessTMDb.getCacheTime());
                        }
                    }
                    shows.add(show);
                }
            }
        } catch (IOException e) {
            returnNetworkError(handle, networkErrMsg, false);
            return;
        }

        Log.d(TAG, String.format("Returning %s List:", type.equals(TV) ? "Tv" : "Movie"));
        listInfo.dump(TAG);
        for (ListItem show: shows) {
            if (show == null) {
                Log.d(TAG, "No show");
            } else {
                show.dump(TAG);
            }
        }

        Bundle reply = new Bundle();
        reply.putParcelable(OnReqCompletedHandler.LIST_INFO, listInfo);
        reply.putParcelableArrayList(OnReqCompletedHandler.SHOW_LIST, (ArrayList<ListItem>) shows);

        int resultCode = type.equals(TV) ?
                OnReqCompletedHandler.TV_LIST :
                OnReqCompletedHandler.MOVIE_LIST;
        sendReply(handle, resultCode, reply);
    }

    private ListInfo getList(String type) throws IOException {
        ListInfo list = mCache.queryList(mPage, mOrderBy, mOrderDir, mMinVotes, mMinRating,
                mStartDate, mEndDate, mGenresAsString, type);
        if (list == null && isNetworkAvailable()) {
            list = mAccessTMDb.getShowList(mPage, mOrderBy, mOrderDir, mMinVotes, mMinRating,
                    mStartDate, mEndDate, mGenresAsString, type);
            mCache.cacheList(list, mAccessTMDb.getCacheTime(), mOrderBy, mOrderDir, mMinVotes,
                    mMinRating, mStartDate, mEndDate, mGenresAsString, type);
        }
        return list;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    void grimReaper() {
        mCache.grimReaper();
    }
}
