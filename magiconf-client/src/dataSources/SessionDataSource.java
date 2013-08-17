package dataSources;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import tables.Author;
import tables.Conference;
import tables.Poster;
import tables.Session;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.magiconf_client.DbHelper;

public class SessionDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
		
	public SessionDataSource(Context context){
		dbHelper = new DbHelper(context);
		
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	/*private static final String SESSIONS_CREATE = 
			"create table "+ TABLE_SESSIONS + "(" + 
					COLUMN_ID + " integer primary key autoincrement, " +
					SESSION_PASSWORD + " text not null, "+
					SESSION_LASTMODIFIED + " integer not null);";*/

	public Session createSession(String password) throws ParseException{
		deleteSessions();
		ContentValues values = new ContentValues();
		values.put(DbHelper.SESSION_LASTMODIFIED, new java.util.Date().getTime());
		values.put(DbHelper.SESSION_PASSWORD, password);
		Log.i("SESSION: Password",password);
				
		long insertId = database.insert(DbHelper.TABLE_SESSIONS, null, values);
		
		Cursor cursor = database.query(DbHelper.TABLE_SESSIONS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Session newSession = cursorToSession(cursor);
		cursor.close();
		return newSession;
	}
	
	
	 /* Deletes the specified session from the local database
	 * @param session to delete
	 */
	public void deleteSession(Session session){
		long id = session.getId();
		database.delete(DbHelper.TABLE_SESSIONS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	public void deleteSession(long id){
		database.delete(DbHelper.TABLE_SESSIONS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns all the sessions present in the local database
	 * It will always return 1 element
	 * @return all session
	 */
	public List<Session> getAllSessions(){
		List<Session> sessions = new LinkedList<Session>();
		Cursor cursor = database.query(DbHelper.TABLE_SESSIONS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Session post = cursorToSession(cursor);
			sessions.add(post);
			cursor.moveToNext();
		}
		
		cursor.close();
		return sessions;
	
	}
	
	/**
	 * Returns the session which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return session which the current cursor is pointing to
	 */
	private Session cursorToSession(Cursor cursor) {
		long sessionID = cursor.getLong(0);
		
		Log.i("Session", "CURSOR TO Session");
		
		Cursor c = database.query(DbHelper.TABLE_SESSIONS, null, DbHelper.COLUMN_ID + " = " + sessionID, null, null, null, null);
		c.moveToFirst();
		
		Session session = new Session();
		
		session.setId(sessionID);
		session.setPassword(c.getString(1));
		
		Timestamp timestamp = new Timestamp(c.getLong(2));
		session.setLastModified(new Date(timestamp.getTime()));

		c.close();
		return session;
	}

	public void deleteSessions() {
		database.delete(DbHelper.TABLE_SESSIONS, null, null);
		
	}
}
