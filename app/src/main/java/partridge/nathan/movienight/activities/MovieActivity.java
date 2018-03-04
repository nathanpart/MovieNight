package partridge.nathan.movienight.activities;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

import partridge.nathan.movienight.fragments.DualpaneFragment;
import partridge.nathan.movienight.fragments.ErrorDialogFragment;
import partridge.nathan.movienight.R;
import partridge.nathan.movienight.fragments.MediaListFragment;
import partridge.nathan.movienight.fragments.MovieDetailDialogFragment;
import partridge.nathan.movienight.fragments.PagerFragment;
import partridge.nathan.movienight.fragments.TvDetailDialogFragment;
import partridge.nathan.movienight.models.ListModel;
import partridge.nathan.movienight.models.GenreMap;
import partridge.nathan.movienight.models.ModelManager;
import partridge.nathan.movienight.models.MovieItem;
import partridge.nathan.movienight.models.TvItem;

import static partridge.nathan.movienight.fragments.AboutDialogFragment.showAboutDialog;
import static partridge.nathan.movienight.fragments.PagerFragment.FILTER_PAGE;
import static partridge.nathan.movienight.fragments.PagerFragment.MOVIE_PAGE;
import static partridge.nathan.movienight.fragments.PagerFragment.TV_PAGE;
import static partridge.nathan.movienight.models.ModelManager.doModeCallbacks;
import static partridge.nathan.movienight.models.ModelManager.isMovieEnabled;
import static partridge.nathan.movienight.models.ModelManager.isTelevisionEnabled;
import static partridge.nathan.movienight.models.ModelManager.saveState;
import static partridge.nathan.movienight.models.ModelManager.setMovieState;
import static partridge.nathan.movienight.models.ModelManager.setTvState;
import static partridge.nathan.movienight.models.ModelManager.shutdownModels;

public class MovieActivity extends AppCompatActivity
        implements ShowError, ActivityUtils, MediaListFragment.OnShowSelectedListener {


    public static final String MAIN_FRAGMENT = "main_fragment";
    private Menu mOptionMenu;
    private GenreMap mGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // We activate models here as android will try and restore saved instances of fragments
        //   when onCreate is called on AppCompatActivity - the fragments need access to models
        ModelManager.initModels(this, savedInstanceState);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean isTablet = getResources().getBoolean(R.bool.is_tablet);


// TODO - Tablet Support -- on hold for now util dual pane bug is found. Both psnes come up blank
//                          but Pane labels show.          
//        if (!isTablet) {
            PagerFragment savedPagerFragment = (PagerFragment) fragmentManager
                    .findFragmentByTag(MAIN_FRAGMENT);
            if (savedPagerFragment == null) {
                int modes = FILTER_PAGE;
                modes |= isMovieEnabled() ? MOVIE_PAGE : 0;
                modes |= isTelevisionEnabled() ? TV_PAGE : 0;
                PagerFragment fragment = PagerFragment.newInstance(modes);
                fragmentManager.beginTransaction()
                        .add(R.id.placeholder, fragment, MAIN_FRAGMENT)
                        .commit();
            }
//        } else {
//            DualpaneFragment savedDualpaneFragment = (DualpaneFragment) fragmentManager
//                    .findFragmentByTag(MAIN_FRAGMENT);
//            if (savedDualpaneFragment == null) {
//                DualpaneFragment fragment = DualpaneFragment.newInstance();
//                fragmentManager.beginTransaction()
//                        .add(R.id.placeholder, fragment, MAIN_FRAGMENT)
//                        .commit();
//            }
//        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

   private void setStatusIcons() {
        Drawable icon;
        Resources resources = getResources();

        icon = isMovieEnabled() ?
                resources.getDrawable(R.drawable.video) :
                resources.getDrawable(R.drawable.video_off);
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mOptionMenu.findItem(R.id.movie_option).setIcon(icon);

        icon = isTelevisionEnabled() ?
                resources.getDrawable(R.drawable.television_classic) :
                resources.getDrawable(R.drawable.television_classic_off);
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mOptionMenu.findItem(R.id.television_option).setIcon(icon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Drawable icon;
        getMenuInflater().inflate(R.menu.options_menu, menu);
        icon = menu.findItem(R.id.about).getIcon();
        icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mOptionMenu = menu;
        setStatusIcons();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.movie_option || itemId == R.id.television_option) {
            if (itemId == R.id.movie_option) {
                if (isMovieEnabled()) {
                    setMovieState(false);
                    if (!isTelevisionEnabled()) setTvState(true);
                } else {
                    setMovieState(true);
                }
            } else {
                if (isTelevisionEnabled()) {
                    setTvState(false);
                    if (!isMovieEnabled()) setMovieState(true);
                } else {
                    setTvState(true);
                }
            }
            setStatusIcons();
            doModeCallbacks();
        }
        if (itemId == R.id.about) {
            showAboutDialog(getSupportFragmentManager());
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void switchToUiTread(Runnable action) {
        runOnUiThread(action);
    }


    @Override
    public GenreMap getGenreList() {
        return mGenres;
    }

    public void showErrorDialog(String msg) {
        ErrorDialogFragment.showError(msg, getSupportFragmentManager());
    }

    @Override
    public void onShowSelected(int item, ListModel model) {
        // Figure out which type of model we got
        if (model.getListItem(item) instanceof MovieItem) {
            MovieDetailDialogFragment.showMovieDetails(item, getSupportFragmentManager());
        } else {
            TvDetailDialogFragment.showTvDetails(item, getSupportFragmentManager());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutdownModels();
    }
}
