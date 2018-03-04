package partridge.nathan.movienight.tmdb_service;


import android.os.Bundle;

public interface OnReqCompletedHandler {
    // Callback result codes
    int BINDED = 1;
    int MOVIE_LIST = 2;
    int TV_LIST = 3;
    int MOVIE_GENRES = 4;
    int TV_GENRES = 5;
    int GENRE_NET_ERROR = 6;
    int NETWORK_ERROR = 7;


    // Callback results bundle field keys
    String ERROR_MSG = "error_msg";
    String GENRE_LIST = "genre_list";
    String LIST_INFO = "list_info";
    String SHOW_LIST = "show_list";

    void OnReqCompleted(int resultCode, Bundle results);
}
