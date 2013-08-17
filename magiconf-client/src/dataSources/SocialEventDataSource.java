package dataSources;


import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import tables.Event;
import tables.SocialEvent;


	import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.magiconf_client.DbHelper;

	public class SocialEventDataSource {
		private SQLiteDatabase database;
		private DbHelper dbHelper;
		
		private String[] allColumns = { 
				DbHelper.COLUMN_ID, 
				DbHelper.SOCIALEVENTS_TITLE,
				DbHelper.SOCIALEVENTS_TIME 
				};
		
		public SocialEventDataSource(Context context){
			dbHelper = new DbHelper(context);
		}
		
		public void open() throws SQLException{
			database = dbHelper.getWritableDatabase();
		}

		public void close(){
			dbHelper.close();
		}
		
		
		public SocialEvent createSocialEvent(String title, Date time, String place){
			ContentValues values = new ContentValues();
			
			values.put(DbHelper.SOCIALEVENTS_TITLE , title);
			values.put(DbHelper.EVENT_PLACE, place);
			values.put(DbHelper.SOCIALEVENTS_TIME, time.getTime());
			values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
			
			long insertId = database.insert(DbHelper.TABLE_EVENTS, null, values);
			
			
			values = new ContentValues();
			values.put("event_id", insertId);
			insertId = database.insert(DbHelper.TABLE_SOCIALEVENTS, null, values);
			
			Cursor cursor = database.query(DbHelper.TABLE_SOCIALEVENTS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
			
			cursor.moveToFirst();
			SocialEvent newSocialEvent = cursorToSocialEvent(cursor);
			cursor.close();
			return newSocialEvent;
		}
		
		public void deleteSocialEvent(SocialEvent socialEvent){
			long id =  socialEvent.getId();
			String column[] = { DbHelper.SOCIALEVENTS_EVENT_ID };
			String whereClause = DbHelper.COLUMN_ID + " = " + id;
			Cursor cursor = database.query(DbHelper.TABLE_SOCIALEVENTS, column, whereClause, null, null, null, null);
			cursor.moveToFirst();
			Long event_id = cursor.getLong(1);
			whereClause = DbHelper.COLUMN_ID + " = " + event_id;
			database.delete(DbHelper.TABLE_EVENTS, whereClause, null);
			whereClause = DbHelper.COLUMN_ID + " = " + id;
			database.delete(DbHelper.TABLE_SOCIALEVENTS, whereClause, null);
			
		}
		
		public SocialEvent getSocialEvent(Long id) {
			String whereClause = DbHelper.COLUMN_ID + " = " + id;
			Log.i("Database", ""+database);
			Cursor cursor = database.query(DbHelper.TABLE_SOCIALEVENTS, null, whereClause, null, null, null, null);
			boolean hasData = cursor.moveToFirst();
			Log.i("Cursor", ""+hasData);
			if (!hasData)
				return null;
			return cursorToSocialEvent(cursor);
		}
		
		public void update(Long id, String title, Date time, String place, Date lastModifiedDate) {
			String[] column = { 
					DbHelper.SOCIALEVENTS_EVENT_ID
					};
			String whereClause = DbHelper.COLUMN_ID + " = " + id;
			Log.i("Database socialevent", ""+database);
			Cursor cursor = database.query(DbHelper.TABLE_SOCIALEVENTS, column, whereClause, null, null, null, null);
			cursor.moveToFirst();
			Long eventID = cursor.getLong(0);
			ContentValues values = new ContentValues();
			values.put(DbHelper.EVENT_TITLE, title);
			values.put(DbHelper.EVENT_TIME, time.getTime());
			values.put(DbHelper.EVENT_PLACE, place);
			values.put(DbHelper.LAST_MODIFIED_DATE, lastModifiedDate.getTime());
			database.update(DbHelper.TABLE_EVENTS, values, DbHelper.COLUMN_ID + " = " + eventID, null);
		}
		
		public List<SocialEvent> getAllSocialEvents(){
			List<SocialEvent> socialEvents = new ArrayList<SocialEvent>();
			Cursor cursor = database.query(DbHelper.TABLE_SOCIALEVENTS, allColumns, null, null, null, null, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				SocialEvent socialEvent = cursorToSocialEvent(cursor);
				socialEvents.add(socialEvent);
				cursor.moveToNext();
			}
			
			cursor.close();
			return socialEvents;
		
		}
		
		private SocialEvent cursorToSocialEvent(Cursor cursor){
			
			long eventID = cursor.getLong(1);
			Cursor c = database.query(DbHelper.TABLE_EVENTS, null, DbHelper.COLUMN_ID + " = " + eventID, null, null, null, null);
			c.moveToFirst();
			
			SocialEvent sEvent = new SocialEvent();
			
			sEvent.setId(cursor.getLong(0));
			sEvent.setTitle(c.getString(1));
			sEvent.setPlace(c.getString(3));
			sEvent.setInAgenda(c.getInt(5) == 0 ? false : true);
			
			Timestamp timestamp = new Timestamp(c.getLong(2));
			sEvent.setTime(new Date(timestamp.getTime()));
			timestamp = new Timestamp(c.getLong(4));
			sEvent.setLastModified(new Date(timestamp.getTime()));
			
			return sEvent;
		}
		
	}



