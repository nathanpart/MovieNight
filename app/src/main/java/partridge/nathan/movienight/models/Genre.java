package partridge.nathan.movienight.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Genre  implements Parcelable {
    private int mGenreId;
    private String mMovieLabel;
    private String mTvLabel;       //
    private boolean mIsTv;         // Genre listed for use is tv listings
    private boolean mIsMovie;      // Genre listed for use in movie listings
    private long mExpirationTime;  // Time when cached item from TMDb expires

    public Genre() {
        mGenreId = 0;
        mMovieLabel = "";
        mTvLabel = "";
        mIsTv = false;
        mIsMovie = false;
    }


    private Genre(Parcel parcel) {
        mGenreId = parcel.readByte();
        mMovieLabel = parcel.readString();
        mTvLabel = parcel.readString();
        mIsTv = parcel.readByte() == 1;
        mIsMovie = parcel.readByte() == 1;
        mExpirationTime = parcel.readLong();
    }

    public int getGenreId() {
        return mGenreId;
    }

    public void setGenreId(int genreId) {
        this.mGenreId = genreId;
    }

    public String getMovieLabel() {
        return mMovieLabel;
    }

    public void setMovieLabel(String movieLabel) {
        this.mMovieLabel = movieLabel;
    }

    public String getTvLabel() {
        return mTvLabel;
    }

    public void setTvLabel(String tvLabel) {
        this.mTvLabel = tvLabel;
    }

    public boolean isTv() {
        return mIsTv;
    }

    public void setTv(boolean tv) {
        mIsTv = tv;
    }

    public boolean isMovie() {
        return mIsMovie;
    }

    public void setMovie(boolean movie) {
        mIsMovie = movie;
    }

    public static final Parcelable.Creator<Genre> CREATOR = new Creator<Genre>() {
        @Override
        public Genre createFromParcel(Parcel parcel) {
            return new Genre(parcel);
        }

        @Override
        public Genre[] newArray(int i) {
            return new Genre[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mGenreId);
        parcel.writeString(mMovieLabel);
        parcel.writeString(mTvLabel);
        parcel.writeByte((byte) (mIsTv ? 1 : 0));
        parcel.writeByte((byte) (mIsMovie ? 1 : 0));
        parcel.writeLong(mExpirationTime);
    }

    public long getExpirationTime() {
        return mExpirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        mExpirationTime = expirationTime;
    }

    public void dump(String tag) {
        Log.d(tag, String.format("    Genre Id:              %d", mGenreId));
        Log.d(tag, String.format("    Movie Label:           %s", mMovieLabel));
        Log.d(tag, String.format("    Tv Label:              %s", mTvLabel));
        Log.d(tag, String.format("    Is Tv:                 %s", mIsTv));
        Log.d(tag, String.format("    Is Movie:              %s", mIsMovie));
        Log.d(tag, String.format("    Cache Expiration Time: %d", mExpirationTime));
    }
}