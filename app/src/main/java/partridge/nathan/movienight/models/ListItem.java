package partridge.nathan.movienight.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import partridge.nathan.movienight.tmdb_service.CacheSql;


// Plain old data - Used to pass data from the models to the Recycler view
public abstract class ListItem implements Parcelable {
    private int mId;
    private String mTitle;
    private double mPopularity;
    private int mVoteCount;
    private double mVoteAverage;
    private String mOverview;
    private List<String> mGenres;
    private String mDate;

    ListItem() {
        mId = 0;
        mTitle = "";
        mPopularity = 0;
        mVoteCount = 0;
        mVoteAverage = 0;
        mOverview = "";
        mGenres = null;
        mDate = "";
    }

    ListItem(Parcel in) {
        mId = in.readInt();
        mTitle = in.readString();
        mPopularity = in.readDouble();
        mVoteCount = in.readInt();
        mVoteAverage = in.readDouble();
        mOverview = in.readString();
        mGenres = in.createStringArrayList();
        mDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mId);
        parcel.writeString(mTitle);
        parcel.writeDouble(mPopularity);
        parcel.writeInt(mVoteCount);
        parcel.writeDouble(mVoteAverage);
        parcel.writeString(mOverview);
        parcel.writeStringList(mGenres);
        parcel.writeString(mDate);
    }

    public void dump(String tag) {
        Log.d(tag, String.format("    Id: %d", mId));
        Log.d(tag, String.format("    Title: %s", mTitle));
        Log.d(tag, String.format("    Popularity: %s", mPopularity));
        Log.d(tag, String.format("    Vote Count: %d", mVoteCount));
        Log.d(tag, String.format("    Rating: %f", mVoteAverage));
        Log.d(tag, String.format("    Overview: %s", mOverview));
        Log.d(tag, String.format("    Date: %s", mDate));
        Log.d(tag, "    Genres:");
        if (mGenres != null) {
            for (String genre: mGenres) {
                Log.d(tag, String.format("      %s", mGenres));
            }
        }
    }

    public interface GetListDetails {
        List<String> getListDetails(SQLiteDatabase database, String table, String prefix, long id);

    }


    public static ListItem listItemFactory(String type) {
        switch (type) {
            case "movie":
                return new MovieItem();
            case "tv":
                return new TvItem();
            default:
                throw new RuntimeException(String.format("List Item Factory: Bad Media type (%s).", type));
        }
    }


    public static List listItemListFactory(String type) {
        switch (type) {
            case "movie":
                return new ArrayList<MovieItem>();
            case "tv":
                return new ArrayList<TvItem>();
            default:
                return null;
        }

    }

    public boolean isHasDetails() {
        return mHasDetails;
    }

    public void setHasDetails(boolean hasDetails) {
        mHasDetails = hasDetails;
    }

    private boolean mHasDetails;

    public ListItem(String title, float rating, int voteCount, double popularity, boolean hasRevenue, int revenue, boolean isReleaseDate, String date) {
        mTitle = title;
        mVoteAverage = rating;
        mVoteCount = voteCount;
        mPopularity = popularity;
        mDate = date;
    }

    public void SetFromDatabase(SQLiteDatabase database, Cursor cursor, GetListDetails getListDetails) {
        mId = cursor.getInt(cursor.getColumnIndex(CacheSql.CD_MEDIA_ID));
        mTitle = cursor.getString(cursor.getColumnIndex(CacheSql.CD_TITLE));
        mPopularity = cursor.getDouble(cursor.getColumnIndex(CacheSql.CD_POPULARITY));
        mVoteCount = cursor.getInt(cursor.getColumnIndex(CacheSql.CD_VOTE_COUNT));
        mVoteAverage = cursor.getDouble(cursor.getColumnIndex(CacheSql.CD_RATING));
        mOverview = cursor.getString(cursor.getColumnIndex(CacheSql.CD_OVERVIEW));
        mDate = cursor.getString(cursor.getColumnIndex(CacheSql.CD_DATE));
        mGenres = getListDetails.getListDetails(database, CacheSql.GENRE_DETAIL_LIST,
                "GDL",
                cursor.getLong(cursor.getColumnIndex(CacheSql.CD_ID)));
    }

    public long cacheData(SQLiteDatabase database, long exp_time) {
        ContentValues values = new ContentValues();
        values.put(CacheSql.CD_MEDIA_ID, mId);
        values.put(CacheSql.CD_TITLE, mTitle);
        values.put(CacheSql.CD_POPULARITY, mPopularity);
        values.put(CacheSql.CD_VOTE_COUNT, mVoteCount);
        values.put(CacheSql.CD_RATING, mVoteAverage);
        values.put(CacheSql.CD_OVERVIEW, mOverview);
        values.put(CacheSql.CD_DATE, mDate);
        values.put(CacheSql.CD_EXP_TIME, exp_time);
        long commonId = database.insert(CacheSql.COMMON_DETAL, null, values);

        for (String genre: mGenres) {
            values = new ContentValues();
            values.put(CacheSql.GDL_SHOW_ID, commonId);
            values.put(CacheSql.GDL_NAME, genre);
            values.put(CacheSql.GDL_EXP_TIME, exp_time);
            database.insert(CacheSql.GENRE_DETAIL_LIST, null, values);
        }

        return commonId;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public double getPopularity() {
        return mPopularity;
    }

    public void setPopularity(double popularity) {
        mPopularity = popularity;
    }

    public int getVoteCount() {
        return mVoteCount;
    }

    public void setVoteCount(int voteCount) {
        mVoteCount = voteCount;
    }

    public double getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        mVoteAverage = voteAverage;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public List<String> getGenres() {
        return mGenres;
    }

    public void setGenres(List<String> genres) {
        mGenres = genres;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public float getRating() {
        return (float) mVoteAverage;
    }


}
