package partridge.nathan.movienight.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import partridge.nathan.movienight.R;
import partridge.nathan.movienight.models.MovieItem;

import static partridge.nathan.movienight.models.ModelManager.getMovies;

public class MovieDetailDialogFragment extends DialogFragment {
    private static final String ARG_MOVIE_ID = "movie-id";

    private int mIndex;

    public MovieDetailDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param  movieId Index number of the movie to show the details of
     * @param manager Fragment manager used to show the dialog with
     */
    public static void showMovieDetails(int movieId, FragmentManager manager) {
        MovieDetailDialogFragment fragment = new MovieDetailDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieId);
        fragment.setArguments(args);
        fragment.show(manager, "movie-details");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIndex = getArguments().getInt(ARG_MOVIE_ID);
        } else {
            mIndex = 0;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflateMovieDetails())
                .setPositiveButton("Ok", (dialog, id)->{});
        return builder.create();
    }

    private View inflateMovieDetails() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_movie_detail_dialog, null);

        renderDetails(view);

        return view;
    }

    private void renderDetails(View view) {
        TextView textItem;
        TextView textItemLabel;
        LinearLayout groupContainer;

        MovieItem movieItem = getDetails();

        textItem = view.findViewById(R.id.movie_title);
        textItem.setText(movieItem.getTitle());

        textItem = view.findViewById(R.id.tag_line);
        if (movieItem.getTagline() == null || movieItem.getTagline().equals("")) {
            textItem.setVisibility(View.GONE);
        } else {
            textItem.setVisibility(View.VISIBLE);
            textItem.setText(movieItem.getTagline());
        }

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        ratingBar.setRating((float) movieItem.getVoteAverage());

        textItem = view.findViewById(R.id.vote_count);
        textItem.setText(String.format(getString(R.string.detail_vote_count), movieItem.getVoteCount()));

        textItem = view.findViewById(R.id.popularity);
        textItem.setText(String.format(getString(R.string.detail_popularity), movieItem.getPopularity()));

        textItemLabel = view.findViewById(R.id.runtime_label);
        textItem = view.findViewById(R.id.runtime);
        if (movieItem.getRuntime() == 0) {
            textItemLabel.setVisibility(View.GONE);
            textItem.setVisibility(View.GONE);
        } else {
            textItemLabel.setVisibility(View.VISIBLE);
            textItem.setVisibility(View.VISIBLE);
            int hours = movieItem.getRuntime() / 60;
            int min = movieItem.getRuntime() % 60;

            String minStr = "";
            String hourStr;
            String str = "";

            if (min != 0) {
                minStr = String.format(getString(R.string.detail_runtime_min), min);
                str = minStr;
            }

            if (hours != 0) {
                hourStr = String.format(getString(R.string.detail_runtime_hour), hours);
                str = String.format(getString(R.string.detail_runtime), hourStr, minStr);
            }

            textItem.setText(str);
        }


        textItem = view.findViewById(R.id.overview);
        textItem.setText(movieItem.getOverview());

        textItem = view.findViewById(R.id.release_date);
        textItem.setText(String.format(getString(R.string.detail_release_date), movieItem.getDate()));

        textItemLabel = view.findViewById(R.id.revenue_label);
        textItem = view.findViewById(R.id.revenue);
        if (movieItem.getRevenue() == 0) {
            textItemLabel.setVisibility(View.GONE);
            textItem.setVisibility(View.GONE);
        } else {
            textItemLabel.setVisibility(View.VISIBLE);
            textItem.setVisibility(View.VISIBLE);
            textItem.setText(String.format(getString(R.string.detail_revenue), movieItem.getRevenue()));
        }

        textItemLabel = view.findViewById(R.id.budget_label);
        textItem = view.findViewById(R.id.budget);
        if (movieItem.getBudget() == 0) {
            textItemLabel.setVisibility(View.GONE);
            textItem.setVisibility(View.GONE);
        } else {
            textItemLabel.setVisibility(View.VISIBLE);
            textItem.setVisibility(View.VISIBLE);
            textItem.setText(String.format(getString(R.string.detail_budget), movieItem.getBudget()));
        }

        groupContainer = view.findViewById(R.id.genres_container);
        renderStringList(movieItem.getGenres(), groupContainer);

        groupContainer = view.findViewById(R.id.production_container);
        renderStringList(movieItem.getProductionCompanies(), groupContainer);
    }

    private void renderStringList(List<String> strings, LinearLayout groupContainer) {
        for (String str : strings) {
            TextView textView = new TextView(getContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setText(str);
            groupContainer.addView(textView);
        }
    }

    private MovieItem getDetails() {
        return getMovies().getListItem(mIndex);
    }

}
