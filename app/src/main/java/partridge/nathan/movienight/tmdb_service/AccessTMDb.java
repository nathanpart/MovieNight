package partridge.nathan.movienight.tmdb_service;


import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import partridge.nathan.movienight.R;
import partridge.nathan.movienight.models.Genre;
import partridge.nathan.movienight.models.ListInfo;
import partridge.nathan.movienight.models.ListItem;
import partridge.nathan.movienight.models.MovieItem;
import partridge.nathan.movienight.models.TvItem;


class AccessTMDb {
    private final String[] popularity_sort = {"popularity.asc", "popularity.desc"};
    private final String[] release_date_sort = {"primary_release_date.asc", "primary_release_date.desc"};
    private final String[] rvenue_sort = {"revenue.asc", "revenue.desc"};
    private final String[] title_sort = {"original_title.asc", "original_title.desc"};
    private final String[] vote_avg_sort = {"vote_average.asc", "vote_average.desc"};
    private final String[] vote_count_sort = {"vote_count.asc", "vote_count.desc"};
    private final String[] first_air_sort = {"first_air_date.asc", "first_air_date.desc"};

    private final String[][] mMovieSortKeys = {popularity_sort, release_date_sort,
            rvenue_sort, title_sort,
            vote_avg_sort, vote_count_sort};

    private final String[][] mTvSortKeys = {popularity_sort, first_air_sort, vote_avg_sort};

    private final OkHttpClient mClient = new OkHttpClient();
    private Context mContext;
    private static String TAG = AccessTMDb.class.getSimpleName();
    private long mRatePeriodEnd;
    private int mRateRemaining;

    private String mApiKey;
    private String mApiLocale;

    // Hold to get return the cache time
    private long mCacheTime;


    AccessTMDb(Context context) {
        mContext = context;
        mApiKey = context.getString(R.string.tmdb_api_key);
        mApiLocale = context.getString(R.string.tmdb_locale);
    }

    // Make request to the TMDb API, handling rate limiting control responses
    @SuppressWarnings("ConstantConditions")
    private String doTMDbGet(Request request) throws IOException {
        Response response;
        Headers responseHeaders;
        String body;

        // Loop to restart this code after delaying for any rate throttling needs
        do {
            long now = System.currentTimeMillis();
            // Handle if out of requests for current time period
            Log.d(TAG, String.format("Now: %d  Period End: %d  Remaining: %d",
                    now, mRatePeriodEnd, mRateRemaining));
            if (mRateRemaining == 0 && mRatePeriodEnd > now) {
                try {
                    long delay = mRatePeriodEnd - now;
                    Log.d(TAG, String.format("Sleeping for %d", delay));
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Problem delaying.", e);
                }
                continue;
            }
            response = mClient.newCall(request).execute();
            responseHeaders = response.headers();

            for (int i = 0; i < responseHeaders.size(); i++) {
                Log.d(TAG, String.format("Header Field: %s   Value: %s",
                        responseHeaders.name(i), responseHeaders.value(i)));
            }
            body = response.body().string();
            Log.d(TAG, String.format("Body: %s", body));
            if (!response.isSuccessful()) {
                // Catch any rate limiting responses
                if (response.code() == 429) {
                    long retry_after;
                    try {
                        retry_after = Integer.parseInt(responseHeaders.get("Retry-After"));
                    } catch (NumberFormatException e) {
                        retry_after = 10;
                    }
                    retry_after *= 1000; // convert from seconds to milliseconds
                    try {
                        Log.d(TAG, String.format("Retrying aftger %d", retry_after));
                        Thread.sleep(retry_after);
                        continue;
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Problem delaying.", e);
                    }
                }
                try {
                    @SuppressWarnings("ConstantConditions")
                    JSONObject responseInfo = new JSONObject(response.body().string());
                    String msg = responseInfo.getString("status_message");
                    int code = responseInfo.getInt("status_code");
                    Log.e(TAG, String.format("%d: %s", code, msg));
                } catch (JSONException e) {
                    Log.e(TAG, "Unable to read response error message from TMDb.");
                }
                throw new IOException("Network Error. Please try again later.");
            }
            break;  // break out of the rate throttling restart loop
        } while (true);

        // Rate limiting header info
        try {
            mRateRemaining = Integer.parseInt(responseHeaders.get("X-RateLimit-Remaining"));
        } catch (NumberFormatException e) {
            mRateRemaining = 0;
        }
        try {
            mRatePeriodEnd = Integer.parseInt(responseHeaders.get("X-RateLimit-Reset"));
        } catch (NumberFormatException e) {
            mRatePeriodEnd = 0;
        }

        // Convert from seconds to milliseconds and if default flag of 0 detected, then set
        // to 10 seconds into future (i.e. full 10s window)
        long now = System.currentTimeMillis();
        mRatePeriodEnd = (mRatePeriodEnd == 0) ?
                now + (10 * 1000) :      // 10 * 1000 = ten seconds in milliseconds
                mRatePeriodEnd * 1000;

        Log.d(TAG, String.format("Updated: Remaining: %d   Period End: %d",
                mRateRemaining, mRatePeriodEnd));

        // Cache time, if unreadable return -1 to indicate unknown cache lifetime
        String cacheControl = responseHeaders.get("Cache-Control");
        if (cacheControl == null) {
            mCacheTime = -1;
        } else {
            // cacheControl has format of "public, max-age=28000"  to get secs split on =
            String[] bits = cacheControl.split("=");
            if (bits.length < 2) {
                // we got an malformed cache time, so mark as unknown
                mCacheTime = -1;
            } else {
                try {
                    mCacheTime = Integer.parseInt(bits[1]);
                } catch (NumberFormatException e) {
                    mCacheTime = -1;
                }
            }
        }
        if (mCacheTime < 0) {
            mCacheTime = (60 * 10) * 1000; // 10 minuet cache time if unable to obtain from header
        } else {
            mCacheTime *= 1000; // Convert from seconds to milliseconds
        }
        mCacheTime += now;      // From length to time that recs are to expire
        Log.d(TAG, String.format("Cache Time: %d", mCacheTime));
        //OkHTTP docs report that body() will not return null when response is returned by execute()
        //  so we just have the null IDE warning turned off.
        //noinspection ConstantConditions
        return body;
    }


    private Boolean parseMediaType(String type) {
        switch (type) {
            case "movie":
                return true;

            case "tv":
                return false;

            default:
                throw new RuntimeException(String.format("%s: Bad media type given to getGenreList()", TAG));
        }
    }

    private List<String> getNameStringList(JSONObject obj, String arrayName) throws JSONException {
        List<String> names = new ArrayList<>();
        JSONArray items = obj.getJSONArray(arrayName);
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            names.add(item.getString("name"));
        }
        return names;
    }

    private void getMovieDetails(MovieItem item, JSONObject obj) throws JSONException {
        item.setBudget(obj.getInt("budget"));
        item.setImbd_id(obj.optString("imdb_id", ""));
        item.setRevenue(obj.getInt("revenue"));
        item.setRuntime(obj.optInt("runtime", 0));
        item.setTagline(obj.optString("tagline", ""));
        item.setProductionCompanies(getNameStringList(obj, "production_companies"));
    }

    private void getTvDetails(TvItem item, JSONObject obj) throws JSONException {
        item.setNumberEpisodes(obj.optInt("number_of_episodes"));
        item.setNumberSeasons(obj.optInt("number_of_seasons"));
        item.setShowType(obj.optString("type"));
        item.setNetworks(getNameStringList(obj, "networks"));
    }

    ListItem getDetails(String type, int id) throws IOException {
        Boolean isMovie = parseMediaType(type);

        String detailEndpoint = String.format(mContext.getString(R.string.detail_endpoint), type, id);
        String url = String.format(mContext.getString(R.string.request_noparms_url),
                detailEndpoint, mApiLocale, mApiKey);

        Log.d(TAG, String.format("Get Details URL: %s", url));

        String results = doTMDbGet(new Request.Builder()
            .url(url)
            .build());

        ListItem item = ListItem.listItemFactory(type);
        try {
            JSONObject returnedData = new JSONObject(results);
            item.setId(returnedData.getInt("id"));
            item.setTitle(returnedData.getString(isMovie ? "original_title" : "name"));
            item.setPopularity(returnedData.getDouble("popularity"));
            item.setVoteCount(returnedData.getInt("vote_count"));
            item.setVoteAverage(returnedData.getDouble("vote_average"));

            // For movies the API docs list that overview can return NULL instead of string
            if (returnedData.isNull("overview")) {
                item.setOverview("Overview not available");
            } else {
                item.setOverview(returnedData.getString("overview"));
            }

            item.setDate(returnedData.getString(isMovie ? "release_date" : "first_air_date"));

            // Get genres this program is listed under
            item.setGenres(getNameStringList(returnedData, "genres"));

            if (item instanceof MovieItem) {
                getMovieDetails((MovieItem) item, returnedData);
            } else if (item instanceof TvItem) {
                getTvDetails((TvItem) item, returnedData);
            }

            item.dump(TAG);

        } catch (JSONException e) {
            Log.e(TAG, String.format("Jason parse issue: %s", e.getMessage()));
            throw new IOException("Network error. Please try again later.");
        }

        return item;
    }


    ListInfo getShowList(int page, int sort_by, int sort_dir, int min_votes,
                         double min_rating, String start, String end,
                         String genres, String type) throws IOException {

        Boolean isMovie = parseMediaType(type);

        String params;
        if (isMovie) {
            params = String.format(mContext.getString(R.string.movie_list_params),
                    genres.length() > 0 ? "with_genres=" + genres + "&" : "",
                    min_rating,
                    min_votes,
                    end,
                    start,
                    page,
                    mMovieSortKeys[sort_by][sort_dir]
                    );
        } else {
            params = String.format(mContext.getString(R.string.tv_list_params),
                    genres,
                    min_votes,
                    min_rating,
                    page,
                    end,
                    start,
                    mTvSortKeys[sort_by][sort_dir]
            );
        }

        String endpoint = String.format(mContext.getString(R.string.list_endpoint), type);
        String url = String.format(mContext.getString(R.string.request_base_url),
                endpoint,
                params,
                mApiLocale,
                mApiKey);

        Log.d(TAG, String.format("Get List URL: %s", url));

        String results = doTMDbGet(new Request.Builder()
                .url(url)
                .build());

        ListInfo listInfo = new ListInfo();
        try {
            JSONObject jo = new JSONObject(results);
            listInfo.setTotalItems(jo.getInt("total_results"));
            listInfo.setTotalPages(jo.getInt("total_pages"));
            listInfo.setPage(jo.getInt("page"));
            List<Integer> showIds = new ArrayList<>();
            JSONArray shows = jo.getJSONArray("results");
            for (int i = 0; i < shows.length(); i++) {
                JSONObject show = shows.getJSONObject(i);
                showIds.add(show.getInt("id"));
            }
            listInfo.setMediaIds(showIds);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listInfo.dump(TAG);

        return listInfo;
    }

    List<Genre> getGenreList(String type) throws IOException {
        Boolean isMovie = parseMediaType(type);

        String endpoint = String.format(mContext.getString(R.string.genre_endpoint), type);
        String url = String.format(mContext.getString(R.string.request_noparms_url),
                endpoint, mApiLocale, mApiKey);

        Log.d(TAG, String.format("Genre List URL: %s", url));

        String results = doTMDbGet(new Request.Builder()
                .url(url)
                .build());

        List<Genre> genres = new ArrayList<>();

        try {
            JSONObject returnedData = new JSONObject(results);
            JSONArray genreList = returnedData.getJSONArray("genres");
            for (int i=0; i < genreList.length(); i++) {
                Genre genre = new Genre();
                JSONObject genreItem = genreList.getJSONObject(i);
                genre.setGenreId(genreItem.getInt("id"));

                if (isMovie) {
                    genre.setMovieLabel(genreItem.getString("name"));
                    genre.setMovie(true);
                    genre.setTv(false);
                } else {
                    genre.setTvLabel(genreItem.getString("name"));
                    genre.setMovie(false);
                    genre.setTv(true);
                }

                genre.setExpirationTime(mCacheTime);
                genres.add(genre);
            }

            Log.d(TAG,"Genre Lists:");
            for (Genre genre : genres) {
                genre.dump(TAG);
            }

            return genres;
        } catch (JSONException e) {
            throw new IOException(String.format("%s: Jsson parse error for genre list", TAG), e);
        }
    }

    long getCacheTime() {
        return mCacheTime;
    }
}
