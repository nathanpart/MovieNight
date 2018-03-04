package partridge.nathan.movienight.activities;


import partridge.nathan.movienight.models.GenreMap;

public interface ActivityUtils  {
    void switchToUiTread(Runnable action);
    GenreMap getGenreList();

}
