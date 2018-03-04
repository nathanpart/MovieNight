package partridge.nathan.movienight.models;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.util.Log;

import java.util.List;

import partridge.nathan.movienight.tmdb_service.CacheSql;

public class TvItem extends ListItem {
    private int mNumberEpisodes;
    private int mNumberSeasons;
    private String mShowType;
    private List<String> mNetworks;

    TvItem() {
        super();
        mNumberEpisodes = 0;
        mNumberSeasons = 0;
        mShowType = "";
        mNetworks = null;
    }

    private TvItem(Parcel in) {
        super(in);
        mNumberEpisodes = in.readInt();
        mNumberSeasons = in.readInt();
        mShowType = in.readString();
        mNetworks = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeInt(mNumberEpisodes);
        parcel.writeInt(mNumberSeasons);
        parcel.writeString(mShowType);
        parcel.readStringList(mNetworks);
    }

    @Override
    public void dump(String tag) {
        Log.d(tag, "  Television Item:");
        super.dump(tag);
        Log.d(tag, String.format("    Episodes:  %d", mNumberEpisodes));
        Log.d(tag, String.format("    Seasons:   %d", mNumberEpisodes));
        Log.d(tag, String.format("    Show Type: %s", mNumberEpisodes));
        Log.d(tag, "    Networks:");
        if (mNetworks != null) {
            for (String net: mNetworks) {
                Log.d(tag, String.format("      %s", net));
            }
        }
    }

    public static final Creator<TvItem> CREATOR = new Creator<TvItem>() {
        @Override
        public TvItem createFromParcel(Parcel parcel) {
            return new TvItem(parcel);
        }

        @Override
        public TvItem[] newArray(int size) {
            return new TvItem[size];
        }
    };

    public int getNumberEpisodes() {
        return mNumberEpisodes;
    }

    public void setNumberEpisodes(int numberEpisodes) {
        mNumberEpisodes = numberEpisodes;
    }

    public int getNumberSeasons() {
        return mNumberSeasons;
    }

    public void setNumberSeasons(int numberSeasons) {
        mNumberSeasons = numberSeasons;
    }

    public String getShowType() {
        return mShowType;
    }

    public void setShowType(String showType) {
        mShowType = showType;
    }


    public List<String> getNetworks() {
        return mNetworks;
    }

    public void setNetworks(List<String> networks) {
        mNetworks = networks;
    }


    @Override
    public void SetFromDatabase(SQLiteDatabase database, Cursor cursor, GetListDetails getListDetails) {
        super.SetFromDatabase(database, cursor, getListDetails);
        mNumberEpisodes = cursor.getInt(cursor.getColumnIndex(CacheSql.TD_EPISODES));
        mNumberSeasons = cursor.getInt(cursor.getColumnIndex(CacheSql.TD_SEASONS));
        mShowType = cursor.getString(cursor.getColumnIndex(CacheSql.TD_SHOWTYPE));

        mNetworks = getListDetails.getListDetails(database, CacheSql.NETWORK, "N",
                cursor.getLong(cursor.getColumnIndex(CacheSql.CD_ID)));

    }

    @Override
    public long cacheData(SQLiteDatabase database, long exp_time) {
        long commonId = super.cacheData(database, exp_time);

        ContentValues values = new ContentValues();
        values.put(CacheSql.TD_EPISODES, mNumberEpisodes);
        values.put(CacheSql.TD_SEASONS, mNumberSeasons);
        values.put(CacheSql.TD_SHOWTYPE, mShowType);
        values.put(CacheSql.TD_COM_ID, commonId);
        values.put(CacheSql.TD_EXP_TIME, exp_time);
        database.insert(CacheSql.TV_DETAIL, null, values);

        for (String network: mNetworks) {
            values = new ContentValues();
            values.put(CacheSql.N_SHOW_ID, commonId);
            values.put(CacheSql.N_NAME, network);
            values.put(CacheSql.N_EXP_TIME, exp_time);
            database.insert(CacheSql.NETWORK, null, values);
        }

        return commonId;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
