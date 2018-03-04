package partridge.nathan.movienight.tmdb_service;

import android.content.Context;
import android.os.Looper;

public class TmdbThread extends Thread {
    private Context mContext;
    private TmdbService mService;

    TmdbThread(Context context, TmdbService service) {
        mContext = context;
        mService = service;
    }

    @Override
    public void run() {
        Looper.prepare();
        TmdbHandler handler = new TmdbHandler(mContext, mService);
        mService.setHandler(handler);
        Looper.loop();
    }
}
