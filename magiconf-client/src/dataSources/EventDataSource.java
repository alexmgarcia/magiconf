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

import dataRetrieving.AuthInfo;

import tables.Event;
import utils.Pair;
import utils.ValuesHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.format.DateFormat;
import android.util.Log;

/**
 * @author Alexandre
 * 
 * Data source to the Event table
 */
public class EventDataSource {
	
	private SQLiteDatabase database;
	private DbHelper dbHelper;
	private ParticipantEventDataSource participantEventDB;
		
	public EventDataSource(Context context){
		dbHelper = new DbHelper(context);
		participantEventDB = new ParticipantEventDataSource(context);
	}
	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
		participantEventDB.open();
		Log.i("DbHelper", ""+dbHelper);
		Log.i("Start", ""+database);
	}

	public void close(){
		dbHelper.close();
		participantEventDB.close();
	}
	
	
	/**
	 * Inserts a new event
	 * @param title title of the event
	 * @param place place of the event
	 * @param time time of the event
	 * @param duration duration of the event
	 * @return inserted event
	 */
	public Event createEvent(Long event_id,String title, String place, Date time, int duration){
		ContentValues values = new ContentValues();
		
		values.put(DbHelper.EVENT_TITLE, title);
		values.put(DbHelper.DJANGO_ID, event_id);
		values.put(DbHelper.EVENT_PLACE,place);
		values.put(DbHelper.EVENT_TIME,time.getTime());
		values.put(DbHelper.EVENT_DURATION, duration);
		values.put(DbHelper.LAST_MODIFIED_DATE, new java.util.Date().getTime());
		long insertId = database.insert(DbHelper.TABLE_EVENTS, null, values);
		
		Cursor cursor = database.query(DbHelper.TABLE_EVENTS, null, DbHelper.COLUMN_ID + " = " +insertId, null, null, null, null);
		
		cursor.moveToFirst();
		Event newEvent = cursorToEvent(cursor);
		cursor.close();
		return newEvent;
	}
	
	public void update(long id,long djangoID,String title, String place, Date time, int duration, java.util.Date lastModifiedDate) {
		String whereClause = DbHelper.COLUMN_ID + " = " + id;
		Log.i("Database socialevent", ""+database);
		Cursor cursor = database.query(DbHelper.TABLE_EVENTS, null, whereClause, null, null, null, null);
		cursor.moveToFirst();
		Long eventID = cursor.getLong(0);
		ContentValues values = new ContentValues();
		values.put(DbHelper.EVENT_TITLE, title);
		values.put(DbHelper.EVENT_TIME, time.getTime());
		values.put(DbHelper.EVENT_DURATION, duration);
		values.put(DbHelper.EVENT_PLACE, place);
		values.put(DbHelper.LAST_MODIFIED_DATE, lastModifiedDate.getTime());
		database.update(DbHelper.TABLE_EVENTS, values, DbHelper.COLUMN_ID + " = " + eventID, null);
	}
	
	/**
	 * Deletes the specified event from the local database
	 * @param event to delete
	 */
	public void deleteEvent(Event event){
		long id = event.getId();
		database.delete(DbHelper.TABLE_EVENTS, DbHelper.COLUMN_ID +" = "+ id, null);
	}
	
	/**
	 * Returns the event with the specified id
	 * @param djangoid of the event
	 * @return event with the specified id
	 */
	public Event getEvent(Long djangoid) {
		String whereClause = DbHelper.DJANGO_ID+"=" + djangoid;
		Log.i("Database", ""+database);
		Cursor cursor = database.query(DbHelper.TABLE_EVENTS, null, whereClause, null, null, null, null);
		boolean hasData = cursor.moveToFirst();
		Log.i("Cursor", ""+hasData);
		if (!hasData)
			return null;
		return cursorToEvent(cursor);
	}
	
	/**
	 * Returns all events from a day
	 * @param d day to get events
	 * @return all events from a day
	 * @throws ParseException
	 */
	public List<Event> getEventsFromDate(java.util.Date d) throws ParseException {
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
		
		String whereClause = DbHelper.EVENT_TIME + " between " + startTimestamp + " and " + endTimestamp;
		String orderBy = DbHelper.EVENT_TIME + " ASC";
		List<Event> eventList = new LinkedList<Event>();
		Cursor cursor = database.query(DbHelper.TABLE_EVENTS, null, whereClause, null, null, null, orderBy);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			eventList.add(cursorToEvent(cursor));
			cursor.moveToNext();
		}
		return eventList;
	}
	
	/**
	 * Returns all events in the local database
	 * @return all events in the local database
	 */
	public List<Event> getAllEvents(){
		List<Event> events = new ArrayList<Event>();
		Cursor cursor = database.query(DbHelper.TABLE_EVENTS, null, null, null, null, null, null);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Event event = cursorToEvent(cursor);
			events.add(event);
			cursor.moveToNext();
		}
		
		cursor.close();
		return events;
	
	}
		
	/**
	 * Returns the event which the current cursor is pointing to
	 * @param cursor current cursor
	 * @return event which the current cursor is pointing to
	 */
	private Event cursorToEvent(Cursor cursor){
		Event event = new Event();
		event.setId(cursor.getLong(0));
		event.setTitle(cursor.getString(1));
		event.setPlace(cursor.getString(3));
		Timestamp timestamp = new Timestamp(cursor.getLong(2));
		java.sql.Date time = new java.sql.Date(timestamp.getTime());
		event.setInAgenda(participantEventDB.isInAgenda(AuthInfo.username, event.getId()));
		event.setTime(time);
		event.setDuration(cursor.getInt(4));
		timestamp = new Timestamp(cursor.getLong(5));
		event.setLastModified(new Date(timestamp.getTime()));
		event.setDjangoId(cursor.getLong(6));
		
		return event;
	}
	
	/**
	 * Returns all events in the local database that matches de search query
	 */
	public Pair<List<Event>,List<Event>> search(String query){
		
		String lowerQuery = query.toLowerCase();
		String convertedQuery = "%"+lowerQuery.toLowerCase()+"%";
		String whereClause = "lower("+DbHelper.EVENT_TITLE+") like '"+convertedQuery+"'"; 
		
		
		List<Event> events = new ArrayList<Event>();
		List<Event> eventsInAgenda = new ArrayList<Event>();
		Cursor cursor = database.query(DbHelper.TABLE_EVENTS, null, whereClause, null, null, null, DbHelper.EVENT_TIME);
		cursor.moveToFirst();
		while(!cursor.isAfterLast()){
			Event event = cursorToEvent(cursor);
			events.add(event);
			if(event.getInAgenda()){
				eventsInAgenda.add(event);
			}
			cursor.moveToNext();
		}
		
		cursor.close();
		return new Pair<List<Event>,List<Event>>(events, eventsInAgenda);
	
	}
	

	
}
