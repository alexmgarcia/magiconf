package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Keynote;
import tables.KeynoteSession;
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
 * Data source to the KeynoteSession table
 */
public class KeynoteSessionDataSource {

	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public KeynoteSessionDataSource(Context context) {
		dbHelper = new DbHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Inserts a new KeynoteSession
	 * @param title title of the session
	 * @param time time of the session
	 * @param place place of the session
	 * @param description description of the session
	 * @param keynoteSpeakers keynote speaker present in this session
	 * @return inserted keynote session
	 */
	public KeynoteSession createKeynoteSession(Long event_id,String title, Date time, int duration,
			String place, String description, List<Keynote> keynoteSpeakers) {

		ContentValues values = new ContentValues();

		values.put(DbHelper.DJANGO_ID,event_id);
		values.put(DbHelper.EVENT_TITLE, title);
		values.put(DbHelper.EVENT_PLACE, place);
		values.put(DbHelper.EVENT_TIME, time.getTime());
		values.put(DbHelper.EVENT_DURATION, duration);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
		// Insert into Events table because of inheritance
		long insertId = database.insert(DbHelper.TABLE_EVENTS, null, values);

		values = new ContentValues();
		values.put("event_id", insertId);
		values.put(DbHelper.DJANGO_ID,event_id);
		values.put(DbHelper.KEYSESSION_DESCRIPTION, description);
		// Insert into KeynoteSessions table
		insertId = database.insert(DbHelper.TABLE_KEYSESSIONS, null, values);
		long keyNoteId;
		Iterator<Keynote> it = keynoteSpeakers.iterator();
		Keynote next;
		while (it.hasNext()) {
			next = it.next();
			values = new ContentValues();
			values.put(DbHelper.KEYNOTE_NAME, next.getName());
			Log.i("Inserting", next.getName());
			values.put(DbHelper.KEYNOTE_EMAIL, next.getEmail());
			values.put(DbHelper.KEYNOTE_COUNTRY, next.getCountry());
			values.put(DbHelper.KEYNOTE_PHOTO, next.getPhoto());
			values.put(DbHelper.KEYNOTE_WORKPLACE, next.getWork_place());
			
			Keynote k = getKeynote(next.getEmail());
			if( k == null){
				// Insert the keynote speaker into the Keynotes table
				keyNoteId = database.insert(DbHelper.TABLE_KEYNOTES, null, values);
			}
			else
				keyNoteId = k.getId();
			
			
			values = new ContentValues();
			values.put(DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTE_ID, keyNoteId);
			values.put(DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID, insertId);
			// Insert the relationship between the keynote speaker and the keynote session into keynotes_KeynoteSessions table
			database.insert(DbHelper.TABLE_RELATIONSHIP_KEYNOTES_KEYSESSIONS, null, values);
		}

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DbHelper.TABLE_KEYSESSIONS + " INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_KEYSESSIONS + "." + DbHelper.KEYSESSION_EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = queryBuilder.query(db, null, DbHelper.TABLE_KEYSESSIONS + "." + DbHelper.COLUMN_ID + " = " + insertId, null, null, null, null);

		cursor.moveToFirst();
		KeynoteSession newKeynoteSession = cursorToKeynoteSession(cursor);
		cursor.close();
		return newKeynoteSession;
	}
	
	public Keynote getKeynote(String email) {
		Cursor cursor = database.query(DbHelper.TABLE_KEYNOTES, null, DbHelper.KEYNOTE_EMAIL + " = \"" + email +"\"", null, null, null, null);
		cursor.moveToFirst();
		
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return cursorToKeynote(cursor);
	}
	
	private Keynote cursorToKeynote(Cursor cursor){
		Keynote keynote = new Keynote();
		keynote.setId(cursor.getLong(0));
		keynote.setEmail(cursor.getString(1));
		keynote.setName(cursor.getString(2));
		keynote.setWork_place(cursor.getString(3));
		keynote.setCountry(cursor.getString(4));
		keynote.setPhoto(cursor.getString(5));
		
		return keynote;
	}

	/**
	 * Deletes the specified keynote session from the local database
	 * @param keynoteSession keynote session to delete
	 */
	public void deleteKeynoteSession(KeynoteSession keynoteSession) {
		long eventId = keynoteSession.getEventId();
		long id = keynoteSession.getId();

		database.delete(DbHelper.TABLE_EVENTS, DbHelper.COLUMN_ID + " = "
				+ eventId, null);
		database.delete(DbHelper.TABLE_KEYSESSIONS, DbHelper.COLUMN_ID + " = "
				+ id, null);
	}

	/**
	 * Returns all the keynote sessions present in the local database
	 * @return all the keynote sessions present in the local database
	 */
	public List<KeynoteSession> getAllKeynotesSessions() {
		List<KeynoteSession> keynotesSessions = new LinkedList<KeynoteSession>();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DbHelper.TABLE_KEYSESSIONS + " INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_KEYSESSIONS + "." + DbHelper.KEYSESSION_EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = queryBuilder.query(db, null, null, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			KeynoteSession keynoteSession = cursorToKeynoteSession(cursor);
			keynotesSessions.add(keynoteSession);
			cursor.moveToNext();
		}

		cursor.close();
		return keynotesSessions;
	}

	/**
	 * Returns the keynote speaker which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return keynote speaker which the current cursor is pointing to
	 */
	private Keynote cursorToKeynoteSpeaker(Cursor cursor) {
		Cursor c = database.query(DbHelper.TABLE_KEYNOTES, null, DbHelper.COLUMN_ID + " = " + cursor.getLong(0), null, null, null, null);
		c.moveToFirst();
		Keynote keynote = new Keynote();
		keynote.setId(c.getLong(0));
		keynote.setEmail(c.getString(1));
		keynote.setName(c.getString(2));
		keynote.setWork_place(c.getString(3));
		keynote.setCountry(c.getString(4));
		keynote.setPhoto(c.getString(5));

		return keynote;
	}

	/**
	 * Returns the keynote session which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return keynote session which the current cursor is pointing to
	 */
	private KeynoteSession cursorToKeynoteSession(Cursor cursor) {
		long eventID = cursor.getLong(1);

		KeynoteSession keynoteSession = new KeynoteSession();
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

		keynoteSession.setId(cursor.getLong(0));
		keynoteSession.setEventId(eventID);
		keynoteSession.setDescription(cursor.getString(2));
		Log.i("KeynoteSession", cursor.getString(3));
		Log.i("KeynoteSession", cursor.getString(4));
		keynoteSession.setTitle(cursor.getString(5));
		keynoteSession.setDuration(cursor.getInt(8));
		keynoteSession.setPlace(cursor.getString(7));

		Timestamp timestamp = new Timestamp(cursor.getLong(6));
		keynoteSession.setTime(new Date(timestamp.getTime()));
		timestamp = new Timestamp(cursor.getLong(9));
		keynoteSession.setLastModified(new Date(timestamp.getTime()));
		keynoteSession.setDjangoId(cursor.getLong(3));

		// Get the keynote speakers
		Cursor c = database.query(DbHelper.TABLE_RELATIONSHIP_KEYNOTES_KEYSESSIONS, null, DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID + " = " + keynoteSession.getId(), null, null, null, null);
		c.moveToFirst();
		int size = c.getCount();
		Log.i("Size", ""+size);
		ArrayList<Keynote> keynoteSpeakers = new ArrayList<Keynote>();
		while (!c.isAfterLast()) {
			keynoteSpeakers.add(cursorToKeynoteSpeaker(c));
			c.moveToNext();
		}
		keynoteSession.setKeynotes(keynoteSpeakers);
		c.close();
		return keynoteSession;
	}

	public void update(long id ,long djangoID, String title, Date time, int duration, String place, String description, java.util.Date lastModified, List<Keynote> keynoteSpeakers) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Log.i("Database keynotesession", "" + database);
		Cursor cursor = database.query(DbHelper.TABLE_KEYSESSIONS, null,
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
		/*values.put("event_id", insertId);
		values.put(DbHelper.DJANGO_ID,djangoID);*/

		values.put(DbHelper.KEYSESSION_DESCRIPTION, description);


		database.update(DbHelper.TABLE_KEYSESSIONS, values,whereClause,null);

		long keyNoteId;
		Iterator<Keynote> it = keynoteSpeakers.iterator();
		Keynote next;
		while (it.hasNext()) {
			next = it.next();
			values = new ContentValues();
			values.put(DbHelper.KEYNOTE_NAME, next.getName());
			Log.i("Inserting", next.getName());
			values.put(DbHelper.KEYNOTE_EMAIL, next.getEmail());
			values.put(DbHelper.KEYNOTE_COUNTRY, next.getCountry());
			values.put(DbHelper.KEYNOTE_PHOTO, next.getPhoto());
			values.put(DbHelper.KEYNOTE_WORKPLACE, next.getWork_place());

			String keynoteWhereClause = DbHelper.KEYNOTE_EMAIL + " = " + "\""+next.getEmail()+"\"";

			int nrRowsAffected = database.update(DbHelper.TABLE_KEYNOTES, values,keynoteWhereClause,null);
			if(nrRowsAffected > 0){
				//updated then
			}
			else{
				// Insert the keynote speaker into the Keynotes table
				keyNoteId = database.insert(DbHelper.TABLE_KEYNOTES, null, values);
				values = new ContentValues();
				values.put(DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTE_ID, keyNoteId);
				values.put(DbHelper.RELATIONSHIP_KEYNOTES_KEYSESSIONS_KEYNOTESESSION_ID, id);
				// Insert the relationship between the keynote speaker and the keynote session into keynotes_KeynoteSessions table
				database.insert(DbHelper.TABLE_RELATIONSHIP_KEYNOTES_KEYSESSIONS, null, values);
			}
		}



	}

	/**
	 * Returns the keynote session with the specified id
	 * @param id id of the keynote session to get
	 * @return keynote session with specified id
	 */
	public KeynoteSession getKeynoteSession(Long id) {
		String whereClause = DbHelper.TABLE_KEYSESSIONS + "." + DbHelper.COLUMN_ID + " = " + id;
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(DbHelper.TABLE_KEYSESSIONS + " INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_KEYSESSIONS + "." + DbHelper.KEYSESSION_EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);

		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = queryBuilder.query(db, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return cursorToKeynoteSession(cursor);
	}

	/**
	 * Returns the keynote session that has the specified event id as its parent
	 * @param eventID parent's event id
	 * @return keynote session that has the specified event id as its parent
	 */
	public KeynoteSession getKeynoteSessionEvent(Long eventID) {
		String[] columns = { DbHelper.EVENT_ID, DbHelper.COLUMN_ID , DbHelper.DJANGO_ID};
		String whereClause = DbHelper.DJANGO_ID + " = " + eventID;
		Cursor cursor = database.query(DbHelper.TABLE_KEYSESSIONS, columns,
				whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return getKeynoteSession(cursor.getLong(1));
	}

	/**
	 * Checks if exists a keynote session that has the specified event id as its parent
	 * @param eventID parent's event id
	 * @return true if exists a keynote session that has the specified event id as its parent; false otherwise
	 */
	public boolean hasKeynoteSessionEvent(Long eventID) {
		String[] columns = { DbHelper.EVENT_ID, DbHelper.COLUMN_ID };
		String whereClause = DbHelper.EVENT_ID + " = " + eventID;
		Cursor cursor = database.query(DbHelper.TABLE_KEYSESSIONS, columns,
				whereClause, null, null, null, null);
		return cursor.moveToFirst();
	}

}
