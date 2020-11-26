package ca.cmpt276.project.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.widget.Toast;


public class DBAdapter_restaurants{

    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter_restaurants";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
    /*
     * CHANGE 1:
     */
    // TODO: Setup your fields here:
    //COMMON TABLE COLUMN
    public static final String KEY_TRACKING = "track";

    // RESTAURANTS TABLE COLUMNS
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_CITY = "city";
    public static final String KEY_LATITUDE = "lat";
    public static final String KEY_LONGITUDE = "long";
    public static final String KEY_INSPECTION_LIST = "inspections";

    // INSPECTIONS TABLE COLUMNS
    public static final String KEY_DATE = "date";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NUM_CRITICAL = "num_critical";
    public static final String KEY_NUM_NON_CRITICAL = "num_non_critical";
    public static final String KEY_VIOLUMP = "violump";
    public static final String KEY_HAZARD = "hazard";

    // TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
    public static final int COL_TRACKING = 1;
    public static final int COL_NAME = 2;
    public static final int COL_ADDRESS = 3;
    public static final int COL_CITY = 4;
    public static final int COL_LATITUDE = 5;
    public static final int COL_LONGITUDE = 6;
    public static final int COL_INSPECTION_LIST = 7;

    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TRACKING, KEY_NAME, KEY_ADDRESS, KEY_CITY, KEY_LATITUDE, KEY_LONGITUDE, KEY_INSPECTION_LIST};

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "MyDb";
    public static final String DATABASE_TABLE_1 = "RestaurantsTable";
    public static final String DATABASE_TABLE_2 = "InspectionsTable";
    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_RESTAURANTS =
            "create table " + DATABASE_TABLE_1
                    + " (" + KEY_ROWID + " integer primary key autoincrement, " + KEY_TRACKING + " text not null,"
                    + KEY_NAME + " text not null," + KEY_ADDRESS + " text not null," + KEY_CITY + " text not null,"
                    + KEY_LATITUDE + " integer not null," + KEY_LONGITUDE + " integer not null," + KEY_INSPECTION_LIST + " text" + ");";

    private static final String CREATE_TABLE_INSPECTIONS =
            "create table " + DATABASE_TABLE_2
                    + " (" + KEY_ROWID + " integer primary key autoincrement, " + KEY_TRACKING + " text not null,"
                    + KEY_DATE + " text not null," + KEY_TYPE + " text not null," + KEY_NUM_CRITICAL + " integer not null,"
                    + KEY_NUM_NON_CRITICAL + " integer not null," + KEY_VIOLUMP + " text," + KEY_HAZARD + " text not null" + ");";

    // Context of application who uses us.
    private final Context context;

    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public DBAdapter_restaurants(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBAdapter_restaurants open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRowRestaurant(String tracking, String name, String address, String city, float lat, float log, String inspections) {

        SQLiteDatabase database = myDBHelper.getWritableDatabase();
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TRACKING, tracking);
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_ADDRESS, address);
        initialValues.put(KEY_CITY, city);
        initialValues.put(KEY_LATITUDE, lat);
        initialValues.put(KEY_LONGITUDE, log);
        initialValues.put(KEY_INSPECTION_LIST, inspections);
        // Insert it into the database.
        Log.d(TAG, "added a row to restaurants " + "[" + tracking + "]");
        return database.insert(DATABASE_TABLE_1, null, initialValues);
    }

    public long insertRowInspection(String tracking, String date, String type, int numCritical, int numNonCritical, String violump, String hazard) {
        SQLiteDatabase database = myDBHelper.getWritableDatabase();
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TRACKING, tracking);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_NUM_CRITICAL, numCritical);
        initialValues.put(KEY_NUM_NON_CRITICAL, numNonCritical);
        initialValues.put(KEY_VIOLUMP, violump);
        initialValues.put(KEY_HAZARD, hazard);

        // Insert it into the database.
        Log.d(TAG, "added a row to inspections");
        return database.insert(DATABASE_TABLE_2, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE_1, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE_1, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // sql search on android studio from https://www.youtube.com/watch?v=mqqzt-Yvtbo
    public Cursor getRelevantRowsEqual(String searchTerm){
        Cursor c = null;
        if(searchTerm != null && searchTerm.length() > 0){
            String sql = "SELECT * FROM " + DATABASE_TABLE_1 + " WHERE " + KEY_NAME + " = '%" + searchTerm + "%'";
            c = db.rawQuery(sql, null);
        }
        return c;
    }

    public Cursor retrieveByInspection(){
        Cursor c = null;
        return c;
    }

    // https://stackoverflow.com/questions/9076561/android-sqlitedatabase-query-with-like
    public Cursor retrieveByConstraint(String filter, boolean trackingSearch){
        Cursor c = null;
        String[] selectionArgs = new String[] {"%" + filter + "%"};
        String [] column = null;
        String selection = KEY_NAME + " LIKE ?";
        if(trackingSearch){
            selection  = KEY_TRACKING + " LIKE ?";
            Log.d(TAG, "looking for: " + selection);
        }
        c = db.query(DATABASE_TABLE_1, ALL_KEYS, selection, selectionArgs, null, null, null, null);

        return c;
    }


    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE_1, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String tracking, String name, String address, String city, float lat, float log, String inspections) {
        String where = KEY_ROWID + "=" + rowId;

        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_TRACKING, tracking);
        newValues.put(KEY_NAME, name);
        newValues.put(KEY_ADDRESS, address);
        newValues.put(KEY_CITY, city);
        newValues.put(KEY_LATITUDE, lat);
        newValues.put(KEY_LONGITUDE, log);
        newValues.put(KEY_INSPECTION_LIST, inspections);
        // Insert it into the database.
        return db.update(DATABASE_TABLE_1, newValues, where, null) != 0;
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
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_1);

            // Recreate new database:
            onCreate(_db);
        }
    }
}
