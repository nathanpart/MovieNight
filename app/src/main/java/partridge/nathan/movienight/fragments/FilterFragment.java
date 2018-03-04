package partridge.nathan.movienight.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import partridge.nathan.movienight.activities.ModeNotification;
import partridge.nathan.movienight.activities.ShowError;
import partridge.nathan.movienight.R;
import partridge.nathan.movienight.models.Genre;
import partridge.nathan.movienight.models.ListModel;

import static partridge.nathan.movienight.models.ModelManager.getMovies;
import static partridge.nathan.movienight.models.ModelManager.getTvShows;
import static partridge.nathan.movienight.models.ModelManager.isMovieEnabled;
import static partridge.nathan.movienight.models.ModelManager.isTelevisionEnabled;
import static partridge.nathan.movienight.models.ModelManager.registerModeChangeCallback;
import static partridge.nathan.movienight.models.ModelManager.unregisterModeChangeCallback;


public class FilterFragment extends Fragment implements ModeNotification, ListModel.GenreNotification {
    public static final String CALLBACK_KEY = "Filter";

    private Button mMovieStart;
    private Button mMovieEnd;

    private Button mTvStart;
    private Button mTvEnd;
    private ProgressBar mMovieProgressBar;
    private ProgressBar mTvProgressBar;
    private Button mMovieGenreButton;
    private Button mTvGenreButton;

    private boolean mIsPaused;


    public FilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment FilterFragment.
     */
    public static FilterFragment newInstance() {
        return new FilterFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }


    private void setVoteCountListener(EditText edit, ListModel.Filter model) {
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String str = charSequence.toString();
                try {
                    model.setMinVoteCount(Integer.parseInt(str, 0));
                } catch (NumberFormatException e) {
                    model.setMinVoteCount(0);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void setRatingListener(RatingBar rating, ListModel.Filter model) {
        rating.setOnRatingBarChangeListener((ratingBar, v, b) -> model.setMinRating(v * 2));
    }

    private void setDateButtonClickListener(Button button, int whichButton) {
        button.setOnClickListener((v) -> DateDialogFragment.pickDate(whichButton, getFragmentManager()));
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Access models
        ListModel.Filter movies = getMovies().getFilter();
        ListModel.Filter tv = getTvShows().getFilter();

        // Wire up the minimum vote count to the models
        EditText voteCount = getActivity().findViewById(R.id.movie_vote_count);
        voteCount.setText(String.format(Locale.US, "%d", movies.getMinVoteCount()));
        setVoteCountListener(voteCount, movies);

        voteCount = getActivity().findViewById(R.id.tv_vote_count);
        voteCount.setText(String.format(Locale.US, "%d", tv.getMinVoteCount()));
        setVoteCountListener(voteCount, tv);

        // Wire up the minimum ratings to the models
        RatingBar ratings = getActivity().findViewById(R.id.movie_ratings);
        ratings.setRating(movies.getMinRating() / 2);
        setRatingListener(ratings, movies);

        ratings = getActivity().findViewById(R.id.tv_ratings);
        ratings.setRating(tv.getMinRating() / 2);
        setRatingListener(ratings, tv);

        // Wire up the movie date buttons to the Date Picker
        mMovieStart = getActivity().findViewById(R.id.movie_start_button);
        setDateButtonClickListener(mMovieStart, DateDialogFragment.RELEASE_RANGE_START_DATE);

        mMovieEnd = getActivity().findViewById(R.id.movie_end_button);
        setDateButtonClickListener(mMovieEnd, DateDialogFragment.RELEASE_RANGE_END_DATE);

        // Wire up the tv date buttons to the Date Picker
        mTvStart = getActivity().findViewById(R.id.tv_start_button);
        setDateButtonClickListener(mTvStart, DateDialogFragment.AIRDATE_RANGE_START_DATE);

        mTvEnd = getActivity().findViewById(R.id.tv_end_button);
        setDateButtonClickListener(mTvEnd, DateDialogFragment.AIRDATE_RANGE_END_DATE);

        // Wire up the genre buttons to the Genre picker
        mMovieProgressBar = getActivity().findViewById(R.id.movie_progress_bar);
        mMovieGenreButton = getActivity().findViewById(R.id.movie_genre_button);
        mTvProgressBar = getActivity().findViewById(R.id.tv_progress_bar);
        mTvGenreButton = getActivity().findViewById(R.id.tv_genre_button);

        mMovieProgressBar.setVisibility(View.GONE);
        mTvProgressBar.setVisibility(View.GONE);

        mMovieGenreButton.setOnClickListener(v -> {
            mMovieGenreButton.setVisibility(View.GONE);
            mMovieProgressBar.setVisibility(View.VISIBLE);
            getMovies().getGenreList(this);
        });

        mTvGenreButton.setOnClickListener(v -> {
            mTvGenreButton.setVisibility(View.GONE);
            mTvProgressBar.setVisibility(View.VISIBLE);
            getTvShows().getGenreList(this);
        });


        // Label the date buttons
        labelDateButtons(movies, tv);

        // Set visibility to current visibility mode
        onModeChangeNotification();
    }

    private void labelDateButtons(ListModel.Filter movie, ListModel.Filter tv) {
        mMovieStart.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(movie.getStartDate().getTime()));
        mMovieEnd.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(movie.getEndDate().getTime()));
        mTvStart.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(tv.getStartDate().getTime()));
        mTvEnd.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(tv.getEndDate().getTime()));
    }


    public void onMovieDateChange(Date start, Date end) {
        mMovieStart.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(start));
        mMovieEnd.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(end));
    }

    public void onTvDateChange(Date start, Date end) {
        mTvStart.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(start));
        mTvEnd.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(end));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ListModel.Filter m = getMovies().getFilter();
        ListModel.Filter t = getTvShows().getFilter();

        // Register callbacks
        registerModeChangeCallback(CALLBACK_KEY, this);
        m.registerDateRangeChangeHandler(CALLBACK_KEY, this::onMovieDateChange);
        t.registerDateRangeChangeHandler(CALLBACK_KEY, this::onTvDateChange);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ListModel m = getMovies();
        ListModel t = getTvShows();

        // Unregister the callbacks
        unregisterModeChangeCallback(CALLBACK_KEY);
        m.getFilter().unregisterDateRangeChangeHandler(CALLBACK_KEY);
        t.getFilter().unregisterDateRangeChangeHandler(CALLBACK_KEY);
    }

    @Override
    public void onModeChangeNotification() {
        if (mIsPaused) return;
        int movieVisibility = isMovieEnabled() ? View.VISIBLE : View.GONE;
        int tvVisibility = isTelevisionEnabled() ? View.VISIBLE : View.GONE;
        getActivity().findViewById(R.id.movie_section).setVisibility(movieVisibility);
        getActivity().findViewById(R.id.television_section).setVisibility(tvVisibility);
    }


    // Got a list of genres so show them
    @Override
    public void onHaveGenreList(ArrayList<Genre> genres, boolean isMovie) {
        if (isMovie) {
            mMovieProgressBar.setVisibility(View.GONE);
            mMovieGenreButton.setVisibility(View.VISIBLE);
        } else {
            mTvProgressBar.setVisibility(View.GONE);
            mTvGenreButton.setVisibility(View.VISIBLE);
        }
        if (genres != null && genres.size() > 0) {
            GenreDialogFragment.pickGenres(genres, isMovie, getFragmentManager());
        } else {
            ((ShowError)getActivity()).showErrorDialog("TMDb returned no genres to select from.");
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsPaused = false;

        // Make sure section viability matches current state
        onModeChangeNotification();
    }

    @Override
    public void errorGenre(String message) {
        ((ShowError)getActivity()).showErrorDialog(message);
    }
}
