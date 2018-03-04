package partridge.nathan.movienight.tmdb_service;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import partridge.nathan.movienight.models.Genre;
import partridge.nathan.movienight.models.ListInfo;
import partridge.nathan.movienight.models.ListItem;

public class Cache implements ListItem.GetListDetails {
    private static final String TAG = Cache.class.getSimpleName();
    private CacheSql mCacheSql;

    Cache(Context context) {
        mCacheSql = new CacheSql(context);
    }

    private SQLiteDatabase open() {
        return mCacheSql.getWritableDatabase();
    }

    private void close(SQLiteDatabase database) {
        database.close();
    }

    @Override
    public List<String> getListDetails(SQLiteDatabase database, String table, String prefix, long id) {
        List<String> items = new ArrayList<>();
        Cursor cursor = database.query(table,
                new String[] {prefix + "_NAME"},
                String.format(Locale.US,prefix + "_SHOW_ID=%d", id),
                null,
                null,
                null,
                prefix + "_NAME" + " ASC");
        Log.d(TAG, String.format("Query SELECT %s_NAME FROM %s WHERE %s_SHOW_ID=%d ORDER BY %s_NAME ASC",
                                  prefix, table, prefix, id, prefix));
        Log.d(TAG, "Query returned:");
        if (cursor.moveToFirst()) {
            do {
                String item = cursor.getString(cursor.getColumnIndex(prefix + "_NAME"));
                Log.d(TAG, String.format("    %s", item));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    ListItem queryDetails(String type, long detailId) {
        ListItem item;

        SQLiteDatabase database = open();

        String sql = CacheSql.buildDetailQuery(type, detailId);
        Log.d(TAG, String.format("Query %s", sql));
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            // Cache Hit
            Log.d(TAG, "Results:");
            item = ListItem.listItemFactory(type);
            item.SetFromDatabase(database, cursor, this);
            item.dump(TAG);
        } else {
            // Cache Miss
            Log.d(TAG, "Results: Nothing");
            item = null;
        }

        cursor.close();
        database.close();
        return item;
    }

    void cacheDetails(ListItem item, long listId, long exp_time) {
        SQLiteDatabase database = open();
        database.beginTransaction();
        Log.d(TAG, "Cache Detials");
        item.dump(TAG);

        // Put the item into the detail tables
        long detailId = item.cacheData(database, exp_time);

        // Update foreign keys in the List Detail tables
        ContentValues values = new ContentValues();
        values.put(CacheSql.LI_DETAIL_ID, detailId);
        database.update(CacheSql.LIST_ITEMS, values,
                String.format(Locale.US,"%s=%d AND %s=%d",
                        CacheSql.LI_LIST_ID, listId, CacheSql.LI_MEDIA_ID, item.getId()), null);
        Log.d(TAG, String.format("UPDATE %s SET %s=%d WHERE %s=%d AND %s=%d", CacheSql.LIST_ITEMS,
                CacheSql.LI_DETAIL_ID, detailId, CacheSql.LI_LIST_ID, listId,
                CacheSql.LI_MEDIA_ID, item.getId()));
        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

    ListInfo queryList(int page, int sort_by, int sort_dir, int min_votes, double min_rating,
                       String start, String end, String genres, String type) {
        ListInfo listInfo;

        SQLiteDatabase database = open();
        String sql = CacheSql.buildListQuery(page, sort_by, sort_dir, min_votes,
                min_rating, start, end, genres, type);

        Log.d(TAG, String.format("Query: %s", sql));

        Cursor cursor = database.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            // Cache hit
            listInfo = new ListInfo();
            listInfo.setTotalItems(cursor.getInt(cursor.getColumnIndex(CacheSql.L_TOTAL_ITEMS)));
            listInfo.setTotalPages(cursor.getInt(cursor.getColumnIndex(CacheSql.L_PAGE_COUNT)));
            listInfo.setPage(cursor.getInt(cursor.getColumnIndex(CacheSql.L_PAGE_NUM)));
            listInfo.setListId(cursor.getLong(cursor.getColumnIndex(CacheSql.L_ID)));

            List<Integer> mediaIds = new ArrayList<>();
            List<Long> detailIds = new ArrayList<>();
            do {
                mediaIds.add(cursor.getInt(cursor.getColumnIndex(CacheSql.LI_MEDIA_ID)));
                int columnIndex = cursor.getColumnIndex(CacheSql.LI_DETAIL_ID);
                if (cursor.isNull(columnIndex)) {
                    detailIds.add(null);
                } else {
                    detailIds.add(cursor.getLong(columnIndex));
                }
            } while (cursor.moveToNext());
            listInfo.setMediaIds(mediaIds);
            listInfo.setDetailIds(detailIds);
            listInfo.dump(TAG);
        } else {
            // Cache miss
            Log.d(TAG, "  List Cache miss");
            listInfo = null;
        }
        cursor.close();
        close(database);
        return listInfo;
    }

    void cacheList(ListInfo list, long exp_time, int sort_by, int sort_dir, int min_votes,
                           double min_rating, String start, String end, String genres, String type) {
        SQLiteDatabase database = open();
        database.beginTransaction();

        Log.d(TAG,"Cache List");
        list.dump(TAG);
        Log.d(TAG, String.format("  Expiration Time: %d", exp_time));
        Log.d(TAG, String.format("  Sort By: %d", sort_by));
        Log.d(TAG, String.format("  Sort Dir: %d", sort_dir));
        Log.d(TAG, String.format("  Min Votes:: %d", min_votes));
        Log.d(TAG, String.format("  Min Rating:: %f", min_rating));
        Log.d(TAG, String.format("  Start Date: %s", start));
        Log.d(TAG, String.format("  End Date: %s", end));
        Log.d(TAG, String.format("  Genres: %s", genres));
        Log.d(TAG, String.format("  Type: %s", type));

        ContentValues values = new ContentValues();
        values.put(CacheSql.L_TOTAL_ITEMS, list.getTotalItems());
        values.put(CacheSql.L_PAGE_COUNT, list.getTotalPages());
        values.put(CacheSql.L_PAGE_NUM, list.getPage());
        values.put(CacheSql.L_SORT_BY, sort_by);
        values.put(CacheSql.L_SORT_DIR, sort_dir);
        values.put(CacheSql.L_MIN_VOTES, min_votes);
        values.put(CacheSql.L_MIN_RATING, min_rating);
        values.put(CacheSql.L_START_DATE, start);
        values.put(CacheSql.L_END_DATE, end);
        values.put(CacheSql.L_GENRES, genres);
        values.put(CacheSql.L_TYPE, type);
        values.put(CacheSql.L_EXP_TIME, exp_time);
        long listId = database.insert(CacheSql.LIST, null, values);
        list.setListId(listId);

        int item_num = 0;
        List<Long> detailIds = new ArrayList<>(list.getMediaIds().size());
        for (int mediaId : list.getMediaIds()) {
            values = new ContentValues();
            values.put(CacheSql.LI_ITEM_NUM, item_num);
            values.put(CacheSql.LI_LIST_ID, listId);
            values.put(CacheSql.LI_MEDIA_ID, mediaId);
            values.put(CacheSql.LI_EXP_TIME, exp_time);

            // See if we have the show in cache
            String sql;
            if (type.equals(JobController.MOVIE)) {
                sql = String.format(Locale.US, CacheSql.GET_MOVIE_MEDIA, mediaId);
            } else {
                sql = String.format(Locale.US, CacheSql.GET_TV_MEDIA, mediaId);
            }
            Cursor cursor = database.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                long detailId = cursor.getLong(cursor.getColumnIndex(CacheSql.CD_ID));
                detailIds.add(detailId);
                values.put(CacheSql.LI_DETAIL_ID, detailId);
            } else {
                // not setting LI_DETAIL_ID results null id db record
                detailIds.add(null);
            }
            cursor.close();

            database.insert(CacheSql.LIST_ITEMS, null, values);
            item_num++;
        }
        list.setDetailIds(detailIds);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }


    List<Genre> queryGenreList(String type) {
        List<Genre> genres;

        Log.d(TAG, "Query Genre List:");
        Log.d(TAG, String.format("  Type: %s", type));

        Log.d(TAG, String.format(" Query SELECT %s, %s, %s, %s FROM %s WHERE %s='%s' ORDER BY %s ASC",
                CacheSql.G_TYPE, CacheSql.G_GENRE_ID, CacheSql.G_LABEL, CacheSql.G_EXP_TIME,
                CacheSql.GENRE, CacheSql.G_TYPE, type, CacheSql.G_TYPE));
        SQLiteDatabase database = open();
        Cursor cursor = database.query(CacheSql.GENRE,
                new String[] {CacheSql.G_TYPE, CacheSql.G_GENRE_ID, CacheSql.G_LABEL, CacheSql.G_EXP_TIME},
                String.format(Locale.US, CacheSql.G_TYPE + "='%s'", type),
                null,
                null,
                null,
                CacheSql.G_LABEL + " ASC");
        if (cursor.moveToFirst()) {
            genres = new ArrayList<>();
            do {
                Genre genre = new Genre();
                genre.setGenreId(cursor.getInt(cursor.getColumnIndex(CacheSql.G_GENRE_ID)));
                if (type.equals("tv"))
                    genre.setTvLabel(cursor.getString(cursor.getColumnIndex(CacheSql.G_LABEL)));
                if (type.equals("movie"))
                    genre.setMovieLabel(cursor.getString(cursor.getColumnIndex(CacheSql.G_LABEL)));
                genre.setTv(type.equals("tv"));
                genre.setMovie(type.equals("movie"));
                genre.setExpirationTime(cursor.getLong(cursor.getColumnIndex(CacheSql.G_EXP_TIME)));
                genre.dump(TAG);
                genres.add(genre);
            } while (cursor.moveToNext());
        } else {
            Log.d(TAG, "    Nothing reutrned");
            genres = null;
        }

        cursor.close();
        database.close();
        return genres;

    }

    void cacheGenreList(List<Genre> genres, String type) {
        SQLiteDatabase database = open();

        Log.d(TAG, "Cache Genre List");

        database.beginTransaction();
        for (Genre genre : genres) {
            genre.dump(TAG);

            ContentValues values = new ContentValues();
            values.put(CacheSql.G_GENRE_ID, genre.getGenreId());
            values.put(CacheSql.G_LABEL, type.equals("movie") ? genre.getMovieLabel() : genre.getTvLabel());
            values.put(CacheSql.G_TYPE, type);
            values.put(CacheSql.G_EXP_TIME, genre.getExpirationTime());

            database.insert(CacheSql.GENRE, null, values);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }


    // Clean out stale (expired) cached items
    void grimReaper() {
        SQLiteDatabase database = open();
        database.beginTransaction();
        long now = System.currentTimeMillis();

        Log.d(TAG, "Grim Reaper");
        Log.d(TAG, String.format("  Now: %d", now));

        // Delete records in the three main tables, and foreign key constraints should cascade to the
        // others
        database.delete(CacheSql.LIST,
                String.format(Locale.US,"%s<%d", CacheSql.L_EXP_TIME, now), null);

        database.delete(CacheSql.LIST_ITEMS,
                String.format(Locale.US,"%s<%d", CacheSql.LI_EXP_TIME, now), null);

        database.delete(CacheSql.COMMON_DETAL,
                String.format(Locale.US, "%s<%d", CacheSql.CD_EXP_TIME, now), null);

        database.delete(CacheSql.MOVIE_DETAIL,
                String.format(Locale.US, "%s<%d", CacheSql.MD_EXP_TIME, now), null);

        database.delete(CacheSql.TV_DETAIL,
                String.format(Locale.US, "%s<%d", CacheSql.TD_EXP_TIME, now), null);

        database.delete(CacheSql.GENRE,
                String.format(Locale.US, "%s<%d", CacheSql.G_EXP_TIME, now), null);

        database.delete(CacheSql.GENRE_DETAIL_LIST,
                String.format(Locale.US, "%s<%d", CacheSql.GDL_EXP_TIME, now), null);

        database.delete(CacheSql.PRODUCTION_COMPANY,
                String.format(Locale.US, "%s<%d", CacheSql.PC_EXP_TIME, now), null);

        database.delete(CacheSql.NETWORK,
                String.format(Locale.US, "%s<%d", CacheSql.N_EXP_TIME, now), null);

        database.setTransactionSuccessful();
        database.endTransaction();

        close(database);
    }

     void dumpdbs() {
        SQLiteDatabase db = open();
        Cursor cursor;
        Log.d(TAG, "Database Dump:");

        Log.d(TAG, String.format("  Database: %s", CacheSql.GENRE));
        Log.d(TAG, String.format("    %s|%s|%s|%s|%s", CacheSql.G_ID, CacheSql.G_GENRE_ID,
                CacheSql.G_LABEL, CacheSql.G_TYPE, CacheSql.G_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.GENRE), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%s|%s|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.G_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.G_GENRE_ID)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.G_LABEL)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.G_TYPE)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.G_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, String.format("  Database: %s", CacheSql.COMMON_DETAL));
        Log.d(TAG, String.format("    %s|%s|%s|%s|%s|%s|%s|%s|%s", CacheSql.G_ID, CacheSql.CD_MEDIA_ID,
                CacheSql.CD_TITLE, CacheSql.CD_POPULARITY, CacheSql.CD_VOTE_COUNT,
                CacheSql.CD_RATING, CacheSql.CD_OVERVIEW, CacheSql.CD_DATE, CacheSql.CD_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.COMMON_DETAL), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%s|%d|%d|%f|%s|%s|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.CD_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.CD_MEDIA_ID)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.CD_TITLE)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.CD_POPULARITY)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.CD_VOTE_COUNT)),
                        cursor.getDouble(cursor.getColumnIndex(CacheSql.CD_RATING)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.CD_OVERVIEW)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.CD_DATE)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.CD_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, String.format("  Database: %s", CacheSql.MOVIE_DETAIL));
        Log.d(TAG, String.format("    %s|%s|%s|%s|%s|%s|%s", CacheSql.MD_ID, CacheSql.MD_COM_ID,
                CacheSql.MD_BUDGET, CacheSql.MD_REVENUE, CacheSql.MD_RUNTIME,
                CacheSql.MD_TAGLINE, CacheSql.MD_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.MOVIE_DETAIL), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%d|%d|%d|%s|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.MD_ID)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.MD_COM_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.MD_BUDGET)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.MD_REVENUE)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.MD_RUNTIME)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.MD_TAGLINE)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.MD_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, String.format("  Database: %s", CacheSql.TV_DETAIL));
        Log.d(TAG, String.format("    %s|%s|%s|%s|%s|%s", CacheSql.TD_ID, CacheSql.TD_COM_ID,
                CacheSql.TD_EPISODES, CacheSql.TD_SEASONS, CacheSql.TD_SHOWTYPE,
                CacheSql.TD_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.TV_DETAIL), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%d|%d|%s|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.TD_ID)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.TD_COM_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.TD_EPISODES)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.TD_SEASONS)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.TD_SHOWTYPE)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.TD_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, String.format("  Database: %s", CacheSql.GENRE_DETAIL_LIST));
        Log.d(TAG, String.format("    %s|%s|%s|%s", CacheSql.GDL_ID, CacheSql.GDL_SHOW_ID,
                CacheSql.GDL_NAME, CacheSql.GDL_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.GENRE_DETAIL_LIST), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%s|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.GDL_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.GDL_SHOW_ID)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.GDL_NAME)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.GDL_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, String.format("  Database: %s", CacheSql.PRODUCTION_COMPANY));
        Log.d(TAG, String.format("    %s|%s|%s|%s", CacheSql.PC_ID, CacheSql.PC_SHOW_ID,
                CacheSql.PC_NAME, CacheSql.PC_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.PRODUCTION_COMPANY), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%s|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.PC_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.PC_SHOW_ID)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.PC_NAME)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.PC_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, String.format("  Database: %s", CacheSql.NETWORK));
        Log.d(TAG, String.format("    %s|%s|%s|%s", CacheSql.N_ID, CacheSql.N_SHOW_ID,
                CacheSql.N_NAME, CacheSql.N_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.NETWORK), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%s|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.N_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.N_SHOW_ID)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.N_NAME)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.N_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, String.format("  Database: %s", CacheSql.LIST));
        Log.d(TAG, String.format("    %s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s", CacheSql.L_ID,
                CacheSql.L_TOTAL_ITEMS, CacheSql.L_PAGE_COUNT, CacheSql.L_PAGE_NUM,
                CacheSql.L_SORT_BY, CacheSql.L_SORT_DIR, CacheSql.L_MIN_VOTES, CacheSql.L_MIN_RATING,
                CacheSql.L_START_DATE, CacheSql.L_END_DATE, CacheSql.L_GENRES, CacheSql.L_TYPE,
                CacheSql.L_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.LIST), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%d|%d|%d|%d|%d|%f|%s|%s|%s|%s|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.L_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.L_TOTAL_ITEMS)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.L_PAGE_COUNT)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.L_PAGE_NUM)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.L_SORT_BY)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.L_SORT_DIR)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.L_MIN_VOTES)),
                        cursor.getDouble(cursor.getColumnIndex(CacheSql.L_MIN_RATING)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.L_START_DATE)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.L_END_DATE)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.L_GENRES)),
                        cursor.getString(cursor.getColumnIndex(CacheSql.L_TYPE)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.L_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, String.format("  Database: %s", CacheSql.LIST_ITEMS));
        Log.d(TAG, String.format("    %s|%s|%s|%s|%s|%s", CacheSql.LI_ID, CacheSql.LI_ITEM_NUM,
                CacheSql.LI_LIST_ID, CacheSql.LI_DETAIL_ID, CacheSql.LI_MEDIA_ID,
                CacheSql.LI_EXP_TIME));
        cursor = db.rawQuery(String.format("SELECT * FROM %s", CacheSql.LIST_ITEMS), null);
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, String.format("    %d|%d|%d|%d|%d|%d",
                        cursor.getLong(cursor.getColumnIndex(CacheSql.LI_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.LI_ITEM_NUM)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.LI_LIST_ID)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.LI_DETAIL_ID)),
                        cursor.getInt(cursor.getColumnIndex(CacheSql.LI_MEDIA_ID)),
                        cursor.getLong(cursor.getColumnIndex(CacheSql.LI_EXP_TIME))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        close(db);
    }
}
