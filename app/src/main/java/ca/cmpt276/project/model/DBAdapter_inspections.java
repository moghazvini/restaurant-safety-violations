package ca.cmpt276.project.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import ca.cmpt276.project.model.types.HazardLevel;


public class DBAdapter_inspections {

    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter_inspections";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
    /*
     * CHANGE 1:
     */
    // TODO: Setup your fields here:
    public static final String KEY_TRACKING = "track";
    public static final String KEY_DATE = "date";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NUM_CRITICAL = "num_critical";
    public static final String KEY_NUM_NON_CRITICAL = "num_non_critical";
    public static final String KEY_VIOLUMP = "violump";
    public static final String KEY_HAZARD = "hazard";

    // TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
    public static final int COL_TRACKING = 1;
    public static final int COL_DATE = 2;
    public static final int COL_TYPE = 3;
    public static final int COL_NUM_CRITICAL = 4;
    public static final int COL_NUM_NON_CRITICAL = 5;
    public static final int COL_VIOLUMP = 6;
    public static final int COL_HAZARD = 7;

    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TRACKING, KEY_DATE, KEY_TYPE, KEY_NUM_CRITICAL, KEY_NUM_NON_CRITICAL, KEY_VIOLUMP, KEY_HAZARD};

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "MyDb";
    public static final String DATABASE_TABLE = "InspectionTable";
    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, " + KEY_TRACKING + " text not null,"
                    + KEY_DATE + " text not null," + KEY_TYPE + " text not null," + KEY_NUM_CRITICAL + " integer not null,"
                    + KEY_NUM_NON_CRITICAL + " integer not null," + KEY_VIOLUMP + " text," + KEY_HAZARD + " text not null" + ");";

    // Context of application who uses us.
    private final Context context;

    private ca.cmpt276.project.model.DBAdapter_inspections.DatabaseHelper myDBHelper;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public DBAdapter_inspections(Context ctx) {
        this.context = ctx;
        myDBHelper = new ca.cmpt276.project.model.DBAdapter_inspections.DatabaseHelper(context);
    }

    // Open the database connection.
    public ca.cmpt276.project.model.DBAdapter_inspections open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRow(String tracking, String date, String type, int numCritical, int numNonCritical, String violump, String hazard) {

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
        Log.d(TAG, "added a row");
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
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
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }


    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String tracking, String date, String type, int numCritical, int numNonCritical, String violump, String hazard) {
        String where = KEY_ROWID + "=" + rowId;

        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_TRACKING, tracking);
        newValues.put(KEY_DATE, date);
        newValues.put(KEY_TYPE, type);
        newValues.put(KEY_NUM_CRITICAL, numCritical);
        newValues.put(KEY_NUM_NON_CRITICAL, numNonCritical);
        newValues.put(KEY_VIOLUMP, violump);
        newValues.put(KEY_HAZARD, hazard);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
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
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }
}

