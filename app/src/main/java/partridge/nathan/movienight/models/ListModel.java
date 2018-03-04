package partridge.nathan.movienight.models;

//  What this needs to do
//    - Hold current filter and paging position state
//    - Hold current List of items




import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import partridge.nathan.movienight.tmdb_service.OnReqCompletedHandler;
import partridge.nathan.movienight.tmdb_service.TmdbApi;
import partridge.nathan.movienight.tmdb_service.TmdbService;

public class ListModel<T extends ListItem> implements Parcelable {
    public static final String TAG = ListModel.class.getSimpleName();

    // Type of date this list model is holding
    private String mModelType;

    private final Filter mFilter;
    private final ListOrder mListOrder;
    private final Paging mPaging;

    private Refresher mRefresher;

    // Allows the data refreshing to be lazy until a view is showing the data
    private boolean mIsDirty;
    private boolean mHasViewer;

    // List of shows
    private List<T> mShows;

    public void shutdown() {
        mRefresher = null;
    }

    private Refresher getRef() {
        if (mRefresher == null) {
            mRefresher = new Refresher();
        }
        return mRefresher;
    }

    //----------------------------------------------------------------------------------------------
    // Internal classes
    //----------------------------------------------------------------------------------------------

    // Filter state
    public class Filter  {

        private int mMinVoteCount;

        private float mMinRating;
        private Calendar mStartDate;
        private Calendar mEndDate;
        private  int[] mGenreList;
        private Map<String, RangeDateNotification> mDateChangeCallbacks;
        Filter() {
            mMinRating = 0;
            mMinRating = 5.0F;
            mStartDate = getDefaultStart();
            mEndDate = getDefaultEnd();
            mGenreList = new int[0];
            mDateChangeCallbacks = new HashMap<>();
        }

        Filter(Parcel in) {
            mMinVoteCount = in.readInt();
            mMinRating = in.readFloat();
            mGenreList = in.createIntArray();

            // Start date
            int year = in.readInt();
            int month = in.readInt();
            int day = in.readInt();
            mStartDate = Calendar.getInstance();
            mStartDate.set(year, month, day);

            // End date
            year = in.readInt();
            month = in.readInt();
            day = in.readInt();
            mEndDate = Calendar.getInstance();
            mEndDate.set(year, month, day);

            mDateChangeCallbacks = new HashMap<>();
        }

        void writeToParcel(Parcel parcel) {
            parcel.writeInt(mMinVoteCount);
            parcel.writeFloat(mMinRating);
            parcel.writeIntArray(mGenreList);

            // Start date
            parcel.writeInt(mStartDate.get(Calendar.YEAR));
            parcel.writeInt(mStartDate.get(Calendar.MONTH));
            parcel.writeInt(mStartDate.get(Calendar.DAY_OF_MONTH));

            // End date
            parcel.writeInt(mEndDate.get(Calendar.YEAR));
            parcel.writeInt(mEndDate.get(Calendar.MONTH));
            parcel.writeInt(mEndDate.get(Calendar.DAY_OF_MONTH));
        }

        private Calendar getDefaultStart() {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
            return c;
        }

        private Calendar getDefaultEnd() {
            return Calendar.getInstance();
        }

        public void registerDateRangeChangeHandler(String key, RangeDateNotification callback) {
            mDateChangeCallbacks.put(key, callback);
        }

        public void unregisterDateRangeChangeHandler(String key) {
            if (mDateChangeCallbacks.containsKey(key)) {
                mDateChangeCallbacks.remove(key);
            }
        }

        private void callDateRangeChangeCallbaks() {
            Date start = mStartDate.getTime();
            Date end = mEndDate.getTime();
            for (String key : mDateChangeCallbacks.keySet()) {
                mDateChangeCallbacks.get(key).onDateChange(start, end);
            }
        }

        public int getMinVoteCount() {
            return mMinVoteCount;
        }

        public void setMinVoteCount(int minVoteCount) {
            mMinVoteCount = minVoteCount;
            doLazyRefresh();
        }


        public float getMinRating() {
            return mMinRating;
        }

        public void setMinRating(float minRating) {
            mMinRating = minRating;
            doLazyRefresh();
        }

        public Calendar getStartDate() {
            return mStartDate;
        }

        public void setStartDate(Calendar startDate) {
            mStartDate = startDate;
            callDateRangeChangeCallbaks();
            doLazyRefresh();
        }

        public Calendar getEndDate() {
            return mEndDate;
        }

        public void setEndDate(Calendar endDate) {
            mEndDate = endDate;
            callDateRangeChangeCallbaks();
            doLazyRefresh();
        }

        public int[] getGenreList() {
            return mGenreList;
        }

        public void setGenreList(int[] genreList) {
            mGenreList = genreList;
            doLazyRefresh();
        }

        String getStartDateStr() {
            return String.format(Locale.US, "%d-%d-%d",
                    mStartDate.get(Calendar.YEAR),
                    mStartDate.get(Calendar.MONTH ) + 1,
                    mStartDate.get(Calendar.DAY_OF_MONTH));
        }

        String getEndDateStr() {
            return String.format(Locale.US, "%d-%d-%d",
                    mEndDate.get(Calendar.YEAR),
                    mEndDate.get(Calendar.MONTH) + 1,
                    mEndDate.get(Calendar.DAY_OF_MONTH));
        }

        ArrayList<Integer> getGenreArrayList() {
            ArrayList<Integer> list = new ArrayList<>();
            for (int genre: mGenreList) list.add(genre);
            return list;
        }

        void dump() {
            Log.d(TAG, "  Filter Parameters:");
            Log.d(TAG, String.format("    Min Votes:  %d", mMinVoteCount));
            Log.d(TAG, String.format("    Min Rating: %f", mMinRating));
            Log.d(TAG, String.format("    Start Date: %s", getStartDateStr()));
            Log.d(TAG, String.format("    End Date:   %s", getEndDateStr()));
            StringBuilder glist = new StringBuilder();
            for (int i = 0; i < mGenreList.length; i++) {
                glist.append(String.format(Locale.US," %d", i));
            }
            Log.d(TAG, String.format("    Genres: %s", glist));
            Log.d(TAG, "    Registered Callbacks:");
            for (String key : mDateChangeCallbacks.keySet()) {
                Log.d(TAG, String.format("      %s", key));
            }
        }
    }

    // List ordering state
    public class ListOrder {
        private int mCurrentSortBy;
        private boolean mIsAscendingSort;
        ListOrder() {
            mCurrentSortBy = 0;
            mIsAscendingSort = true;
        }

        ListOrder(Parcel in) {
            mCurrentSortBy = in.readInt();
            mIsAscendingSort = in.readByte() != 0;
        }

        void writeToParcel(Parcel parcel) {
            parcel.writeInt(mCurrentSortBy);
            parcel.writeByte((byte) (mIsAscendingSort ? 1 : 0));
        }

        public int getCurrentSortBy() {
            return mCurrentSortBy;
        }

        public void setCurrentSortBy(int currentSortBy) {
            mCurrentSortBy = currentSortBy;
            doLazyRefresh();
        }

        public boolean isAscendingSort() {
            return mIsAscendingSort;
        }

        public void toggleSortDirection() {
            mIsAscendingSort = !mIsAscendingSort;
            doLazyRefresh();
        }

        void dump() {
            Log.d(TAG, String.format("  Sort Order %d %s",
                    mCurrentSortBy, mIsAscendingSort ? "ASC" : "DESC"));
        }
    }

    // Pagination state
    public class Paging {
        // Paging state
        private int mCurrentPage;
        private int mNumberOfPages;
        private int mNumberOfItems;
        Paging() {
            mCurrentPage = 1;
            mNumberOfPages = 0;
            mNumberOfItems = 0;

        }

        Paging(Parcel in) {
            mCurrentPage = in.readInt();
            mNumberOfPages = in.readInt();
            mNumberOfItems = in.readInt();

        }

        void writeToParcel(Parcel parcel) {
            parcel.writeInt(mCurrentPage);
            parcel.writeInt(mNumberOfPages);
            parcel.writeInt(mNumberOfItems);
        }

        public int getCurrentPage() {
            return mCurrentPage;
        }

        public int getNumberOfPages() {
            return mNumberOfPages;
        }

        public void gotoFirstPage() {
            if (mCurrentPage > 1) {
                mCurrentPage = 1;
                doLazyRefresh();
            }
        }

        public void gotoPrevPage() {
            if (mCurrentPage > 1) {
                mCurrentPage -= 1;
                doLazyRefresh();
            }
        }

        public void gotoNextPage() {
            if (mCurrentPage <= mNumberOfPages) {
                mCurrentPage += 1;
                doLazyRefresh();
            }
        }

        public void gotoLastPage() {
            if (mCurrentPage != mNumberOfPages) {
                mCurrentPage = mNumberOfPages;
                doLazyRefresh();
            }
        }

        void dump() {
            Log.d(TAG, "  Paging:");
            Log.d(TAG, String.format("    Page: %d", mCurrentPage));
            Log.d(TAG, String.format("    Number of pages: %d", mNumberOfPages));
            Log.d(TAG, String.format("    Number of items: %d", mNumberOfItems));
        }

    }

    // Data Source
    private class Refresher implements OnReqCompletedHandler {

        // Map of callbacks to notify if we have had a start or end range date change
        private Map<String, DataChangeNotification> mDataChangeCallbacks;

        private GenreNotification mGenreCallback;
        private TmdbApi mTMDb;
        private int mTMDbHandle;
        Refresher() {
            mDataChangeCallbacks = new HashMap<>();
            // Obtain the reference to the DAO
            mTMDb = ModelManager.getDAO();
            mTMDbHandle = mTMDb.open(this);
        }

        void dump() {
            Log.d(TAG, "  Refresher:");
            Log.d(TAG, "    Data Change Callbacks:");
            for (String key : mDataChangeCallbacks.keySet()) {
                Log.d(TAG, String.format("      %s", key));
            }
            if (mTMDb != null) {
                Log.d(TAG, String.format("    Channel open to database. Handle: %d", mTMDbHandle));
            } else {
                Log.d(TAG, "    Not currently connected to database.");
            }
        }

        @Override
        public void OnReqCompleted(int resultCode, Bundle results) {
            switch (resultCode) {
                case BINDED:

                case MOVIE_LIST:
                    processListResults(results);
                    callDataChangeCallbacks();
                    break;

                case TV_LIST:
                    processListResults(results);
                    callDataChangeCallbacks();
                    break;

                case MOVIE_GENRES:
                    mGenreCallback.onHaveGenreList(results.getParcelableArrayList(GENRE_LIST), true);
                    break;

                case TV_GENRES:
                    mGenreCallback.onHaveGenreList(results.getParcelableArrayList(GENRE_LIST), false);
                    break;

                case GENRE_NET_ERROR:
                    mGenreCallback.errorGenre(results.getString(ERROR_MSG));

                case NETWORK_ERROR:
                    callDataErrorCallbacks(results.getString(ERROR_MSG));
                    break;
            }

        }

        private void refreshData() {
            if (mTMDb == null) return;

            int votes = mFilter.getMinVoteCount();
            float rating = mFilter.getMinRating();
            String start = mFilter.getStartDateStr();
            String end = mFilter.getEndDateStr();
            ArrayList<Integer> genres = mFilter.getGenreArrayList();

            int orderBy = mListOrder.getCurrentSortBy();
            boolean dir = mListOrder.isAscendingSort();

            int page = mPaging.getCurrentPage();

            if (mModelType.equals(MovieItem.class.getSimpleName())) {
                mTMDb.getMovieList(mTMDbHandle, orderBy, dir, page, votes, rating, start, end, genres);
            } else {
                mTMDb.getTvList(mTMDbHandle, orderBy, dir, page, votes, rating, start, end, genres);
            }
            callUpdateStartedCallbacks();
        }

        int getSortOrderList() {
            return (mModelType.equals(MovieItem.class.getSimpleName())) ?
                    mTMDb.getMovieSortLabels() :
                    mTMDb.getTvSortLabels();
        }


        void registerDataChangeCallback(String key, DataChangeNotification callback) {
            mDataChangeCallbacks.put(key, callback);
        }

        boolean unregisterDataChangeHandler(String key) {
            if (mDataChangeCallbacks.containsKey(key)) {
                mDataChangeCallbacks.remove(key);
            }
            return !mDataChangeCallbacks.isEmpty();
        }

        private void callDataChangeCallbacks() {
            for (String key : mDataChangeCallbacks.keySet()) {
                mDataChangeCallbacks.get(key).onDataChange();
            }
        }

        private void callUpdateStartedCallbacks() {
            for (String key : mDataChangeCallbacks.keySet()) {
                mDataChangeCallbacks.get(key).onUpdateStarted();
            }
        }

        private void callDataErrorCallbacks(String message) {
            for (String key: mDataChangeCallbacks.keySet()) {
                mDataChangeCallbacks.get(key).onDataError(message);
            }
        }

        void close() {
            mTMDb.close(mTMDbHandle);
            mTMDbHandle = -1;
            mTMDb = null;
        }

        void getGenreList(GenreNotification callback) {
            mGenreCallback = callback;
            if (mModelType.equals(MovieItem.class.getSimpleName())) {
                mTMDb.getMovieGenreList(mTMDbHandle);
            } else {
                mTMDb.getTvGenreList(mTMDbHandle);
            }
        }
    }


    //----------------------------------------------------------------------------------------------
    // Interfaces
    //----------------------------------------------------------------------------------------------
    // Interface for date range change callback
    public interface RangeDateNotification {
        void onDateChange(Date start, Date end);
    }

    // Interface for model clients to be notified when the current listing data changes.
    public interface DataChangeNotification {
        void onDataChange();
        void onDataError(String message);
        void onUpdateStarted();
    }

    public interface GenreNotification {
        void onHaveGenreList(ArrayList<Genre> genres, boolean isMovie);
        void errorGenre(String message);
    }




    //----------------------------------------------------------------------------------------------
    // Main model constructors
    //----------------------------------------------------------------------------------------------
    public ListModel(Class<?> cls) {
        //simple name of the the List type
        mModelType = cls.getSimpleName();

        mFilter = new Filter();
        mListOrder = new ListOrder();
        mPaging = new Paging();

        mRefresher = new Refresher();

        mIsDirty = false;
        mHasViewer = false;

        mShows = new ArrayList<>();
    }

    private ListModel(Parcel in) {
        mModelType = in.readString();

        mFilter = new Filter(in);
        mListOrder = new ListOrder(in);
        mPaging = new Paging(in);

        mRefresher = new Refresher();

        mIsDirty = in.readByte() != 0;
        mHasViewer = false;

        mShows = new ArrayList<>();
    }

    //----------------------------------------------------------------------------------------------
    // Parceling support
    //----------------------------------------------------------------------------------------------

    public static final Creator<ListModel> CREATOR = new Creator<ListModel>() {

        @Override
        public ListModel createFromParcel(Parcel in) {
            return new ListModel(in);
        }

        @Override
        public ListModel[] newArray(int size) {
            return new ListModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mModelType);
        mFilter.writeToParcel(parcel);
        mListOrder.writeToParcel(parcel);
        mPaging.writeToParcel(parcel);
        parcel.writeByte((byte) (mIsDirty ? 1 : 0));
    }

    private void processListResults(Bundle bundle) {
        ListInfo info = bundle.getParcelable(Refresher.LIST_INFO);
        if (info == null) return;

        mPaging.mCurrentPage = info.getPage();
        mPaging.mNumberOfPages = info.getTotalPages();
        mPaging.mNumberOfItems = info.getTotalItems();

        mShows = bundle.getParcelableArrayList(Refresher.SHOW_LIST);

        // Adjust Listing info if no listing items
        if (mShows == null) {
            mShows = new ArrayList<>();  // Empty list
            mPaging.mCurrentPage = 0;
            mPaging.mNumberOfItems = 0;
            mPaging.mNumberOfPages = 0;
        }
    }


    // Model state dumper
    public void dump() {
        Log.d(TAG, String.format("Listings Model for %s", mModelType));
        Log.d(TAG, String.format("  Needs update: %s", mIsDirty ? "true" : "false"));
        Log.d(TAG, String.format("  Has a viewer: %s", mHasViewer ? "true" : "false"));
        mFilter.dump();
        mListOrder.dump();
        mPaging.dump();
        if (mRefresher == null) {
            Log.d(TAG, "  No Refresher.");
        } else {
            mRefresher.dump();
        }

    }

    public int getSortOrderList() {
        return getRef().getSortOrderList();
    }


    public void attachViewer(String key, DataChangeNotification callback) {
        if (mRefresher == null) {
            throw new RuntimeException("ListModel: open() has not been called yet");
        }
        getRef().registerDataChangeCallback(key, callback);
        mHasViewer = true;
        if (mIsDirty) {
            getRef().refreshData();
            mIsDirty = false;
        }
    }

    public void detachView(String key) {
        if (mRefresher == null) return;
        mHasViewer = getRef().unregisterDataChangeHandler(key);
    }

    private void doLazyRefresh() {
        if (mHasViewer && mRefresher != null) {
            getRef().refreshData();
        } else {
            mIsDirty = true;
        }
    }

    public Filter getFilter() {
        return mFilter;
    }

    public Paging getPaging() {
        return mPaging;
    }

    public ListOrder getListOrder() {
        return mListOrder;
    }

    public void getGenreList(GenreNotification callback) {
        getRef().getGenreList(callback);
    }
    //----------------------------------------------------------------------------------------------
    // List View Methods
    //----------------------------------------------------------------------------------------------
    public int getListSize() {
        return mShows.size();
    }

    public T getListItem(int item) {
        return mShows.get(item);
    }

    // Get container type
    public String getModelType() {
        return mModelType;
    }


}
