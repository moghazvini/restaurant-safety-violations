package ca.cmpt276.project.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import ca.cmpt276.project.R;


public class DBAdapter {

    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter_restaurants";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
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
    public static final String KEY_HAZARD = "hazard";
    public static final String KEY_NUM_CRITICAL = "num_critical";

    public static final int COL_TRACKING = 1;
    public static final int COL_NAME = 2;
    public static final int COL_ADDRESS = 3;
    public static final int COL_CITY = 4;
    public static final int COL_LATITUDE = 5;
    public static final int COL_LONGITUDE = 6;
    public static final int COL_INSPECTION_LIST = 7;
    public static final int COL_FAVOURITE = 8;
    public static final int COL_HAZARD = 9;
    public static final int COL_NUM_CRITICAL = 10;

    // ALL_KEYS_RESTAURANT
    public static final String[] ALL_KEYS = new String[] {KEY_ROWID,KEY_TRACKING, KEY_NAME, KEY_ADDRESS, KEY_CITY, KEY_LATITUDE, KEY_LONGITUDE, KEY_INSPECTION_LIST, KEY_FAVOURITE};

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "MyDb";
    public static final String TABLE_RESTAURANTS = "RestaurantsTable";
    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_RESTAURANTS =
            "CREATE TABLE " + TABLE_RESTAURANTS + " ("
                    + KEY_ROWID + " integer primary key autoincrement,"
                    + KEY_TRACKING + " text not null,"
                    + KEY_NAME + " text not null,"
                    + KEY_ADDRESS + " text not null,"
                    + KEY_CITY + " text not null,"
                    + KEY_LATITUDE + " integer not null,"
                    + KEY_LONGITUDE + " integer not null,"
                    + KEY_INSPECTION_LIST + " text,"
                    + KEY_FAVOURITE + " boolean not null,"
                    + KEY_HAZARD + " text,"
                    + KEY_NUM_CRITICAL + " integer"
            + ");";


    // Context of application who uses us.
    private final Context context;

    private final DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public void open() {
        db = myDBHelper.getWritableDatabase();
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
    public void insertRowRestaurant(String tracking, String name, String address, String city, float lat, float log, String inspections, int favourite) {
        ContentValues rowValues = new ContentValues();

        rowValues.put(KEY_TRACKING, tracking);
        rowValues.put(KEY_NAME, name);
        rowValues.put(KEY_ADDRESS, address);
        rowValues.put(KEY_CITY, city);
        rowValues.put(KEY_LATITUDE, lat);
        rowValues.put(KEY_LONGITUDE, log);
        rowValues.put(KEY_INSPECTION_LIST, inspections);
        rowValues.put(KEY_FAVOURITE, favourite);

        db.insert(TABLE_RESTAURANTS, null, rowValues);
    }

    public void deleteAll() {
        db.delete(TABLE_RESTAURANTS, null, null);
    }

    // Return all data in the database.
    // TODO: Rename to getAllRestaurants()
    public Cursor getAllRows() {
        String where = null;
        Cursor c = db.query(true, TABLE_RESTAURANTS, ALL_KEYS,
                where, null, null, null, KEY_NAME, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getRestaurantRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, TABLE_RESTAURANTS, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public enum MatchString {
        EQUALS,
        CONTAINS
    }

    // https://stackoverflow.com/questions/9076561/android-sqlitedatabase-query-with-like
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

    public Cursor filterRestaurants(String name, String hazard, int numCritical, String lessMore, boolean favFilter){
        String selection = sqlSelectionBuilder(name, hazard, numCritical, lessMore, favFilter);
        String[] selectionArgs = sqlArgsBuilder(name, hazard, numCritical, lessMore, favFilter);
        if(selectionArgs != null){
            return db.query(TABLE_RESTAURANTS, ALL_KEYS, selection, selectionArgs, null, null, KEY_NAME, null);
        }
        else {return null;}
    }

    public String sqlSelectionBuilder(String name, String hazard, int numCritical, String lessMore, boolean favFilter){
        String selection = "";
        if(name.length() > 0){
            selection = DBAdapter.KEY_NAME + " LIKE ?";
        }
        if(!hazard.equals(context.getString(R.string.off))){
            if(name.length() > 0){
                selection += " AND ";
            }
            selection = selection + DBAdapter.KEY_HAZARD + " LIKE ?";
        }
        if(numCritical >= 0 && !lessMore.equals(context.getString(R.string.off))){
            if(selection.length() > 0){
                selection += " AND ";
            }
            if(lessMore.equals(context.getString(R.string.less_than))){
                selection = selection + DBAdapter.KEY_NUM_CRITICAL + " < ?";
            } else {
                selection = selection + DBAdapter.KEY_NUM_CRITICAL + " > ?";
            }
        }
        if(favFilter){
            if(selection.length() > 0){
                selection += " AND ";
            }
            selection = selection + DBAdapter.KEY_FAVOURITE + " = ?";
            Log.d(TAG, selection);
        }
        Log.d(TAG, "selection: " + selection);
        return selection;
    }

    public String[] sqlArgsBuilder(String name, String hazard, int numCritical, String lessMore, boolean favFilter){
        ArrayList<String> selectionArgsList = new ArrayList<>();
        if(name.length() > 0){
            selectionArgsList.add("%" + name + "%");
        }
        if(!hazard.equals(context.getString(R.string.off))){
            hazard = translateHazard(hazard);
            selectionArgsList.add(hazard);
        }
        if(numCritical >= 0 && !lessMore.equals(context.getString(R.string.off))){
            String numCriticalStr = Integer.toString(numCritical);
            selectionArgsList.add(numCriticalStr);
        }
        if(favFilter){
            selectionArgsList.add("1");
        }
        int size = selectionArgsList.size();
        String [] selectionArgs;
        if(size > 0) {
            selectionArgs = new String[selectionArgsList.size()];
            for(int i = 0; i < selectionArgsList.size(); i++){
                selectionArgs[i] = selectionArgsList.get(i);
                Log.d(TAG, "ADD: " + selectionArgs[i]);
            }
        } else {
            selectionArgs = null;
        }
        return selectionArgs;
    }

    private String translateHazard(String hazardFr){
        String hazardEn = hazardFr;
        if(hazardFr.equals("FAIBLE")){
            hazardEn = "LOW";
        } else if (hazardFr.equals("MODÉRER")){
            hazardEn = "MODERATE";
        } else if (hazardFr.equals("HAUTE")){
            hazardEn = "HIGH";
        }
        return hazardEn;
    }

     public void updateRow(String key_column, String trackingID, String values) {
         String where = KEY_TRACKING + "='" + trackingID + "'";
         ContentValues newValues = new ContentValues();

         if (key_column.equals(KEY_FAVOURITE)) {
             newValues.put(key_column, Integer.parseInt(values));
         } else {
             newValues.put(key_column, values);
         }
         db.update(TABLE_RESTAURANTS, newValues, where, null);
     }

    public void updateRestaurantRow(String trackingID, String hazard, int numCritical) {
        String where = KEY_TRACKING + "='" + trackingID + "'";
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_HAZARD, hazard);
        newValues.put(KEY_NUM_CRITICAL, numCritical);
        db.update(TABLE_RESTAURANTS, newValues, where, null);
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
            //_db.execSQL(CREATE_TABLE_INSPECTIONS);
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
