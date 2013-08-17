package dataSources;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.example.magiconf_client.DbHelper;

import tables.Keynote;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


/**
 * @author Alexandre
 *
 * Data source to the Participant - Contact relationship
 */
public class ParticipantContactDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public ParticipantContactDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createParticipantContact(String participant_username, Long contact_id){
		ContentValues values = new ContentValues();
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME, participant_username);
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_CONTACT_CONTACT_ID, contact_id);
		database.insert(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_CONTACT, null, values);
	}
	
	public void deleteParticipantContact(String participant_username, Long contact_id){
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME + " = '" + participant_username + "' and " + DbHelper.RELATIONSHIP_PARTICIPANT_CONTACT_CONTACT_ID + " = " + contact_id;
		database.delete(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_CONTACT, whereClause, null);
	}
	
	public boolean hasParticipantContact(String participant_username, Long contact_id) {
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_CONTACT_PARTICIPANT_USERNAME + " = '" + participant_username + "' and " + DbHelper.RELATIONSHIP_PARTICIPANT_CONTACT_CONTACT_ID + " = " + contact_id;
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_CONTACT, null, whereClause, null, null, null, null);
		boolean result = cursor.moveToFirst();
		cursor.close();
		return result;
	}
	
}
