package partridge.nathan.movienight.tmdb_service;


import java.util.ArrayList;

public interface TmdbApi {


    boolean getMovieGenreList(int handle);
    boolean getTvGenreList(int handle);
    boolean getMovieList(int handle, int orderBy, boolean orderDir, int page,
                         int minVotes, double minRating, String start, String end,
                         ArrayList<Integer> genres);
    boolean getTvList(int handle, int orderBy, boolean orderDir, int page,
                      int minVotes, double minRating, String start, String end,
                      ArrayList<Integer> genres);
    int getMovieSortLabels();
    int getTvSortLabels();
    int open(OnReqCompletedHandler reqCompletedHandler);
    void close(int handle);
    void shutdown();
}
