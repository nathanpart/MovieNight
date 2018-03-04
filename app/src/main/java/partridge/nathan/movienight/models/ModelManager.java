package partridge.nathan.movienight.models;


import android.content.Context;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import partridge.nathan.movienight.activities.ModeNotification;
import partridge.nathan.movienight.tmdb_service.TmdbApi;
import partridge.nathan.movienight.tmdb_service.TmdbService;

public class ModelManager {
    private static final String MOVIE_STATE = "movie_state";
    private static final String TV_STATE = "tv_state";
    private static final String MOVIE_ENABLED = "movie_enable_flag";
    private static final String TV_ENABLED = "tv_enable_flag";

    private static ListModel<MovieItem> sMovies = null;
    private static ListModel<TvItem> sTvShows = null;

    private static TmdbApi sDAO = null;
    private static boolean sMovieSatate = true;
    private static boolean sTvState = true;
    private static Map<String, ModeNotification> sModeCallbacks = new HashMap<>();

    public static ListModel<MovieItem> getMovies() {
        if (sMovies == null) {
            throw new RuntimeException("initModels has not been called.");
        }
        return sMovies;
    }

    public static ListModel<TvItem> getTvShows() {
        if (sTvShows == null) {
            throw new RuntimeException("initModels has not been called.");
        }
        return sTvShows;
    }

    public static ListModel findModelByName(String modelName) {
        if (modelName.equals(MovieItem.class.getSimpleName())) {
            return getMovies();
        }
        if (modelName.equals(TvItem.class.getSimpleName())) {
            return getTvShows();
        }
        throw new RuntimeException(String.format("Unknown List Model name %s.", modelName));
    }

    public static void initModels(Context context, Bundle savedInstanceState) {
        sDAO = new TmdbService(context);
        if (savedInstanceState == null) {
            sMovies = new ListModel<>(MovieItem.class);
            sTvShows = new ListModel<>(TvItem.class);
            sMovieSatate = true;
            sTvState = true;
        } else {
            sMovies = savedInstanceState.getParcelable(MOVIE_STATE);
            sTvShows = savedInstanceState.getParcelable(TV_STATE);
            sMovieSatate = savedInstanceState.getBoolean(MOVIE_ENABLED);
            sTvState = savedInstanceState.getBoolean(TV_ENABLED);
        }
    }

    public static void shutdownModels() {
        sMovies.shutdown();
        sTvShows.shutdown();
        sMovies = null;
        sTvShows = null;

        sDAO.shutdown();    // Shutdown dao threads
        sDAO = null;
    }

    public static void saveState(Bundle outState) {
        outState.putParcelable(MOVIE_STATE, sMovies);
        outState.putParcelable(TV_STATE, sTvShows);
        outState.putBoolean(MOVIE_ENABLED, sMovieSatate);
        outState.putBoolean(TV_ENABLED, sTvState);
    }

    static TmdbApi getDAO() {
        return sDAO;
    }

    public static boolean isMovieEnabled() {
        return sMovieSatate;
    }

    public static boolean isTelevisionEnabled() {
        return sTvState;
    }

    public static void setMovieState(boolean movieSatate) {
        sMovieSatate = movieSatate;
    }

    public static void setTvState(boolean tvState) {
        sTvState = tvState;
    }

    public static void registerModeChangeCallback(String key, ModeNotification callback) {
        sModeCallbacks.put(key, callback);
    }

    public static void unregisterModeChangeCallback(String key) {
        if (sModeCallbacks.containsKey(key)) {
            sModeCallbacks.remove(key);
        }
    }

    public static void doModeCallbacks() {
        for (String key : sModeCallbacks.keySet()) {
            sModeCallbacks.get(key).onModeChangeNotification();
        }
    }

}
