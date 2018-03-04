package partridge.nathan.movienight.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

import partridge.nathan.movienight.R;
import partridge.nathan.movienight.models.Genre;
import partridge.nathan.movienight.models.ListModel;

import static android.content.DialogInterface.BUTTON_POSITIVE;
import static partridge.nathan.movienight.models.ModelManager.getMovies;
import static partridge.nathan.movienight.models.ModelManager.getTvShows;

/**
 *  Dialog Fragment to select genres to filter by
 */
public class GenreDialogFragment extends DialogFragment implements DialogInterface.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_GENRES = "genre_list";
    public static final String ARG_TYPE = "genre_type";
    private static final String GENRE_SELECTION = "genre_selection";
    public static final String GENRE_LABELS = "genre_labels";
    public static final String GENRE_LIST = "genre_list";

    private boolean mIsMovie;
    private boolean[] mGenreIsSelected;
    private String[] mGenreLabels;
    private ArrayList<Genre> mGenres;
    private ListModel.Filter mFilter;


    public GenreDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to show the genre selection dialog.  Activity returned by getActivity
     * needs to implement the ActivityUtils interface, as dialog will call getGenreList() to obtain
     * access to the genre model.
     *  @param genres list of genres to select from
     * @param genreType type of genres to be selected from true-movie false-tv
     * @param manager Fragment manager used to show the dialog with
     */
    public static void pickGenres(ArrayList<Genre> genres, boolean genreType, FragmentManager manager) {
        GenreDialogFragment fragment = new GenreDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_TYPE, genreType);
        args.putParcelableArrayList(ARG_GENRES, genres);
        fragment.setArguments(args);
        fragment.show(manager, "genres");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsMovie = getArguments().getBoolean(ARG_TYPE);
            mGenres = getArguments().getParcelableArrayList(ARG_GENRES);
        }
        mFilter = mIsMovie ? getMovies().getFilter() : getTvShows().getFilter();

        int[] selectedGenres = mFilter.getGenreList();
        int i = 0;
        mGenreLabels = new String[mGenres.size()];
        mGenreIsSelected = new boolean[mGenres.size()];
        for (Genre genre: mGenres) {
            mGenreLabels[i] = mIsMovie ? genre.getMovieLabel() : genre.getTvLabel();
            mGenreIsSelected[i] = false;
            for (int id: selectedGenres) {
                if (genre.getGenreId() == id) {
                    mGenreIsSelected[i] = true;
                }
            }
            i++;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mGenreIsSelected = savedInstanceState.getBooleanArray(GENRE_SELECTION);
            mGenres = savedInstanceState.getParcelableArrayList(GENRE_LIST);
            mGenreLabels = savedInstanceState.getStringArray(GENRE_LABELS);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mIsMovie ? R.string.select_movie_genres : R.string.select_tv_genres)
                .setMultiChoiceItems(mGenreLabels, mGenreIsSelected, (dialogInterface, i, b) -> mGenreIsSelected[i] = b)
                .setPositiveButton("Submit", this)
                .setNegativeButton("Cancel", this);
        return builder.create();
    }

    private void storeSelectedGenres() {
        ArrayList<Integer> selectedIds = new ArrayList<>();
        for (int i = 0; i < mGenreIsSelected.length; i++) {
            if (mGenreIsSelected[i]) {
                selectedIds.add(mGenres.get(i).getGenreId());
            }
        }
        int[] genreIdArray = new int[selectedIds.size()];
        for (int i = 0; i < selectedIds.size(); i++) genreIdArray[i] = selectedIds.get(i);
        mFilter.setGenreList(genreIdArray);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBooleanArray(GENRE_SELECTION, mGenreIsSelected);
        outState.putParcelableArrayList(GENRE_LIST, mGenres);
        outState.putStringArray(GENRE_LABELS, mGenreLabels);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == BUTTON_POSITIVE) {
            storeSelectedGenres();
        }
    }
}
