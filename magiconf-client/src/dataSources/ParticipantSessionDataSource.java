package dataSources;

import tables.Conference;
import tables.Participant;
import tables.ParticipantSession;
import tables.Session;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.magiconf_client.DbHelper;

public class ParticipantSessionDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public ParticipantSessionDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createParticipantSession(String participant_username, Long session_id){
		ContentValues values = new ContentValues();
		
		deleteParticipantSessions();
		
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_SESSIONS_USERNAME, participant_username);
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_SESSIONS_ID, session_id);
		database.insert(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS, null, values);
	}
	
	private void deleteParticipantSessions() {
		database.delete(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS, null, null);
		
	}

	public void deleteParticipantSession(String participant_username, Long session_id){
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_SESSIONS_USERNAME + " = \"" + participant_username + "\" and " + DbHelper.RELATIONSHIP_PARTICIPANT_SESSIONS_ID + " = " + session_id;
		database.delete(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS, whereClause, null);
	}
	
	public void resetParticipantsessionTable(){
		database.delete(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS, null, null);
	}

	public ParticipantSession getSessionByUsername(String username) {
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS, null, DbHelper.RELATIONSHIP_PARTICIPANT_SESSIONS_USERNAME + " = \"" + username+"\"", null, null, null, null);
		Log.i("DB_PARTICIPANT_SESSION",""+cursor.getCount());
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			ParticipantSession partSession = cursorToParticipantSession(cursor);
			cursor.close();
			return partSession;
		}
		else 
			return null;
	}

	private ParticipantSession cursorToParticipantSession(Cursor cursor) {
		ParticipantSession participantSession= new ParticipantSession();
		participantSession.setUsername(cursor.getString(0));
		participantSession.setSessionId(cursor.getLong(1));

		return participantSession;
	}

	public ParticipantSession getFirstSession() {
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_SESSIONS, null, null, null, null, null, null);
		cursor.moveToFirst();
		ParticipantSession partSession = null;
		if(!cursor.isAfterLast()){
			partSession = cursorToParticipantSession(cursor);
		}
		
		cursor.close();
		return partSession;
	}

}
