package partridge.nathan.movienight.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import partridge.nathan.movienight.R;

import static partridge.nathan.movienight.fragments.PagerFragment.MOVIE_PAGE;
import static partridge.nathan.movienight.fragments.PagerFragment.TV_PAGE;
import static partridge.nathan.movienight.models.ModelManager.isMovieEnabled;
import static partridge.nathan.movienight.models.ModelManager.isTelevisionEnabled;

/**
 *  Fragment used on wide devices to display filter fragment along side of the pager fragment.
 */
public class DualpaneFragment extends Fragment {
    public static final String FILTER_FRAGMENT = "filter_fragment";
    public static final String LISTING_FRAGMENT = "listings_fragment";

    public DualpaneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment DualpaneFragment.
     */
    public static DualpaneFragment newInstance() {
        return new DualpaneFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dualpane, container, false);

        FragmentManager fragmentManager = getChildFragmentManager();

        FilterFragment savedFilterFragment = (FilterFragment) fragmentManager
                .findFragmentByTag(FILTER_FRAGMENT);
        if (savedFilterFragment == null) {
            FilterFragment filterFragment = FilterFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(R.id.leftPlaceholder, filterFragment, FILTER_FRAGMENT)
                    .commit();
        }

        PagerFragment savedPagerFragment = (PagerFragment) fragmentManager
                .findFragmentByTag(LISTING_FRAGMENT);
        if (savedPagerFragment == null) {
            int modes = 0;
            modes |= isMovieEnabled() ? MOVIE_PAGE : 0;
            modes |= isTelevisionEnabled() ? TV_PAGE : 0;
            PagerFragment pagerFragment = PagerFragment.newInstance(modes);
            fragmentManager.beginTransaction()
                    .add(R.id.rightPlaceholder, pagerFragment, LISTING_FRAGMENT)
                    .commit();
        }

        return view;
    }
}