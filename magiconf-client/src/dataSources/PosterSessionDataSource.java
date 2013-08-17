package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Author;
import tables.KeynoteSession;
import tables.Poster;
import tables.PosterSession;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author Farah
 * 
 * Data source to the PostersSession table
 */

public class PosterSessionDataSource {

	private SQLiteDatabase database;
	private DbHelper dbHelper;

	public PosterSessionDataSource(Context context){
		dbHelper = new DbHelper(context);
	}

	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}


	/**
	 * Inserts a new PostersSession
	 * @param l 
	 * @param title title of the session
	 * @param time time of the session
	 * @param duration duration of the session
	 * @param place place of the session
	 * @param posters to be presented in this session
	 * @return inserted poster session
	 */
	public PosterSession createPostersSession(long event_id, String title, Date time, int duration,
			String place, List<Poster> posters){
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

		// Insert into PosterSession table
		insertId = database.insert(DbHelper.TABLE_POSTERSESSION, null, values);


		Iterator<Poster> it = posters.iterator();
		Poster next;
		while (it.hasNext()) {
			next = it.next();
			values = new ContentValues();
			values.put(DbHelper.POSTER_TITLE, next.getTitle());
			Log.i("Inserting", next.getTitle());
			values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());

			// Insert the article speaker into the Articles table
			long posterId = database.insert(DbHelper.TABLE_POSTERS, null, values);
			values = new ContentValues();
			values.put(DbHelper.POSTERS_PRESENTED_POSTER_ID, posterId);
			values.put(DbHelper.POSTERS_PRESENTED_POSTERSESSION_ID, insertId);
			// Insert the relationship between the talk and the article into poster_presented table
			database.insert(DbHelper.TABLE_POSTERS_PRESENTED, null, values);

			long authorId;
			Iterator<Author> itAut = next.getAuthors().iterator();
			Author nextAut; 
			while (itAut.hasNext()) {
				nextAut = itAut.next();

				Cursor cAuthors = database.query(DbHelper.TABLE_AUTHORS, null,
						DbHelper.AUTHOR_EMAIL + " = '" + nextAut.getEmail()+"'", null, null, null, null);
				if(cAuthors.getCount()==0){
					Log.i("Autor em PosterSession","Não existia");
					values = new ContentValues();
					values.put(DbHelper.AUTHOR_NAME, nextAut.getName());
					Log.i("Inserting", nextAut.getName());
					values.put(DbHelper.AUTHOR_EMAIL, nextAut.getEmail());
					values.put(DbHelper.AUTHOR_COUNTRY, nextAut.getCountry());
					values.put(DbHelper.AUTHOR_PHOTO, nextAut.getPhoto());
					values.put(DbHelper.AUTHOR_WORKPLACE, nextAut.getWork_place());

					// Insert the author into the Authors table
					authorId = database.insert(DbHelper.TABLE_AUTHORS, null, values);
				}else{
					Log.i("Autor em PosterSession","Existia");
					cAuthors.moveToFirst();
					authorId= cAuthors.getLong(0);
				}
				values = new ContentValues();
				values.put(DbHelper.RELATIONSHIP_POSTER_AUTHOR_POSTER_ID, posterId);
				values.put(DbHelper.RELATIONSHIP_POSTER_AUTHOR_AUTHOR_ID, authorId);

				// Insert the relationship between the author and the poster into poster_article table
				database.insert(DbHelper.TABLE_RELATIONSHIP_POSTER_AUTHOR, null, values);
			}

		}


		Cursor cursor = database.query(DbHelper.TABLE_POSTERSESSION, null,
				DbHelper.COLUMN_ID + " = " + insertId, null, null, null, null);

		cursor.moveToFirst();
		PosterSession newPosterSession = cursorToPosterSession(cursor);
		cursor.close();
		return newPosterSession;
	}

	/**
	 * Deletes the specified poster session from the local database
	 * @param poster session to delete
	 */	
	public void deletePosterSession(PosterSession ps){
		long eventId = ps.getEventId();
		long id = ps.getId();

		database.delete(DbHelper.TABLE_POSTERSESSION, DbHelper.COLUMN_ID +" = "+ id, null);
		database.delete(DbHelper.TABLE_EVENTS, DbHelper.COLUMN_ID +" = "+ eventId, null);

	}

	/**
	 * Returns all the poster sessions present in the local database
	 * @return all the poster sessions present in the local database
	 */
	public List<PosterSession> getAllPosterSessions() {
		List<PosterSession> posterSessions = new LinkedList<PosterSession>();
		Cursor cursor = database.query(DbHelper.TABLE_POSTERSESSION, null, null,
				null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			PosterSession ps = cursorToPosterSession(cursor);
			posterSessions.add(ps);
			cursor.moveToNext();
		}

		cursor.close();
		return posterSessions;
	}

	/**
	 * Returns the posterSession which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return posterSession which the current cursor is pointing to
	 */
	private PosterSession cursorToPosterSession(Cursor cursor) {
		long eventID = cursor.getLong(1);

		// Get the data from the Event table
		Cursor c = database.query(DbHelper.TABLE_EVENTS, null, DbHelper.COLUMN_ID + " = " + eventID, null, null, null, null);
		c.moveToFirst();

		Log.i("Cursor", "i ="+0 +cursor.getString(0));
		Log.i("Cursor", "i ="+1 +cursor.getString(1));
		Log.i("Cursor", "i ="+2 +cursor.getString(2));

		Log.i("Cursor", "total:" +c.getColumnNames());

		PosterSession ps = new PosterSession();

		ps.setId(cursor.getLong(0));
		ps.setEventId(eventID);
		ps.setTitle(c.getString(1));
		ps.setDuration(c.getInt(4));
		ps.setPlace(c.getString(3));

		Timestamp timestamp = new Timestamp(c.getLong(2));
		ps.setTime(new Date(timestamp.getTime()));
		timestamp = new Timestamp(c.getLong(5));
		ps.setLastModified(new Date(timestamp.getTime()));

		// Get the posters
		c = database.query(DbHelper.TABLE_POSTERS_PRESENTED, null, DbHelper.POSTERS_PRESENTED_POSTERSESSION_ID + " = " + ps.getId(), null, null, null, null);
		c.moveToFirst();
		int size = c.getCount();
		Log.i("Size", ""+size);
		ArrayList<Poster> posters = new ArrayList<Poster>();
		while (!c.isAfterLast()) {
			posters.add(cursorToPoster(c));
			c.moveToNext();
		}
		ps.setPosters(posters);
		c.close();
		return ps;
	}

	/**
	 * Returns the poster which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return poster which the current cursor is pointing to
	 */
	private Poster cursorToPoster(Cursor cursor) {

		long posterID = cursor.getLong(0);

		Cursor c = database.query(DbHelper.TABLE_POSTERS, null, DbHelper.COLUMN_ID + " = " + posterID, null, null, null, null);
		c.moveToFirst();
		Poster p = new Poster();
		p.setId(c.getLong(0));
		p.setTitle(c.getString(1));

		return p;
	}

	/**
	 * Updates a poster session
	 */
	public void update(long id, String title, Date time, int duration, String place, Date lastModified) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Log.i("Database posterSession", "" + database);
		Cursor cursor = database.query(DbHelper.TABLE_POSTERSESSION, null,
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
	}

	/**
	 * Returns the posterSession with the specified id
	 * @param id id of the poster session to get
	 * @return poster session with specified id
	 */
	public PosterSession getPosterSession(Long id) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Cursor cursor = database.query(DbHelper.TABLE_POSTERSESSION, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return cursorToPosterSession(cursor);
	}

	/**
	 * Returns the poster session that has the specified event id as its parent
	 * @param eventID parent's event id
	 * @return poster session that has the specified event id as its parent
	 */
	public PosterSession getPosterSessionEvent(Long eventID) {
		String[] columns = { DbHelper.EVENT_ID, DbHelper.COLUMN_ID , DbHelper.DJANGO_ID};
		String whereClause = DbHelper.DJANGO_ID + " = " + eventID;
		Cursor cursor = database.query(DbHelper.TABLE_POSTERSESSION, columns,
				whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		if (!hasData)
			return null;
		return getPosterSession(cursor.getLong(1));
	}

	/**
	 * Checks if exists a poster session that has the specified event id as its parent
	 * @param eventID parent's event id
	 * @return true if exists a poster session that has the specified event id as its parent; false otherwise
	 */
	public boolean hasPosterSessionEvent(Long eventID) {
		String[] columns = { DbHelper.EVENT_ID, DbHelper.COLUMN_ID };
		String whereClause = DbHelper.EVENT_ID + " = " + eventID;
		Cursor cursor = database.query(DbHelper.TABLE_POSTERSESSION, columns,
				whereClause, null, null, null, null);
		return cursor.moveToFirst();
	}

	public void updatePostersSession(long id, long djangoId,
			String title, Date d, int duration, String place,
			List<Poster> posters) {

		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Log.i("Database posterSession", "" + database);
		Cursor cursor = database.query(DbHelper.TABLE_POSTERSESSION, null,
				whereClause, null, null, null, null);
		cursor.moveToFirst();
		Long eventId = cursor.getLong(1);
		ContentValues values = new ContentValues();
		values.put(DbHelper.EVENT_TITLE, title);
		values.put(DbHelper.EVENT_TIME, d.getTime());
		values.put(DbHelper.EVENT_DURATION, duration);
		values.put(DbHelper.EVENT_PLACE, place);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
		database.update(DbHelper.TABLE_EVENTS, values, DbHelper.COLUMN_ID
				+ " = " + eventId, null);

		/*values = new ContentValues();
		values.put("event_id", id);
		values.put(DbHelper.DJANGO_ID,djangoId);

		// Insert into PosterSession table
		database.update(DbHelper.TABLE_POSTERSESSION, values, DbHelper.COLUMN_ID
				+ " = " + id, null);*/

		Iterator<Poster> it = posters.iterator();
		Poster next;
		while (it.hasNext()) {
			next = it.next();
			values = new ContentValues();
			values.put(DbHelper.POSTER_TITLE, next.getTitle());
			Log.i("Inserting", next.getTitle());
			values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());

			String posterWhereClause = DbHelper.POSTER_TITLE + " = " + "\""+next.getTitle()+"\"";
			int postersAffected = database.update(DbHelper.TABLE_POSTERS, values, posterWhereClause, null);

			if(postersAffected > 0){
				//updated then
			}
			else {
				// Insert the article speaker into the Articles table
				long posterId = database.insert(DbHelper.TABLE_POSTERS, null, values);
				values = new ContentValues();
				values.put(DbHelper.POSTERS_PRESENTED_POSTER_ID, posterId);
				values.put(DbHelper.POSTERS_PRESENTED_POSTERSESSION_ID, id);
				// Insert the relationship between the talk and the article into poster_presented table
				database.insert(DbHelper.TABLE_POSTERS_PRESENTED, null, values);

				long authorId;
				Iterator<Author> itAut = next.getAuthors().iterator();
				Author nextAut; 
				while (itAut.hasNext()) {
					nextAut = itAut.next();

					Cursor cAuthors = database.query(DbHelper.TABLE_AUTHORS, null,
							DbHelper.AUTHOR_EMAIL + " = '" + nextAut.getEmail()+"'", null, null, null, null);
					if(cAuthors.getCount()==0){
						Log.i("Autor em PosterSession","Não existia");
						values = new ContentValues();
						values.put(DbHelper.AUTHOR_NAME, nextAut.getName());
						Log.i("Inserting", nextAut.getName());
						values.put(DbHelper.AUTHOR_EMAIL, nextAut.getEmail());
						values.put(DbHelper.AUTHOR_COUNTRY, nextAut.getCountry());
						values.put(DbHelper.AUTHOR_PHOTO, nextAut.getPhoto());
						values.put(DbHelper.AUTHOR_WORKPLACE, nextAut.getWork_place());

						// Insert the author into the Authors table
						authorId = database.insert(DbHelper.TABLE_AUTHORS, null, values);
					}else{
						Log.i("Autor em PosterSession","Existia");
						cAuthors.moveToFirst();
						authorId= cAuthors.getLong(0);
					}
					values = new ContentValues();
					values.put(DbHelper.RELATIONSHIP_POSTER_AUTHOR_POSTER_ID, posterId);
					values.put(DbHelper.RELATIONSHIP_POSTER_AUTHOR_AUTHOR_ID, authorId);

					// Insert the relationship between the author and the poster into poster_article table
					database.insert(DbHelper.TABLE_RELATIONSHIP_POSTER_AUTHOR, null, values);
				}
			}
		}
	}
}
