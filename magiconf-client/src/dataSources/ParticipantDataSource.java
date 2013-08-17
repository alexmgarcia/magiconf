package dataSources;


import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import tables.Keynote;
import tables.Participant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.magiconf_client.DbHelper;

public class ParticipantDataSource {
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	//private java.text.DateFormat dateFormat;??

	private String[] allColumns = {
			DbHelper.PARTICIPANT_USERNAME,
			DbHelper.PARTICIPANT_NAME,
			DbHelper.PARTICIPANT_EMAIL,
			DbHelper.PARTICIPANT_COUNTRY,
			DbHelper.PARTICIPANT_WORKPLACE,
			DbHelper.PARTICIPANT_PHONE,
			DbHelper.PARTICIPANT_PHOTO,
			DbHelper.PARTICIPANT_QRCODE,
			DbHelper.PARTICIPANT_LINKEDIN,
			DbHelper.PARTICIPANT_FACEBOOK,

			DbHelper.LAST_CHECK};

	public ParticipantDataSource(Context context){
		dbHelper = new DbHelper(context);
		//dateFormat = DateFormat.getDateFormat(context); ???
	}

	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}



	public Participant getParticipant(Long id) {
		Cursor cursor = database.query(DbHelper.TABLE_PARTICIPANTS, null, DbHelper.COLUMN_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		Participant participant = cursorToParticipant(cursor);
		cursor.close();
		return participant;
	}

	public Participant getParticipantByUsername(String participantUsername) {
		Cursor cursor = database.query(DbHelper.TABLE_PARTICIPANTS, null, DbHelper.PARTICIPANT_USERNAME + " = \"" + participantUsername+"\"", null, null, null, null);
		Log.i("DB_PARTICIPANT",""+cursor.getCount());
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			Participant participant = cursorToParticipant(cursor);
			cursor.close();
			return participant;
		}
		else 
			return null;
	}
	
	public Participant getParticipantByEmail(String participantEmail) {
		Cursor cursor = database.query(DbHelper.TABLE_PARTICIPANTS, null, DbHelper.PARTICIPANT_EMAIL + " = \"" + participantEmail+"\"", null, null, null, null);
		Log.i("DB_PARTICIPANT",""+cursor.getCount());
		if(cursor.getCount() > 0){
			cursor.moveToFirst();
			Participant participant = cursorToParticipant(cursor);
			cursor.close();
			return participant;
		}
		else 
			return null;
	}


	public Participant createParticipant(String country,String username,String  name,String phone, String email,String photo,String workPlace,String qrCode, String linkedinUrl, String facebookUrl){
		ContentValues values = new ContentValues();

		values.put(DbHelper.PARTICIPANT_USERNAME , username);
		values.put(DbHelper.PARTICIPANT_NAME, name);
		values.put(DbHelper.PARTICIPANT_EMAIL, email);
		values.put(DbHelper.PARTICIPANT_COUNTRY, country);
		values.put(DbHelper.PARTICIPANT_WORKPLACE, workPlace);
		values.put(DbHelper.PARTICIPANT_PHONE, phone);
		values.put(DbHelper.PARTICIPANT_PHOTO, photo);
		values.put(DbHelper.PARTICIPANT_QRCODE, qrCode);
		values.put(DbHelper.PARTICIPANT_LINKEDIN, linkedinUrl);
		values.put(DbHelper.PARTICIPANT_FACEBOOK, facebookUrl);
		values.put(DbHelper.LAST_CHECK,  new java.util.Date().getTime());
		
		Log.i("DATA PARTICIPANT", username+ "\n"+country+"\n"+qrCode+"\n");
		
		long insertId = database.insert(DbHelper.TABLE_PARTICIPANTS, null, values);

		Cursor cursor = database.query(DbHelper.TABLE_PARTICIPANTS, allColumns, DbHelper.PARTICIPANT_USERNAME + " = \"" +username+"\"", null, null, null, null);

		cursor.moveToFirst();
		Participant newParticipant = cursorToParticipant(cursor);
		cursor.close();
		return newParticipant;
	}

	public void deleteParticipant(Participant participant){
		long id = participant.getId();
		String name = participant.getName();
		System.out.println(name + " removido.");
		database.delete(DbHelper.TABLE_PARTICIPANTS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	public void updateSocialNetworksByUsername(String username, String linkedinUrl, String facebookUrl) {
		String whereClause = DbHelper.PARTICIPANT_USERNAME + " = '" + username + "'";
		ContentValues values = new ContentValues();
		values.put(DbHelper.PARTICIPANT_LINKEDIN, linkedinUrl);
		values.put(DbHelper.PARTICIPANT_FACEBOOK, facebookUrl);
		database.update(DbHelper.TABLE_PARTICIPANTS, values, whereClause, null);
	}
	
	public void updateLastCheckByUsername(String username, long timeStamp) {
		String whereClause = DbHelper.PARTICIPANT_USERNAME + " = '" + username + "'";
		ContentValues values = new ContentValues();
		values.put(DbHelper.LAST_CHECK,timeStamp);
		database.update(DbHelper.TABLE_PARTICIPANTS, values, whereClause, null);
	}

	public List<Participant> getAllParticipants(){
		List<Participant> participants = new ArrayList<Participant>();
		Cursor cursor = database.query(DbHelper.TABLE_PARTICIPANTS, allColumns, null, null, null, null, null);
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()){
			Participant participant = cursorToParticipant(cursor);
			participants.add(participant);
			cursor.moveToNext();
		}
		
		cursor.close();
		return participants;

	}

	private Participant cursorToParticipant(Cursor cursor){
		Participant participant= new Participant();
		participant.setUsername(cursor.getString(0));
		participant.setName(cursor.getString(1));
		participant.setEmail(cursor.getString(2));
		participant.setCountry(cursor.getString(3));
		participant.setWork_place(cursor.getString(4));
		participant.setPhone_number(cursor.getString(5));
		participant.setPhoto(cursor.getString(6));
		participant.setQrcode(cursor.getString(7));
		participant.setLinkedinUrl(cursor.getString(8));
		participant.setFacebookUrl(cursor.getString(9));
		Timestamp timestamp = new Timestamp(cursor.getLong(10));
		participant.setLastCheck(new Date(timestamp.getTime()));

		return participant;
	}

}


