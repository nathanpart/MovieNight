package partridge.nathan.movienight.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import partridge.nathan.movienight.R;

import static partridge.nathan.movienight.models.ModelManager.registerModeChangeCallback;
import static partridge.nathan.movienight.models.ModelManager.unregisterModeChangeCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PagerFragment extends Fragment  {

    // Flag values indicating wich pages are viewable
    public static final int FILTER_PAGE = PagerAdapter.FILTER_PAGE; // Filter and Movie Listings
    public static final int MOVIE_PAGE  = PagerAdapter.MOVIE_PAGE;  // Filter and Tv Listings
    public static final int TV_PAGE     = PagerAdapter.TV_PAGE;    // Movie Listings, and Tv Listings

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PAGE_FLAGS = "page_flags";

    private int mPageFlags;
    private PagerAdapter mPagerAdapter;


    public PagerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param mode Initial set of pages to be showing.
     * @return A new instance of fragment PagerFragment.
     */
    public static PagerFragment newInstance(int mode) {
        PagerFragment fragment = new PagerFragment();
        Bundle args = new Bundle();
        args.putInt(PAGE_FLAGS, mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPageFlags = savedInstanceState.getInt(PAGE_FLAGS, -1);
            if (mPageFlags != -1) {
                return;
            }
        }
        if (getArguments() != null) {
            mPageFlags = getArguments().getInt(PAGE_FLAGS, -1);
            if (mPageFlags != -1) {
                return;
            }
        }
        // Default set of pages
        mPageFlags = FILTER_PAGE | MOVIE_PAGE | TV_PAGE;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(PAGE_FLAGS, mPageFlags);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View pagerView = inflater.inflate(R.layout.fragment_pager, container, false);
        mPagerAdapter = new PagerAdapter(getChildFragmentManager(), mPageFlags,
                pagerView.getContext());
        ViewPager viewPager = pagerView.findViewById(R.id.pager);
        viewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = pagerView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        return pagerView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        registerModeChangeCallback("pager", this::onPagesLineupChange);
    }

    @Override
    public void onDetach() {
        unregisterModeChangeCallback("pager");
        super.onDetach();
    }

    public void onPagesLineupChange() {
        mPagerAdapter.notifyDataSetChanged();
    }
}
