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
import partridge.nathan.movienight.models.TvItem;

import static partridge.nathan.movienight.models.ModelManager.getTvShows;


public class TvDetailDialogFragment extends DialogFragment {
    private static final String ARG_MOVIE_ID = "movie-id";

    private int mIndex;

    public TvDetailDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param  showId Index number of the movie to show the details of
     * @param manager Fragment manager used to show the dialog with
     */
    public static void showTvDetails(int showId, FragmentManager manager) {
        TvDetailDialogFragment fragment = new TvDetailDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, showId);
        fragment.setArguments(args);
        fragment.show(manager, "tv-details");
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
        builder.setView(inflateTvDetails())
                .setPositiveButton("Ok", (dialog, id)->{});
        return builder.create();
    }

    private View inflateTvDetails() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_tv_detail_dialog, null);

        renderDetails(view);

        return view;
    }

    private void renderDetails(View view) {
        TextView textItem;
        TextView textItemLabel;
        LinearLayout groupContainer;

        TvItem showItem = getDetails();

        textItem = view.findViewById(R.id.show_title);
        textItem.setText(showItem.getTitle());

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        ratingBar.setRating((float) showItem.getVoteAverage());

        textItem = view.findViewById(R.id.vote_count);
        textItem.setText(String.format(getString(R.string.detail_vote_count), showItem.getVoteCount()));

        textItem = view.findViewById(R.id.popularity);
        textItem.setText(String.format(getString(R.string.detail_popularity), showItem.getPopularity()));

        textItem = view.findViewById(R.id.overview);
        textItem.setText(showItem.getOverview());

        textItem = view.findViewById(R.id.airdate);
        textItem.setText(String.format(getString(R.string.detail_air_date), showItem.getDate()));

        textItem = view.findViewById(R.id.episodes);
        textItemLabel = view.findViewById(R.id.episodes_label);
        if (showItem.getNumberEpisodes() == 0) {
            textItem.setVisibility(View.GONE);
            textItemLabel.setVisibility(View.GONE);
        } else {
            textItem.setVisibility(View.VISIBLE);
            textItemLabel.setVisibility(View.VISIBLE);
            textItem.setText(String.format(getString(R.string.number_episodes), showItem.getNumberEpisodes()));
        }

        textItem = view.findViewById(R.id.seasons);
        textItemLabel = view.findViewById(R.id.seasons_label);
        if (showItem.getNumberSeasons() == 0) {
            textItem.setVisibility(View.GONE);
            textItemLabel.setVisibility(View.GONE);
        } else {
            textItem.setVisibility(View.VISIBLE);
            textItemLabel.setVisibility(View.VISIBLE);
            textItem.setText(String.format(getString(R.string.number_seasons), showItem.getNumberSeasons()));
        }

        textItem = view.findViewById(R.id.show_type);
        textItemLabel = view.findViewById(R.id.episodes_label);
        if (showItem.getShowType() == null || showItem.getShowType().equals("")) {
            textItem.setVisibility(View.GONE);
            textItemLabel.setVisibility(View.GONE);
        } else {
            textItem.setVisibility(View.VISIBLE);
            textItemLabel.setVisibility(View.VISIBLE);
            textItem.setText(String.format(getString(R.string.show_type), showItem.getShowType()));
        }

        groupContainer = view.findViewById(R.id.genres_container);
        renderStringList(showItem.getGenres(), groupContainer);

        groupContainer = view.findViewById(R.id.network_container);
        renderStringList(showItem.getNetworks(), groupContainer);

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

    private TvItem getDetails() {
        return getTvShows().getListItem(mIndex);
    }

}
