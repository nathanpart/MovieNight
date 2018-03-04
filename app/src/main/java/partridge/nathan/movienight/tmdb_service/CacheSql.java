package partridge.nathan.movienight.tmdb_service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;


public class CacheSql extends SQLiteOpenHelper {
    private static final String DB_NAME = "movie_night_cache.db";
    private static final int DB_VERSION = 1;


    static final String GENRE = "GENRE";
    static final String G_ID = "G_ID";
    static final String G_GENRE_ID = "G_TYPE_ID";
    static final String G_LABEL = "G_LABEL";
    static final String G_TYPE = "G_TYPE";
    static final String G_EXP_TIME = "G_EXP_TIME";

    public static final String COMMON_DETAL = "COMMON_DETAIL";

    public static final String CD_ID = "CD_ID";
    public static final String CD_MEDIA_ID = "CD_MEDIA_ID";
    public static final String CD_TITLE = "CD_TITLE";
    public static final String CD_POPULARITY = "CD_POPULARITY";
    public static final String CD_VOTE_COUNT = "CD_VOTE_COUNT";
    public static final String CD_RATING = "CD_RATING";
    public static final String CD_OVERVIEW = "CD_OVERVIEW";
    public static final String CD_DATE = "CD_DATE";
    public static final String CD_EXP_TIME = "CD_EXP_TIME";

    public static final String MOVIE_DETAIL = "MOVIE_DETAIL";

    static final String MD_ID = "MD_ID";

    public static final String MD_COM_ID = "MD_COM_ID";
    public static final String MD_BUDGET = "MD_BUDGET";
    public static final String MD_REVENUE = "MD_REVENUE";
    public static final String MD_RUNTIME = "MD_RUNTIME";
    public static final String MD_TAGLINE = "MD_TAGLINE";
    public static final String MD_EXP_TIME = "MD_EXP_TIME";

    public static final String TV_DETAIL = "TV_DETAIL";

    static final String TD_ID = "TD_ID";

    public static final String TD_COM_ID = "TD_COM_ID";
    public static final String TD_EPISODES = "TD_EPISODES";
    public static final String TD_SEASONS = "TD_SEASONS";
    public static final String TD_SHOWTYPE = "TD_SHOW_TYPE";
    public static final String TD_EXP_TIME = "TD_EXP_TIME";

    static final String GET_MOVIE_MEDIA = "SELECT * FROM " + COMMON_DETAL +
            " INNER JOIN " + MOVIE_DETAIL + " ON " + CD_ID + "=" + MD_COM_ID +
            " WHERE " + CD_MEDIA_ID + "=%d";


    static final String GET_TV_MEDIA = "SELECT * FROM " + COMMON_DETAL +
            " INNER JOIN " + TV_DETAIL + " ON " + CD_ID + "=" + TD_COM_ID +
            " WHERE " + CD_MEDIA_ID + "=%d";

    public static final String GENRE_DETAIL_LIST = "GENRE_DETAIL_LIST";

    static final String GDL_ID = "GDL_ID";
    public static final String GDL_SHOW_ID = "GDL_SHOW_ID";
    public static final String GDL_NAME = "GDL_NAME";
    public static final String GDL_EXP_TIME = "GDL_EXP_TIME";
    private static final String CREATE_GENRE_DETAIL_LIST =
            "CREATE TABLE " + GENRE_DETAIL_LIST + "(" +
                    GDL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    GDL_SHOW_ID + " INTEGER," +
                    GDL_NAME + " TEXT," +
                    GDL_EXP_TIME + " INTEGER," +
                    "FOREIGN KEY(" + GDL_SHOW_ID + ") REFERENCES " + COMMON_DETAL + "(" + CD_ID + ") ON DELETE CASCADE)";

    public static final String PRODUCTION_COMPANY = "PRODUCTION_COMPANY";

    static final String PC_ID = "PC_ID";
    public static final String PC_SHOW_ID = "PC_SHOW_ID";
    public static final String PC_NAME = "PC_NAME";
    public static final String PC_EXP_TIME = "PC_EXP_TIME";
    private static final String CREATE_PRODUCTION_COMPANY =
            "CREATE TABLE " + PRODUCTION_COMPANY + "(" +
                    PC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PC_SHOW_ID + " INTEGER," +
                    PC_NAME + " TEXT," +
                    PC_EXP_TIME + " INTEGER," +
                    "FOREIGN KEY(" + PC_SHOW_ID + ") REFERENCES " + COMMON_DETAL + "(" + CD_ID + ") ON DELETE CASCADE)";

    public static final String NETWORK = "NETWORK";

    static final String N_ID = "N_ID";
    public static final String N_SHOW_ID = "N_SHOW_ID";
    public static final String N_NAME = "N_NAME";
    public static final String N_EXP_TIME = "N_EXP_TIME";
    private static final String CREATE_NETWORK =
            "CREATE TABLE " + NETWORK + "(" +
                    N_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    N_SHOW_ID + " INTEGER," +
                    N_NAME + " TEXT," +
                    N_EXP_TIME + " INTEGER," +
                    "FOREIGN KEY(" + N_SHOW_ID + ") REFERENCES " + COMMON_DETAL + "(" + CD_ID + ") ON DELETE CASCADE)";

    static final String LIST = "LIST";
    static final String L_ID = "L_ID";
    static final String L_TOTAL_ITEMS = "TOTAL_ITEMS";
    static final String L_PAGE_COUNT = "PAGE_COUNT";
    static final String L_PAGE_NUM = "PAGE_NUM";
    static final String L_SORT_BY = "SORT_BY";
    static final String L_SORT_DIR = "SORT_DIR";
    static final String L_MIN_VOTES = "MIN_VOTES";
    static final String L_MIN_RATING = "MIN_RATING";
    static final String L_START_DATE = "START_DATE";
    static final String L_END_DATE = "END_DATE";
    static final String L_GENRES = "GENRES";
    static final String L_TYPE = "TYPE";
    static final String L_EXP_TIME = "EXP_TIME";

    static final String LIST_ITEMS = "LIST_ITEMS";
    static final String LI_ID = "LI_ID";
    static final String LI_ITEM_NUM = "ITEM_NUM";
    static final String LI_LIST_ID = "LIST_ID";
    static final String LI_DETAIL_ID = "DETAIL_ID";
    static final String LI_MEDIA_ID = "MEDIA_ID";
    static final String LI_EXP_TIME = "EXP_TIME";

    CacheSql(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_GENRE = "CREATE TABLE " + GENRE + "(" +
                G_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                G_GENRE_ID + " INTEGER," +
                G_LABEL + " TEXT," +
                G_TYPE + " TEXT," +
                G_EXP_TIME + " INTEGER)";
        sqLiteDatabase.execSQL(CREATE_GENRE);

        String CREATE_LIST = "CREATE TABLE " + LIST + "(" +
                L_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                L_TOTAL_ITEMS + " INTEGER," +
                L_PAGE_COUNT + " INTEGER," +
                L_PAGE_NUM + " INTEGER," +
                L_SORT_BY + " INTEGER," +
                L_SORT_DIR + " INTEGER," +
                L_MIN_VOTES + " INTEGER," +
                L_MIN_RATING + " REAL," +
                L_START_DATE + " INTEGER," +
                L_END_DATE + " INTEGER," +
                L_GENRES + " TEXT," +
                L_TYPE + " TEXT," +
                L_EXP_TIME + " INTEGER)";
        sqLiteDatabase.execSQL(CREATE_LIST);

        String CREATE_LIST_ITEMS = "CREATE TABLE " + LIST_ITEMS + "(" +
                LI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LI_ITEM_NUM + " INTEGER," +
                LI_LIST_ID + " INTEGER," +
                LI_DETAIL_ID + " INTEGER," +
                LI_MEDIA_ID + " INTEGER," +
                LI_EXP_TIME + " INTEGER," +
                "FOREIGN KEY(" + LI_LIST_ID + ") REFERENCES " + LIST + "(" + L_ID + ") ON DELETE CASCADE," +
                "FOREIGN KEY(" + LI_DETAIL_ID + ") REFERENCES " + COMMON_DETAL + "(" + CD_ID + ") ON DELETE SET NULL)";
        sqLiteDatabase.execSQL(CREATE_LIST_ITEMS);

        String CREATE_COMMON_DETAIL = "CREATE TABLE " + COMMON_DETAL + "(" +
                CD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CD_MEDIA_ID + " INTEGER," +
                CD_TITLE + " TEXT," +
                CD_POPULARITY + " REAL," +
                CD_VOTE_COUNT + " INTEGER," +
                CD_RATING + " REAL," +
                CD_OVERVIEW + " TEXT," +
                CD_DATE + " TEXT," +
                CD_EXP_TIME + " INTEGER)";
        sqLiteDatabase.execSQL(CREATE_COMMON_DETAIL);

        String CREATE_MOVIE_DETAIL = "CREATE TABLE " + MOVIE_DETAIL + "(" +
                MD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MD_COM_ID + " INTEGER," +
                MD_BUDGET + " INTEGER," +
                MD_REVENUE + " INTEGER," +
                MD_RUNTIME + " INTEGER," +
                MD_TAGLINE + " TEXT," +
                MD_EXP_TIME + " INTEGER," +
                "FOREIGN KEY(" + MD_COM_ID + ") REFERENCES " + COMMON_DETAL + "(" + CD_ID + ") ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(CREATE_MOVIE_DETAIL);

        String CREATE_TV_DETAIL = "CREATE TABLE " + TV_DETAIL + "(" +
                TD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TD_COM_ID + " INTEGER," +
                TD_EPISODES + " INTEGER," +
                TD_SEASONS + " INTEGER," +
                TD_SHOWTYPE + " TEXT," +
                TD_EXP_TIME + " INTEGER," +
                "FOREIGN KEY(" + TD_COM_ID + ") REFERENCES " + COMMON_DETAL + "(" + CD_ID + ") ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(CREATE_TV_DETAIL);

        sqLiteDatabase.execSQL(CREATE_GENRE_DETAIL_LIST);
        sqLiteDatabase.execSQL(CREATE_PRODUCTION_COMPANY);
        sqLiteDatabase.execSQL(CREATE_NETWORK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // No Migrations yet
    }

    private static String buildListWhere(int page, int sort_by, int sort_dir, int min_votes,
                                         double min_rating, String start, String end,
                                         String genres, String type) {
        String where = L_PAGE_NUM + "=%d AND " +
                L_SORT_BY + "=%d AND " +
                L_SORT_DIR + "=%d AND " +
                L_MIN_VOTES + "=%d AND " +
                L_MIN_RATING + "=%f AND " +
                L_START_DATE + "='%s' AND " +
                L_END_DATE + "='%s' AND " +
                L_GENRES + "='%s' AND " +
                L_TYPE + "='%s'";
        return String.format(Locale.US, where, page, sort_by, sort_dir, min_votes,
                min_rating, start, end, genres, type);
    }


    static String buildListQuery(int page, int sort_by, int sort_dir, int min_votes,
                                 double min_rating, String start, String end,
                                 String genres, String type) {
        String where = buildListWhere(page, sort_by, sort_dir, min_votes,
                min_rating, start, end, genres, type);
        return  "SELECT " +  L_ID + ", " + L_PAGE_COUNT + ", " + L_PAGE_NUM + ", " +L_TOTAL_ITEMS + ", " +
                             LI_ITEM_NUM + ", " + LI_DETAIL_ID + ", " + LI_MEDIA_ID +
                " FROM " + LIST_ITEMS + " INNER JOIN " + LIST +
                " ON " + LI_LIST_ID + "=" + L_ID +
                " WHERE " + where + " ORDER BY " + LI_ITEM_NUM + " ASC";

    }


    static String buildDetailQuery(String type, long id) {

        String detailTableName = String.format("%s_DETAIL", type.toUpperCase());
        String detailId = String.format("%cD_ID", type.toUpperCase().charAt(0));

        return String.format(Locale.US,"SELECT * FROM " + COMMON_DETAL +
                        " INNER JOIN %s ON " + CD_ID + "=%s WHERE " + CD_ID + "=%d",
                detailTableName,
                detailId,
                id);
    }

}
