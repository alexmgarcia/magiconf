package dataSources;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.example.magiconf_client.DbHelper;

import tables.Event;
import tables.Keynote;
import utils.ValuesHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;


/**
 * @author Alexandre
 *
 * Data source to the Participant - Event relationship
 */
public class ParticipantEventDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	
	public ParticipantEventDataSource(Context context){
		dbHelper = new DbHelper(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}

	public void close(){
		dbHelper.close();
	}
	
	
	public void createParticipantEvent(String username, Long event_id){
		ContentValues values = new ContentValues();
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_EVENT_PARTICIPANT_USERNAME, username);
		values.put(DbHelper.RELATIONSHIP_PARTICIPANT_EVENT_EVENT_ID, event_id);
		database.insert(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_EVENT, null, values);
	}
	
	public void deleteParticipantEvent(String username, Long event_id){
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_EVENT_PARTICIPANT_USERNAME + " = '" + username + "' and " + DbHelper.RELATIONSHIP_PARTICIPANT_EVENT_EVENT_ID + " = " + event_id;
		database.delete(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_EVENT, whereClause, null);
	}
	
	/**
	 * Checks if an event is in a user's agenda
	 * @param username user username to check agenda
	 * @param event_id id of the event to check if is in agenda
	 * @return true if the event is in username's agenda; false otherwise
	 */
	public boolean isInAgenda(String username, Long event_id) {
		String whereClause = DbHelper.RELATIONSHIP_PARTICIPANT_EVENT_PARTICIPANT_USERNAME + " = '" + username + "' and " + DbHelper.RELATIONSHIP_PARTICIPANT_EVENT_EVENT_ID + " = " + event_id;
		Cursor cursor = database.query(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_EVENT, null, whereClause, null, null, null, null);
		return cursor.moveToFirst();
	}
	
	/**
	 * Returns all events from a day that are in the agenda of a participant
	 * @param d day to get events of
	 * @param username username of participant to get agenda
	 * @return all events from a day that are in the agenda of a participant
	 * @throws ParseException
	 */
	public List<Event> getEventsInAgenda(java.util.Date d, String username) throws ParseException {
		List<Event> eventList = new LinkedList<Event>();
		String[] columns = { DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID, 
							 DbHelper.TABLE_EVENTS + "." + DbHelper.EVENT_TITLE,
							 DbHelper.TABLE_EVENTS + "." + DbHelper.EVENT_TIME,
							 DbHelper.TABLE_EVENTS + "." + DbHelper.EVENT_PLACE,
							 DbHelper.TABLE_EVENTS + "." + DbHelper.EVENT_DURATION,
							 DbHelper.TABLE_EVENTS + "." + DbHelper.DJANGO_ID };
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(ValuesHelper.DATE_FORMAT, Locale.US);
		String dateStr = dateFormat.format(d);
		long startTimestamp = dateFormat.parse(dateStr).getTime();
		Calendar calEnd = new GregorianCalendar();
		calEnd.setTime(d);
		calEnd.add(Calendar.DAY_OF_YEAR, 1);
		calEnd.set(Calendar.HOUR_OF_DAY, 0);
		calEnd.set(Calendar.MINUTE, 0);
		calEnd.set(Calendar.SECOND, 0);
		calEnd.set(Calendar.MILLISECOND, 0);
		String endDateStr = dateFormat.format(calEnd.getTime());
		long endTimestamp = dateFormat.parse(endDateStr).getTime();
		String whereClause = DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_EVENT + "." + DbHelper.RELATIONSHIP_PARTICIPANT_EVENT_PARTICIPANT_USERNAME + " = '" + username + "' and " + DbHelper.TABLE_EVENTS + "." + DbHelper.EVENT_TIME + " between " + startTimestamp + " and " + endTimestamp;
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_EVENT + " INNER JOIN " + DbHelper.TABLE_EVENTS + " ON " + DbHelper.TABLE_RELATIONSHIP_PARTICIPANT_EVENT + "." + DbHelper.RELATIONSHIP_PARTICIPANT_EVENT_EVENT_ID + " = " + DbHelper.TABLE_EVENTS + "." + DbHelper.COLUMN_ID);
		
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = queryBuilder.query(db, columns, whereClause, null, null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			eventList.add(cursorToEventInAgenda(cursor));
			cursor.moveToNext();
		}
		return eventList;
	}
	
	/**
	 * Returns the event which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return event which the current cursor is pointing to
	 */
	private Event cursorToEventInAgenda(Cursor cursor){
		Event event = new Event();
		event.setId(cursor.getLong(0));
		event.setTitle(cursor.getString(1));
		event.setPlace(cursor.getString(3));
		Timestamp timestamp = new Timestamp(cursor.getLong(2));
		java.sql.Date time = new java.sql.Date(timestamp.getTime());
		event.setInAgenda(true);
		event.setTime(time);
		event.setDuration(cursor.getInt(4));
		event.setDjangoId(cursor.getLong(5));
		return event;
	}
	
}
