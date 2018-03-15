package com.eriksanne.edinburghbus.EdinburghBus.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.eriksanne.edinburghbus.EdinburghBus.BusStop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Erik on 11/03/2018.
 */

public final class BusStopDatabase extends SQLiteOpenHelper {

        public static final String DB_NAME = "BusStop.db";
        public static final String TABLE_NAME = "BusStops";
        public static final String COLUMN_NAME_STOPID = "stopId";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_CAP = "cap";

        private static BusStopDatabase instance = null;

        public static BusStopDatabase getInstance(final Context context){

            if(instance == null){
                instance = new BusStopDatabase(context);
            }

            return instance;
        }

        private BusStopDatabase(Context context) {
            super(context, DB_NAME, null, 1);
        }

    /**
     * Method for database creation.
     * @param db
     */
    @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                "CREATE TABLE " + TABLE_NAME + " (" +
                 COLUMN_NAME_STOPID + "  TEXT, " +
                 COLUMN_NAME_NAME + "  TEXT, " +
                 COLUMN_NAME_LATITUDE + "  TEXT, " +
                 COLUMN_NAME_LONGITUDE + " TEXT, " +
                 COLUMN_NAME_CAP + " INTEGER)"
            );
        }

        /**
        * onUpgrade the databse should be reset completely .
        * @param db
        * @param oldVersion
        * @param newVersion
        */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        }

        /**
        * Method to reset the DB
        */
        public void resetDB() {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

        /**
        * Method to insert a new BusStop into the database.
        * @param stopId
        * @param name
        * @param latitude
        * @param longitude
        * @param cap
        */
        public void insertBusStop (String stopId, String name, String latitude, String longitude, int cap){

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("stopId", stopId);
            contentValues.put("name", name);
            contentValues.put("latitude", latitude);
            contentValues.put("longitude", longitude);
            contentValues.put("cap", cap);

            db.insert("BusStops", null, contentValues);
            db.close();
        }

        /**
        * Method that gets and parses all the BusStops from the database and returns them as an ArrayList.
        * @return
        */
        public ArrayList<BusStop> getAllStops(){

                ArrayList<BusStop> stopList = new ArrayList<BusStop>();

                SQLiteDatabase db = this.getWritableDatabase();
                String selectQuery = "SELECT * FROM " + TABLE_NAME;
                Cursor  cursor = db.rawQuery(selectQuery, null);


                if(cursor.moveToFirst()) {
                    do{
                        BusStop stop = new BusStop(cursor.getString(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getInt(4)
                        );

                        stopList.add(stop);

                    }while(cursor.moveToNext());
                }

                cursor.close();
                return stopList;

            }

    /**
     * Method to check if the Database is empty.
     * @return
     */
    public boolean isEmpty(){

            String DB_FULL_PATH = "//data/data/com.eriksanne.edinburghbus/databases/BusStop.db";

            SQLiteDatabase checkDB = null;
            try {
                checkDB = SQLiteDatabase.openDatabase(DB_FULL_PATH, null,
                        SQLiteDatabase.OPEN_READONLY);
                checkDB.close();
            } catch (SQLiteException e) {
                return true;
            }
            return false;
        }



}
