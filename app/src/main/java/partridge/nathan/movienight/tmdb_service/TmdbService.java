package partridge.nathan.movienight.tmdb_service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Timer;

import partridge.nathan.movienight.R;

public class TmdbService extends Handler implements TmdbApi {
    private static final String TAG = TmdbService.class.getSimpleName();

    private int mHandle = 0;
    private TmdbHandler mTMDbHandler = null;
    private SparseArray<OnReqCompletedHandler> mOnReqCompletedHandlers;
    private final Timer mGrimReaper;

    public TmdbService(Context context) {
        super();
        TmdbThread thread = new TmdbThread(context, this);
        thread.setName("TmdbThread");
        thread.start();

        while (mTMDbHandler == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.w(TAG, "Delay for handler wait interrupted.");
            }
        }

        mGrimReaper = new Timer();
        mGrimReaper.schedule(new GrimReaper(mTMDbHandler), 60 * 1000,  60 * 1000);
        mOnReqCompletedHandlers = new SparseArray<>();
    }

    void setHandler(TmdbHandler handler) {
        mTMDbHandler = handler;
    }

    @Override
    public void handleMessage(Message msg) {
        OnReqCompletedHandler listener = mOnReqCompletedHandlers.get(msg.arg1);

        listener.OnReqCompleted(msg.what, msg.getData());
    }

    // Client Methods

    @Override
    public int open(OnReqCompletedHandler reqCompletedHandler) {
        mOnReqCompletedHandlers.append(mHandle, reqCompletedHandler);
        return mHandle++;
    }

    @Override
    public void close(int handle) {
        mOnReqCompletedHandlers.delete(handle);
    }

    @Override
    public void shutdown() {
        mTMDbHandler.getLooper().quitSafely();
        mGrimReaper.cancel();
        mTMDbHandler = null;
    }

    @Override
    public boolean getMovieGenreList(int handle) {
        // Check for invalid callback handles
        if (mOnReqCompletedHandlers.indexOfKey(handle) < 0) {
            return false;
        }
        Message message = Message.obtain();
        message.what = TmdbHandler.REQ_MOVIE_GENRES;          // Job code
        message.arg1 = handle;                                // Handler to send reply to
        mTMDbHandler.sendMessage(message);                    // Send to worker thread
        return true;
    }

    @Override
    public boolean getTvGenreList(int handle) {
        // Check for invalid callback handles
        if (mOnReqCompletedHandlers.indexOfKey(handle) < 0) {
            return false;
        }
        Message message = Message.obtain();
        message.what = TmdbHandler.REQ_TV_GENRES;
        message.arg1 = handle;
        mTMDbHandler.sendMessage(message);
        return true;
    }

    @Override
    public boolean getMovieList(int handle, int orderBy, boolean orderDir, int page,
                                int minVotes, double minRating, String start, String end,
                                ArrayList<Integer> genres) {
        // Check for invalid callback handles
        if (mOnReqCompletedHandlers.indexOfKey(handle) < 0) {
            return false;
        }

        bundleSendMessage(TmdbHandler.REQ_MOVIE_LIST, handle , orderBy, orderDir,
                page, minVotes, minRating, start, end, genres);


        return true;
    }

    @Override
    public boolean getTvList(int handle, int orderBy, boolean orderDir, int page,
                                int minVotes, double minRating, String start, String end,
                                ArrayList<Integer> genres) {
        // Check for invalid callback handles
        if (mOnReqCompletedHandlers.indexOfKey(handle) < 0) {
            return false;
        }

        bundleSendMessage(TmdbHandler.REQ_TV_LIST, handle, orderBy, orderDir,
                page, minVotes, minRating, start, end, genres);


        return true;
    }

    @Override
    public int getMovieSortLabels() {
        return R.array.movie_sort_order;
    }

    @Override
    public int getTvSortLabels() {
        return R.array.tv_sort_order;
    }

    private void bundleSendMessage(int reqId, int handle, int orderBy, boolean orderDir, int page,
                                   int minVotes, double minRating, String start, String end,
                                   ArrayList<Integer> genres) {

        // Bundle up the arguments
        Bundle bundle = new Bundle();
        bundle.putInt(JobController.ORDER_BY, orderBy);
        bundle.putBoolean(JobController.ORDER_DIR, orderDir);
        bundle.putInt(JobController.PAGE, page);
        bundle.putInt(JobController.MIN_VOTES, minVotes);
        bundle.putDouble(JobController.MIN_RATING, minRating);
        bundle.putString(JobController.START_DATE, start);
        bundle.putString(JobController.END_DATE, end);
        bundle.putIntegerArrayList(JobController.GENRE_LIST, genres);

        Message message = Message.obtain();
        message.what = reqId;
        message.arg1 = handle;
        message.setData(bundle);

        mTMDbHandler.sendMessage(message);
    }

}
