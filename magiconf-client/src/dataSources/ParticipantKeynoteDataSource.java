package dataSources;


import com.example.magiconf_client.DbHelper;

import tables.ParticipantKeynote;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * @author Alexandre
 *
 * Data source to the Participant - Keynote relationship
 */
public class ParticipantKeynoteDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public ParticipantKeynoteDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createParticipantKeynote(String participant_username, Long keynote_id){
		ContentValues values = new ContentValues();
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME, participant_username);
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_KEYNOTE_KEYNOTE_ID, keynote_id);
		database.insert(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_KEYNOTE, null, values);
	}
	
	public void deleteParticipantKeynote(String participant_username, Long keynote_id){
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME + " = '" + participant_username + "' and " + DbHelper.RELATIONSHIP_PARTICIPANT_KEYNOTE_KEYNOTE_ID + " = " + keynote_id;
		database.delete(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_KEYNOTE, whereClause, null);
	}
	
	public ParticipantKeynote getParticipantKeynoteDB(String username, long id) {
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_KEYNOTE_PARTICIPANT_USERNAME + " = '" + username+"' AND "+DbHelper.RELATIONSHIP_PARTICIPANT_KEYNOTE_KEYNOTE_ID+" = "+id;
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_KEYNOTE, null, whereClause, null, null, null, null);
		Log.i("DB_PARTICIPANT_KEYNOTE",""+cursor.getCount());
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			ParticipantKeynote partKeynote = cursorToParticipantKeynote(cursor);
			cursor.close();
			return partKeynote;
		}
		else 
			return null;
	}
	
	private ParticipantKeynote cursorToParticipantKeynote(Cursor cursor) {
		ParticipantKeynote participantKeynote= new ParticipantKeynote();
		participantKeynote.setUsername(cursor.getString(0));
		participantKeynote.setKeynoteId(cursor.getLong(1));

		return participantKeynote;
	}
	
}
