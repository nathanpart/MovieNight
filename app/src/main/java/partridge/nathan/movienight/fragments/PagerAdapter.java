package partridge.nathan.movienight.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import partridge.nathan.movienight.R;
import partridge.nathan.movienight.models.MovieItem;
import partridge.nathan.movienight.models.TvItem;

import static partridge.nathan.movienight.models.ModelManager.isMovieEnabled;
import static partridge.nathan.movienight.models.ModelManager.isTelevisionEnabled;


public class PagerAdapter extends FragmentStatePagerAdapter {
    // Flag values indicating wich pages are viewable
    static final int FILTER_PAGE = 1; // Filter and Movie Listings
    static final int MOVIE_PAGE  = 2; // Filter and Tv Listings
    static final int TV_PAGE     = 4; // Movie Listings, and Tv Listings

    private String[] mPageTitles;

    private FilterFragment mFilter;
    private MediaListFragment mMovieListing;
    private MediaListFragment mTvListing;
    private int mPageFlags;


    PagerAdapter(FragmentManager fm, int pageFlags, Context context) {
        super(fm);

        // Create the page fragments
        mFilter = FilterFragment.newInstance();
        mMovieListing = MediaListFragment.newInstance(MovieItem.class.getSimpleName());
        mTvListing = MediaListFragment.newInstance(TvItem.class.getSimpleName());

        mPageFlags = pageFlags;
        mPageTitles = context.getResources().getStringArray(R.array.page_tab_strings);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    private boolean hasFilterPage() {
        return ((mPageFlags & FILTER_PAGE) == 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                if (hasFilterPage()) {
                    return mPageTitles[0];
                } else if (isMovieEnabled()) {
                    return mPageTitles[1];
                } else {
                    return mPageTitles[2];
                }

            case 1:
                if (hasFilterPage()) {
                    if (isMovieEnabled()) {
                        return mPageTitles[1];
                    } else {
                        return mPageTitles[2];
                    }
                } else {
                    return mPageTitles[2];
                }

            case 2:
                return mPageTitles[2];

            // Should never happen as only have three pages
            default:
                return mPageTitles[0];
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (hasFilterPage()) {
                    return mFilter;
                } else if (isMovieEnabled()) {
                    return mMovieListing;
                } else {
                    return mTvListing;
                }

            case 1:
                if (hasFilterPage()) {
                    if (isMovieEnabled()) {
                        return mMovieListing;
                    } else {
                        return mTvListing;
                    }
                } else {
                    return mTvListing;
                }

            case 2:
                return mTvListing;

            // Should never happen as only have three pages
            default:
                return mFilter;
        }

    }

    @Override
    public int getCount() {
        int numPages = 0;

        if (hasFilterPage()) {
            numPages++;
        }

        if (isMovieEnabled()) {
            numPages++;
        }

        if (isTelevisionEnabled()) {
            numPages++;
        }
        return numPages;
    }
}
