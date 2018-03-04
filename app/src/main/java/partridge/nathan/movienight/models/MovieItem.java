package partridge.nathan.movienight.models;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.util.Log;

import java.util.List;

import partridge.nathan.movienight.tmdb_service.CacheSql;

public class MovieItem extends ListItem {
    private int mBudget;
    private String imbd_id;
    private int mRevenue;
    private int mRuntime;
    private String mTagline;
    private List<String> mProductionCompanies;

    MovieItem() {
        super();
        mBudget = 0;
        imbd_id = "";
        mRevenue = 0;
        mRuntime = 0;
        mTagline = "";
        mProductionCompanies = null;
    }

    private MovieItem(Parcel in) {
        super(in);
        mBudget = in.readInt();
        imbd_id = in.readString();
        mRevenue = in.readInt();
        mRuntime = in.readInt();
        mTagline = in.readString();
        mProductionCompanies = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeInt(mBudget);
        parcel.writeString(imbd_id);
        parcel.writeInt(mRevenue);
        parcel.writeInt(mRuntime);
        parcel.writeString(mTagline);
        parcel.writeStringList(mProductionCompanies);
    }

    @Override
    public void dump(String tag) {
        Log.d(tag, "   Movie Item:");
        super.dump(tag);
        Log.d(tag, String.format("    Budget:   %d", mBudget));
        Log.d(tag, String.format("    Imbd Id:  %s", imbd_id));
        Log.d(tag, String.format("    Revenue:  %d", mRevenue));
        Log.d(tag, String.format("    Runtime:  %d", mRuntime));
        Log.d(tag, String.format("    Tag Line: %s", mTagline));
        Log.d(tag, "    Production Companies:");
        if (mProductionCompanies != null) {
            for (String co: mProductionCompanies) {
                Log.d(tag, String.format("      %s", co));
            }
        }
    }

    public static final Creator<MovieItem> CREATOR = new Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel parcel) {
            return new MovieItem(parcel);
        }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    public int getBudget() {
        return mBudget;
    }

    public void setBudget(int budget) {
        mBudget = budget;
    }

    public String getImbd_id() {
        return imbd_id;
    }


    public void setImbd_id(String imbd_id) {
        this.imbd_id = imbd_id;
    }

    public int getRevenue() {
        return mRevenue;
    }

    public void setRevenue(int revenue) {
        mRevenue = revenue;
    }

    public int getRuntime() {
        return mRuntime;
    }

    public void setRuntime(int runtime) {
        mRuntime = runtime;
    }

    public String getTagline() {
        return mTagline;
    }


    public void setTagline(String tagline) {
        mTagline = tagline;
    }

    public List<String> getProductionCompanies() {
        return mProductionCompanies;
    }

    public void setProductionCompanies(List<String> productionCompanies) {
        mProductionCompanies = productionCompanies;
    }

    @Override
    public void SetFromDatabase(SQLiteDatabase database, Cursor cursor, GetListDetails getListDetails) {
        super.SetFromDatabase(database, cursor, getListDetails);

        mBudget = cursor.getInt(cursor.getColumnIndex(CacheSql.MD_BUDGET));
        mRevenue = cursor.getInt(cursor.getColumnIndex(CacheSql.MD_REVENUE));
        mRevenue = cursor.getInt(cursor.getColumnIndex(CacheSql.MD_RUNTIME));
        mTagline = cursor.getString(cursor.getColumnIndex(CacheSql.MD_TAGLINE));

        mProductionCompanies = getListDetails.getListDetails(database, CacheSql.PRODUCTION_COMPANY,
                "PC", cursor.getLong(cursor.getColumnIndex(CacheSql.CD_ID)));

    }

    @Override
    public long cacheData(SQLiteDatabase database, long exp_time) {
        long commonId = super.cacheData(database, exp_time);

        ContentValues values = new ContentValues();
        values.put(CacheSql.MD_COM_ID, commonId);
        values.put(CacheSql.MD_BUDGET, mBudget);
        values.put(CacheSql.MD_REVENUE, mRevenue);
        values.put(CacheSql.MD_RUNTIME, mRuntime);
        values.put(CacheSql.MD_TAGLINE, mTagline);
        values.put(CacheSql.MD_EXP_TIME, exp_time);
        database.insert(CacheSql.MOVIE_DETAIL, null, values);

        for (String p: mProductionCompanies) {
            values = new ContentValues();
            values.put(CacheSql.PC_SHOW_ID, commonId);
            values.put(CacheSql.PC_NAME, p);
            values.put(CacheSql.PC_EXP_TIME, exp_time);
            database.insert(CacheSql.PRODUCTION_COMPANY, null, values);
        }

        return commonId;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
