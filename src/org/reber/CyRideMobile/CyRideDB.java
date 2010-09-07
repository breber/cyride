package org.reber.CyRideMobile;


import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A Database Wrapper for CyRide Scheduling
 * 
 * @author brianreber
 */
public class CyRideDB 
{
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ROUTEID = "routeid";
	public static final String KEY_ROUTENAME = "routename";
	public static final String KEY_STATIONNAME = "stationname";
	public static final String KEY_STATIONID = "stationid";
	public static final String KEY_TIMESTRING = "timestring";
	public static final String KEY_TIME = "time";
	public static final String KEY_DAY = "day";
	public static final String KEY_ROWNUM = "rownum";
	private static final String TAG = "CyRideDB";

	private static final String DATABASE_NAME = "cyride.db";
	private static final String DATABASE_TABLE = "cyride";
	private static final int DATABASE_VERSION = 1;

	private int dayOfWeek = 0;

	private static final String DATABASE_CREATE =
		"CREATE TABLE " + DATABASE_TABLE + "(" + KEY_ROWID + " INTEGER PRIMARY KEY autoincrement, " + KEY_ROUTEID + " INTEGER, " 
		+ KEY_ROUTENAME + " TEXT, " + KEY_STATIONNAME + " TEXT, " + KEY_STATIONID + " INTEGER, " + KEY_TIMESTRING + " TEXT, " 
		+ KEY_TIME + " INTEGER, " + KEY_DAY + " INTEGER, " + KEY_ROWNUM + " INTEGER);";

	private final Context context; 

	private DatabaseHelper dbHelper;

	public CyRideDB(Context ctx) {
		this.context = ctx;
		open();
	}

	public void open() {
		dbHelper = new DatabaseHelper(context);
	}

	public void close() {
		dbHelper.close();
	}

	public void setDayOfWeek(int dOW) {
		this.dayOfWeek = dOW;
	}

	public int getDayOfWeek() {
		return this.dayOfWeek;
	}

	public void insertRoute(int routeId, String routeName, String station, int stationId, String timeString,
			int time, int dayOfWeek, int rowNum) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_ROUTEID, routeId);
			initialValues.put(KEY_ROUTENAME, routeName);
			initialValues.put(KEY_STATIONNAME, station);
			initialValues.put(KEY_STATIONID, stationId);
			initialValues.put(KEY_TIMESTRING, timeString);
			initialValues.put(KEY_TIME, time);
			initialValues.put(KEY_DAY, dayOfWeek);
			initialValues.put(KEY_ROWNUM, rowNum);
			db.insert(DATABASE_TABLE, null, initialValues);
		} finally {
			db.close();
		}
	}

	public Cursor getAllRoutes() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		try {
			String query = "SELECT * FROM " + DATABASE_TABLE;
			c = db.rawQuery(query, null);
		} finally {
			db.close();
		}

		return c;
	}

	public void deleteAllRoutes() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL("DROP TABLE " + DATABASE_TABLE);
		} finally {
			db.close();
		}
	}

	public int getCountRoute() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		int temp;
		try {
			String subquery = "SELECT " + KEY_STATIONNAME + " FROM " + DATABASE_TABLE;
			Log.d("QUERY", subquery);
			c = db.rawQuery(subquery, null);
			temp = c.getCount();
		} finally {
			c.close();
			db.close();
		}
		return temp;
	}
	
	public String getNameOfRouteById(int id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = null;
		String name = "";
		try {
			String subquery = "SELECT DISTINCT " + KEY_ROUTENAME + " FROM " + DATABASE_TABLE + " WHERE " + 
			KEY_DAY + " = " + dayOfWeek + " AND " + KEY_ROUTEID + " = " + id;
			Log.d("QUERY", subquery);
			c = db.rawQuery(subquery, null);
			c.moveToFirst();
			if (c.getCount() > 1)
				name = c.getString(c.getColumnIndex(KEY_ROUTENAME));
		} finally {
			c.close();
			db.close();
		}
		return name;
	}

	public List<NameIdWrapper> getRouteNames() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Log.d("DB",db.getPath());
		List<NameIdWrapper> list = new ArrayList<NameIdWrapper>();
		Cursor c = null;
		try {
			String subquery = "SELECT DISTINCT " + KEY_ROUTENAME + ", " + KEY_ROUTEID + " FROM " + DATABASE_TABLE + " WHERE " + KEY_DAY + " = " + dayOfWeek
			 + " ORDER BY " + KEY_ROUTEID;
			Log.d("QUERY", subquery);
			c = db.rawQuery(subquery, null);
			Log.d("SIZE", c.getCount()+"");
			if (c.getCount() > 1) {
				do {
					c.moveToNext();
					list.add(new NameIdWrapper(c.getString(c.getColumnIndex(KEY_ROUTENAME)).replaceAll("&amp;", "&"),
							c.getInt(c.getColumnIndex(KEY_ROUTEID))));
				} while (!c.isLast());
			}
		} finally {
			if (c != null)
				c.close();
			db.close();
		}

		return list;
	}

	public List<NameIdWrapper> getStationNamesForRoute(int routeId) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<NameIdWrapper> list = new ArrayList<NameIdWrapper>();
		Cursor c = null;
		try {
			String subquery = "SELECT DISTINCT " + KEY_STATIONNAME + ", " + KEY_STATIONID + " FROM " + DATABASE_TABLE + 
			" WHERE " + KEY_DAY + " = " + dayOfWeek + " AND " + KEY_ROUTEID + " = " + routeId + " ORDER BY " + KEY_STATIONID;
			Log.d("QUERY", subquery);
			c = db.rawQuery(subquery, null);
			if (c.getCount() > 1) {
				do {
					c.moveToNext();
					list.add(new NameIdWrapper(c.getString(c.getColumnIndex(KEY_STATIONNAME)).replaceAll("&amp;", "&"),
							c.getInt(c.getColumnIndex(KEY_STATIONID))));
				} while (!c.isLast());
			}
		} finally { 
			if (c != null)
				c.close();
			db.close();
		}

		return list;
	}

	public List<String> getTimesForRouteAndStation(int routeId, int stationId) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		List<String> list = new ArrayList<String>();
		Cursor c = null;
		try {
			String subquery = "SELECT " + KEY_TIMESTRING + " FROM " + DATABASE_TABLE + " WHERE " + KEY_DAY + " = " + dayOfWeek + 
			" AND " + KEY_ROUTEID + " = " + routeId + " AND " + KEY_STATIONID + " = " + stationId + " ORDER BY " + KEY_TIME;
			Log.d("QUERY", subquery);
			c = db.rawQuery(subquery, null);
			if (c.getCount() > 1) {
				do {
					c.moveToNext();
					list.add(c.getString(c.getColumnIndex(KEY_TIMESTRING)));
				} while (!c.isLast());
			}
		} finally {
			if (c != null)
				c.close();
			db.close();
		}

		return list;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper 
	{
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}    
}