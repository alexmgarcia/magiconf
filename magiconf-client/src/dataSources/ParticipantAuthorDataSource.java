package dataSources;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Keynote;
import tables.ParticipantAuthor;
import tables.ParticipantSession;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * @author Alexandre
 *
 * Data source to the Participant - Author relationship
 */
public class ParticipantAuthorDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public ParticipantAuthorDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createParticipantAuthor(String participant_username, Long author_id){
		ContentValues values = new ContentValues();
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME, participant_username);
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_AUTHOR_AUTHOR_ID, author_id);
		database.insert(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_AUTHOR, null, values);
	}
	
	public void deleteParticipantAuthor(String participant_username, Long author_id){
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME + " = '" + participant_username + "' and " + DbHelper.RELATIONSHIP_PARTICIPANT_AUTHOR_AUTHOR_ID + " = " + author_id;
		database.delete(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_AUTHOR, whereClause, null);
	}

	public ParticipantAuthor getParticipantAuthorDB(String username, long id) {
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_AUTHOR_PARTICIPANT_USERNAME + " = '" + username+"' AND "+DbHelper.RELATIONSHIP_PARTICIPANT_AUTHOR_AUTHOR_ID+" = "+id;
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_AUTHOR, null, whereClause, null, null, null, null);
		Log.i("DB_PARTICIPANT_AUTHOR",""+cursor.getCount());
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			ParticipantAuthor partAuthor = cursorToParticipantAuthor(cursor);
			cursor.close();
			return partAuthor;
		}
		else 
			return null;
	}
	
	private ParticipantAuthor cursorToParticipantAuthor(Cursor cursor) {
		ParticipantAuthor participantAuthor= new ParticipantAuthor();
		participantAuthor.setUsername(cursor.getString(0));
		participantAuthor.setAuthorId(cursor.getLong(1));

		return participantAuthor;
	}
	
}
