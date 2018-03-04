package partridge.nathan.movienight.tmdb_service;


import android.os.Message;

import java.util.TimerTask;

class GrimReaper extends TimerTask {
    private TmdbHandler mHandler;

    GrimReaper(TmdbHandler tmdbHandler) {
        mHandler = tmdbHandler;
    }

    @Override
    public void run() {
        Message message = Message.obtain();
        message.what = TmdbHandler.REQ_GRIM_REAPER;
        mHandler.sendMessage(message);
    }
}
