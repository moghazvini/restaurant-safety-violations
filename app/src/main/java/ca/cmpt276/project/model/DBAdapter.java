package ca.cmpt276.project.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBAdapter {

    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter_restaurants";

    // DB Fields

    //COMMON TABLE COLUMN
    public static final String KEY_TRACKING = "track";

    // RESTAURANTS TABLE COLUMNS
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_CITY = "city";
    public static final String KEY_LATITUDE = "lat";
    public static final String KEY_LONGITUDE = "long";
    public static final String KEY_INSPECTION_LIST = "inspections";
    public static final String KEY_FAVOURITE = "favourite";

    // INSPECTIONS TABLE COLUMNS
    public static final String KEY_ROWID = "_id"; // KEY_INSPECTION_ID
    public static final String KEY_DATE = "date";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NUM_CRITICAL = "num_critical";
    public static final String KEY_NUM_NON_CRITICAL = "num_non_critical";
    public static final String KEY_VIOLUMP = "violump";
    public static final String KEY_HAZARD = "hazard";

    public static final int COL_TRACKING = 0;
    public static final int COL_NAME = 1;
    public static final int COL_ADDRESS = 2;
    public static final int COL_CITY = 3;
    public static final int COL_LATITUDE = 4;
    public static final int COL_LONGITUDE = 5;
    public static final int COL_INSPECTION_LIST = 6;
    public static final int COL_FAVOURITE = 7;

    // ALL_KEYS_RESTAURANT
    public static final String[] ALL_KEYS = new String[] {KEY_TRACKING, KEY_NAME, KEY_ADDRESS, KEY_CITY, KEY_LATITUDE, KEY_LONGITUDE, KEY_INSPECTION_LIST, KEY_FAVOURITE};

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "MyDb";
    public static final String TABLE_RESTAURANTS = "RestaurantsTable";
    public static final String TABLE_INSPECTIONS = "InspectionsTable";
    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_RESTAURANTS =
            "CREATE TABLE " + TABLE_RESTAURANTS + " ("
                    + KEY_TRACKING + " text primary key,"
                    + KEY_NAME + " text not null,"
                    + KEY_ADDRESS + " text not null,"
                    + KEY_CITY + " text not null,"
                    + KEY_LATITUDE + " integer not null,"
                    + KEY_LONGITUDE + " integer not null,"
                    + KEY_INSPECTION_LIST + " text,"
                    + KEY_FAVOURITE + " boolean not null"
            + ");";

    private static final String CREATE_TABLE_INSPECTIONS =
            "CREATE TABLE " + TABLE_INSPECTIONS + " ("
                    + KEY_ROWID + " integer primary key autoincrement, "
                    + KEY_TRACKING + " text not null,"
                    + KEY_DATE + " text not null,"
                    + KEY_TYPE + " text not null,"
                    + KEY_NUM_CRITICAL + " integer not null,"
                    + KEY_NUM_NON_CRITICAL + " integer not null,"
                    + KEY_VIOLUMP + " text,"
                    + KEY_HAZARD + " text not null"
            + ");";

    // Context of application who uses us.
    private final Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    public void beginTransaction() {
        db.beginTransaction();
    }

    public void endTransactionSuccessful() {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    // Add a new set of values to the database.
    public long insertRowRestaurant(String tracking, String name, String address, String city, float lat, float log, String inspections, int favourite) {
        ContentValues rowValues = new ContentValues();

        rowValues.put(KEY_TRACKING, tracking);
        rowValues.put(KEY_NAME, name);
        rowValues.put(KEY_ADDRESS, address);
        rowValues.put(KEY_CITY, city);
        rowValues.put(KEY_LATITUDE, lat);
        rowValues.put(KEY_LONGITUDE, log);
        rowValues.put(KEY_INSPECTION_LIST, inspections);
        rowValues.put(KEY_FAVOURITE, favourite);

        return db.insert(TABLE_RESTAURANTS, null, rowValues);
    }

    public long insertRowInspection(String tracking, String date, String type, int numCritical, int numNonCritical, String violump, String hazard) {
        ContentValues rowValues = new ContentValues();

        rowValues.put(KEY_TRACKING, tracking);
        rowValues.put(KEY_DATE, date);
        rowValues.put(KEY_TYPE, type);
        rowValues.put(KEY_NUM_CRITICAL, numCritical);
        rowValues.put(KEY_NUM_NON_CRITICAL, numNonCritical);
        rowValues.put(KEY_VIOLUMP, violump);
        rowValues.put(KEY_HAZARD, hazard);

        return db.insert(TABLE_INSPECTIONS, null, rowValues);
    }

    public void deleteAll() {
        db.delete(TABLE_INSPECTIONS, null, null);
        db.delete(TABLE_RESTAURANTS, null, null);
    }

    // Return all data in the database.
    // TODO: Rename to getAllRestaurants()
    public Cursor getAllRows() {
        String where = null;
        Cursor c = db.query(true, TABLE_RESTAURANTS, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // selection = "
    // WHERE tracking IN (
    //     SELECT i.tracking
    //     FROM inspections i
    //     JOIN 
    //     (
    //         SELECT tracking, MAX(date) as maxDate
    //         FROM inspections
    //         GROUP BY tracking

    //     ) li ON li.tracking = i.tracking AND li.maxDate = i.date
    //     WHERE i.hazard > ?
    // )
    // ";

    public enum MatchString {
        EQUALS,
        CONTAINS
    }

    public enum MatchInteger {
        LESS,
        EQUALS,
        GREATER
    }

    // https://stackoverflow.com/questions/9076561/android-sqlitedatabase-query-with-like
    // TODO: Rename to searchForRestaurants
    // retrieveByConstraint
    public Cursor searchRestaurants(String column, String searchTerm, MatchString matchType) {
        String selection;
        String[] selectionArgs;

        if(matchType == MatchString.EQUALS) {
            selection = column + "= ?";
            selectionArgs = new String[] {searchTerm};
        } else {
            selection = column + " LIKE ?";
            selectionArgs = new String[] {"%" + searchTerm + "%"};
        }

        return db.query(TABLE_RESTAURANTS, ALL_KEYS, selection, selectionArgs, null, null, null, null);
    }

    public Cursor searchRestaurants(String column, int searchTerm, MatchInteger matchType) {
        String operation = "=";
        switch(matchType) {
            case LESS:
                operation = "<";
                break;
            case GREATER:
                operation = ">";
                break;
        }

        String selection = column + " " + operation + " ?";
        String[] selectionArgs = new String[] {"" + searchTerm};

        return db.query(TABLE_RESTAURANTS, ALL_KEYS, selection, selectionArgs, null, null, null, null);
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(String tracking, String name, String address, String city, float lat, float log, String inspections) {
        String where = KEY_TRACKING + " = ?";
        String[] whereArgs = new String[] {tracking};

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_TRACKING, tracking);
        newValues.put(KEY_NAME, name);
        newValues.put(KEY_ADDRESS, address);
        newValues.put(KEY_CITY, city);
        newValues.put(KEY_LATITUDE, lat);
        newValues.put(KEY_LONGITUDE, log);
        newValues.put(KEY_INSPECTION_LIST, inspections);

        return db.update(TABLE_RESTAURANTS, newValues, where, whereArgs) != 0;
    }

     public boolean updateRow(String key_column, String trackingID, String values) {
         String where = KEY_TRACKING + "='" + trackingID + "'";
         ContentValues newValues = new ContentValues();

         if (key_column.equals(KEY_FAVOURITE)) {
             newValues.put(key_column, Integer.parseInt(values));
         } else {
             newValues.put(key_column, values);
         }
         return db.update(TABLE_RESTAURANTS, newValues, where, null) != 0;
     }

    /////////////////////////////////////////////////////////////////////
    //	Private Helper Classes:
    /////////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(CREATE_TABLE_RESTAURANTS);
            _db.execSQL(CREATE_TABLE_INSPECTIONS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESTAURANTS);

            // Recreate new database:
            onCreate(_db);
        }
    }
}
