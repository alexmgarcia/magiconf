package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Talk;
import tables.Workshop;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

/**
 * @author Alexandre
 * 
 * Data source to the Workshop table
 */
public class WorkshopDataSource {

	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public WorkshopDataSource(Context context) {
		dbHelper = new DbHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Inserts a new Workshop
	 * @param title title of the session
	 * @param time time of the session
	 * @param place place of the session
	 * @param description description of the session
	 * @return inserted workshop session
	 */
	public Workshop createWorkshop(Long event_id, String title, Date time, int duration,
			String place, String description) {

		ContentValues values = new ContentValues();
		values.put(DbHelper.DJANGO_ID, event_id);
		values.put(DbHelper.EVENT_TITLE, title);
		values.put(DbHelper.EVENT_PLACE, place);
		values.put(DbHelper.EVENT_TIME, time.getTime());
		values.put(DbHelper.EVENT_DURATION, duration);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
		// Insert into Events table because of inheritance
		long insertId = database.insert(DbHelper.TABLE_EVENTS, null, values);

		values = new ContentValues();
		values.put(DbHelper.DJANGO_ID, event_id);
		values.put(DbHelper.WORKSHOP_EVENT_ID, insertId);
		values.put(DbHelper.KEYSESSION_DESCRIPTION, description);
		// Insert into KeynoteSessions table
		insertId = database.insert(DbHelper.TABLE_WORKSHOPS, null, values);

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DbHelper.TABLE_WORKSHOPS + " INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_WORKSHOPS + "." + DbHelper.WORKSHOP_EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = queryBuilder.query(db, null, DbHelper.TABLE_WORKSHOPS + "." + DbHelper.COLUMN_ID + " = " + insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Workshop newWorkshop = cursorToWorkshop(cursor);
		cursor.close();
		return newWorkshop;
	}

	/**
	 * Deletes the specified workshop session from the local database
	 * @param keynoteSession workshop session to delete
	 */
	public void deleteWorkshop(Workshop workshop) {
		long eventId = workshop.getEventId();
		long id = workshop.getId();
		
		database.delete(DbHelper.TABLE_EVENTS, DbHelper.COLUMN_ID + " = "
				+ eventId, null);
		database.delete(DbHelper.TABLE_WORKSHOPS, DbHelper.COLUMN_ID + " = "
				+ id, null);
	}

	/**
	 * Returns all the workshop sessions present in the local database
	 * @return all the workshop sessions present in the local database
	 */
	public List<Workshop> getAllWorkshopSessions() {
		List<Workshop> workshopSessions = new LinkedList<Workshop>();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DbHelper.TABLE_WORKSHOPS + " INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_WORKSHOPS + "." + DbHelper.WORKSHOP_EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = queryBuilder.query(db, null, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Workshop workshop = cursorToWorkshop(cursor);
			workshopSessions.add(workshop);
			cursor.moveToNext();
		}

		cursor.close();
		return workshopSessions;
	}
	
	/**
	 * Returns the workshop session which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return workshop session which the current cursor is pointing to
	 */
	private Workshop cursorToWorkshop(Cursor cursor) {
		long eventID = cursor.getLong(1);

		Log.i("Cursor", "i ="+0 +cursor.getString(0));
		Log.i("Cursor", "i ="+1 +cursor.getString(1));
		Log.i("Cursor", "i ="+2 +cursor.getString(2));
		Log.i("Cursor", "i ="+3 +cursor.getString(3));
		Log.i("Cursor", "i ="+4 +cursor.getString(4));
		Log.i("Cursor", "i ="+5 +cursor.getString(5));
		Log.i("Cursor", "i ="+6 +cursor.getString(6));
		Log.i("Cursor", "i ="+7 +cursor.getString(7));
		Log.i("Cursor", "i ="+8 +cursor.getString(8));
		Log.i("Cursor", "i ="+9 +cursor.getString(9));
		Workshop workshop = new Workshop();
		
		workshop.setId(cursor.getLong(0));
		workshop.setEventId(eventID);
		workshop.setDescription(cursor.getString(2));
		workshop.setDjangoId(cursor.getLong(3));
		
		workshop.setTitle(cursor.getString(5));
		workshop.setDuration(cursor.getInt(8));
		workshop.setPlace(cursor.getString(7));
		
		Timestamp timestamp = new Timestamp(cursor.getLong(6));
		workshop.setTime(new Date(timestamp.getTime()));
		timestamp = new Timestamp(cursor.getLong(9));
		workshop.setLastModified(new Date(timestamp.getTime()));
				
		return workshop;
	}

	public void update(long id, long djangoID, String title, Date time, int duration, String place,String description, java.util.Date lastModified) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Cursor cursor = database.query(DbHelper.TABLE_WORKSHOPS, null,
				whereClause, null, null, null, null);
		cursor.moveToFirst();
		Long eventId = cursor.getLong(1);
		ContentValues values = new ContentValues();
		values.put(DbHelper.EVENT_TITLE, title);
		values.put(DbHelper.EVENT_TIME, time.getTime());
		values.put(DbHelper.EVENT_DURATION, duration);
		values.put(DbHelper.EVENT_PLACE, place);
		values.put(DbHelper.LAST_MODIFIED_DATE, lastModified.getTime());
		database.update(DbHelper.TABLE_EVENTS, values, DbHelper.COLUMN_ID
				+ " = " + eventId, null);
		
		
		values = new ContentValues();
		values.put(DbHelper.DJANGO_ID, djangoID);
		values.put(DbHelper.WORKSHOP_EVENT_ID, id);
		values.put(DbHelper.KEYSESSION_DESCRIPTION, description);
		
		database.update(DbHelper.TABLE_WORKSHOPS,values,whereClause ,null);
	}

	/**
	 * Returns the workshop session with the specified id
	 * @param id id of the workshop session to get
	 * @return workshop session with specified id
	 */
	public Workshop getWorkshop(Long id) {
		String whereClause = DbHelper.TABLE_WORKSHOPS + "." + DbHelper.COLUMN_ID + " = " + id;
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DbHelper.TABLE_WORKSHOPS + " INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_WORKSHOPS + "." + DbHelper.WORKSHOP_EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = queryBuilder.query(db, null, whereClause, null, null, null, null);
		
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return cursorToWorkshop(cursor);
	}
	
	/**
	 * Returns the workshop that has the specified event id as its parent
	 * @param eventID parent's event id
	 * @return workshop that has the specified event id as its parent
	 */
	public Workshop getWorkshopEvent(Long eventID) {
		String[] columns = { DbHelper.EVENT_ID, DbHelper.COLUMN_ID ,DbHelper.DJANGO_ID};
		String whereClause = DbHelper.DJANGO_ID + " = " + eventID;
		Cursor cursor = database.query(DbHelper.TABLE_WORKSHOPS, columns,
				whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return getWorkshop(cursor.getLong(1));
	}
	
	/**
	 * Checks if exists a workshop that has the specified event id as its parent
	 * @param eventID parent's event id
	 * @return true if exists a workshop that has the specified event id as its parent; false otherwise
	 */
	public boolean hasWorkshopEvent(Long eventID) {
		String[] columns = { DbHelper.EVENT_ID, DbHelper.COLUMN_ID };
		String whereClause = DbHelper.EVENT_ID + " = " + eventID;
		Cursor cursor = database.query(DbHelper.TABLE_WORKSHOPS, columns,
				whereClause, null, null, null, null);
		return cursor.moveToFirst();
	}

}
