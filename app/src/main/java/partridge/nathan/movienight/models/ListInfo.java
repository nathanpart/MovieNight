package partridge.nathan.movienight.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ListInfo implements Parcelable {
    private long mListId;
    private int mTotalItems;
    private int mTotalPages;
    private int mPage;
    private List<Integer> mMediaIds;
    private List<Long> mDetailIds;

    public ListInfo() {
        mListId = 0;
        mTotalItems = 0;
        mTotalPages = 0;
        mPage = 0;
        mMediaIds = null;
        mDetailIds = null;
    }

    private ListInfo(Parcel in) {

        mListId = in.readLong();
        mTotalItems = in.readInt();
        mTotalPages = in.readInt();
        mPage = in.readInt();
        int mShowCount = in.readInt();
        mMediaIds = new ArrayList<>(mShowCount);
        mDetailIds = new ArrayList<>(mShowCount);

        int[] mediaIds = new int[mShowCount];
        in.readIntArray(mediaIds);
        for (int id: mediaIds) mMediaIds.add(id);

        long[] detailIds = new long[mShowCount];
        in.readLongArray(detailIds);
        for (long id: detailIds) mDetailIds.add(id);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mListId);
        parcel.writeInt(mTotalItems);
        parcel.writeInt(mTotalPages);
        parcel.writeInt(mPage);

        int arrayCount = mMediaIds.size();
        parcel.writeInt(arrayCount);

        int[] mediaIds = new int[arrayCount];
        long[] detailIds = new long[arrayCount];
        for (int i = 0; i < arrayCount; i++) {
            mediaIds[i] = mMediaIds.get(i);
            detailIds[i] = mDetailIds.get(i);
        }
        parcel.writeIntArray(mediaIds);
        parcel.writeLongArray(detailIds);
    }

    public static final Creator<ListInfo> CREATOR = new Creator<ListInfo>() {
        @Override
        public ListInfo createFromParcel(Parcel in) {
            return new ListInfo(in);
        }

        @Override
        public ListInfo[] newArray(int size) {
            return new ListInfo[size];
        }
    };

    public long getListId() {
        return mListId;
    }

    public void setListId(long listId) {
        mListId = listId;
    }

    public int getTotalItems() {
        return mTotalItems;
    }

    public void setTotalItems(int totalItems) {
        mTotalItems = totalItems;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public List<Integer> getMediaIds() {
        return mMediaIds;
    }

    public void setMediaIds(List<Integer> items) {
        mMediaIds = items;
    }

    public List<Long> getDetailIds() {
        return mDetailIds;
    }

    public void setDetailIds(List<Long> detailIds) {
        mDetailIds = detailIds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void dump(String tag) {
        Log.d(tag, "  List Info:");
        Log.d(tag, String.format("    List Id:     %d", mListId));
        Log.d(tag, String.format("    Total Items: %d", mTotalItems));
        Log.d(tag, String.format("    Total Pages: %d", mTotalPages));
        Log.d(tag, String.format("    Page:        %d", mPage));
        Log.d(tag, "    TMDB Movie Ids:");
        if (mMediaIds != null) {
            for (int id: mMediaIds) {
                Log.d(tag, String.format("      %d", id));
            }
        }
        Log.d(tag, "    Cache Detail Ids:");
        if (mDetailIds != null) {
            for (Long id: mDetailIds) {
                if (id != null) {
                    Log.d(tag, String.format("      %d", id));
                } else {
                    Log.d(tag, "");
                }
            }
        }
    }
}
